"""
Monta o conteúdo de Evomon, Jujutsu Shenanigans e Ninja Tycoon a partir da coleta (branch roblox-data).
Uso: python3 tools/build_roblox.py <pasta-da-branch-roblox-data>
Fontes: wikis Fandom (CC BY-SA) + API pública do Roblox + sites de códigos citados em cada código.
"""
import json, os, re, shutil, sys
from PIL import Image

SRC = sys.argv[1] if len(sys.argv) > 1 else "/home/claude/rd"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
CONTENT = os.path.join(ROOT, "app", "src", "main", "assets", "content")
MEDIA = os.path.join(ROOT, "media", "roblox")
RAW = "https://raw.githubusercontent.com/diegoalves8/Guia-de-jogos/main/media/roblox"
TODAY = "2026-11-07"


# ---------------------------------------------------------------- utilidades
def load(game):
    return json.load(open(os.path.join(SRC, game, "pages.json"), encoding="utf-8")), json.load(open(os.path.join(SRC, game, "roblox.json"), encoding="utf-8"))


def small(url, w=300):
    """Imagem da Fandom reduzida (carrega rápido no carro e no celular)."""
    if not url or "nocookie.net" not in url or "scale-to-width" in url:
        return url
    base, _, q = url.partition("?")
    if "/revision/latest" in base:
        base = base.split("/revision/latest")[0] + f"/revision/latest/scale-to-width-down/{w}"
    return base + ("?" + q if q else "")


def clean(t):
    t = t or ""
    t = re.sub(r"<span[^>]*>|</span>|<big>|</big>|<small>|</small>|<u>|</u>|<center>|</center>|<br\s*/?>", " ", t)
    t = re.sub(r"\[\[(?:File|Image|Arquivo):[^\]]*\]\]", "", t)
    t = re.sub(r"\{\{V\|([\d.]+)\}\}", r"V\1", t)
    t = re.sub(r"(?i)\{\{(?:JJK|Update)\|([^}|]+)\}\}", r"\1", t)
    t = re.sub(r"\{\{([A-Z][^{}|]{1,40})\}\}", r"\1", t)  # {{Vessel}} -> Vessel
    t = re.sub(r"\{\{[^{}]*\}\}", "", t)
    t = re.sub(r"\[\[[^\]|]*\|([^\]]*)\]\]", r"\1", t)
    t = re.sub(r"\[\[([^\]]*)\]\]", r"\1", t)
    t = re.sub(r"\[https?://\S+ ([^\]]+)\]", r"\1", t)
    t = re.sub(r"'''?", "", t)
    t = re.sub(r"<[^>]+>", "", t)
    return re.sub(r"\s+", " ", t).strip(" :-|")


def slug(s):
    s = re.sub(r"[^a-z0-9]+", "-", s.lower()).strip("-")
    return s or "x"


def section(text, name):
    m = re.search(r"^==\s*" + re.escape(name) + r"\s*==\s*$(.*?)(?=^==[^=]|\Z)", text, re.S | re.M)
    return m.group(1) if m else ""


def field(text, name):
    text = re.sub(r"\{\{Robux\}\}", "Robux", text)
    m = re.search(r"\|\s*" + name + r"\s*=([^|]*?)(?=\n\||\|\w+\s*=|\}\})", text, re.S)
    return m.group(1).strip() if m else ""


def cat(id, title, emoji, desc, sections, status="available"):
    return {"id": id, "title": title, "emoji": emoji, "description": desc, "status": status, "updatedAt": TODAY, "sections": sections}


def code(c, reward, status, note=""):
    return {"code": c, "reward": reward, "status": status, "note": note}


def creator(name, country, about, youtube="", tiktok="", source=""):
    return {"name": name, "country": country, "about": about, "youtube": youtube, "tiktok": tiktok, "source": source}


def yt(q):
    return "https://www.youtube.com/results?search_query=" + re.sub(r"\s+", "+", q.strip())


def media_copy(game):
    """Copia ícone e capas do Roblox para o repositório (as URLs do Roblox expiram)."""
    src = os.path.join(SRC, game, "media")
    dst = os.path.join(MEDIA, game)
    os.makedirs(dst, exist_ok=True)
    out = {}
    for f in sorted(os.listdir(src)) if os.path.isdir(src) else []:
        im = Image.open(os.path.join(src, f)).convert("RGB")
        name = f.replace(".png", ".jpg")
        im.save(os.path.join(dst, name), "JPEG", quality=86, optimize=True)
        out[name.split(".")[0]] = f"{RAW}/{game}/{name}"
    return out


