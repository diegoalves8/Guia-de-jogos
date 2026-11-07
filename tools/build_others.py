"""Gera conteúdo de Brawl Stars, Free Fire, Murder Mystery 2 e Blox Fruits a partir do game-data (wikis Fandom)."""
import json, re, sys, urllib.parse

GD = "/home/claude/gd"
TODAY = "2026-11-07"

def meta(g): return json.load(open(f"{GD}/{g}/meta.json"))
def page(g, m, title):
    p = m["pages"].get(title)
    return open(f"{GD}/{g}/{p}", encoding="utf-8").read() if p else ""
def thumb(m, title, w=300):
    pi = m["pageimages"].get(title) or {}
    u = pi.get("thumb") or pi.get("original") or ""
    return re.sub(r"/scale-to-width-down/\d+", f"/scale-to-width-down/{w}", u) if u else ""
def num(n):
    return f"{int(n):,}".replace(",", ".")
def clean(s):
    s = re.sub(r"<ref[^>]*>.*?</ref>|<ref[^>]*/>", "", s, flags=re.S)
    s = re.sub(r"\[https?://\S+ ([^\]]+)\]", r"\1", s)
    s = re.sub(r"\[\[(?:[^|\]]*\|)?([^\]]+)\]\]", r"\1", s)
    s = re.sub(r"\{\{(?:Icon|Currency|Rarity|Money|Robux)\|([^|}]+)[^}]*\}\}", r"\1", s)
    s = re.sub(r"\{\{[^{}]*\}\}", "", s)
    s = re.sub(r"<[^>]+>", " ", s).replace("'''", "").replace("''", "")
    return re.sub(r"\s+", " ", s).strip()
def yt(q): return "https://www.youtube.com/results?search_query=" + urllib.parse.quote_plus(q)
def tt(q): return "https://www.tiktok.com/search?q=" + urllib.parse.quote(q)
def slug(s): return re.sub(r"[^a-z0-9]+", "-", s.lower()).strip("-")
def credit(src): return {"type": "text", "title": "", "body": f"Dados e imagens: {src} (Fandom, conteúdo CC BY-SA). As imagens precisam de internet na primeira vez; depois ficam salvas no aparelho."}

def creators_section(lst):
    """lst: (nome, plataforma/idioma, conhecido por, fonte)"""
    return {"type": "items", "title": "Criadores para acompanhar",
            "items": [{"name": n, "tags": tags, "description": desc,
                       "fields": {"Onde encontrar": "Toque nos links abaixo", "Fonte da indicação": src}} for n, tags, desc, src in lst]}
def creators_links(lst, game):
    links = []
    for n, *_ in lst:
        links.append({"label": f"{n} no YouTube", "url": yt(f"{n} {game}"), "official": False})
        links.append({"label": f"{n} no TikTok", "url": tt(f"{n} {game}"), "official": False})
    return {"type": "links", "title": "Ver vídeos (YouTube e TikTok)", "links": links}
CREATORS_NOTE = {"type": "notice", "title": "", "tone": "info",
                 "body": "Os links abrem a busca pelo nome do criador no YouTube e no TikTok, assim nunca levam a um perfil errado ou apagado. No Instagram, procure pelo mesmo nome."}

