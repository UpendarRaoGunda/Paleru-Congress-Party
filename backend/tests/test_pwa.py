import json
import re
import unittest
from pathlib import Path


BACKEND_DIR = Path(__file__).resolve().parents[1]
PWA_DIR = BACKEND_DIR / "pwa"


class PwaStaticAssetTests(unittest.TestCase):
    """Validate installability without starting the API or connecting to MongoDB."""

    @classmethod
    def setUpClass(cls):
        cls.index = (PWA_DIR / "index.html").read_text(encoding="utf-8")
        cls.script = (PWA_DIR / "app.js").read_text(encoding="utf-8")
        cls.manifest = json.loads(
            (PWA_DIR / "manifest.webmanifest").read_text(encoding="utf-8")
        )
        cls.worker = (PWA_DIR / "sw.js").read_text(encoding="utf-8")

    def test_required_static_files_exist(self):
        for relative_path in (
            "index.html",
            "app.css",
            "app.js",
            "manifest.webmanifest",
            "sw.js",
        ):
            with self.subTest(path=relative_path):
                path = PWA_DIR / relative_path
                self.assertTrue(path.is_file(), f"Missing PWA asset: {path}")
                self.assertGreater(path.stat().st_size, 0, f"Empty PWA asset: {path}")

    def test_index_links_manifest_and_registers_service_worker(self):
        self.assertRegex(
            self.index,
            r'<link[^>]+rel=["\'][^"\']*manifest[^"\']*["\'][^>]+>',
        )
        self.assertIn("manifest.webmanifest", self.index)
        self.assertIn("app.js", self.index)
        self.assertIn("serviceWorker", self.script)
        self.assertIn("sw.js", self.script)

    def test_manifest_has_installable_app_metadata(self):
        required = {
            "name",
            "short_name",
            "start_url",
            "scope",
            "display",
            "theme_color",
            "background_color",
            "icons",
        }
        self.assertFalse(required - self.manifest.keys())
        self.assertIn(self.manifest["start_url"], {"./", "/app/"})
        self.assertIn(self.manifest["scope"], {"./", "/app/"})
        self.assertIn(self.manifest["display"], {"standalone", "fullscreen", "minimal-ui"})

    def test_manifest_icons_are_local_and_present(self):
        icons = self.manifest["icons"]
        self.assertGreaterEqual(len(icons), 1)
        sizes = {icon.get("sizes") for icon in icons}
        self.assertTrue(
            "any" in sizes or {"192x192", "512x512"}.issubset(sizes),
            "Provide a scalable icon or both standard install icon sizes",
        )

        for icon in icons:
            with self.subTest(icon=icon):
                source = icon.get("src", "")
                self.assertFalse(source.startswith(("http://", "https://")))
                icon_path = PWA_DIR / source.lstrip("./")
                self.assertTrue(icon_path.is_file(), f"Missing manifest icon: {icon_path}")

    def test_service_worker_caches_the_app_shell(self):
        self.assertRegex(self.worker, r"addEventListener\s*\(\s*['\"]install['\"]")
        self.assertRegex(self.worker, r"addEventListener\s*\(\s*['\"]fetch['\"]")
        self.assertIn("manifest.webmanifest", self.worker)
        self.assertTrue(
            re.search(r"(?:['\"]\.?/['\"]|index\.html)", self.worker),
            "Service worker should cache the app entry point",
        )

    def test_fastapi_mounts_pwa_and_redirects_to_trailing_slash(self):
        source = (BACKEND_DIR / "main.py").read_text(encoding="utf-8")
        self.assertIn('@app.get("/app"', source)
        self.assertIn('RedirectResponse(url="/app/"', source)
        self.assertRegex(source, r'app\.mount\(\s*["\']/app["\']')
        self.assertIn("StaticFiles(directory=PWA_DIR, html=True)", source)


if __name__ == "__main__":
    unittest.main()
