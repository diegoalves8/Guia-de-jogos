"""Gera o conteúdo do Dandy's World (Toons, skins, maestria) a partir do wiki-data."""
import json, re, sys, urllib.parse
W = "/home/claude/wiki"
idx = json.load(open(f"{W}/images.json"))
IMG = idx["images"]
SKINS = json.load(open("/home/claude/scripts/skins_raw.json"))
TODAY = "2026-11-07"

def img(fn, width=400):
    i = IMG.get(fn)
    if not i: return ""
    u = i["url"]
    return u.replace("/revision/latest", f"/revision/latest/scale-to-width-down/{width}", 1)

def clean(s):
    s = re.sub(r"<ref[^>]*>.*?</ref>|<ref[^>]*/>", "", s, flags=re.S)
    s = re.sub(r"\{\{Tooltip\|((?:[^{}|]|\[\[[^\]]*\]\])+)\|[^{}]*\}\}", r"\1", s)
    s = re.sub(r"\{\{Currency\|Research\|([^}|]+)\}\}", r"\1", s)
    s = re.sub(r"\{\{Currency\|([^}|]+)\|([^}|]+)\}\}", r"\2 \1", s)
    s = re.sub(r"\{\{(?:CI|TI|AI|HT|II|Type|Skin)\|([^|}]+)[^}]*\}\}", r"\1", s)
    s = re.sub(r"\[\[(?:[^|\]]*\|)?([^\]]+)\]\]", r"\1", s)
    s = re.sub(r"\{\{[^{}]*\}\}", "", s)
    s = re.sub(r"<[^>]+>", " ", s).replace("'''", "").replace("''", "")
    return re.sub(r"\s+", " ", s).strip()

def field(text, name):
    m = re.search(r"^\|" + name + r"\s*=(.*?)(?=^\||^\}\})", text, flags=re.M | re.S)
    return m.group(1).strip() if m else ""

def split_top(body):
    parts, depth, cur, i = [], 0, "", 0
    while i < len(body):
        two = body[i:i+2]
        if two in ("{{", "[["): depth += 1; cur += two; i += 2; continue
        if two in ("}}", "]]"): depth -= 1; cur += two; i += 2; continue
        if body[i] == "|" and depth == 0: parts.append(cur); cur = ""; i += 1; continue
        cur += body[i]; i += 1
    return parts + [cur]

def num(n):
    n = int(n); return f"{n:,}".replace(",", ".")