# =====================================================================
# BRAWL STARS
# =====================================================================
def brawl_stars():
    g = "brawl-stars"; m = meta(g)
    RAR = {"Starting Brawler": ("Inicial", "Grátis (você já começa com ele)"),
           "Rare": ("Raro", "160 Créditos ou 29 Gemas"), "Super Rare": ("Super Raro", "430 Créditos ou 79 Gemas"),
           "Epic": ("Épico", "925 Créditos, 169 Gemas ou 1 Chave de Brawler"),
           "Mythic": ("Mítico", "1.900 Créditos, 349 Gemas ou 2 Chaves de Brawler"),
           "Legendary": ("Lendário", "3.800 Créditos, 699 Gemas ou 4 Chaves de Brawler"),
           "Ultra Legendary": ("Ultra Lendário", "5.500 Créditos, 999 Gemas ou 6 Chaves de Brawler"),
           "Ultra": ("Ultra Lendário", "5.500 Créditos, 999 Gemas ou 6 Chaves de Brawler")}
    CLS = {"Damage Dealer": "Dano", "Tank": "Tanque", "Support": "Suporte", "Controller": "Controle",
           "Assassin": "Assassino", "Marksman": "Atirador", "Artillery": "Artilharia"}
    ORDER = ["Inicial", "Raro", "Super Raro", "Épico", "Mítico", "Lendário", "Ultra Lendário"]
    def tr_cost(c):
        c = clean(c).replace(" • E", "").replace(" • S", "")
        R = [(r"\bGems\b", "Gemas"), (r"\bor\b", "ou"), (r"Brawl Pass Exclusive", "Exclusiva do Brawl Pass"),
             (r"Brawl Pass Plus", "Brawl Pass Plus"), (r"(?i)\bfree\b", "grátis"), (r"\bBling\b", "Bling"),
             (r"Trophies", "troféus"), (r"after reaching", "ao chegar a"), (r"\bfrom\b", "de"), (r"\bfor\b", "por")]
        for a, b in R: c = re.sub(a, b, c)
        return c
    brawlers = []
    for title in m["categories"].get("Category:Brawlers", []):
        t = page(g, m, title)
        if "{{Brawler Infobox" not in t: continue
        ib = t[t.index("{{Brawler Infobox"):]
        ib = ib[:ib.index("}}\n") if "}}\n" in ib else len(ib)]
        info = {k: v.strip() for k, v in re.findall(r"\|\s*([A-Za-z0-9]+)\s*=\s*([^|]*)", ib)}
        rar_en = clean(info.get("Rarity", "")).replace(" Brawler", "") if "Starting" not in info.get("Rarity", "") else "Starting Brawler"
        rar, cost = RAR.get(rar_en, (rar_en, ""))
        cls = CLS.get(clean(info.get("Class", "")), clean(info.get("Class", "")))
        def sec(prefix):
            mm = re.search(r"^==\s*" + prefix + r":\s*(.+?)\s*==\s*$", t, flags=re.M)
            return clean(mm.group(1)) if mm else ""
        def subs(head):
            mm = re.search(r"^==\s*" + head + r"\s*==\s*$(.*?)(?=^==[^=])", t, flags=re.M | re.S)
            return [clean(x) for x in re.findall(r"^===\s*(.+?)\s*===\s*$", mm.group(1), flags=re.M)] if mm else []
        kit = []
        if sec("Attack"): kit.append(f"Ataque: {sec('Attack')}")
        if sec("Super"): kit.append(f"Super: {sec('Super')}")
        g_ = subs("Gadgets"); sp = subs("Star Powers")
        if g_: kit.append("Gadgets: " + ", ".join(g_))
        if sp: kit.append("Star Powers: " + ", ".join(sp))
        if sec("Hypercharge"): kit.append(f"Hipercarga: {sec('Hypercharge')}")
        stats = []
        if info.get("Health"): stats.append(f"Vida: {clean(info['Health'])}")
        if info.get("MovementSpeed"): stats.append("Velocidade: " + clean(info["MovementSpeed"]).replace("Very Fast", "muito rápida").replace("Fast", "rápida").replace("Normal", "normal").replace("Slow", "lenta").replace("with", "com").replace("Very ", "muito "))
        skins = []
        sg = re.search(r"\{\{Skin Gallery(.*?)\}\}", t, flags=re.S)
        if sg:
            body = sg.group(1)
            for key, name in re.findall(r"\|Name(P?\d+)\s*=\s*([^|\n]+)", body):
                cm = re.search(r"\|Cost" + key + r"\s*=\s*([^|\n]+)", body)
                ca = re.search(r"\|Campaign" + key + r"\s*=\s*([^|\n]+)", body)
                nm = clean(name)
                skins.append({"name": nm if title.lower() in nm.lower() else f"{nm} {title}", "image": "", "category": clean(ca.group(1)) if ca else "Skin",
                              "howTo": tr_cost(cm.group(1)) if cm else ""})
        img = thumb(m, title)
        tut = [
            {"title": f"Conheça {title}", "text": f"Brawler {rar} da classe {cls}.", "image": img},
            {"title": "1. Junte Créditos", "text": "Créditos vêm da Loja, Starr Drops, Sequência Diária, Trilha de Troféus, Brawl Pass, Missões e Desafios.", "image": img},
            {"title": "2. Desbloqueie", "text": f"Custo da raridade {rar}: {cost}. Com Créditos, o desbloqueio acontece no Brawler Blast (sorteio entre os Brawlers da raridade). Gemas ou Chaves de Brawler liberam um Brawler específico.", "image": img},
            {"title": "3. Evolua", "text": "Suba o nível de poder com Moedas e Pontos de Poder para liberar Gadgets, Star Powers, Engrenagens e a Hipercarga.", "image": img},
        ]
        brawlers.append({"id": slug(title), "name": title, "image": img, "vintageImage": "", "kind": f"{rar} · {cls}",
                         "cost": cost, "requirements": [], "mastery": [], "skins": skins, "tutorial": tut,
                         "videoUrl": yt(f"{title} Brawl Stars dicas"),
                         "infoBlocks": [{"title": "⚔️ Kit do Brawler", "items": kit}, {"title": "📊 Atributos", "items": stats}],
                         "_rar": rar})
    brawlers.sort(key=lambda b: (ORDER.index(b["_rar"]) if b["_rar"] in ORDER else 99, b["name"]))
    groups = []
    for r in ORDER:
        lst = [ {k: v for k, v in b.items() if k != "_rar"} for b in brawlers if b["_rar"] == r]
        if lst: groups.append({"type": "toons", "title": f"{r} ({len(lst)})", "toons": lst})
    CRE = [
        ("KairosTime Gaming", ["YouTube", "Inglês"], "Listas de melhores Brawlers (tier list) e análise do meta.", "Sites de rankings de criadores (Feedspot, SponsorRadar)"),
        ("Lex - Brawl Stars", ["YouTube", "Inglês"], "Gameplay, guias de estratégia e momentos engraçados.", "Feedspot / vidIQ"),
        ("SpenLC - Brawl Stars", ["YouTube", "Inglês"], "Ex-jogador profissional da Tribe Gaming e campeão mundial de 2019; conteúdo competitivo.", "Descrição do canal (Feedspot)"),
        ("Rey - Brawl Stars", ["YouTube", "Inglês"], "Gameplay de alto nível.", "SponsorRadar"),
        ("Coach Cory - Brawl Stars", ["YouTube", "Inglês"], "Canal de dicas e treino.", "SponsorRadar"),
        ("Bruno Clash", ["YouTube", "Português"], "Personalidade brasileira dos jogos da Supercell e narrador de campeonatos.", "Blog oficial da Supercell"),
    ]
    data = {"id": g, "name": "Brawl Stars", "emoji": "⭐", "color": "#FACC15",
            "cover": thumb(m, "Shelly", 400),
            "description": "Jogo de batalhas rápidas da Supercell. Todos os Brawlers com raridade, custo, kit completo e skins.",
            "categories": [
        {"id": "brawlers", "title": "Brawlers", "emoji": "🦸", "description": f"{len(brawlers)} Brawlers com custo, kit e skins.",
         "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "text", "title": "", "body": "Toque em um Brawler para ver custo, kit (ataque, Super, Gadgets, Star Powers, Hipercarga), atributos, skins e o mini tutorial."}] + groups + [credit("Brawl Stars Wiki")]},
        {"id": "como-desbloquear", "title": "Como Desbloquear Brawlers", "emoji": "🔓", "description": "Créditos, Brawler Blast, Gemas e Chaves.",
         "status": "available", "updatedAt": TODAY,
         "sections": [
            {"type": "text", "title": "Brawler Blast", "body": "Desde setembro de 2026 os Brawlers são liberados pelo Brawler Blast: você junta Créditos para uma raridade e, ao completar, entra num estande de tiro com alvos dos Brawlers daquela raridade. Às vezes você pode escolher entre 2 a 4 Brawlers. Ele substituiu a antiga Starr Road."},
            {"type": "items", "title": "Custo por raridade", "items": [
                {"name": r, "tags": [], "description": c, "fields": {}} for r, c in
                [("Raro", "160 Créditos ou 29 Gemas"), ("Super Raro", "430 Créditos ou 79 Gemas"), ("Épico", "925 Créditos, 169 Gemas ou 1 Chave"),
                 ("Mítico", "1.900 Créditos, 349 Gemas ou 2 Chaves"), ("Lendário", "3.800 Créditos, 699 Gemas ou 4 Chaves"), ("Ultra Lendário", "5.500 Créditos, 999 Gemas ou 6 Chaves")]]},
            {"type": "notice", "title": "", "tone": "info", "body": "Gemas e Chaves de Brawler liberam um Brawler específico na hora, mas sem os bônus de nível de poder e melhorias que o Brawler Blast pode dar."},
            credit("Brawl Stars Wiki")]},
        {"id": "codigos", "title": "Códigos", "emoji": "🎁", "description": "Brawl Stars não usa códigos de resgate como os jogos do Roblox.",
         "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "text", "title": "", "body": "Brawl Stars não tem uma tela de resgate de códigos. As recompensas grátis chegam por links oficiais divulgados pela Supercell (que abrem o jogo e entregam o prêmio) e por eventos. Desconfie de sites que pedem login ou senha prometendo Gemas."},
                      {"type": "links", "title": "", "links": [{"label": "Site oficial do Brawl Stars (Supercell)", "url": "https://supercell.com/en/games/brawlstars/", "official": True}]}]},
        {"id": "guias", "title": "Guias e Dicas", "emoji": "🧭", "description": "Do iniciante ao profissional.",
         "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "levels", "title": "Dicas por nível", "levels": [
            {"level": "🟢 Iniciante", "tips": [
                "Comece com Shelly: alcance médio, muito dano de perto e um Super que destrói obstáculos. Nita, Colt e Poco também são fáceis. (Guia Brawl Stars Personagens)",
                "Use a mira manual nos ataques importantes: muita gente erra tiros fáceis no automático. (MuMu Player)",
                "Troque de Brawler para subir troféus em vários ao mesmo tempo. (4gnews)",
                "Jogue com amigos ou com o clube: vários modos dependem de trabalho em equipe. (4gnews)"]},
            {"level": "🟡 Intermediário", "tips": [
                "Aprenda a desviar: antecipe o tiro do adversário e ande para o lado oposto. (Esports.net)",
                "Conheça os papéis: tanques seguram dano, atiradores causam dano à distância, suportes curam e controlam. (ExitLag)",
                "Posicionamento e controle do mapa fazem mais diferença do que só dano. (Esports.net)"]},
            {"level": "🟠 Avançado", "tips": [
                "Assista às suas partidas e procure os erros que se repetem. (Esports.net)",
                "Acompanhe a tier list da temporada (KairosTime publica uma a cada atualização) para escolher Brawlers fortes no meta. (KairosTime Gaming)"]},
            {"level": "🔴 Profissional", "tips": [
                "Assista a jogadores profissionais e campeonatos para aprender táticas de equipe e composições. (ExitLag / Canaltech)",
                "Estude os mapas de cada modo: arbustos, paredes e rotas mudam a escolha do Brawler. (Digitei)"]}]},
            {"type": "notice", "title": "", "tone": "info", "body": "Entre parênteses está a fonte de cada dica."}]},
        {"id": "criadores", "title": "Criadores de Conteúdo", "emoji": "📺", "description": "YouTubers e tiktokers para aprender.",
         "status": "available", "updatedAt": TODAY,
         "sections": [creators_section(CRE), CREATORS_NOTE, creators_links(CRE, "Brawl Stars")]},
        {"id": "info", "title": "Links Oficiais", "emoji": "🔗", "description": "Fontes oficiais.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "links", "title": "", "links": [{"label": "Site oficial do Brawl Stars (Supercell)", "url": "https://supercell.com/en/games/brawlstars/", "official": True},
                                                              {"label": "Brawl Stars Wiki", "url": "https://brawlstars.fandom.com/wiki/Brawl_Stars_Wiki", "official": False}]}]},
    ]}
    return data, {"characters": len(brawlers), "skins": sum(len(b["skins"]) for b in brawlers), "label": "Brawlers"}

