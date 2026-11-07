"""Verifica se todas as imagens (URLs) usadas no conteúdo abrem. Uso: python3 tools/check_images.py"""
import json, pathlib, sys, urllib.request
from concurrent.futures import ThreadPoolExecutor

urls = set()
def walk(o):
    if isinstance(o, dict):
        for k, v in o.items():
            if k in ("image", "vintageImage", "cover") and isinstance(v, str) and v.startswith("http"): urls.add(v)
            else: walk(v)
    elif isinstance(o, list):
        for v in o: walk(v)
for f in pathlib.Path("app/src/main/assets/content").rglob("*.json"):
    walk(json.loads(f.read_text(encoding="utf-8")))

def check(u):
    req = urllib.request.Request(u, headers={"User-Agent": "okhttp/4.12.0"})  # mesmo tipo de cliente do app (Coil)
    for _ in range(3):
        try:
            with urllib.request.urlopen(req, timeout=20) as r:
                ct = r.headers.get("Content-Type", "")
                if r.status == 200 and ct.startswith("image/"): return None
                last = f"{r.status} {ct}"
        except Exception as e:
            last = str(e)
    return f"{u} -> {last}"

with ThreadPoolExecutor(8) as ex:
    fails = [x for x in ex.map(check, sorted(urls)) if x]
print(f"Imagens verificadas: {len(urls)} | com problema: {len(fails)}")
for x in fails: print("  ✗", x)
sys.exit(1 if fails else 0)