# ---------- traduções ----------
def tr_quest(q):
    q = q.strip().rstrip(".")
    R = [
      (r"^Finish (\d+) Machines$", lambda m: f"Terminar {num(m[1])} máquinas"),
      (r"^Survive (\d+) Floors$", lambda m: f"Sobreviver a {num(m[1])} andares"),
      (r"^Travel (\d+) [Mm]eters$", lambda m: f"Percorrer {num(m[1])} metros"),
      (r"^Pick [Uu]p (\d+) Research Capsules$", lambda m: f"Pegar {num(m[1])} cápsulas de pesquisa"),
      (r"^Pick [Uu]p (\d+) Items$", lambda m: f"Pegar {num(m[1])} itens"),
      (r"^Use (\d+) Items$", lambda m: f"Usar {num(m[1])} itens"),
      (r"^Use (\d+) (.+)$", lambda m: f"Usar {num(m[1])} {m[2]} (item)"),
      (r"^Use this Toon's Active Ability (\d+) [Tt]imes$", lambda m: f"Usar a habilidade ativa deste Toon {num(m[1])} vezes"),
      (r"^Activate [Tt]his Toon's Passive Ability (\d+) times$", lambda m: f"Ativar a habilidade passiva deste Toon {num(m[1])} vezes"),
      (r"^Purchase (\d+) [Ii]tems from Dandy's Elevator Shop$", lambda m: f"Comprar {num(m[1])} itens na loja do elevador do Dandy"),
      (r"^Experience (\d+) Blackouts$", lambda m: f"Passar por {num(m[1])} apagões (Blackouts)"),
      (r"^Experience (\d+) Ichor Spills$", lambda m: f"Passar por {num(m[1])} vazamentos de Ichor (Ichor Spills)"),
      (r"^Encounter (\d+) Twisteds$", lambda m: f"Encontrar {num(m[1])} Twisteds"),
      (r"^Reach Floor (\d+)$", lambda m: f"Chegar ao andar {m[1]}"),
      (r"^Survive (\d+) Floors with (\d+) other Players in your round$", lambda m: f"Sobreviver a {m[1]} andares com outros {m[2]} jogadores na partida"),
      (r"^Survive (\d+) Floors with (\d+) or more Players in your round$", lambda m: f"Sobreviver a {m[1]} andares com {m[2]} ou mais jogadores na partida"),
      (r"^Survive (\d+) Floors with any Main in your round$", lambda m: f"Sobreviver a {m[1]} andares com algum personagem principal (Main) na partida"),
      (r"^Survive (\d+) Floors with (.+?) in your (?:round|run)$", lambda m: f"Sobreviver a {m[1]} andares com {m[2].replace(' and ', ' e ')} na partida"),
      (r"^Complete (\d+) Duo Machines with another Player$", lambda m: f"Completar {m[1]} máquinas em dupla com outro jogador"),
      (r"^Complete Floor (\d+) with the (.+) equipped$", lambda m: f"Completar o andar {m[1]} com o trinket {m[2]} equipado"),
      (r"^Eat from (\d+) Bookshelves$", lambda m: f"Comer de {m[1]} estantes de livros (Bookshelves)"),
      (r"^Collect (\d+)% of Twisted Research$", lambda m: f"Juntar {num(m[1])}% de pesquisa de Twisteds no total"),
    ]
    for pat, f in R:
        m = re.match(pat, q)
        if m: return f(m)
    print("SEM TRADUÇÃO (missão):", q, file=sys.stderr); return q

CUR = {"Ichor": "Ichor", "Pumpkins": "Pumpkins (abóboras)", "Ornaments": "Ornaments (enfeites de Natal)", "Baskets": "Baskets (cestas de Páscoa)"}
def tr_req(r):
    r = r.strip().lstrip("*").strip()
    m = re.match(r"^(\d+) (Ichor|Pumpkins|Ornaments|Baskets)$", r)
    if m: return ("cost", f"{num(m[1])} {CUR[m[2]]}")
    m = re.match(r"^(\d+)$", r)
    if m: return ("cost", f"{num(m[1])} Ichor")
    R = [
      (r"^(\d+)% Research on Twisted (.+)$", lambda m: f"{m[1]}% de pesquisa do Twisted {m[2]}"),
      (r"^(\d+)% Research on any Twisted$", lambda m: f"{m[1]}% de pesquisa de qualquer Twisted"),
      (r"^(\d+)% Research on (\d+) Twisteds$", lambda m: f"{m[1]}% de pesquisa em {m[2]} Twisteds diferentes"),
      (r"^Complete all Mastery Quests on (.+)$", lambda m: f"Completar toda a Maestria de {m[1]}"),
      (r"^Complete (\d+) Mastery Quests on (.+)$", lambda m: f"Completar {m[1]} missões de Maestria de {m[2]}"),
      (r"^Encounter Twisted (.+)$", lambda m: f"Encontrar o Twisted {m[1]} em uma partida"),
      (r"^Buy (\d+) Items from Dandy's Elevator Shop$", lambda m: f"Comprar {m[1]} itens na loja do elevador do Dandy"),
      (r"^Experience (\d+) Blackouts$", lambda m: f"Passar por {m[1]} apagões (Blackouts)"),
      (r"^Finish (\d+) Machines$", lambda m: f"Terminar {num(m[1])} máquinas"),
      (r"^Own (\d+) or more Toons$", lambda m: f"Ter {m[1]} ou mais Toons"),
      (r"^Own (\d+) or more Trinkets$", lambda m: f"Ter {m[1]} ou mais Trinkets"),
      (r"^Own (\d+)\+ Titles$", lambda m: f"Ter {m[1]} ou mais Títulos"),
      (r"^Pick up (\d+) Rare or higher tier Items$", lambda m: f"Pegar {m[1]} itens raros ou melhores"),
      (r"^Survive (\d+) Floors$", lambda m: f"Sobreviver a {m[1]} andares"),
    ]
    for pat, f in R:
        m = re.match(pat, r)
        if m: return ("req", f(m))
    print("SEM TRADUÇÃO (requisito):", r, file=sys.stderr); return ("req", r)