# =====================================================================
# FREE FIRE
# =====================================================================
def free_fire():
    g = "free-fire"; m = meta(g)
    chars = []
    for title in m["categories"].get("Category:Characters", []):
        t = page(g, m, title)
        ab = re.search(r"\|hability=([^|}]+)", t)
        ability = clean(ab.group(1)) if ab else ""
        img = thumb(m, title)
        chars.append({"id": slug(title), "name": title, "image": img, "vintageImage": "", "kind": "Personagem",
                      "cost": "Loja do jogo ou eventos (varia)", "requirements": [], "mastery": [], "skins": [],
                      "tutorial": [
                          {"title": f"Conheça {title}", "text": f"Habilidade: {ability or 'veja no jogo'}.", "image": img},
                          {"title": "1. Abra a aba Personagens", "text": "No lobby, toque em Personagem para ver todos os personagens e suas habilidades.", "image": img},
                          {"title": "2. Desbloqueie", "text": "Personagens podem ser liberados na loja do jogo ou em eventos. O preço e a forma mudam com o tempo; confira no próprio jogo.", "image": img},
                          {"title": "3. Equipe a habilidade", "text": "Equipe a habilidade do personagem nos espaços de habilidade e teste no modo treino.", "image": img}],
                      "videoUrl": yt(f"{title} Free Fire habilidade como usar"),
                      "infoBlocks": [{"title": "✨ Habilidade", "items": [ability] if ability else ["Consulte no jogo."]}]})
    pets = [{"image": thumb(m, p), "caption": p, "subtitle": "Pet"} for p in m["categories"].get("Category:Pets", [])]
    CRE = [
        ("Nobru", ["YouTube", "TikTok", "Instagram"], "Um dos influenciadores brasileiros mais lembrados pelos gamers; cofundador da Fluxo.", "Meio & Mensagem / TheTVDB"),
        ("Cerol", ["YouTube", "Instagram"], "Eleito melhor streamer no Prêmio eSports Brasil 2019; cofundador da Fluxo.", "TheTVDB / Fragster"),
        ("El Gato", ["YouTube", "Instagram"], "Dono de uma das maiores organizações de esports e grande influenciador do mobile.", "Fragster"),
        ("Thurzin", ["YouTube", "TikTok"], "Streamer e jogador profissional, um dos maiores nomes da história do Free Fire.", "Fragster"),
        ("Loud Babi", ["YouTube", "Instagram"], "Uma das streamers e criadoras de Free Fire mais populares do Brasil.", "oEmbed (lista de canais)"),
        ("PlayHard", ["YouTube"], "Canal grande de gameplay de Free Fire.", "Free Fire Mania"),
        ("Jefão", ["YouTube"], "Esteve no top 10 de criadores em ascensão do YouTube em 2020.", "Free Fire Mania"),
    ]
    data = {"id": g, "name": "Free Fire", "emoji": "🔥", "color": "#F97316",
            "cover": thumb(m, "Free Fire", 400),
            "description": "Battle royale da Garena. Personagens com habilidades, pets, dicas e códigos pelo site oficial.",
            "categories": [
        {"id": "personagens", "title": "Personagens", "emoji": "🦸", "description": f"{len(chars)} personagens e suas habilidades.",
         "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "toons", "title": "Todos os personagens", "toons": chars}, credit("FreeFire Wiki")]},
        {"id": "pets", "title": "Pets", "emoji": "🐾", "description": "Companheiros que ajudam na partida.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "gallery", "title": "Pets", "images": pets}, credit("FreeFire Wiki")]},
        {"id": "codigos", "title": "Códigos e Como Resgatar", "emoji": "🎁", "description": "Resgate pelo site oficial da Garena.",
         "status": "available", "updatedAt": TODAY,
         "sections": [
            {"type": "steps", "title": "Como resgatar (site oficial)", "items": [
                "Abra o site oficial de resgate da Garena (link abaixo).",
                "Entre com a mesma conta vinculada ao seu Free Fire (Facebook, Google, VK, X ou Apple).",
                "Digite o código de 12 caracteres e confirme.",
                "A recompensa chega no correio do jogo (pode demorar até 24 horas)."]},
            {"type": "notice", "title": "", "tone": "warning", "body": "Use apenas o site oficial. Nunca informe sua senha em sites que prometem códigos ou diamantes grátis. Contas de convidado não conseguem resgatar."},
            {"type": "codes", "title": "Códigos ativos", "codes": []},
            {"type": "links", "title": "", "links": [{"label": "Site oficial de resgate (Garena)", "url": "https://reward.ff.garena.com/", "official": True}]}]},
        {"id": "guias", "title": "Guias e Dicas", "emoji": "🧭", "description": "Do iniciante ao profissional.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "levels", "title": "Dicas por nível", "levels": [
            {"level": "🟢 Iniciante", "tips": [
                "Pule em lugares com poucos jogadores para pegar itens com calma. (Buscapé / Free Fire Mania)",
                "Ajuste os controles e a sensibilidade antes de jogar ranqueada. (Free Fire Mania)",
                "Conheça bem a habilidade do seu personagem. (Showmetech)",
                "Fique de olho na zona segura e não pegue loot parado no meio do caminho. (Showmetech)"]},
            {"level": "🟡 Intermediário", "tips": [
                "Treine a mira e o controle de recuo no campo de treino. (Free Fire Mania)",
                "Use o modo Contra Squad para melhorar mira, movimentação e decisões. (Free Fire Mania)",
                "Use veículos principalmente no começo da partida. (Showmetech)"]},
            {"level": "🟠 Avançado", "tips": [
                "Leia as notas de atualização: mudanças de armas e personagens mudam o meta. (Canaltech)",
                "Domine a parede de gel para se proteger rápido em trocação. (Free Fire Mania)"]},
            {"level": "🔴 Profissional", "tips": [
                "Assista aos profissionais e criadores jogando para copiar rotações e posicionamento. (Canaltech)",
                "Jogue com o mesmo squad e combine funções antes da partida. (Free Fire Mania)"]}]},
            {"type": "notice", "title": "", "tone": "info", "body": "Entre parênteses está a fonte de cada dica."}]},
        {"id": "criadores", "title": "Criadores de Conteúdo", "emoji": "📺", "description": "YouTubers, tiktokers e Instagram.", "status": "available", "updatedAt": TODAY,
         "sections": [creators_section(CRE), CREATORS_NOTE, creators_links(CRE, "Free Fire")]},
        {"id": "info", "title": "Links Oficiais", "emoji": "🔗", "description": "Fontes oficiais.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "links", "title": "", "links": [{"label": "Site oficial do Free Fire (Garena)", "url": "https://ff.garena.com/", "official": True},
                                                              {"label": "Site oficial de resgate (Garena)", "url": "https://reward.ff.garena.com/", "official": True}]}]},
    ]}
    return data, {"characters": len(chars), "skins": 0, "label": "Personagens"}

