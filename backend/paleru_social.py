"""Private Paleru Congress social feed backed by the shared MongoDB cluster.

The Mongo URI is read only from the server environment. Paleru records are
isolated in PALERU_SOCIAL_MONGODB_DB (default: paleru_congress).
"""

from __future__ import annotations

import base64
import binascii
import hashlib
import os
import re
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Any, Literal, Protocol

from bson import ObjectId
from fastapi import APIRouter, Depends, Header, HTTPException, Query, Response, status
from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase
from pydantic import BaseModel, Field, field_validator
from pymongo import ASCENDING, DESCENDING
from pymongo.errors import DuplicateKeyError, PyMongoError


router = APIRouter(prefix="/paleru-social", tags=["Paleru Congress Social"])
MAX_IMAGE_BYTES = 8 * 1024 * 1024
DEVICE_ID_RE = re.compile(r"^[A-Za-z0-9._-]{16,128}$")
DEVICE_KEY_RE = re.compile(r"^[A-Za-z0-9_-]{32,256}$")


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


def iso(value: datetime | None) -> str | None:
    if value is None:
        return None
    if value.tzinfo is None:
        value = value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc).isoformat().replace("+00:00", "Z")


def object_id(value: str, label: str = "record") -> ObjectId:
    if not ObjectId.is_valid(value):
        raise HTTPException(status_code=404, detail=f"{label.capitalize()} not found")
    return ObjectId(value)


def key_hash(key: str) -> str:
    return hashlib.sha256(f"PaleruSocial:v1:{key}".encode()).hexdigest()


@dataclass(frozen=True)
class DeviceCredentials:
    device_id: str
    device_key: str


async def device_credentials(
    x_paleru_device_id: str = Header(..., alias="X-Paleru-Device-Id"),
    x_paleru_device_key: str = Header(..., alias="X-Paleru-Device-Key"),
) -> DeviceCredentials:
    device_id = x_paleru_device_id.strip()
    device_key = x_paleru_device_key.strip()
    if not DEVICE_ID_RE.fullmatch(device_id) or not DEVICE_KEY_RE.fullmatch(device_key):
        raise HTTPException(status_code=401, detail="Invalid device credentials")
    return DeviceCredentials(device_id, device_key)


class PostCreate(BaseModel):
    author_name: str = Field(min_length=2, max_length=100)
    content: str = Field(min_length=1, max_length=4000)
    place: str = Field(default="", max_length=160)
    media_base64: str | None = Field(default=None, max_length=12_000_000)
    media_mime: str | None = Field(default=None, max_length=80)

    @field_validator("author_name", "content", "place")
    @classmethod
    def clean_text(cls, value: str) -> str:
        return value.strip()

    @field_validator("media_mime")
    @classmethod
    def image_mime_only(cls, value: str | None) -> str | None:
        if value is None:
            return None
        value = value.strip().lower()
        if value not in {"image/jpeg", "image/png", "image/webp"}:
            raise ValueError("Only JPEG, PNG, or WebP images are supported")
        return value


class PostUpdate(BaseModel):
    content: str = Field(min_length=1, max_length=4000)
    place: str = Field(default="", max_length=160)

    @field_validator("content", "place")
    @classmethod
    def clean_text(cls, value: str) -> str:
        return value.strip()


class CommentCreate(BaseModel):
    author_name: str = Field(min_length=2, max_length=100)
    content: str = Field(min_length=1, max_length=1000)

    @field_validator("author_name", "content")
    @classmethod
    def clean_text(cls, value: str) -> str:
        return value.strip()


class ReactionUpdate(BaseModel):
    reaction: Literal["like", "dislike", "none"]


class SocialStore(Protocol):
    async def health(self) -> dict[str, Any]: ...
    async def list_posts(self, device: DeviceCredentials, limit: int, before: datetime | None) -> list[dict[str, Any]]: ...
    async def create_post(self, device: DeviceCredentials, payload: PostCreate) -> dict[str, Any]: ...
    async def update_post(self, device: DeviceCredentials, post_id: str, payload: PostUpdate) -> dict[str, Any]: ...
    async def delete_post(self, device: DeviceCredentials, post_id: str) -> None: ...
    async def set_reaction(self, device: DeviceCredentials, post_id: str, reaction: str) -> dict[str, Any]: ...
    async def list_comments(self, device: DeviceCredentials, post_id: str) -> list[dict[str, Any]]: ...
    async def create_comment(self, device: DeviceCredentials, post_id: str, payload: CommentCreate) -> dict[str, Any]: ...
    async def delete_comment(self, device: DeviceCredentials, post_id: str, comment_id: str) -> None: ...
    async def get_media(self, media_id: str) -> tuple[bytes, str]: ...