# ---------- classificação ----------
MAIN = {"Pebble", "Shelly", "Sprout", "Vee", "Astro"}
EVENT = {"Gourdy": "Halloween", "Eclipse": "Halloween", "Soulvester": "Halloween", "Ribecca": "Halloween",
         "Bobette": "Natal", "Coal": "Natal", "Ginger": "Natal", "Rudie": "Natal",
         "Bassie": "Páscoa", "Cocoa": "Páscoa", "Flyte": "Páscoa", "Eggson": "Páscoa"}
EVENT_MAIN = {"Gourdy", "Bobette", "Bassie"}
STORE = {"Halloween": "Gourdy's Store (loja de Halloween)", "Natal": "Bobette's Store (loja de Natal)", "Páscoa": "Bassie's Store (loja de Páscoa)"}
ORDER = ["Boxten","Poppy","Shrimpo","Toodles","Finn","Tisha","Cosmo","Looey","Rodger","Brightney","Brusha",
 "Teagan","Flutter","Scraps","Goob","Connie","Gigi","Razzle & Dazzle","Yatta","Glisten","Blot","Squirm",
 "Waxwell","Pebble","Shelly","Sprout","Vee","Astro","Gourdy","Eclipse","Soulvester","Ribecca","Bobette",
 "Coal","Ginger","Rudie","Bassie","Cocoa","Flyte","Eggson"]

def skin_how(s):
    c, sub, toon = s["cat"], s["sub"], s["toon"]
    if c == "Vintage": return "Vintage", f"Complete todas as missões de Maestria de {toon}. Grátis."
    if c == "Dyle's Store Skins": return "Dyle's Store", "Compre na Dyle's Store (lobby), aba Skins, por 600 Ichor."
    if c == "Event-Exclusive":
        if "Christmas" in sub: return "Natal", "Loja de Natal da Bobette, paga com Ornaments. Só durante o evento de Natal."
        if "Easter" in sub: return "Páscoa", "Loja de Páscoa da Bassie, paga com Baskets. Só durante o evento de Páscoa."
        if "Halloween" in sub: return "Halloween", "Loja de Halloween do Gourdy, paga com Pumpkins. Só durante o evento de Halloween."
    if c == "Golden Skins":
        if sub == "Toon of the Week": return "Dourada", f"Missões do Toon da Semana (Toon of the Week) de {toon}."
        ev = EVENT.get(toon, "")
        return f"Dourada de {ev}" if ev else "Dourada", f"Complete as missões do evento de {ev or 'feriado'} jogando com {toon}. Só durante o evento."
    if c == "Gamepasses":
        if sub == "Toon of the Week": return "Robux", "Gamepass do Toon da Semana, comprado com Robux na Dyle's Store."
        return "Robux", f"Pacote cosmético {sub.replace(' Skins','')} (Gamepass), 40 Robux por Toon do pacote."
    if c == "Advent Calendar": return "Calendário do Advento", "Prêmio do Calendário do Advento (evento de Natal)."
    if c == "Code": return "Código", "Resgatada com código. Segundo a wiki, o código volta todo 1º de abril."
    return "Especial", "Forma de obtenção especial (consulte a wiki)."