# =====================================================================
# MURDER MYSTERY 2
# =====================================================================
def murder_mystery():
    g = "murder-mystery"; m = meta(g)
    items = []
    names = list(dict.fromkeys(m["categories"].get("Category:Ancient", []) + m["categories"].get("Category:Godly", [])))
    for title in names:
        t = page(g, m, title)
        if "Infobox" not in t: continue
        f = lambda k: (re.search(r"^\|\s*" + k + r"\s*=(.*?)(?=^\||^\}\})", t, flags=re.M | re.S) or [None, ""])[1]
        typ = clean(f("type"))
        if typ not in ("Knife", "Gun"): continue
        rar = clean(f("rarity"))
        obtain = clean(f("obtain"))
        val = clean(f("value")).split("(")[0].strip()
        low = obtain.lower()
        EVN = {"christmas": "Natal", "halloween": "Halloween", "summer": "Verão", "easter": "Páscoa", "thanksgiving": "Ação de Graças", "valentine": "Namorados", "winter": "Inverno"}
        ev = next((pt for en, pt in EVN.items() if en in low), "")
        yr = (re.search(r"(20\d\d)", obtain) or [None, ""])[1]
        origin = (f"{ev} {yr}".strip()) if ev else ""
        if "nikilis" in low: status = "Dado pelo criador do jogo"
        elif ("formerly" in low or "offsale" in low) and "trading" in low: status = "Hoje só por troca"
        elif "unbox" in low or "box" in low: status = "Caixa"
        elif "craft" in low: status = "Criação (crafting)"
        elif "trading" in low: status = "Troca"
        else: status = "Ver no jogo"
        ob = " · ".join(x for x in [origin, status] if x)
        items.append({"name": title, "image": thumb(m, title), "type": "Faca" if typ == "Knife" else "Arma (gun)",
                      "rarity": "Ancient" if "ncient" in rar else ("Godly" if "odly" in rar else rar), "obtain": ob, "value": val})
    def v(x):
        try: return float(re.sub(r"[^\d.]", "", x["value"]) or 0)
        except: return 0
    items.sort(key=lambda x: -v(x))
    anc = [i for i in items if i["rarity"] == "Ancient"]
    god = [i for i in items if i["rarity"] == "Godly"]
    gal = lambda lst: [{"image": i["image"], "caption": i["name"], "subtitle": f"{i['type']} · {i['obtain']}" + (f" · valor ~{i['value']}" if i["value"] else "")} for i in lst]
    CRE = [
        ("FocusRBX", ["YouTube"], "Canal focado em Murder Mystery 2, com vídeos e shorts.", "The Famous People"),
        ("iFres", ["YouTube", "Espanhol"], "Vídeos de MM2 com centenas de milhares de visualizações.", "vidIQ"),
        ("Nikilis", ["Criador do jogo"], "Desenvolvedor do Murder Mystery 2; acompanhe as novidades oficiais.", "Rowatcher"),
    ]
    data = {"id": g, "name": "Murder Mystery 2", "emoji": "🔪", "color": "#EF4444",
            "cover": thumb(m, "Murder Mystery 2", 400),
            "description": "Jogo do Roblox criado por Nikilis: um Assassino, um Xerife e vários Inocentes por rodada.",
            "categories": [
        {"id": "papeis", "title": "Papéis (como jogar)", "emoji": "🎭", "description": "Inocente, Xerife, Assassino e Herói.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "items", "title": "", "items": [
            {"name": "🙂 Inocente", "tags": ["Maioria dos jogadores"], "description": "Fuja e se esconda do Assassino, junte moedas e ajude a descobrir quem ele é.", "fields": {}},
            {"name": "🔫 Xerife", "tags": ["1 por rodada"], "description": "Tem a arma. Precisa descobrir e atirar no Assassino, trabalhando com os Inocentes.", "fields": {}},
            {"name": "🔪 Assassino", "tags": ["1 por rodada"], "description": "Precisa eliminar todos sem levar tiro do Xerife.", "fields": {}},
            {"name": "🦸 Herói", "tags": ["Especial"], "description": "Se o Xerife morrer, qualquer Inocente pode pegar a arma caída. Quem acertar o Assassino com ela vira Herói.", "fields": {}}]},
            credit("Murder Mystery 2 Wiki")]},
        {"id": "ancient", "title": "Armas Ancient", "emoji": "🟣", "description": f"{len(anc)} facas e armas Ancient com foto.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "text", "title": "", "body": "Ancient é a raridade mais alta. A maioria veio de eventos antigos e hoje só se consegue por troca. Os valores são aproximados, de sites de valores da comunidade citados pela wiki, e mudam com o tempo."},
                      {"type": "gallery", "title": "Ancients", "images": gal(anc)}, credit("Murder Mystery 2 Wiki")]},
        {"id": "godly", "title": "Armas Godly", "emoji": "🔴", "description": f"{len(god)} facas e armas Godly com foto.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "text", "title": "", "body": "Godly é a raridade logo abaixo de Ancient. Muitas vieram de caixas e eventos; as antigas só por troca. Ordenadas do maior para o menor valor aproximado."},
                      {"type": "gallery", "title": "Godlies", "images": gal(god)}, credit("Murder Mystery 2 Wiki")]},
        {"id": "codigos", "title": "Códigos", "emoji": "🎁", "description": "Como resgatar e códigos antigos.", "status": "available", "updatedAt": TODAY,
         "sections": [
            {"type": "steps", "title": "Como resgatar", "items": ["Abra o Murder Mystery 2 no Roblox.", "Toque no ícone de Inventário.", "Procure a caixa de código, digite e confirme."]},
            {"type": "codes", "title": "Códigos expirados", "codes": [{"code": c, "reward": r, "status": "expired", "note": ""} for c, r in
                [("COMB4T2", "Faca Combat II"), ("PR1SM", "Faca Prism"), ("AL3X", "Faca Alex"), ("SUBo", "Faca verde"), ("D3NIS", "Faca azul-escura"), ("CORL", "Faca azul"), ("SK3TCH", "Faca laranja"), ("NatureUpdate", "500 de ouro")]]},
            {"type": "notice", "title": "", "tone": "info", "body": "Não encontramos códigos ativos confirmados no momento."}]},
        {"id": "guias", "title": "Guias e Dicas", "emoji": "🧭", "description": "Dicas por papel e por nível.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "levels", "title": "Dicas por nível", "levels": [
            {"level": "🟢 Iniciante", "tips": [
                "Aprenda os três papéis antes de tudo: Inocente, Xerife e Assassino. (Gameranx)",
                "Como Inocente, não se mova demais: correr à toa chama atenção. (Pocket Gamer)",
                "Conheça o mapa e planeje rotas de fuga. (Gameranx)"]},
            {"level": "🟡 Intermediário", "tips": [
                "Como Inocente, fique perto de quem age normal, mas evite grupos grandes. (Guia MM2 Beginner's Tips)",
                "Como Xerife, mantenha distância de todo mundo e só atire quando tiver certeza. (Gamepur)",
                "Se o Xerife cair, cuidado ao buscar a arma: o Assassino costuma esperar perto dela. (Guia MM2 Beginner's Tips)"]},
            {"level": "🟠 Avançado", "tips": [
                "Como Assassino, finja ser inocente e escolha bem as habilidades (perks). (Gamepur)",
                "Se a arma estiver muito vigiada, junte moedas e espere: dá para vencer pelo tempo. (Guia MM2 Beginner's Tips)"]},
            {"level": "🔴 Profissional", "tips": [
                "Fique de olho em quem anda de forma estranha para identificar o papel cedo. (Guia MM2 Beginner's Tips)",
                "Nas trocas, pesquise o valor dos itens antes para não cair em golpe. (Guia MM2 Beginner's Tips)"]}]},
            {"type": "notice", "title": "", "tone": "info", "body": "Entre parênteses está a fonte de cada dica."}]},
        {"id": "criadores", "title": "Criadores de Conteúdo", "emoji": "📺", "description": "Canais de MM2.", "status": "available", "updatedAt": TODAY,
         "sections": [creators_section(CRE), CREATORS_NOTE, creators_links(CRE, "Murder Mystery 2")]},
        {"id": "info", "title": "Links", "emoji": "🔗", "description": "Wiki e página do jogo.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "links", "title": "", "links": [{"label": "Murder Mystery 2 no Roblox (busca oficial)", "url": "https://www.roblox.com/discover/?Keyword=Murder%20Mystery%202", "official": True},
                                                              {"label": "Murder Mystery 2 Wiki", "url": "https://murder-mystery-2.fandom.com/wiki/Murder_Mystery_2_Wiki", "official": False}]}]},
    ]}
    return data, {"characters": len(items), "skins": len(items), "label": "Armas"}

