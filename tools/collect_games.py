"""
Coleta dados de vários jogos para o Guia de Jogos (roda no GitHub Actions).
Fontes: API pública Brawlify (Brawl Stars) e wikis Fandom (MediaWiki API, conteúdo CC BY-SA).
Saída: <out>/<jogo>/...  (gravado na branch game-data)
"""
import json, os, re, sys, time, urllib.parse, urllib.request

OUT = sys.argv[1] if len(sys.argv) > 1 else "game-data"
UA = "GuiaDeJogosBot/1.0 (app pessoal de guia; github.com/diegoalves8/Guia-de-jogos)"

def fetch(url, tries=3):
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "application/json"})
    for a in range(tries):
        try:
            with urllib.request.urlopen(req, timeout=40) as r:
                return r.read().decode("utf-8")
        except Exception as e:
            print("retry", url[:120], e); time.sleep(2 + 3 * a)
    return None

def save(path, data):
    path = os.path.join(OUT, path); os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(data if isinstance(data, str) else json.dumps(data, ensure_ascii=False, indent=1))

# ---------- Brawl Stars (Brawlify) ----------
for name, url in [("brawlers", "https://api.brawlify.com/v1/brawlers"),
                  ("gamemodes", "https://api.brawlify.com/v1/gamemodes"),
                  ("events", "https://api.brawlify.com/v1/events")]:
    t = fetch(url)
    print("brawlify", name, "ok" if t else "FALHOU", len(t or ""))
    if t: save(f"brawl-stars/{name}.json", t)

# ---------- Wikis Fandom ----------
WIKIS = {
    "brawl-stars": (["brawlstars.fandom.com"], ["Category:Skins", "Category:Brawlers"]),
    "free-fire": (["freefire.fandom.com", "free-fire.fandom.com", "garena-free-fire.fandom.com"],
                  ["Category:Characters", "Category:Pets", "Category:Weapons"]),
    "murder-mystery": (["murder-mystery-2.fandom.com", "murdermystery2.fandom.com"],
                       ["Category:Godly", "Category:Godlies", "Category:Ancient", "Category:Ancients",
                        "Category:Knives", "Category:Guns", "Category:Pets", "Category:Chroma", "Category:Game Modes"]),
    "blox-fruits": (["blox-fruits.fandom.com"],
                    ["Category:Blox Fruits", "Category:Fruits", "Category:Natural", "Category:Elemental",
                     "Category:Beast", "Category:Fighting Styles", "Category:Swords", "Category:Races", "Category:Islands"]),
}
EXTRA_PAGES = {
    "free-fire": ["Characters", "Free Fire", "Diamonds", "Gold", "Pets", "Ranked Mode", "Weapons"],
    "murder-mystery": ["Murder Mystery 2", "Murderer", "Sheriff", "Innocent", "Hero", "Godly", "Ancient", "Unique", "Chroma", "Coins", "Levels", "Prestige"],
    "blox-fruits": ["Blox Fruits", "Fruits", "Blox Fruit Dealer", "Beli", "Fragments", "Races", "Fighting Styles", "Seas", "First Sea", "Second Sea", "Third Sea", "Leveling"],
    "brawl-stars": ["Brawlers", "Skins", "Starr Road", "Credits", "Gems", "Brawl Pass", "Trophies", "Ranked"],
}

def api(domain, params):
    params = {**params, "format": "json", "formatversion": "2"}
    t = fetch(f"https://{domain}/api.php?" + urllib.parse.urlencode(params))
    return json.loads(t) if t else None

for game, (domains, cats) in WIKIS.items():
    dom = None
    for d in domains:
        si = api(d, {"action": "query", "meta": "siteinfo"})
        if si and "query" in si:
            dom = d; print(game, "wiki:", d, si["query"]["general"]["sitename"]); break
    if not dom:
        print(game, "NENHUMA WIKI ENCONTRADA"); continue
    meta = {"domain": dom, "categories": {}, "pages": {}, "pageimages": {}}
    # lista das maiores categorias (para ajustar a coleta depois)
    ac = api(dom, {"action": "query", "list": "allcategories", "acprop": "size", "aclimit": "500", "acmin": "5"})
    if ac: meta["allcategories"] = sorted([(c["category"], c["size"]) for c in ac["query"]["allcategories"]], key=lambda x: -x[1])[:250]
    titles = list(EXTRA_PAGES.get(game, []))
    for cat in cats:
        members, cont = [], {}
        while True:
            d = api(dom, {"action": "query", "list": "categorymembers", "cmtitle": cat, "cmlimit": "500", **cont})
            if not d or "query" not in d: break
            members += [m["title"] for m in d["query"]["categorymembers"] if m["ns"] == 0]
            if "continue" not in d: break
            cont = d["continue"]
        meta["categories"][cat] = members
        print(game, cat, len(members))
        titles += members
    titles = list(dict.fromkeys(titles))[:700]
    for t in titles:
        d = api(dom, {"action": "parse", "page": t, "prop": "wikitext", "redirects": "1"})
        if not d or "parse" not in d: continue
        safe = re.sub(r"[^A-Za-z0-9_-]+", "_", d["parse"]["title"])[:80]
        save(f"{game}/raw/{safe}.wikitext", d["parse"]["wikitext"])
        meta["pages"][t] = f"raw/{safe}.wikitext"
        time.sleep(0.15)
    for i in range(0, len(titles), 50):
        d = api(dom, {"action": "query", "titles": "|".join(titles[i:i+50]), "prop": "pageimages", "piprop": "original|thumbnail", "pithumbsize": "400", "redirects": "1"})
        if not d or "query" not in d: continue
        for pg in d["query"].get("pages", []):
            if "original" in pg or "thumbnail" in pg:
                meta["pageimages"][pg["title"]] = {"original": pg.get("original", {}).get("source"), "thumb": pg.get("thumbnail", {}).get("source")}
    save(f"{game}/meta.json", meta)
    print(game, "páginas", len(meta["pages"]), "imagens", len(meta["pageimages"]))