CAT_ORDER = ["Vintage", "Dourada de Natal", "Dourada de Halloween", "Dourada de Páscoa", "Dourada", "Dyle's Store",
             "Natal", "Halloween", "Páscoa", "Calendário do Advento", "Robux", "Código", "Especial"]

toons = []
for name in ORDER:
    f = idx["pages"][name]["file"]
    text = open(f"{W}/{f}", encoding="utf-8").read()
    raw_reqs = []
    for n in range(1, 6):
        v = field(text, f"requirement_{n}")
        if v:
            raw_reqs += [x for x in re.split(r"\s*\*\s*", clean(v)) if x]
    cost, reqs = [], []
    if name in ("Boxten", "Poppy"):
        other = "Poppy" if name == "Boxten" else "Boxten"
        cost = ["Grátis (Toon inicial)"]
        reqs = [f"Grátis se escolhido no início do jogo; se você escolheu {other}, custa 100 Ichor na Dandy's Store"]
    else:
        for r in raw_reqs:
            kind, val = tr_req(r)
            (cost if kind == "cost" else reqs).append(val)
    m = re.search(r"\{\{Mastery\|(.*?)\}\}\s*$", text, flags=re.M)
    quests = [tr_quest(clean(p)) for p in split_top(m.group(1)) if clean(p)] if m else []
    gal = dict((k, v.strip()) for v, k in re.findall(r"^([^|\n]+\.(?:png|gif|jpg|webp))\|(Render|Full Render|Icon)$", field(text, "image1"), flags=re.M | re.I))
    render = img(gal.get("Render") or gal.get("Full Render") or gal.get("Icon", ""))
    tskins = []
    for s in SKINS:
        if s["toon"] != name: continue
        cat, how = skin_how(s)
        tskins.append({"name": s["skin"], "image": img(s["file"]), "category": cat, "howTo": how})
    tskins.sort(key=lambda x: CAT_ORDER.index(x["category"]) if x["category"] in CAT_ORDER else 99)
    vintage = next((s["image"] for s in tskins if s["category"] == "Vintage"), "")
    ev = EVENT.get(name)
    if name in MAIN: kind = "Personagem principal"
    elif ev: kind = f"Toon de evento ({ev})" + (" · principal" if name in EVENT_MAIN else "")
    else: kind = "Toon comum"
    store = STORE[ev] if ev else "Dandy's Store (lobby)"
    slug = re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")
    tutorial = [
        {"title": f"Conheça {name}", "text": f"{kind}. Toque nas abas abaixo para ver as skins e a Maestria.", "image": render},
        {"title": "1. Cumpra os requisitos", "text": "\n".join(["• " + x for x in cost + reqs]) or "Sem requisitos extras.", "image": render},
        {"title": "2. Desbloqueie o Toon", "text": f"No lobby, vá até a {store} e compre {name}." + (" Só aparece durante o evento." if ev else ""), "image": render},
        {"title": "3. Faça a Maestria", "text": "Jogue com este Toon e complete:\n" + "\n".join("• " + q for q in quests), "image": render},
        {"title": "4. Ganhe a skin Vintage", "text": f"Completou todas as missões? A skin Vintage {name} é sua, de graça!", "image": vintage or render},
    ]
    q = urllib.parse.quote_plus(f"Dandy's World {name} mastery vintage")
    toons.append({"id": slug, "name": name, "image": render, "vintageImage": vintage, "kind": kind,
                  "cost": " + ".join(cost), "requirements": reqs, "mastery": quests,
                  "skins": tskins, "tutorial": tutorial,
                  "videoUrl": f"https://www.youtube.com/results?search_query={q}"})

json.dump(toons, open("/home/claude/scripts/toons_final.json", "w"), ensure_ascii=False, indent=1)
print("toons", len(toons), "skins", sum(len(t["skins"]) for t in toons))
for t in toons:
    if not t["image"] or not t["vintageImage"] or not t["mastery"] or not t["cost"]:
        print("CHECK", t["name"], bool(t["image"]), bool(t["vintageImage"]), len(t["mastery"]), t["cost"])
