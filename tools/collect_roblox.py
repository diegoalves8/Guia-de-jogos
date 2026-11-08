"""
Coleta dados de jogos do Roblox para o Guia de Jogos (roda no GitHub Actions, que tem internet).
Fontes:
  - wikis Fandom (MediaWiki API, conteúdo CC BY-SA): texto de todas as páginas, imagem principal e categorias;
  - API pública do Roblox: nome, criador, descrição, visitas, data da última atualização, ícone e thumbnails.
Saída: <out>/<jogo>/pages.json e <out>/<jogo>/roblox.json  (gravado na branch roblox-data)
"""
import json, os, sys, time, urllib.parse, urllib.request

OUT = sys.argv[1] if len(sys.argv) > 1 else "roblox-data"
UA = "GuiaDeJogosBot/1.0 (app pessoal de guia; github.com/diegoalves8/Guia-de-jogos)"

GAMES = {
    "evomon": {"wikis": ["evomon.fandom.com", "evomon-roblox.fandom.com", "roblox-evomon.fandom.com", "evomonroblox.fandom.com", "evomon-wiki.fandom.com"],
               "places": [134381727982611], "universes": [9826885587]},
    "jujutsu-shenanigans": {"wikis": ["jujutsu-shenanigans.fandom.com", "jujutsushenanigans.fandom.com"],
                            "places": [9391468976], "universes": []},
    "ninja-tycoon": {"wikis": ["ninja-tycoon.fandom.com"], "places": [221718525], "universes": []},
}


def fetch(url, tries=3, data=None):
    req = urllib.request.Request(url, data=data, headers={"User-Agent": UA, "Accept": "application/json", "Content-Type": "application/json"})
    for a in range(tries):
        try:
            with urllib.request.urlopen(req, timeout=40) as r:
                return r.read().decode("utf-8")
        except Exception as e:
            print("retry", url[:120], e)
            time.sleep(2 + 3 * a)
    return None


def jget(url):
    t = fetch(url)
    try:
        return json.loads(t) if t else None
    except Exception:
        return None


def save(path, data):
    path = os.path.join(OUT, path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)


def api(domain, params):
    params = {**params, "format": "json", "formatversion": "2"}
    return jget(f"https://{domain}/api.php?" + urllib.parse.urlencode(params))


def collect_wiki(game, domains):
    dom = None
    for d in domains:
        si = api(d, {"action": "query", "meta": "siteinfo"})
        if si and "query" in si:
            dom = d
            print(game, "wiki:", d, si["query"]["general"].get("sitename"))
            break
    if not dom:
        print(game, "SEM WIKI")
        return {"domain": None, "pages": {}}
    titles, cont = [], {}
    while True:
        r = api(dom, {"action": "query", "list": "allpages", "apnamespace": 0, "aplimit": 500, "apfilterredir": "nonredirects", **cont})
        if not r:
            break
        titles += [p["title"] for p in r["query"]["allpages"]]
        if "continue" in r and len(titles) < 3000:
            cont = r["continue"]
        else:
            break
    print(game, "páginas:", len(titles))
    pages = {}
    for i in range(0, len(titles), 25):
        batch = titles[i:i + 25]
        r = api(dom, {"action": "query", "titles": "|".join(batch), "prop": "revisions|pageimages|categories",
                      "rvprop": "content", "rvslots": "main", "piprop": "original", "cllimit": "max"})
        if not r:
            continue
        for p in r["query"].get("pages", []):
            rev = (p.get("revisions") or [{}])[0]
            text = rev.get("slots", {}).get("main", {}).get("content", "")
            pages[p["title"]] = {"text": text, "image": (p.get("original") or {}).get("source", ""),
                                 "categories": [c["title"].replace("Category:", "") for c in p.get("categories", [])]}
        time.sleep(0.3)
    # Arquivos de imagem citados nas páginas (para resolver [[File:x.png]] e |image = x.png)
    files = set()
    import re
    for p in pages.values():
        for m in re.finditer(r"(?:File|Image|Arquivo):([^|\]\n]+?\.(?:png|jpg|jpeg|gif|webp))", p["text"], re.I):
            files.add(m.group(1).strip())
        for m in re.finditer(r"=\s*([^|=\n\[\]{}]+?\.(?:png|jpg|jpeg|gif|webp))", p["text"], re.I):
            files.add(m.group(1).strip())
    urls, files = {}, sorted(files)
    for i in range(0, len(files), 40):
        r = api(dom, {"action": "query", "titles": "|".join("File:" + f for f in files[i:i + 40]), "prop": "imageinfo", "iiprop": "url"})
        if not r:
            continue
        norm = {n["to"]: n["from"] for n in r["query"].get("normalized", [])}
        for p in r["query"].get("pages", []):
            ii = p.get("imageinfo")
            if ii:
                name = norm.get(p["title"], p["title"]).split(":", 1)[1]
                urls[name] = ii[0]["url"]
                urls[p["title"].split(":", 1)[1]] = ii[0]["url"]
        time.sleep(0.3)
    print(game, "arquivos:", len(urls))
    return {"domain": dom, "pages": pages, "files": urls}


def collect_roblox(game, cfg):
    out = {"universes": []}
    unis = list(cfg["universes"])
    for pid in cfg["places"]:
        r = jget(f"https://apis.roblox.com/universes/v1/places/{pid}/universe")
        if r and r.get("universeId"):
            unis.append(r["universeId"])
    unis = sorted(set(unis))
    if not unis:
        return out
    ids = ",".join(map(str, unis))
    out["games"] = (jget(f"https://games.roblox.com/v1/games?universeIds={ids}") or {}).get("data", [])
    out["icons"] = (jget(f"https://thumbnails.roblox.com/v1/games/icons?universeIds={ids}&size=512x512&format=Png&isCircular=false") or {}).get("data", [])
    out["thumbnails"] = (jget(f"https://thumbnails.roblox.com/v1/games/multiget/thumbnails?universeIds={ids}&countPerUniverse=5&size=768x432&format=Png") or {}).get("data", [])
    out["votes"] = (jget(f"https://games.roblox.com/v1/games/votes?universeIds={ids}") or {}).get("data", [])
    out["universes"] = unis
    for g in out["games"]:
        print(game, "roblox:", g.get("name"), g.get("creator", {}).get("name"), g.get("visits"), g.get("updated"))
    return out


def download(url, path):
    try:
        req = urllib.request.Request(url, headers={"User-Agent": UA})
        with urllib.request.urlopen(req, timeout=40) as r:
            data = r.read()
        path = os.path.join(OUT, path); os.makedirs(os.path.dirname(path), exist_ok=True)
        open(path, "wb").write(data)
        return True
    except Exception as e:
        print("falha ao baixar", url, e)
        return False


for game, cfg in GAMES.items():
    w = collect_wiki(game, cfg["wikis"])
    save(f"{game}/pages.json", w)
    rb = collect_roblox(game, cfg)
    save(f"{game}/roblox.json", rb)
    # Ícone e capas do jogo (as URLs do Roblox expiram; o app usa cópias guardadas no repositório)
    for ic in rb.get("icons", [])[:1]:
        if ic.get("imageUrl"): download(ic["imageUrl"], f"{game}/media/icon.png")
    for th in rb.get("thumbnails", [])[:1]:
        for i, t in enumerate(th.get("thumbnails", [])[:5]):
            if t.get("imageUrl"): download(t["imageUrl"], f"{game}/media/thumb{i + 1}.png")
print("fim")
