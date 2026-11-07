"""Confere se perfis do YouTube/TikTok existem. Uso: python3 tools/check_links.py arquivo_de_handles"""
import re, sys, urllib.request
from concurrent.futures import ThreadPoolExecutor
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Safari/537.36"
handles = [l.strip() for l in open(sys.argv[1]) if l.strip() and not l.startswith("#")]
urls = []
for h in handles:
    urls += [f"https://www.youtube.com/@{h}", f"https://www.tiktok.com/@{h}"]
def get(u):
    try:
        req = urllib.request.Request(u, headers={"User-Agent": UA, "Accept-Language": "pt-BR,pt;q=0.9"})
        with urllib.request.urlopen(req, timeout=25) as r:
            html = r.read(400000).decode("utf-8", "ignore")
            t = re.search(r"<title>(.*?)</title>", html, re.S)
            title = re.sub(r"\s+", " ", t.group(1)).strip() if t else ""
            subs = re.search(r'"subscriberCountText":\{"accessibility":\{"accessibilityData":\{"label":"([^"]+)"', html) or re.search(r'([\d.,]+\s*(?:mi|mil|M|K)?\s*(?:de )?inscritos)', html)
            desc = re.search(r'<meta name="description" content="([^"]{0,160})', html)
            fol = re.search(r'"followerCount":(\d+)', html)
            return f"{r.status} | {u} | {title} | {subs.group(1) if subs else ''} | {('fol=' + fol.group(1)) if fol else ''} | {desc.group(1) if desc else ''}"
    except Exception as e:
        return f"ERR | {u} | {e}"
with ThreadPoolExecutor(6) as ex:
    for line in ex.map(get, urls): print(line)
