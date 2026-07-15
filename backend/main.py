from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.responses import FileResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles

from paleru_social import get_social_store, router


@asynccontextmanager
async def lifespan(_: FastAPI):
    store = await get_social_store()
    await store.health()
    yield


app = FastAPI(
    title="Paleru Congress Party API",
    version="1.3.0",
    docs_url="/docs",
    redoc_url=None,
    lifespan=lifespan,
)
app.include_router(router)


@app.get("/", tags=["Service"])
async def root():
    return {
        "service": "Paleru Congress Party API",
        "status": "online",
        "health": "/paleru-social/health",
        "android_apk": "/downloads/PaleruCongress.apk",
        "web_app": "/app/",
    }


@app.get("/health", tags=["Service"])
async def health():
    store = await get_social_store()
    database = await store.health()
    return {"ok": True, "service": "paleru-congress-party", "database": database}


@app.get("/downloads/PaleruCongress.apk", include_in_schema=False)
async def download_apk():
    apk = Path(__file__).resolve().parent / "PaleruCongress.apk"
    if not apk.is_file():
        from fastapi import HTTPException
        raise HTTPException(status_code=404, detail="APK is not included in this release")
    return FileResponse(
        apk,
        media_type="application/vnd.android.package-archive",
        filename="PaleruCongress.apk",
        headers={"Cache-Control": "public, max-age=3600"},
    )


PWA_DIR = Path(__file__).resolve().parent / "pwa"


@app.get("/app", include_in_schema=False)
async def web_app_redirect():
    return RedirectResponse(url="/app/", status_code=307)


app.mount("/app", StaticFiles(directory=PWA_DIR, html=True), name="pwa")
