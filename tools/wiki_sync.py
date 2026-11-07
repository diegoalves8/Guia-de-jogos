"""
Coleta dados da Dandy's World Wiki (Fandom, conteúdo CC BY-SA) para o app.

Roda no GitHub Actions (workflow "Wiki Sync"). Salva em wiki-data/:
  raw/<Pagina>.wikitext   -> texto-fonte das páginas (Toons, Skins, Maestria...)
  images.json             -> URL original de cada imagem encontrada
  img/<arquivo>           -> imagens baixadas (redimensionadas depois pelo build)
"""
import json, os, re, sys, time, urllib.parse, urllib.request

API = "https://dandys-world-robloxhorror.fandom.com/api.php"
UA = "GuiaDeJogosBot/1.0 (app pessoal de guia; github.com/diegoalves8/Guia-de-jogos)"
OUT = sys.argv[1] if len(sys.argv) > 1 else "wiki-data"
os.makedirs(f"{OUT}/raw", exist_ok=True)
os.makedirs(f"{OUT}/img", exist_ok=True)

def get(params):
    params = {**params, "format": "json", "formatversion": "2"}
    url = API + "?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.load(r)
        except Exception as e:
            print("retry", url, e); time.sleep(2 + attempt * 3)
    raise RuntimeError(url)

def category(cat):
    out, cont = [], {}
    while True:
        d = get({"action": "query", "list": "categorymembers", "cmtitle": cat, "cmlimit": "500", **cont})
        out += [m["title"] for m in d["query"]["categorymembers"] if m["ns"] == 0]
        if "continue" not in d: return out
        cont = d["continue"]

TOONS = ["Boxten","Poppy","Shrimpo","Toodles","Finn","Tisha","Cosmo","Looey","Rodger","Brightney","Brusha",
 "Teagan","Flutter","Scraps","Goob","Connie","Gigi","Razzle & Dazzle","Yatta","Glisten","Blot","Squirm",
 "Waxwell","Pebble","Shelly","Sprout","Vee","Astro","Gourdy","Eclipse","Soulvester","Ribecca","Bobette",
 "Coal","Ginger","Rudie","Bassie","Cocoa","Flyte","Eggson"]
EXTRA = ["Skins","Toon Skins","Mastery","Mastery Quests","Vintage Skins","Holiday Quests","Golden Skins","Toons"]

pages = list(dict.fromkeys(TOONS + EXTRA))
for cat in ["Category:Toons", "Category:Skins", "Category:Regular Toons", "Category:Main Characters"]:
    try:
        found = category(cat); print(cat, len(found)); pages += [p for p in found if p not in pages]
    except Exception as e:
        print("cat fail", cat, e)

index = {"pages": {}, "images": {}}
for title in pages:
    try:
        d = get({"action": "parse", "page": title, "prop": "wikitext|images", "redirects": "1"})
    except Exception as e:
        print("page fail", title, e); continue
    if "error" in d:
        print("missing", title); continue
    p = d["parse"]
    safe = re.sub(r"[^A-Za-z0-9_-]+", "_", p["title"])
    open(f"{OUT}/raw/{safe}.wikitext", "w", encoding="utf-8").write(p["wikitext"])
    index["pages"][title] = {"resolved": p["title"], "file": f"raw/{safe}.wikitext", "images": p.get("images", [])}
    print("ok", title, len(p["wikitext"]), "imgs", len(p.get("images", [])))
    time.sleep(0.3)

# Resolve URLs de todas as imagens referenciadas (em lotes de 50).
all_imgs = sorted({i for v in index["pages"].values() for i in v["images"]})
for i in range(0, len(all_imgs), 50):
    batch = all_imgs[i:i+50]
    d = get({"action": "query", "titles": "|".join("File:" + n for n in batch), "prop": "imageinfo", "iiprop": "url|size"})
    for pg in d["query"]["pages"]:
        if "imageinfo" in pg:
            ii = pg["imageinfo"][0]
            index["images"][pg["title"][5:]] = {"url": ii["url"], "w": ii.get("width"), "h": ii.get("height")}
json.dump(index, open(f"{OUT}/images.json", "w"), indent=1, ensure_ascii=False)
print("pages", len(index["pages"]), "images", len(index["images"]))