class MongoPaleruSocialStore:
    def __init__(self, uri: str, database_name: str) -> None:
        self.client = AsyncIOMotorClient(
            uri,
            serverSelectionTimeoutMS=8_000,
            connectTimeoutMS=8_000,
            socketTimeoutMS=15_000,
            uuidRepresentation="standard",
        )
        self.db: AsyncIOMotorDatabase = self.client[database_name]
        self.database_name = database_name
        self.devices = self.db["devices"]
        self.posts = self.db["posts"]
        self.comments = self.db["comments"]
        self.reactions = self.db["reactions"]
        self.media = self.db["media"]
        self._initialized = False

    async def initialize(self) -> None:
        if self._initialized:
            return
        await self.devices.create_index("device_id", unique=True)
        await self.posts.create_index([("status", ASCENDING), ("created_at", DESCENDING)])
        await self.posts.create_index([("author_device_id", ASCENDING), ("created_at", DESCENDING)])
        await self.comments.create_index([("post_id", ASCENDING), ("created_at", ASCENDING)])
        await self.reactions.create_index([("post_id", ASCENDING), ("device_id", ASCENDING)], unique=True)
        self._initialized = True

    async def health(self) -> dict[str, Any]:
        await self.client.admin.command("ping")
        await self.initialize()
        return {"ok": True, "backend": "mongodb", "database": self.database_name}

    async def _verify_device(self, device: DeviceCredentials) -> None:
        await self.initialize()
        digest = key_hash(device.device_key)
        now = utc_now()
        try:
            await self.devices.update_one(
                {"device_id": device.device_id},
                {"$setOnInsert": {"key_hash": digest, "created_at": now}, "$set": {"last_seen_at": now}},
                upsert=True,
            )
        except DuplicateKeyError:
            pass
        record = await self.devices.find_one({"device_id": device.device_id})
        if not record or record.get("key_hash") != digest:
            raise HTTPException(status_code=401, detail="Device credential mismatch")

    async def _rate_limit(self, collection, device_id: str, limit: int) -> None:
        since = utc_now() - timedelta(hours=1)
        count = await collection.count_documents({"author_device_id": device_id, "created_at": {"$gte": since}})
        if count >= limit:
            raise HTTPException(status_code=429, detail="Hourly posting limit reached")

    async def _post_or_404(self, post_id: str) -> dict[str, Any]:
        record = await self.posts.find_one({"_id": object_id(post_id, "post"), "status": "active"})
        if not record:
            raise HTTPException(status_code=404, detail="Post not found")
        return record

    async def _serialize_post(self, record: dict[str, Any], viewer: str) -> dict[str, Any]:
        post_id = record["_id"]
        likes = await self.reactions.count_documents({"post_id": post_id, "reaction": "like"})
        dislikes = await self.reactions.count_documents({"post_id": post_id, "reaction": "dislike"})
        comment_count = await self.comments.count_documents({"post_id": post_id, "status": "active"})
        reaction = await self.reactions.find_one({"post_id": post_id, "device_id": viewer})
        media_id = record.get("media_id")
        return {
            "id": str(post_id),
            "author_name": record.get("author_name", "Congress member"),
            "content": record.get("content", ""),
            "place": record.get("place", ""),
            "media_url": f"/paleru-social/media/{media_id}" if media_id else None,
            "created_at": iso(record.get("created_at")),
            "updated_at": iso(record.get("updated_at")),
            "like_count": likes,
            "dislike_count": dislikes,
            "comment_count": comment_count,
            "viewer_reaction": reaction.get("reaction") if reaction else "none",
            "viewer_can_edit": record.get("author_device_id") == viewer,
        }

    async def list_posts(self, device: DeviceCredentials, limit: int, before: datetime | None) -> list[dict[str, Any]]:
        await self._verify_device(device)
        query: dict[str, Any] = {"status": "active"}
        if before:
            query["created_at"] = {"$lt": before}
        records = await self.posts.find(query).sort("created_at", DESCENDING).limit(limit).to_list(length=limit)
        return [await self._serialize_post(record, device.device_id) for record in records]

    async def _store_media(self, payload: PostCreate) -> ObjectId | None:
        if not payload.media_base64:
            return None
        if not payload.media_mime:
            raise HTTPException(status_code=422, detail="Image MIME type is required")
        try:
            data = base64.b64decode(payload.media_base64, validate=True)
        except (binascii.Error, ValueError):
            raise HTTPException(status_code=422, detail="Image data is invalid") from None
        if not data or len(data) > MAX_IMAGE_BYTES:
            raise HTTPException(status_code=413, detail="Image must be between 1 byte and 8 MB")
        result = await self.media.insert_one({"data": data, "mime": payload.media_mime, "created_at": utc_now()})
        return result.inserted_id

    async def create_post(self, device: DeviceCredentials, payload: PostCreate) -> dict[str, Any]:
        await self._verify_device(device)
        await self._rate_limit(self.posts, device.device_id, 30)
        media_id = await self._store_media(payload)
        now = utc_now()
        record = {"author_device_id": device.device_id, "author_name": payload.author_name, "content": payload.content,
                  "place": payload.place, "media_id": media_id, "status": "active", "created_at": now, "updated_at": now}
        try:
            result = await self.posts.insert_one(record)
        except Exception:
            if media_id:
                await self.media.delete_one({"_id": media_id})
            raise
        record["_id"] = result.inserted_id
        return await self._serialize_post(record, device.device_id)

    async def update_post(self, device: DeviceCredentials, post_id: str, payload: PostUpdate) -> dict[str, Any]:
        await self._verify_device(device)
        record = await self._post_or_404(post_id)
        if record.get("author_device_id") != device.device_id:
            raise HTTPException(status_code=403, detail="Only the author can edit this post")
        await self.posts.update_one({"_id": record["_id"]}, {"$set": {"content": payload.content, "place": payload.place, "updated_at": utc_now()}})
        return await self._serialize_post(await self._post_or_404(post_id), device.device_id)

    async def delete_post(self, device: DeviceCredentials, post_id: str) -> None:
        await self._verify_device(device)
        record = await self._post_or_404(post_id)
        if record.get("author_device_id") != device.device_id:
            raise HTTPException(status_code=403, detail="Only the author can delete this post")
        await self.posts.update_one({"_id": record["_id"]}, {"$set": {"status": "deleted", "updated_at": utc_now()}})
        await self.comments.delete_many({"post_id": record["_id"]})
        await self.reactions.delete_many({"post_id": record["_id"]})
        if record.get("media_id"):
            await self.media.delete_one({"_id": record["media_id"]})

    async def set_reaction(self, device: DeviceCredentials, post_id: str, reaction: str) -> dict[str, Any]:
        await self._verify_device(device)
        record = await self._post_or_404(post_id)
        query = {"post_id": record["_id"], "device_id": device.device_id}
        if reaction == "none":
            await self.reactions.delete_one(query)
        else:
            await self.reactions.update_one(query, {"$set": {"reaction": reaction, "updated_at": utc_now()}}, upsert=True)
        return await self._serialize_post(record, device.device_id)

    def _serialize_comment(self, record: dict[str, Any], viewer: str) -> dict[str, Any]:
        return {"id": str(record["_id"]), "post_id": str(record["post_id"]), "author_name": record.get("author_name", "Congress member"),
                "content": record.get("content", ""), "created_at": iso(record.get("created_at")),
                "viewer_can_delete": record.get("author_device_id") == viewer}

    async def list_comments(self, device: DeviceCredentials, post_id: str) -> list[dict[str, Any]]:
        await self._verify_device(device)
        post = await self._post_or_404(post_id)
        records = await self.comments.find({"post_id": post["_id"], "status": "active"}).sort("created_at", ASCENDING).to_list(length=250)
        return [self._serialize_comment(record, device.device_id) for record in records]

    async def create_comment(self, device: DeviceCredentials, post_id: str, payload: CommentCreate) -> dict[str, Any]:
        await self._verify_device(device)
        post = await self._post_or_404(post_id)
        await self._rate_limit(self.comments, device.device_id, 100)
        record = {"post_id": post["_id"], "author_device_id": device.device_id, "author_name": payload.author_name,
                  "content": payload.content, "status": "active", "created_at": utc_now()}
        result = await self.comments.insert_one(record)
        record["_id"] = result.inserted_id
        return self._serialize_comment(record, device.device_id)

    async def delete_comment(self, device: DeviceCredentials, post_id: str, comment_id: str) -> None:
        await self._verify_device(device)
        post = await self._post_or_404(post_id)
        record = await self.comments.find_one({"_id": object_id(comment_id, "comment"), "post_id": post["_id"], "status": "active"})
        if not record:
            raise HTTPException(status_code=404, detail="Comment not found")
        if record.get("author_device_id") != device.device_id:
            raise HTTPException(status_code=403, detail="Only the author can delete this comment")
        await self.comments.update_one({"_id": record["_id"]}, {"$set": {"status": "deleted"}})

    async def get_media(self, media_id: str) -> tuple[bytes, str]:
        record = await self.media.find_one({"_id": object_id(media_id, "media")})
        if not record:
            raise HTTPException(status_code=404, detail="Media not found")
        return bytes(record["data"]), record.get("mime", "application/octet-stream")