# =====================================================================
# BLOX FRUITS
# =====================================================================
def blox_fruits():
    g = "blox-fruits"; m = meta(g)
    RAR = {"Common": "Comum", "Uncommon": "Incomum", "Rare": "Rara", "Legendary": "Lendária", "Mythical": "Mítica"}
    TYP = {"Natural": "Natural", "Elemental": "Elemental", "Beast": "Besta"}
    ORDER = ["Mítica", "Lendária", "Rara", "Incomum", "Comum"]
    fruits = []
    for title in m["categories"].get("Category:Blox Fruits", []):
        t = page(g, m, title)
        if "{{Blox Fruit Infobox" not in t or "(Admin)" in title or "(Classic)" in title: continue
        f = lambda k: (re.search(r"^\|\s*" + k + r"\s*=\s*([^\n]*)", t, flags=re.M) or [None, ""])[1].strip()
        rar = RAR.get(clean(f("rarity")), clean(f("rarity"))); typ = TYP.get(clean(f("type")), clean(f("type")))
        money = re.sub(r"[^\d]", "", f("money")); robux = re.sub(r"[^\d]", "", f("robux"))
        if not rar: continue
        cost = " ou ".join(x for x in [f"{num(money)} Beli" if money else "", f"{num(robux)} Robux" if robux else ""] if x) or "Não vendida no Dealer"
        img = thumb(m, title)
        fruits.append({"id": slug(title), "name": title, "image": img, "vintageImage": "", "kind": f"{rar} · {typ}",
                       "cost": cost, "requirements": [], "mastery": [], "skins": [],
                       "tutorial": [
                           {"title": f"Conheça {title}", "text": f"Fruta {rar} do tipo {typ}.", "image": img},
                           {"title": "1. Junte Beli", "text": "Faça missões, derrote inimigos e chefes para juntar Beli (a moeda do jogo).", "image": img},
                           {"title": "2. Vá ao Blox Fruit Dealer", "text": f"O vendedor de frutas fica nas ilhas e o estoque muda de tempos em tempos. Preço: {cost}.", "image": img},
                           {"title": "3. Coma e treine", "text": "Comer uma fruta troca a fruta atual. Coloque pontos no atributo Fruta (Blox Fruit) para aumentar o dano dela.", "image": img}],
                       "videoUrl": yt(f"{title} Blox Fruits combo guia"),
                       "infoBlocks": [{"title": "📋 Ficha", "items": [f"Raridade: {rar}", f"Tipo: {typ}", f"Preço: {cost}"] + ([f"Adicionada na atualização {clean(f('update'))}"] if clean(f('update')) else [])}],
                       "_r": rar, "_m": int(money or 0)})
    fruits.sort(key=lambda x: (ORDER.index(x["_r"]) if x["_r"] in ORDER else 9, -x["_m"]))
    groups = []
    for r in ORDER:
        lst = [{k: v for k, v in x.items() if not k.startswith("_")} for x in fruits if x["_r"] == r]
        if lst: groups.append({"type": "toons", "title": f"Frutas {r}s ({len(lst)})".replace("Míticas", "Míticas"), "toons": lst})
    styles = [{"image": thumb(m, s), "caption": s, "subtitle": "Estilo de luta"} for s in m["categories"].get("Category:Fighting Styles", []) if thumb(m, s)]
    swords = [{"image": thumb(m, s), "caption": s, "subtitle": "Espada"} for s in m["categories"].get("Category:Swords", []) if thumb(m, s)]
    races = [{"image": thumb(m, s), "caption": s, "subtitle": "Raça"} for s in m["categories"].get("Category:Races", []) if thumb(m, s)]
    CRE = [
        ("Gamer Robot", ["YouTube", "Oficial"], "Canal oficial do Blox Fruits (Gamer Robot Inc), com novidades e atualizações.", "vidIQ (descrição do canal)"),
        ("Cafazera", ["YouTube", "Instagram", "Português"], "Criador brasileiro focado em Blox Fruits: tutoriais de combos, espadas e bounty.", "CreatorDB / Famous Birthdays"),
        ("Lucas Clash ON", ["YouTube", "Português"], "Também posta tutoriais e gameplay de Blox Fruits.", "Famous Birthdays"),
        ("AccelToWin", ["YouTube", "Inglês"], "Guias de progressão do Primeiro e Segundo Mar para iniciantes.", "Lista de canais de estratégia de Blox Fruits"),
    ]
    data = {"id": g, "name": "Blox Fruits", "emoji": "🍈", "color": "#22C55E",
            "cover": thumb(m, "Blox Fruits", 400),
            "description": "RPG do Roblox (Gamer Robot Inc) inspirado em One Piece: frutas com poderes, três mares, espadas e estilos de luta.",
            "categories": [
        {"id": "frutas", "title": "Frutas", "emoji": "🍈", "description": f"{len(fruits)} frutas com raridade, tipo e preço.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "text", "title": "", "body": "Toque em uma fruta para ver ficha, preço em Beli e Robux e o mini tutorial de como conseguir."}] + groups + [credit("Blox Fruits Wiki")]},
        {"id": "estilos", "title": "Estilos de Luta", "emoji": "🥋", "description": "Fighting Styles.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "gallery", "title": "", "images": styles}, credit("Blox Fruits Wiki")]},
        {"id": "espadas", "title": "Espadas", "emoji": "🗡️", "description": "Swords.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "gallery", "title": "", "images": swords}, credit("Blox Fruits Wiki")]},
        {"id": "racas", "title": "Raças", "emoji": "🧬", "description": "Races.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "gallery", "title": "", "images": races}, credit("Blox Fruits Wiki")]},
        {"id": "guias", "title": "Guias e Dicas", "emoji": "🧭", "description": "Do Primeiro ao Terceiro Mar.", "status": "available", "updatedAt": TODAY,
         "sections": [
            {"type": "items", "title": "Os três mares", "items": [
                {"name": "🌊 Primeiro Mar", "tags": ["Nível 1–700"], "description": "Área de iniciante: foque em missões e subir de nível.", "fields": {}},
                {"name": "🌊 Segundo Mar", "tags": ["Nível 700–1500"], "description": "Novas ilhas, chefes e raids.", "fields": {}},
                {"name": "🌊 Terceiro Mar", "tags": ["Nível 1500+"], "description": "Conteúdo de fim de jogo.", "fields": {}}]},
            {"type": "levels", "title": "Dicas por nível", "levels": [
                {"level": "🟢 Iniciante", "tips": [
                    "No Primeiro Mar, foque só em missões e em subir de nível. (FruityBlox / BioBlox)",
                    "Frutas como Light, Magma ou Flame ajudam muito no começo. (BioBlox)",
                    "PvP não dá experiência: deixe para depois. (Hablamos de Gamers)",
                    "Use códigos ativos para ganhar boost de XP e reset de atributos. (BioBlox)"]},
                {"level": "🟡 Intermediário", "tips": [
                    "Distribua bem os pontos: Melee, Defesa e Fruta são os principais. (Guias de Blox Fruits)",
                    "Sua primeira fruta não precisa ser a definitiva; troque quando achar uma melhor. (Guia para iniciantes)"]},
                {"level": "🟠 Avançado", "tips": [
                    "Para frutas Elementais e de Besta, uma divisão comum é 25% Melee, 25% Defesa e 50% Fruta. (Guia de canais de estratégia)",
                    "Aprenda combos com criadores especializados (Cafazera ensina combos passo a passo). (CreatorDB)"]},
                {"level": "🔴 Profissional", "tips": [
                    "PvP só faz sentido com nível alto; até lá, priorize progressão. (Deltia's Gaming)",
                    "Bounty alto (como 30 milhões) exige muito PvP; estude combos e cooldowns. (CreatorDB / Cafazera)"]}]},
            {"type": "notice", "title": "", "tone": "info", "body": "Entre parênteses está a fonte de cada dica."}]},
        {"id": "codigos", "title": "Códigos", "emoji": "🎁", "description": "Onde encontrar códigos.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "text", "title": "", "body": "Os códigos do Blox Fruits costumam sair no X (Twitter) oficial do jogo e aparecem na wiki. Eles dão boost de XP, Beli ou reset de atributos. Ainda não temos códigos ativos confirmados para listar aqui."},
                      {"type": "codes", "title": "Códigos ativos", "codes": []}]},
        {"id": "criadores", "title": "Criadores de Conteúdo", "emoji": "📺", "description": "YouTubers e tiktokers.", "status": "available", "updatedAt": TODAY,
         "sections": [creators_section(CRE), CREATORS_NOTE, creators_links(CRE, "Blox Fruits")]},
        {"id": "info", "title": "Links", "emoji": "🔗", "description": "Wiki e página do jogo.", "status": "available", "updatedAt": TODAY,
         "sections": [{"type": "links", "title": "", "links": [{"label": "Blox Fruits no Roblox (busca oficial)", "url": "https://www.roblox.com/discover/?Keyword=Blox%20Fruits", "official": True},
                                                              {"label": "Blox Fruits Wiki", "url": "https://blox-fruits.fandom.com/wiki/Blox_Fruits_Wiki", "official": False}]}]},
    ]}
    return data, {"characters": len(fruits), "skins": 0, "label": "Frutas"}

if __name__ == "__main__":
    out = sys.argv[1]
    stats = {}
    for fn in (brawl_stars, free_fire, murder_mystery, blox_fruits):
        data, st = fn()
        json.dump(data, open(f"{out}/games/{data['id']}.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
        stats[data["id"]] = {**st, "cover": data["cover"], "categories": len(data["categories"])}
        print(data["id"], st, "capa:", bool(data["cover"]))
    json.dump(stats, open("/home/claude/scripts/others_stats.json", "w"), ensure_ascii=False, indent=1)