def save_game(game_id, data):
    json.dump(data, open(os.path.join(CONTENT, "games", game_id + ".json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)


def news_sections(events=True):
    return [s for s in [cat("novidades", "Novidades", "📰", "Últimas notícias do jogo, com fonte.", [], "coming_soon"),
            cat("eventos", "Eventos", "🎉", "Eventos ativos e próximas datas.", [], "coming_soon"),
            cat("semana", "Coisas da Semana", "📅", "O que está acontecendo nesta semana.", [], "coming_soon")] if events or s["id"] != "eventos"]


SUMMARY = {}

# ================================================================ EVOMON
def build_evomon():
    w, rb = load("evomon")
    pages, files = w["pages"], w.get("files", {})
    med = media_copy("evomon")
    EL = {"Normal": "Normal", "Fire": "Fogo", "Water": "Água", "Grass": "Planta", "Electric": "Elétrico", "Ice": "Gelo",
          "Fighting": "Lutador", "Ground": "Terra", "Rock": "Pedra", "Flying": "Voador", "Bug": "Inseto", "Dragon": "Dragão",
          "Psychic": "Psíquico", "Poison": "Veneno", "Steel": "Aço", "Dark": "Sombrio", "Light": "Luz"}
    STAT = {"HP": "Vida (HP)", "Attack": "Ataque", "Sp. Attack": "Ataque Especial", "Defence": "Defesa", "Defense": "Defesa",
            "Sp. Defence": "Defesa Especial", "Sp. Defense": "Defesa Especial", "Speed": "Velocidade", "Flair": "Flair (total)"}

    def obtain_pt(t, name):
        raw = clean(t)
        if not raw:
            return []
        out = []
        if "opening cutscene" in raw:
            out.append("Inicial: dá para escolher na abertura do jogo (junto com Bubble, Blazpup e Leafbun).")
            if "invite" in raw:
                out.append("Se não escolheu na abertura, sai nas recompensas por convidar amigos.")
            return out
        for part in re.split(r"\s*\*\s*|\n", t):
            p = clean(part)
            if not p:
                continue
            m = re.match(r"(?i).*?evolv\w* (?:from|it into)? ?([A-Z][\w ]+?)(?: starting at level (\d+))?\.?$", p)
            if re.match(r"(?i)catch after defeat\w* (.+?) boss (?:in|on) (.+)", p):
                mm = re.match(r"(?i)catch after defeat\w* (.+?) boss (?:in|on) (.+)", p)
                out.append(f"Capture depois de derrotar o chefe {mm.group(1).strip()} em {mm.group(2).strip()}.")
            elif re.match(r"(?i)catch by defeating the (.+?) boss on (.+)", p):
                mm = re.match(r"(?i)catch by defeating the (.+?) boss on (.+)", p)
                out.append(f"Capture derrotando o chefe {mm.group(1)} em {mm.group(2).replace('(seasonal world)', '(mundo da temporada)').rstrip('.')}.")
            elif re.match(r"(?i)catch (?:in|on) (.+)", p):
                loc = re.match(r"(?i)catch (?:in|on) (.+)", p).group(1).replace("on the floating rock", "(na pedra flutuante)").replace("(expedition islands)", "(Expedition Isles)")
                out.append(f"Capture em {loc.rstrip('.')}.")
            elif re.match(r"(?i)(can only be obtained from )?evolv\w*", p) and m:
                lv = f" a partir do nível {m.group(2)}" if m.group(2) else ""
                out.append(f"Evolui de {m.group(1).strip()}{lv}.")
            elif re.search(r"(?i)season pass", p):
                out.append("Passe de temporada" + (" (já encerrado)" if "expired" in p else "") + ".")
            elif re.search(r"(?i)battle pass", p):
                out.append("Passe de batalha.")
            elif re.search(r"(?i)seasonal store", p):
                out.append("Loja especial da temporada" + (" (já encerrada)" if "expired" in p else "") + ".")
            elif re.search(r"(?i)legacy store", p):
                out.append("Loja Legacy (com Legacy Coins).")
            elif re.match(r"(?i)season isle", p):
                out.append("Season Isle.")
            elif "Cyanrock" in p:
                out.append("Vá até Cyanrock Valley, nas Expedition Isles (precisa do nível de jogador 55).")
            else:
                out.append("Na wiki (inglês): " + p)
        return out

    mons = []
    for title, p in pages.items():
        t = p["text"]
        if not re.search(r"\{\{Evomon\s*\|", t):
            continue  # páginas sem ficha (só o nome) ficam de fora
        no = field(t, "no")
        el = re.search(r"element=\{\{(\w+)\}\}", t)
        el = el.group(1) if el else ""
        idx_file = field(t, "index_image").replace("{{PAGENAME}}", title).strip()
        full_file = field(t, "image").replace("{{PAGENAME}}", title).strip()
        img = files.get(full_file) or files.get(idx_file) or p["image"] or files.get(f"{title}.png", "")
        icon = files.get(idx_file) or img
        bio = clean(section(t, "Biography").split("\n")[0])
        evo = []
        for m in re.finditer(r"evolves? into \[\[([^\]|]+)[^\]]*\]\](?: starting)? at Level (\d+)", section(t, "Biography")):
            evo.append(f"Evolui para {m.group(1)} no nível {m.group(2)}")
        frm = re.search(r"(?i)evolv\w* from \[\[([^\]|]+)", t)
        stats = []
        tbl = section(t, "Base Stats")
        for m in re.finditer(r"\|\s*(HP|Attack|Sp\. Attack|Defen[cs]e|Sp\. Defen[cs]e|Speed|Flair)\s*\n\|\s*(\d+)", tbl):
            stats.append(f"{STAT.get(m.group(1), m.group(1))}: {m.group(2)}")
        obt = obtain_pt(section(t, "Obtainment"), title)
        mons.append({"name": title, "no": int(no) if no.isdigit() else 999, "el": el, "img": img, "icon": icon, "evo": evo,
                     "from": frm.group(1) if frm else "", "stats": stats, "obt": obt, "bio": bio})
    mons.sort(key=lambda m: (m["no"], m["name"]))
    # Toons agrupados por elemento
    groups = {}
    for m in mons:
        elpt = EL.get(m["el"], "Sem ficha na wiki")
        cost = m["obt"][0] if m["obt"] else ("Evolui de " + m["from"] if m["from"] else "Confira no jogo")
        tut = [{"title": f"Conheça {m['name']}", "text": f"Evomon do elemento {elpt}" + (f", nº {m['no']} na Evodex." if m["no"] < 999 else "."), "image": small(m["img"])}]
        if m["obt"]:
            tut.append({"title": "1. Onde conseguir", "text": " ".join(m["obt"]), "image": small(m["img"])})
        tut.append({"title": "2. Capture", "text": "Enfraqueça o Evomon selvagem na batalha e jogue uma bola. Advanced Balls aumentam a chance de captura.", "image": small(m["img"])})
        if m["evo"]:
            tut.append({"title": "3. Evolua", "text": "; ".join(m["evo"]) + ". Suba de nível batalhando ou com EXP Fruits.", "image": small(m["img"])})
        info = [{"title": "📋 Ficha", "items": [x for x in [f"Número: {m['no']}" if m["no"] < 999 else "", f"Elemento: {elpt}",
                                                        (f"Evolui de: {m['from']}" if m["from"] else ""), *m["evo"]] if x]}]
        if m["stats"]:
            info.append({"title": "📊 Status base", "items": m["stats"]})
        if m["obt"]:
            info.append({"title": "📍 Como conseguir", "items": m["obt"]})
        toon = {"id": slug(m["name"]), "name": m["name"], "image": small(m["img"]), "vintageImage": "", "kind": f"{elpt} · nº {m['no']}" if m["no"] < 999 else elpt,
                "cost": cost, "requirements": m["obt"][1:], "mastery": [], "skins": [], "tutorial": tut,
                "videoUrl": yt(f"{m['name']} Evomon Roblox"), "infoBlocks": info}
        groups.setdefault(elpt, []).append(toon)
    order = sorted(groups, key=lambda k: (k == "Sem ficha na wiki", -len(groups[k])))
    toon_sections = [{"type": "text", "title": "", "body": f"{len(mons)} Evomons da wiki da comunidade, com foto, elemento, evolução, status base e onde conseguir. O jogo tem mais de 200 Evomons (contando variantes Shiny e Sparkle); a lista cresce conforme a wiki é atualizada."}]
    toon_sections += [{"type": "toons", "title": f"{k} ({len(groups[k])})", "toons": groups[k]} for k in order]
    starters = [m for m in mons if m["name"] in ("Bubble", "Blazpup", "Leafbun")]
    cats = [
        cat("codigos", "Códigos e Como Resgatar", "🎁", "Códigos atuais e o passo a passo para resgatar.", [
            {"type": "steps", "title": "Como resgatar", "items": [
                "Abra o Evomon no Roblox e termine o tutorial (antes disso o campo de código não aparece).",
                "Toque no ícone de engrenagem (Configurações) no canto superior esquerdo.",
                "Cole o código no campo \"Enter code\" exatamente como está aqui (maiúsculas e minúsculas contam).",
                "Toque em OK para receber o prêmio."]},
            {"type": "codes", "title": "Códigos ativos", "codes": [
                code("EVOS4LIVE", "5 Trait Reroll Potions, 3 Nature Reroll Potions e 3 Talent Vector Potions", "active", "Código da Temporada 4. Confirmado pelo GamesRadar em 03/11/2026."),
                code("EVOMONSEASON3", "5 Trait Reroll Potions, 3 Talent Vector Potions e 3 Nature Reroll Potions", "active", "Confirmado pelo GamesRadar em 03/11/2026."),
                code("Wmidseason", "10 Legacy Coins e 10 Apex Medals", "active", "Confirmado pelo GamesRadar em 03/11/2026."),
                code("240evoRISE", "5 Summon Tickets e 3 Trait Reroll Potions", "active", "Confirmado pelo GamesRadar em 03/11/2026."),
                code("100DCUNITY", "3 Trait Reroll, 3 Talent Vector e 3 Nature Reroll Potions", "active", "Confirmado pelo GamesRadar em 03/11/2026."),
                code("D50CREW", "10 Evolution Stones e 10 Omni-Stones", "active", "Confirmado pelo GamesRadar em 03/11/2026."),
                code("20KMEMBERS", "Advanced Balls e Trait Reroll Potions", "active", "Confirmado pelo GamesRadar em 03/11/2026."),
                code("12klikeS", "5 Medium EXP Fruits", "active", "Confirmado pelo GamesRadar em 03/11/2026."),
                code("SEASON2GO", "10 Legacy Coins e 10 Apex Medals", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("EMBERDROP", "20 Evolution Stones", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("tunedUP", "10 Apex Medals e 500 Apex Silver", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("EVO120HYPE", "3 Summon Tickets e 2 Trait Reroll Potions", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("EVO60SPARK", "3 Summon Tickets e 2 Trait Reroll Potions", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("30K-LIKES", "2 Trait Reroll, 2 Talent Vector e 2 Nature Reroll Potions", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("20000LIKES", "2 Trait Reroll Potions e 5 Medium EXP Fruits", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("FORGROUP", "5 Medium EXP Fruits", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("DCGIFT", "10 Medium EXP Fruits e 5 Advanced Balls", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("5000DC", "5 Advanced Balls", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("THXFOR5K", "5 Medium EXP Fruits", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("2K-LIKES", "5 Medium EXP Fruits", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026)."),
                code("FORDC1200", "1.000 Coins", "unverified", "Listado como ativo pela evomon.wiki (outubro de 2026).")]},
            {"type": "notice", "title": "", "tone": "info", "body": "Segundo os sites de códigos, nenhum código do Evomon expirou até agora. O próximo código sai quando o jogo bater 400 mil likes (aviso na página oficial do Roblox)."}]),
        cat("evomons", "Evomons", "🐾", f"{len(mons)} Evomons com foto, elemento, evolução e status.", toon_sections),
        cat("iniciais", "Escolha do Inicial", "⭐", "Bubble, Blazpup ou Leafbun: qual escolher.", [
            {"type": "text", "title": "", "body": "No começo do jogo você escolhe um de três Evomons iniciais. Os que você não escolher ainda podem ser conseguidos depois, nas recompensas por convidar amigos."},
            {"type": "items", "title": "Os três iniciais", "items": [
                {"name": m["name"], "image": small(m["icon"]), "tags": [EL.get(m["el"], m["el"])], "description": "; ".join(m["evo"]) + ".",
                 "fields": {"Status base": ", ".join(m["stats"])}} for m in starters]},
            {"type": "notice", "title": "", "tone": "success", "body": "A comunidade costuma indicar o Bubble (Água) para quem está começando, por ser mais equilibrado nas primeiras ilhas. Mas qualquer um dá para zerar o começo do jogo."}]),
        cat("elementos", "Elementos", "🌈", "Os 17 tipos de Evomon.", [
            {"type": "text", "title": "", "body": "Cada Evomon tem um elemento. Usar um elemento forte contra o do adversário faz mais dano, igual nos jogos de monstrinhos clássicos."},
            {"type": "items", "title": "Lista de elementos", "items": [{"name": pt, "tags": [en, "Novo no PvP"] if en in ("Dark", "Light") else [en], "description": f"{sum(1 for m in mons if m['el'] == en)} Evomons deste elemento cadastrados no guia.", "fields": {}} for en, pt in EL.items()]}]),
        cat("guias", "Guias por Nível", "🧭", "Do primeiro Evomon até as dungeons.", [
            {"type": "levels", "title": "Dicas por nível", "levels": [
                {"level": "Iniciante", "tips": ["Termine o tutorial logo: só depois dele dá para resgatar códigos.", "Resgate todos os códigos ativos: eles dão EXP Fruits, bolas e tickets que aceleram muito o começo.",
                                                 "Escolha seu inicial e capture vários Evomons em Verdant Valley para montar um time variado.", "Batalhe bastante: os iniciais evoluem no nível 35 e de novo no nível 90."]},
                {"level": "Intermediário", "tips": ["Monte o time com elementos diferentes para ter vantagem contra mais adversários.", "Use Advanced Balls nos Evomons raros para não perder a chance de captura.",
                                                     "Explore uma ilha de cada vez: cada uma tem Evomons e chefes próprios.", "Guarde Evolution Stones e Omni-Stones para os Evomons que você vai usar de verdade."]},
                {"level": "Avançado", "tips": ["Chame amigos para as dungeons cooperativas: elas dão recompensas melhores.", "Caçe as variantes Shiny e Sparkle, as mais raras do jogo.",
                                                "Use as Trait, Nature e Talent Vector Potions para melhorar os atributos do seu melhor Evomon.", "Acompanhe as temporadas: cada uma traz passe, loja especial e Evomons exclusivos."]}]}]),
        cat("termos", "Termos do Jogo", "📖", "Dicionário para iniciantes.", [
            {"type": "items", "title": "", "items": [
                {"name": n, "tags": [], "description": d, "fields": {}} for n, d in [
                    ("Evodex", "A lista de todos os Evomons, com número de cada um."), ("Shiny", "Versão rara de um Evomon, com cores diferentes."),
                    ("Sparkle", "Variante ainda mais rara e brilhante."), ("Flair", "A soma de todos os status base de um Evomon."),
                    ("EXP Fruit", "Fruta que dá experiência direto para o Evomon subir de nível."), ("Advanced Ball", "Bola de captura melhor que a comum."),
                    ("Evolution Stone / Omni-Stone", "Pedras usadas para evoluir alguns Evomons."), ("Summon Ticket", "Ticket de invocação para sortear recompensas."),
                    ("Trait / Nature / Talent Vector Potion", "Poções que sorteiam de novo características e atributos do Evomon."),
                    ("Legacy Coins e Apex Medals", "Moedas especiais de temporada, usadas em lojas específicas."),
                    ("Dungeon", "Fase cooperativa para jogar com amigos e ganhar recompensas melhores."), ("Montaria", "Alguns Evomons podem ser montados para andar mais rápido pelo mapa.")]]}]),
        cat("criadores", "Criadores de Conteúdo", "📺", "Canais oficiais e vídeos em português.", [
            {"type": "creators", "title": "Para acompanhar", "creators": [
                creator("Evomon (Discord oficial)", "Oficial", "Onde os desenvolvedores anunciam códigos, atualizações e eventos primeiro.", "", "", "evomon.wiki"),
                creator("Vídeos de Evomon em português", "Brasil", "Busca no YouTube com guias, tier lists e dicas de brasileiros.", yt("Evomon Roblox dicas português"), "", "Busca do YouTube")]},
            {"type": "links", "title": "", "links": [{"label": "Discord oficial do Evomon", "url": "https://discord.gg/evomonofficialdc", "official": True}]},
            {"type": "notice", "title": "", "tone": "info", "body": "Ainda não confirmamos um criador brasileiro focado em Evomon. Se você acompanha algum, avise que a gente adiciona com o link direto."}]),
        *news_sections(),
        cat("info", "Links", "🔗", "Página do jogo e wiki.", [
            {"type": "links", "title": "", "links": [
                {"label": "Evomon no Roblox", "url": "https://www.roblox.com/games/134381727982611/Evomon", "official": True},
                {"label": "Wiki da comunidade (Fandom)", "url": "https://evomon-roblox.fandom.com/wiki/Evomon_-_Roblox_Wiki", "official": False},
                {"label": "evomon.wiki (códigos e Evodex)", "url": "https://www.evomon.wiki/codes", "official": False}]}]),
    ]
    g = rb["games"][0]
    save_game("evomon", {"id": "evomon", "name": "Evomon", "emoji": "🐾", "color": "#38BDF8", "cover": med.get("thumb1", ""),
                         "description": "Jogo de capturar, treinar e evoluir monstrinhos no Roblox, no estilo Pokémon.",
                         "credits": ["Desenvolvido e publicado pelo grupo Evomon Devs no Roblox.", "Lançado para todos em 17/06/2026."], "categories": cats})
    SUMMARY["evomon"] = {"characters": len(mons), "label": "Evomons", "cover": med.get("thumb1", ""), "visits": g["visits"], "updated": g["updated"][:10]}


# ================================================================ JUJUTSU SHENANIGANS
def build_jjs():
    w, rb = load("jujutsu-shenanigans")
    pages, files = w["pages"], w.get("files", {})
    med = media_copy("jujutsu-shenanigans")
    COST = {"Free": "Grátis", "Early Access": "Acesso Antecipado (gamepass de 345 Robux)", "Unavailable": "Não disponível para jogadores",
            "Developer-Only": "Só para desenvolvedores", "Being Tze": "Só o criador do jogo (Tze)"}
    SPECIAL = {"Mahoraga": "Use com sucesso o golpe \"Mahoraga\" do Ten Shadows durante a luta.",
               "The Fallen": "Cancele o Mahoraga do Ten Shadows usando o Rush do Vessel."}
    chars = []
    for title, p in pages.items():
        if "Characters" not in p["categories"]:
            continue
        t = p["text"]
        ib = t[t.find("{{Character2"):] if "{{Character2" in t else ""
        cost_raw = clean(field(ib, "cost"))
        hp = clean(field(ib, "hp")).replace("}}", "").strip()
        awak = " / ".join(x.strip() for x in clean(field(ib, "awakening")).split("*") if x.strip())
        if awak in ("N/A", "None"):
            awak = ""
        gal = re.findall(r"^([^|\n<>]+\.(?:png|jpg|jpeg|gif|webp))\|([^\n]+)$", field(ib, "image1"), re.M | re.I)
        imgs = [(files.get(f.strip()), clean(c)) for f, c in gal if files.get(f.strip())]
        mv = section(t, "Moveset")
        tabs = re.split(r"\|-\|\s*([^=\n]+?)\s*=", mv)
        forms = []
        for i in range(1, len(tabs), 2):
            names = []
            for n in re.findall(r"\|\s*Name\s*=([^\n]+)", tabs[i + 1]):
                n = clean(n).strip('"')
                if n and n not in names:
                    names.append(n)
            if names:
                forms.append((clean(tabs[i]), names))
        om = re.search(r"based (?:off|on) (?:of )?(.+?)(?:,| from| who| and his)", t)
        ORIG = {"Cursed Child": "Chara (Undertale)", "Monkey Kid": "Goku (Dragon Ball)", "Mokou": "", "Mahoraga": "Mahoraga (Jujutsu Kaisen)", "Super TZE": ""}
        origin = ORIG.get(title, (clean(om.group(1)).replace(" and ", " e ") + " (Jujutsu Kaisen)") if om else "")
        hp = hp.replace(" in Awakening", " no Despertar").replace("(Boss)", "como chefe").replace("(unconfirmed)", "(não confirmado)")
        hp = re.sub(r"^(\d+),? (\d+ no Despertar|\d+ como chefe)$", r"\1 (\2)", hp)
        hp_short = (re.match(r"\d+", hp) or [""])[0]
        cats_ = p["categories"]
        if title in SPECIAL:
            group, cost = "Secretos", SPECIAL[title]
        elif cost_raw.startswith("Free"):
            group = "Completos" if "Complete Characters" in cats_ else "Só forma base" if "Base Only Characters" in cats_ else "Novos"
            cost = "Grátis"
        elif "Early Access" in cost_raw:
            group, cost = "Novos", COST["Early Access"]
        elif "Private" in cost_raw or "Overpowered Characters" in cats_:
            group, cost = "Especiais", "Só em servidores privados (personagem superpoderoso)"
        else:
            group, cost = "Especiais", COST.get(cost_raw, cost_raw or "Não disponível")
        kind = {"Completos": "Completo", "Só forma base": "Só forma base", "Novos": "Novo", "Secretos": "Secreto", "Especiais": "Especial"}[group]
        img = imgs[0][0] if imgs else p["image"]
        tut = [{"title": f"Conheça {title}", "text": (f"Baseado em {origin}. " if origin else "") + (f"Vida: {hp}." if hp else ""), "image": small(img)},
               {"title": "1. Como liberar", "text": cost + (" Escolha na lista de personagens, no canto superior esquerdo (fora de combate)." if cost == "Grátis" else ""), "image": small(img)}]
        if forms:
            tut.append({"title": f"2. Golpes ({forms[0][0]})", "text": ", ".join(forms[0][1][:6]) + ".", "image": small(img)})
        if awak:
            aw_img = next((u for u, c in imgs if "wak" in c.lower()), img)
            tut.append({"title": "3. Desperte (tecla G)", "text": f"Com a barra de Despertar cheia, aperte G para ativar \"{awak}\" e ganhar golpes novos.", "image": small(aw_img)})
        info = [{"title": "📋 Ficha", "items": [x for x in [f"Tipo: {kind}", f"Vida: {hp}" if hp else "", f"Como liberar: {cost}",
                                                          f"Despertar: {awak}" if awak else "", f"Baseado em: {origin}" if origin else ""] if x]}]
        TAB = {"Awakening": "Despertar", "Awakening Move": "Golpe de Despertar", "Domain Awakening": "Despertar de Domínio", "True Awakening": "Despertar Verdadeiro",
               "Super Awakening": "Super Despertar", "Moves": "Golpes", "Base Rika": "Base com Rika"}
        for fname, names in forms[:4]:
            label = {"Base": "⚔️ Golpes da forma base", "Finishers": "💥 Finalizadores"}.get(fname, f"🔥 {TAB.get(fname, fname)}")
            info.append({"title": label, "items": names[:12]})
        skins = [{"name": c or "Visual", "image": small(u), "category": "Visual", "howTo": ""} for u, c in imgs[:4]] if len(imgs) > 1 else []
        chars.append((group, {"id": slug(title), "name": title, "image": small(img), "vintageImage": "", "kind": f"{kind} · {hp_short} de vida" if hp_short else kind,
                              "cost": cost, "requirements": [], "mastery": [], "skins": skins, "tutorial": tut,
                              "videoUrl": yt(f"{title} Jujutsu Shenanigans combo"), "infoBlocks": info}))
    order = ["Completos", "Só forma base", "Novos", "Secretos", "Especiais"]
    char_sections = [{"type": "text", "title": "", "body": f"{len(chars)} personagens da wiki. Os personagens normais são grátis. Os novos começam em Acesso Antecipado (gamepass) e ficam grátis quando o kit fica pronto."}]
    for gname in order:
        l = sorted([c for g, c in chars if g == gname], key=lambda x: x["name"])
        if l:
            char_sections.append({"type": "toons", "title": f"{gname} ({len(l)})", "toons": l})
    # Skins (por personagem/item)
    sk = pages.get("Skins", {}).get("text", "")
    gal_groups, top, sub, limited = {}, "Kogane", "", False
    for line in sk.splitlines():
        m = re.match(r"^(?:<tabber>|\|-\|)\s*([^=]+?)\s*=\s*\{\{#tag:tabber\|", line)
        if m:
            top, sub, limited = m.group(1).strip(), "", False
            continue
        m = re.match(r"^([A-Z][\w ]+?)\s*=\s*$", line)
        if m:
            sub, limited = m.group(1).strip(), False
            continue
        if line.startswith("==Limited=="):
            limited = True
            continue
        m = re.match(r"^===([^=]+)===", line)
        if m:
            name = m.group(1).strip()
            continue
        m = re.search(r"\[\[File:([^|\]]+)", line)
        if m and files.get(m.group(1).strip()):
            gal_groups.setdefault(top, []).append({"image": small(files[m.group(1).strip()]), "caption": name,
                                                   "subtitle": " · ".join(x for x in [sub, "Limitada" if limited else ""] if x)})
    n_skins = sum(len(v) for v in gal_groups.values())
    # Gamepasses
    gp = pages.get("Gamepasses", {}).get("text", "")
    gps = []
    for blk in re.findall(r"\{\{Gamepass(.*?)\}\}\s*\n", gp, re.S):
        title = clean(field(blk, "title1"))
        price = re.sub(r"\D", "", clean(field(blk, "cost")))
        if title:
            GPT = {"Early Access": "Libera personagens em desenvolvimento antes de ficarem grátis para todos. É permanente.", "Custom Kill Sound": "Muda o som que toca quando você finaliza um jogador.",
                   "More Emote Slots": "Mais espaços para equipar emotes.", "Awakening Outfits": "Roupas especiais para o Despertar.",
                   "+1 Emote Page": "Mais uma página de emotes.", "More Build Saves": "Mais espaços para salvar criações no Build Mode."}
            gps.append({"name": title, "tags": [f"{price} Robux"] if price else [], "description": GPT.get(title, clean(field(blk, "description"))), "fields": {}})
    # Atualizações (notas da wiki)
    ul = pages.get("Update Log", {}).get("text", "")
    ups = []
    for m in list(re.finditer(r"\|-\|(V[\d.]+)=(.*?)(?=\|-\|V|\n</tabber>)", ul, re.S))[:6]:
        bullets = [clean(b) for b in re.findall(r"•([^\n•]+)", m.group(2))]
        ups.append({"name": f"Versão {m.group(1)[1:]}", "tags": ["Notas da wiki (inglês)"], "description": "\n".join("• " + b for b in bullets[:10] if b), "fields": {}})
    cats = [
        cat("codigos", "Códigos e Como Resgatar", "🎁", "Códigos atuais, expirados e o passo a passo.", [
            {"type": "steps", "title": "Como resgatar", "items": [
                "Entre no jogo (não precisa de tutorial).", "Toque no ícone da loja (Shop) no canto superior esquerdo.",
                "Abra a aba Rewards (Recompensas), no fim da página principal da loja.", "Digite o código e toque em Redeem."]},
            {"type": "codes", "title": "Códigos", "codes": [
                code("RIA2026", "Emote RIA2026", "active", "Comemora o prêmio de Melhor Experiência de Ação (RIA 2026). Confirmado pela Dexerto em 26/10/2026."),
                code("A7D2L26RNEPG74A3Q", "Conquista Nep e emote Woven Insight", "active", "Confirmado pela Dexerto em 26/10/2026."),
                code("SLATECONCRETE", "", "expired"), code("X6X31F47UN8JM1NEP", "", "expired", "Valia só para os 100 primeiros."),
                code("WHYEMOTEDISSAPEAR", "", "expired"), code("JJS1YEAR", "", "expired"), code("WHYISTHISCODEHIDDEN", "", "expired"),
                code("370MVISITS", "", "expired"), code("120MVISITS", "", "expired"), code("20MVISITS", "", "expired")]},
            {"type": "notice", "title": "", "tone": "warning", "body": "Os códigos do Jujutsu Shenanigans costumam durar pouco. Resgate assim que sair."}]),
        cat("personagens", "Personagens", "👊", f"{len(chars)} personagens com golpes, vida e despertar.", char_sections),
        cat("controles", "Controles e Combate", "🎮", "Teclas e o básico da luta.", [
            {"type": "steps", "title": "Controles (PC)", "items": [
                "M1 (clique): sequência de socos.", "1, 2, 3 e 4: os golpes do personagem.", "R: especial.", "Q: dash (use no atordoamento para escapar).",
                "F: bloquear.", "W + W: correr (desvia de obstáculos sozinho).", "G: Despertar (quando a barra estiver cheia)."]},
            {"type": "text", "title": "No celular", "body": "Os mesmos comandos aparecem como botões na tela. Para trocar de personagem, saia do combate e toque em \"Characters\" no canto superior esquerdo."},
            {"type": "notice", "title": "", "tone": "info", "body": "O combate é inspirado nos jogos \"battlegrounds\" e a destruição do mapa, no Combat Mayhem (descrição oficial do jogo)."}]),
        cat("skins", "Skins", "🎭", f"{n_skins} skins da Kogane, itens e personagens.", [
            {"type": "text", "title": "", "body": "Skins são só visuais: não mudam os golpes. Saem na loja de forma aleatória por $250 (dinheiro do jogo) ou 25 Robux, e dá para comprar 5 ou 10 de uma vez. Também dá para presentear amigos."},
            *[{"type": "gallery", "title": f"{k} ({len(v)})", "images": v} for k, v in gal_groups.items()]]),
        cat("gamepasses", "Gamepasses", "💎", "O que dá para comprar com Robux.", [
            {"type": "text", "title": "", "body": "Fora o Acesso Antecipado, nenhum gamepass muda as lutas: são só extras visuais e de conforto."},
            {"type": "items", "title": "", "items": gps}]),
        cat("modos", "Modos de Jogo", "🏟️", "Duelos, Roleta, Creator Clash e mais.", [
            {"type": "items", "title": "Lupa na barra de cima", "items": [
                {"name": n, "tags": [], "description": d, "fields": {}} for n, d in [
                    ("Servidores públicos", "O jogo normal, onde você nasce ao entrar."), ("Duelos", "1 contra 1, 2 contra 2 e 3 contra 3 em mapas aleatórios, no modo Casual ou Ranqueado."),
                    ("Roleta", "Minijogos rápidos escolhidos por votação."), ("Creator Clash", "Lutas com golpes personalizados criados pelos jogadores no Workshop."),
                    ("Servidores personalizados", "Servidores privados criados por jogadores."), ("Entrar com um amigo", "Vai direto para o servidor de um amigo, se tiver vaga.")]]}]),
        cat("guias", "Guias por Nível", "🧭", "Do primeiro soco ao Ranqueado.", [
            {"type": "levels", "title": "Dicas por nível", "levels": [
                {"level": "Iniciante", "tips": ["Comece com personagens completos e simples, como Vessel ou Honored One, que têm combos fáceis.", "Aprenda a bloquear (F): muitos golpes são punidos se você só atacar.",
                                                 "Não gaste o dash à toa: ele é sua fuga quando você está atordoado.", "Encha a barra de Despertar lutando e use o G na hora certa para virar a luta."]},
                {"level": "Intermediário", "tips": ["Treine trocas de M1: quem acerta primeiro começa o combo.", "Use golpes de longe para se aproximar com segurança.",
                                                     "Guarde o \"ragdoll cancel\" para escapar de combos longos, em vez de usar logo.", "Teste personagens no Dummy (boneco de treino) antes de levar para a luta."]},
                {"level": "Avançado", "tips": ["Leia o adversário: finja um golpe (feint) para provocar o bloqueio ou o contra-ataque.", "Domine o Black Flash do Vessel e as extensões de combo.",
                                                "Aprenda a sair de Domínios: alguns golpes quebram a borda do Domain Expansion.", "Jogue Duelos Ranqueados para treinar contra gente do seu nível."]}]}]),
        cat("termos", "Termos do Jogo", "📖", "Dicionário para iniciantes.", [
            {"type": "items", "title": "", "items": [
                {"name": n, "tags": [], "description": d, "fields": {}} for n, d in [
                    ("M1", "O ataque básico com clique (sequência de socos)."), ("Despertar (Awakening)", "Modo mais forte do personagem, ativado com G quando a barra enche."),
                    ("Domain Expansion (Domínio)", "Golpe que cria uma cúpula onde o ataque do usuário acerta quase sempre."), ("Black Flash", "Acerto crítico especial, famoso no Vessel."),
                    ("Ragdoll", "Quando o personagem cai mole depois de um golpe forte."), ("Ragdoll cancel", "Usar o dash para sair do ragdoll e escapar do combo."),
                    ("Feint", "Fingir um golpe e cancelar, para enganar o adversário."), ("Block break", "Golpe que quebra o bloqueio do adversário."),
                    ("Acesso Antecipado", "Gamepass que libera personagens novos antes de ficarem prontos e grátis."), ("Kogane", "O mascote que dá emotes, carinho diário ($5 a $15) e o placar do servidor.")]]}]),
        cat("atualizacoes", "Atualizações", "🛠️", "Últimas versões do jogo.", [
            {"type": "notice", "title": "", "tone": "info", "body": f"Última atualização na página do Roblox: {'/'.join(reversed(rb['games'][0]['updated'][:10].split('-')))}. Notas abaixo vêm do Update Log da wiki."},
            {"type": "items", "title": "", "items": ups}]),
        cat("criadores", "Criadores de Conteúdo", "📺", "Criador do jogo e vídeos em português.", [
            {"type": "creators", "title": "Para acompanhar", "creators": [
                creator("Tze (criador do jogo)", "Oficial", "Desenvolvedor do Jujutsu Shenanigans. Novidades e códigos saem no X (Twitter) e no Discord oficial.", "", "", "Deltia's Gaming / Dexerto"),
                creator("Vídeos de Jujutsu Shenanigans em português", "Brasil", "Busca no YouTube com combos, tier lists e guias em português.", yt("Jujutsu Shenanigans combo português"), "", "Busca do YouTube")]},
            {"type": "links", "title": "", "links": [{"label": "Tze no X (Twitter)", "url": "https://x.com/AlsoNot_Tze", "official": True}]},
            {"type": "notice", "title": "", "tone": "info", "body": "Ainda não confirmamos um criador brasileiro focado em JJS. Se você acompanha algum, avise que a gente adiciona com o link direto."}]),
        *news_sections(),
        cat("info", "Links", "🔗", "Página do jogo e wiki.", [
            {"type": "links", "title": "", "links": [
                {"label": "Jujutsu Shenanigans no Roblox", "url": "https://www.roblox.com/games/9391468976/Jujutsu-Shenanigans", "official": True},
                {"label": "Wiki da comunidade (Fandom)", "url": "https://jujutsu-shenanigans.fandom.com/wiki/Jujutsu_Shenanigans_Wiki", "official": False}]}]),
    ]
    g = rb["games"][0]
    save_game("jujutsu-shenanigans", {"id": "jujutsu-shenanigans", "name": "Jujutsu Shenanigans", "emoji": "👊", "color": "#EF4444", "cover": med.get("thumb1", ""),
                                      "description": "Jogo de luta (battlegrounds) do Roblox inspirado em Jujutsu Kaisen.",
                                      "credits": ["Criado por Tze (@AlsoNot_Tze) e publicado pelo grupo Jujutsu Shenanigans! no Roblox.", "Inspirado no anime e mangá Jujutsu Kaisen."], "categories": cats})
    SUMMARY["jujutsu-shenanigans"] = {"characters": len(chars), "skins": n_skins, "label": "Personagens", "cover": med.get("thumb1", ""), "visits": g["visits"], "updated": g["updated"][:10]}


# ================================================================ NINJA TYCOON
def money(v):
    n = re.sub(r"\D", "", v)
    return f"${int(n):,}".replace(",", ".") + " (Cash)" if n else v


def dmg_pt(v):
    r = v
    for a, b in [(r"(?i)First (two|three) hits deal (\d+)", lambda m: f"{'2' if m.group(1).lower() == 'two' else '3'} primeiros golpes: {m.group(2)}"),
                 (r"(?i),? ?big hit deals (\d+)", r", golpe forte: \1"), (r"(?i),? ?bit hit deals (\d+)", r", golpe forte: \1"),
                 (r"(?i)Melee:", "Corpo a corpo:"), (r"(?i)Throw:", "· Arremesso:"), (r"(?i)\beach\b", "cada"), (r"(?i)\bmax\b", "no máximo"),
                 (r"(?i)Above average", "Acima da média"), (r"(?i)Med-Large|Medium-Large", "Médio a grande"), (r"(?i)Small-Med", "Pequeno a médio"),
                 (r"(?i)Med-Long", "Médio, longo alcance"), (r"(?i)\bAverage\b", "Médio"), (r"(?i)\bMedium\b", "Médio"), (r"(?i)\bLarge\b", "Grande"),
                 (r"(?i)\bSmall\b", "Pequeno"), (r"N/A", "Sem dano (utilidade)"), (r"(?i)is a teleport swap ability", "troca de lugar (teleporte)")]:
        r = re.sub(a, b, r)
    return r


def build_ninja():
    w, rb = load("ninja-tycoon")
    pages, files = w["pages"], w.get("files", {})
    med = media_copy("ninja-tycoon")
    BOOST = {"small": "Pequeno", "medium": "Médio", "large": "Grande", "moderate": "Moderado", "none": "Nenhum"}

    def boost(v):
        v = clean(v).lower()
        v = v.replace("(fastest mode)", "(o modo mais rápido)").replace(" to ", " a ")
        for k, pt in BOOST.items():
            v = v.replace(k, pt.lower())
        return v.capitalize() if v and v != "?" else "—"

    def moves(t):
        names = []
        for m in re.finditer(r"•\s*(?:<big>)?\(([^)]+)\)", t):
            names.append(m.group(1))
        for m in re.finditer(r"^\|\s*'''([^']+)'''\s*:", t, re.M):
            names.append(m.group(1))
        mv = field(t, "moves")
        if mv and not re.match(r"^\d", mv):
            names += [x.strip(" -") for x in mv.split("\n") if x.strip(" -")]
        out = []
        for n in names:
            n = clean(n)
            if n and n not in out and len(n) < 40:
                out.append(n)
        return out

    TRANS = [("Baryon mode", "976 Robux"), ("Rinnegon Sosuke & Rikudou Narotu", "876 Robux (só em dezembro e janeiro, ou em datas especiais)"), ("Karma", ""),
             ("Toad Sage Mode", ""), ("Demon Fox Cloak", ""), ("Eight Inner Gates", ""), ("Snake Sage Mode", ""), ("Curse Mark", "")]
    INSP = {"Baryon mode": "Naruto e Kurama fundidos (Boruto)", "Rinnegon Sosuke & Rikudou Narotu": "Sasuke com Rinnegan e Naruto Modo Seis Caminhos",
            "Karma": "Modo Karma de Boruto e Kawaki", "Toad Sage Mode": "Modo Sábio dos Sapos (Naruto e Jiraiya)", "Demon Fox Cloak": "Mantos da Kurama usados por Naruto",
            "Eight Inner Gates": "Os Oito Portões (Guy e Lee)", "Snake Sage Mode": "Modo Sábio das Cobras (Kabuto e Mitsuki)", "Curse Mark": "Selo Amaldiçoado do Sasuke"}
    toons = []
    for name, cost_override in TRANS:
        p = pages.get(name)
        if not p:
            continue
        t = p["text"]
        cost = cost_override or (re.sub(r"(?i)robux", "Robux", clean(field(t, "cost"))) or "")
        if cost and cost.isdigit():
            cost += " Robux"
        hb, sb = boost(field(t, "health_boost")), boost(field(t, "speed_boost"))
        mv = moves(t)
        img = p["image"] or files.get(clean(field(t, "image1")), "")
        info = [{"title": "📋 Ficha", "items": [x for x in [f"Preço: {cost}" if cost else "", f"Bônus de vida: {hb}" if hb != "—" else "", f"Bônus de velocidade: {sb}" if sb != "—" else "",
                                                          f"Inspirado em: {INSP.get(name, '')}"] if x]}]
        if mv:
            info.append({"title": "🌀 Golpes e modos", "items": mv[:12]})
        tut = [{"title": f"Conheça {name}", "text": f"Transformação inspirada em {INSP.get(name, 'Naruto')}.", "image": small(img)},
               {"title": "1. Compre o gamepass", "text": f"Custa {cost}. Compre pelo botão de Robux no jogo ou na página do Roblox." if cost else "Compre pelo botão de Robux no jogo.", "image": small(img)},
               {"title": "2. Ative", "text": "Abra o pergaminho do lado esquerdo da tela, toque no símbolo de Robux e escolha a transformação.", "image": small(img)},
               {"title": "3. Compre os golpes", "text": "Os golpes do gamepass ficam no 2º andar da loja de habilidades e custam Cash.", "image": small(img)}]
        toons.append({"id": slug(name), "name": name, "image": small(img), "vintageImage": "", "kind": "Transformação · Robux", "cost": cost or "Robux",
                      "requirements": [], "mastery": [], "skins": [], "tutorial": tut, "videoUrl": yt(f"{name} Ninja Tycoon Roblox"), "infoBlocks": info})
    # Ryo gamepasses
    ryo_txt = pages.get("Ryo Gamepasses", {}).get("text", "")
    ryo = []
    for row in re.split(r"\n\|-\s*\n", ryo_txt)[1:]:
        cells = [c.strip() for c in re.split(r"\n\|", "\n" + row) if c.strip()]
        if len(cells) >= 3:
            nm = clean(cells[0]); price = clean(cells[1]); rank = clean(cells[2])
            abil = [clean(x) for x in re.findall(r"\*\s*([^\n]+)", row)]
            if nm:
                ryo.append({"name": nm, "tags": [x for x in [price.replace("Halloween Exclusive", "Só no Halloween") + (" Ryo" if price.isdigit() else ""), f"Nota {rank}" if rank else ""] if x],
                            "description": "Golpes: " + ", ".join(abil) if abil else "", "fields": {}})
    # Armas
    wt = pages.get("Jutsus/Weapons", {}).get("text", "")
    weapons = []
    for row in re.split(r"\n\|-\s*\n", wt)[1:]:
        cells = [clean(c) for c in re.split(r"\n\|", "\n" + row) if c.strip()]
        if len(cells) >= 3 and cells[1].startswith("$"):
            weapons.append({"name": cells[0], "tags": [money(cells[1])], "description": "Dano: " + dmg_pt(cells[2]), "fields": {}})
    jutsus_unused = True  # a tabela de armas da wiki já inclui os jutsus com preço e dano
    # Jutsus da loja
    sm = pages.get("Store Moves", {}).get("text", "")
    jutsus = []
    for m in re.finditer(r"^([A-Za-z][^\n]+?)\s*-\s*([\d,]+|Free)\s*cash", sm, re.M | re.I):
        price = "Grátis" if m.group(2).lower() == "free" else "$" + m.group(2).replace(",", ".")
        jutsus.append({"name": m.group(1).strip(), "tags": [price], "description": "", "fields": {}})
    # Droppers
    dr = pages.get("Droppers", {}).get("text", "")
    droppers = []
    for x in re.findall(r"^\*\s*([^\n]+)", dr, re.M):
        mm = re.match(r"(.+?)\s*-\s*(?:(\d+)\$ per click\.\s*)?Price:\s*(\d+)\$", clean(x))
        if mm:
            nm = mm.group(1).replace("(clicker)", "(clique)").replace("dropper", "").replace("Dropper", "").strip()
            droppers.append(f"Dropper {nm}: " + ("grátis, dá $1 por clique" if mm.group(3) == "0" else money(mm.group(3)).replace(" (Cash)", "")))
    # Missões
    ms = pages.get("Missions", {}).get("text", "")
    bosses = []
    for m in re.finditer(r"^(\w+):\s*([A-Z])-[Rr]ank mission that has an? (\d+)% chan\w+ to drop Cash and an? (\d+)% chance to drop Ryo", ms, re.M):
        img = files.get(m.group(1) + ".png", "")
        bosses.append({"name": m.group(1), "image": small(img, 200), "tags": [f"Missão rank {m.group(2)}"], "description": f"{m.group(3)}% de chance de dar Cash e {m.group(4)}% de chance de dar Ryo.", "fields": {}})
    # Atualizações
    up = pages.get("Updates", {}).get("text", "")
    ups = []
    for m in re.finditer(r"===\s*Update ([\d.]+) \(([\d/]+)\)\s*===(.*?)(?====|\Z)", up, re.S):
        bl = [clean(b) for b in re.findall(r"^\*+\s*([^\n]+)", m.group(3), re.M)]
        mo, d, y = (m.group(2).split("/") + ["", "", ""])[:3]
        ups.append({"name": f"Versão {m.group(1)} ({d.zfill(2)}/{mo.zfill(2)}/{y if len(y) == 4 else '20' + y})", "tags": ["Notas da wiki (inglês)"], "description": "\n".join("• " + b for b in bl[:10]), "fields": {}})
    vm = re.search(r"v([\d.]+)", rb["games"][0]["name"])
    VER = vm.group(1) if vm else "—"
    CLANS = [("Akatsuke", "Akatsuki"), ("Hyago", "Hyuga"), ("Sonju", "Senju"), ("Uchika", "Uchiha"), ("Hotate", "Hatake"), ("Otsutsushi", "Otsutsuki"),
             ("Uzumonki", "Uzumaki"), ("Kazeka", "Kazekage"), ("Sarikobi", "Sarutobi")]
    cats = [
        cat("codigos", "Códigos", "🎁", "O jogo tem códigos?", [
            {"type": "text", "title": "", "body": "O Ninja Tycoon não tem sistema de códigos: desde 2015 o criador nunca lançou um código de recompensa. Os botões do jogo levam às lojas."},
            {"type": "codes", "title": "Códigos", "codes": []},
            {"type": "notice", "title": "", "tone": "warning", "body": "Cuidado com sites e vídeos que prometem \"códigos do Ninja Tycoon\": eles não funcionam. Novidades oficiais saem no Discord do criador (Coldjason)."}]),
        cat("transformacoes", "Transformações", "🦊", f"{len(toons)} transformações de Robux com preço, bônus e golpes.", [
            {"type": "text", "title": "", "body": "Transformações são modos que deixam o ninja mais forte (mais vida, velocidade e golpes novos). A maioria é comprada com Robux; algumas com Ryo. O jogo tem 17 transformações; aqui estão as da wiki com detalhes."},
            {"type": "toons", "title": f"Com Robux ({len(toons)})", "toons": toons},
            {"type": "items", "title": "💰 Compradas com Ryo", "items": ryo}]),
        cat("clas", "Clãs (Bases)", "🏯", "Os 9 clãs para escolher no começo.", [
            {"type": "text", "title": "", "body": "Escolher um clã é o primeiro passo. Depois de escolher, não dá para trocar. Cada clã tem a própria base (tycoon), roupa, 3 acessórios e 3 golpes exclusivos. Os nomes são versões \"parecidas\" dos clãs de Naruto."},
            {"type": "items", "title": "Os 9 clãs", "items": [{"name": a, "tags": [f"Inspirado em: {b}"], "description": "", "fields": {}} for a, b in CLANS]},
            {"type": "steps", "title": "O que toda base tem", "items": [
                "15 droppers que geram dinheiro (total de cerca de $60.000).", "Portão e escudo que eliminam quem tenta invadir quando estão ligados.",
                "Recarregador de vida instantâneo.", "Teleporte para as áreas Norte, Sul, Leste, Oeste, Loja e Raid.", "Dois bonecos de treino (um de madeira e um vermelho que pode ser destruído)."]}]),
        cat("dinheiro", "Cash, Ryo e Droppers", "💵", "Como ganhar e gastar dinheiro.", [
            {"type": "items", "title": "As duas moedas", "items": [
                {"name": "Cash ($)", "tags": ["Não salva"], "description": "Vem dos droppers da base. Compra droppers, paredes, armas e jutsus. Também dá para comprar com Robux.", "fields": {}},
                {"name": "Ryo", "tags": ["Salva"], "description": "Vem dos pergaminhos que chefes e NPCs deixam cair (10% de chance). Compra os modos de Ryo. É a única coisa que continua salva quando você sai.", "fields": {}}]},
            {"type": "steps", "title": "Droppers (ordem de compra)", "items": droppers},
            {"type": "steps", "title": "Pergaminhos de Ryo por rank", "items": [
                "F: Rogue Ninja e Reanimated Ninja", "E: Mizouki (Mizuki)", "D: Oroshimaro (Orochimaru)", "C: Pain", "B: Juubito (Obito)", "A: Nadara (Madara)", "S: Kaguyai, Jigan, Tenth Beast e Coldjason"]},
            {"type": "notice", "title": "", "tone": "warning", "body": "Importante: o jogo não salva a base (tycoon) nem as armas quando você sai. O próprio criador avisa isso na página do jogo. Só o Ryo fica salvo."}]),
        cat("armas", "Armas e Jutsus", "🗡️", "Preço e dano das armas e jutsus da loja.", [
            {"type": "items", "title": f"🗡️ Armas e jutsus ({len(weapons)})", "items": weapons},
            {"type": "notice", "title": "", "tone": "info", "body": "Os preços são em Cash. O dano vem da wiki da comunidade e pode mudar com atualizações."}]),
        cat("missoes", "Missões e Chefes", "👹", "Chefes das missões, chances de Cash e Ryo.", [
            {"type": "text", "title": "", "body": "Missões pedem para derrotar chefes, do rank E ao S, e dão Cash e Ryo. Quanto maior o rank, maior a chance de prêmio."},
            {"type": "items", "title": "Chefes das missões", "items": bosses},
            {"type": "items", "title": "Raid Boss", "items": [{"name": "Jigan", "image": small(pages.get("Jigan", {}).get("image", ""), 200), "tags": ["Raid · Cachoeira"],
                                                               "description": "Um dos dois Raid Bosses, aparece na Cachoeira. Tem cerca de 2.000 de vida e, na metade, se transforma em Isshiki. Muito difícil sem gamepasses.", "fields": {}}]}]),
        cat("controles", "Controles e Mecânicas", "🎮", "Teclas e status do jogador.", [
            {"type": "steps", "title": "Controles", "items": ["E: dash (espera de 3 segundos). No celular é um botão; no console, o botão Y.", "Espaço duas vezes: pulo duplo.",
                                                              "Pular na parede: escalar.", "Q: habilidade (só em transformações e alguns golpes).", "Modos de gamepass: ative no menu de Robux, tocando no que você tem."]},
            {"type": "items", "title": "Status iniciais", "items": [{"name": "Vida", "tags": ["100"], "description": "", "fields": {}}, {"name": "Velocidade", "tags": ["16"], "description": "", "fields": {}}]}]),
        cat("guias", "Guias por Nível", "🧭", "Do primeiro dropper ao Raid Boss.", [
            {"type": "levels", "title": "Dicas por nível", "levels": [
                {"level": "Iniciante", "tips": ["Escolha seu clã com calma: depois não dá para trocar.", "Compre os droppers na ordem, do mais barato ao mais caro, e colete o dinheiro sempre.",
                                                 "Ligue o escudo da base quando sair para lutar, para ninguém invadir.", "Compre primeiro armas baratas, como Shuriken e Kunai, para derrotar os NPCs fáceis."]},
                {"level": "Intermediário", "tips": ["Faça as missões dos ranks E e D para juntar Cash e os primeiros Ryo.", "Guarde Ryo: ele é a única coisa que fica salva.",
                                                     "Compre jutsus fortes da loja, como Rasengan e Chidori.", "Complete o segundo andar de droppers (Sharingan em diante) para ganhar muito mais rápido."]},
                {"level": "Avançado", "tips": ["Use Ryo nos modos de Ryo (Sage of Six Bowls, Paper Angel e outros).", "Treine combos com transformação para o PvP.",
                                                "Junte amigos para enfrentar o Jigan, o Raid Boss mais difícil.", "Lembre: como a base não salva, jogue sessões longas para aproveitar o que comprou."]}]}]),
        cat("termos", "Termos do Jogo", "📖", "Dicionário para iniciantes.", [
            {"type": "items", "title": "", "items": [
                {"name": n, "tags": [], "description": d, "fields": {}} for n, d in [
                    ("Tycoon", "Sua base: você compra peças que geram dinheiro sozinhas."), ("Dropper", "Máquina da base que gera Cash."),
                    ("Cash", "Dinheiro normal do jogo (não salva)."), ("Ryo", "Moeda rara dos pergaminhos dos chefes (salva)."),
                    ("Transformação / Modo", "Forma mais forte, com mais vida, velocidade e golpes."), ("Gamepass", "Item comprado com Robux."),
                    ("Raid Boss", "Chefe muito forte para enfrentar em grupo."), ("Jutsu", "Técnica ninja comprada na loja."), ("PvP", "Luta contra outros jogadores.")]]}]),
        cat("atualizacoes", "Atualizações", "🛠️", "Versões do jogo.", [
            {"type": "notice", "title": "", "tone": "info", "body": f"Versão atual: {VER} (atualizado no Roblox em {'/'.join(reversed(rb['games'][0]['updated'][:10].split('-')))}). As notas completas saem no Discord do criador. Abaixo, as versões registradas na wiki."},
            {"type": "items", "title": "", "items": ups}]),
        cat("criadores", "Criadores de Conteúdo", "📺", "Criador do jogo e vídeos em português.", [
            {"type": "creators", "title": "Para acompanhar", "creators": [
                creator("Coldjason (criador do jogo)", "Oficial", "Criador do Ninja Tycoon e do Slayer Tycoon. As notas de atualização saem no Discord dele.", yt("Coldjason Ninja Tycoon"), "", "Página do jogo no Roblox"),
                creator("Vídeos de Ninja Tycoon em português", "Brasil", "Busca no YouTube com dicas e transformações em português.", yt("Ninja Tycoon Roblox português"), "", "Busca do YouTube")]},
            {"type": "notice", "title": "", "tone": "info", "body": "Ainda não confirmamos um criador brasileiro focado em Ninja Tycoon. Se você acompanha algum, avise que a gente adiciona com o link direto."}]),
        *news_sections(events=False),
        cat("info", "Links", "🔗", "Página do jogo e wiki.", [
            {"type": "links", "title": "", "links": [
                {"label": "Ninja Tycoon no Roblox", "url": "https://www.roblox.com/games/221718525/Ninja-Tycoon", "official": True},
                {"label": "Wiki da comunidade (Fandom)", "url": "https://ninja-tycoon.fandom.com/wiki/Ninja_Tycoon_Wiki", "official": False}]}]),
    ]
    g = rb["games"][0]
    save_game("ninja-tycoon", {"id": "ninja-tycoon", "name": "Ninja Tycoon", "emoji": "🥷", "color": "#F97316", "cover": med.get("thumb1", ""),
                               "description": "Tycoon de ninjas do Roblox inspirado em Naruto: monte sua base, compre jutsus e lute.",
                               "credits": ["Criado por Coldjason, com AzuriteStorm na construção e modelagem.", "No Roblox desde fevereiro de 2015 (antes chamado Naruto Tycoon)."], "categories": cats})
    SUMMARY["ninja-tycoon"] = {"characters": len(toons), "skins": len(weapons), "label": "Transformações", "cover": med.get("thumb1", ""), "visits": g["visits"], "updated": g["updated"][:10]}


build_evomon()
build_jjs()
build_ninja()
json.dump(SUMMARY, open(os.path.join(SRC, "summary.json"), "w"), ensure_ascii=False, indent=1)
print(json.dumps(SUMMARY, ensure_ascii=False, indent=1))