_store: MongoPaleruSocialStore | None = None


async def get_social_store() -> SocialStore:
    global _store
    if _store is None:
        uri = os.getenv("MONGODB_URI", "").strip()
        if not uri:
            raise HTTPException(status_code=503, detail="Paleru social MongoDB is not configured")
        database = os.getenv("PALERU_SOCIAL_MONGODB_DB", "paleru_congress").strip() or "paleru_congress"
        _store = MongoPaleruSocialStore(uri, database)
    return _store


def parse_before(value: str | None) -> datetime | None:
    if not value:
        return None
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        raise HTTPException(status_code=422, detail="before must be an ISO-8601 timestamp") from None
    return parsed if parsed.tzinfo else parsed.replace(tzinfo=timezone.utc)


@router.get("/health")
async def social_health(store: SocialStore = Depends(get_social_store)) -> dict[str, Any]:
    try:
        return await store.health()
    except (PyMongoError, OSError) as exc:
        raise HTTPException(status_code=503, detail="Paleru social MongoDB is unavailable") from exc


@router.get("/posts")
async def list_posts(limit: int = Query(30, ge=1, le=50), before: str | None = None,
                     device: DeviceCredentials = Depends(device_credentials), store: SocialStore = Depends(get_social_store)) -> dict[str, Any]:
    return {"posts": await store.list_posts(device, limit, parse_before(before))}


@router.post("/posts", status_code=status.HTTP_201_CREATED)
async def create_post(payload: PostCreate, device: DeviceCredentials = Depends(device_credentials),
                      store: SocialStore = Depends(get_social_store)) -> dict[str, Any]:
    return await store.create_post(device, payload)


@router.patch("/posts/{post_id}")
async def update_post(post_id: str, payload: PostUpdate, device: DeviceCredentials = Depends(device_credentials),
                      store: SocialStore = Depends(get_social_store)) -> dict[str, Any]:
    return await store.update_post(device, post_id, payload)


@router.delete("/posts/{post_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_post(post_id: str, device: DeviceCredentials = Depends(device_credentials),
                      store: SocialStore = Depends(get_social_store)) -> Response:
    await store.delete_post(device, post_id)
    return Response(status_code=204)


@router.put("/posts/{post_id}/reaction")
async def set_reaction(post_id: str, payload: ReactionUpdate, device: DeviceCredentials = Depends(device_credentials),
                       store: SocialStore = Depends(get_social_store)) -> dict[str, Any]:
    return await store.set_reaction(device, post_id, payload.reaction)


@router.get("/posts/{post_id}/comments")
async def list_comments(post_id: str, device: DeviceCredentials = Depends(device_credentials),
                        store: SocialStore = Depends(get_social_store)) -> dict[str, Any]:
    return {"comments": await store.list_comments(device, post_id)}


@router.post("/posts/{post_id}/comments", status_code=status.HTTP_201_CREATED)
async def create_comment(post_id: str, payload: CommentCreate, device: DeviceCredentials = Depends(device_credentials),
                         store: SocialStore = Depends(get_social_store)) -> dict[str, Any]:
    return await store.create_comment(device, post_id, payload)


@router.delete("/posts/{post_id}/comments/{comment_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_comment(post_id: str, comment_id: str, device: DeviceCredentials = Depends(device_credentials),
                         store: SocialStore = Depends(get_social_store)) -> Response:
    await store.delete_comment(device, post_id, comment_id)
    return Response(status_code=204)


@router.get("/media/{media_id}")
async def get_media(media_id: str, store: SocialStore = Depends(get_social_store)) -> Response:
    content, mime = await store.get_media(media_id)
    return Response(content=content, media_type=mime, headers={"Cache-Control": "private, max-age=86400"})
