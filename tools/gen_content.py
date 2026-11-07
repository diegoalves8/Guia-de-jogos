import json, os, sys
root = sys.argv[1]
G = os.path.join(root, "games"); os.makedirs(G, exist_ok=True)
TODAY = "2026-11-07"
TOONS = json.load(open("/home/claude/scripts/toons_final.json"))
EV = {"Bobette":"Natal","Coal":"Natal","Ginger":"Natal","Rudie":"Natal","Gourdy":"Halloween","Eclipse":"Halloween","Soulvester":"Halloween","Ribecca":"Halloween","Bassie":"Páscoa","Cocoa":"Páscoa","Flyte":"Páscoa","Eggson":"Páscoa"}
def skins_where(pred):
    return [(t, sk) for t in TOONS for sk in t["skins"] if pred(t, sk)]
CREDIT = {"type": "text", "title": "", "body": "Dados e imagens: Dandy's World Wiki (Fandom, conteúdo CC BY-SA) e BlushCrunch Studio. As imagens precisam de internet na primeira vez; depois ficam salvas no aparelho."}

def soon(id, title, emoji, desc):
    return {"id": id, "title": title, "emoji": emoji, "description": desc, "status": "coming_soon", "sections": []}
def dump(path, obj):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2); f.write("\n")

ROBLOX_SEARCH = "https://www.roblox.com/discover/?Keyword=Dandy%27s%20World"


ROBLOX_PLAY = "https://play.google.com/store/apps/details?id=com.roblox.client"
ROBLOX_COMPAT = "Jogo do Roblox: Android, iOS, PC, Xbox e PlayStation (precisa do app Roblox)."
META = {
  "dandys-world": {"developer": "BlushCrunch Studio", "genres": ["Terror", "Roblox"], "badges": [],
     "longDescription": "Jogo de terror cooperativo (estilo mascot horror) do Roblox. Você escolhe um Toon, conserta máquinas em cada andar do Gardenview Center e tenta escapar dos Twisteds. Cada Toon tem habilidades, missões de Maestria e skins.",
     "storeUrl": "https://www.roblox.com/discover/?Keyword=Dandy%27s%20World", "storeLabel": "Abrir no Roblox", "compatibility": ROBLOX_COMPAT},
  "brawl-stars": {"developer": "Supercell", "genres": ["Estratégia", "Ação"], "badges": ["Popular"],
     "longDescription": "Jogo de batalhas rápidas 3 contra 3 e Battle Royale da Supercell. Você desbloqueia e evolui Brawlers, cada um com ataque, Super, Gadgets, Star Powers e Hipercarga.",
     "storeUrl": "https://play.google.com/store/apps/details?id=com.supercell.brawlstars", "storeLabel": "Baixar na Play Store", "compatibility": "Android e iOS."},
  "free-fire": {"developer": "Garena", "genres": ["Ação", "Aventura"], "badges": ["Popular"],
     "longDescription": "Battle royale para celular da Garena: até 50 jogadores numa ilha, com personagens que têm habilidades próprias, pets e muitos modos de jogo.",
     "storeUrl": "https://play.google.com/store/apps/details?id=com.dts.freefireth", "storeLabel": "Baixar na Play Store", "compatibility": "Android e iOS."},
  "murder-mystery": {"developer": "Nikilis", "genres": ["Terror", "Roblox"], "badges": ["Popular"],
     "longDescription": "Jogo de mistério do Roblox: a cada rodada um jogador é o Assassino, outro o Xerife e o resto Inocentes. Tem um dos maiores sistemas de troca de facas e armas do Roblox.",
     "storeUrl": "https://www.roblox.com/discover/?Keyword=Murder%20Mystery%202", "storeLabel": "Abrir no Roblox", "compatibility": ROBLOX_COMPAT},
  "blox-fruits": {"developer": "Gamer Robot Inc", "genres": ["Aventura", "Roblox"], "badges": ["Popular", "Novo no app"],
     "longDescription": "RPG de aventura do Roblox inspirado em One Piece. Você navega por três mares, sobe de nível, coleta frutas com poderes, espadas e estilos de luta.",
     "storeUrl": "https://www.roblox.com/discover/?Keyword=Blox%20Fruits", "storeLabel": "Abrir no Roblox", "compatibility": ROBLOX_COMPAT},
}
NEWS = [
  {"date": "2026-11-07", "title": "Visual novo", "body": "Interface redesenhada no estilo Play Store, com busca, favoritos e layout pensado para a tela horizontal da multimídia BYD."},
  {"date": "2026-11-07", "title": "Blox Fruits chegou", "body": "51 frutas com preço em Beli e Robux, estilos de luta, espadas e raças."},
  {"date": "2026-11-07", "title": "4 jogos completos", "body": "Brawl Stars (107 Brawlers), Free Fire (49 personagens), Murder Mystery 2 (156 armas) e criadores de conteúdo em todos os jogos."},
  {"date": "2026-11-07", "title": "Atualização dentro do app", "body": "Em Configurações dá para baixar a versão nova do app e o conteúdo mais recente sem sair dele."},
  {"date": "2026-11-07", "title": "Toons do Dandy's World", "body": "40 Toons com foto, custo, Maestria, 264 skins e mini tutorial animado."},
]
OS = json.load(open("/home/claude/scripts/others_stats.json"))
DW_COVER = "https://static.wikia.nocookie.net/dandys-world-robloxhorror/images/" + json.load(open("/home/claude/wiki/images.json"))["images"]["Dandy Full Render.png"]["url"].split("/images/")[1].replace("/revision/latest", "/revision/latest/scale-to-width-down/400", 1)
def st(gid): o = OS[gid]; return {"characters": o["characters"], "skins": o["skins"], "label": o["label"]}
index = {
  "contentVersion": 4,
  "updatedAt": TODAY,
  "news": NEWS,
  "games": [
    {"id": "dandys-world", "name": "Dandy's World", "subtitle": "Terror cooperativo com Toons", "platform": "Roblox",
     "emoji": "🌼", "color": "#F59E0B", "status": "available", "file": "games/dandys-world.json", "cover": DW_COVER,
     "stats": {"characters": len(TOONS), "skins": sum(len(t["skins"]) for t in TOONS), "label": "Toons"}},
    {"id": "brawl-stars", "name": "Brawl Stars", "subtitle": "Batalhas rápidas da Supercell", "platform": "Android / iOS",
     "emoji": "⭐", "color": "#FACC15", "status": "available", "file": "games/brawl-stars.json", "cover": OS["brawl-stars"]["cover"], "stats": st("brawl-stars")},
    {"id": "free-fire", "name": "Free Fire", "subtitle": "Battle royale da Garena", "platform": "Android / iOS",
     "emoji": "🔥", "color": "#F97316", "status": "available", "file": "games/free-fire.json", "cover": OS["free-fire"]["cover"], "stats": st("free-fire")},
    {"id": "murder-mystery", "name": "Murder Mystery 2", "subtitle": "Descubra quem é o assassino", "platform": "Roblox",
     "emoji": "🔪", "color": "#EF4444", "status": "available", "file": "games/murder-mystery.json", "cover": OS["murder-mystery"]["cover"], "stats": st("murder-mystery")},
    {"id": "blox-fruits", "name": "Blox Fruits", "subtitle": "Frutas, mares e combos", "platform": "Roblox",
     "emoji": "🍈", "color": "#22C55E", "status": "available", "file": "games/blox-fruits.json", "cover": OS["blox-fruits"]["cover"], "stats": st("blox-fruits")},
  ],
}
for g in index["games"]:
    g.update(META[g["id"]])
    g["cover"] = g["cover"].replace("/scale-to-width-down/400", "/scale-to-width-down/800")
dump(os.path.join(root, "games.json"), index)

dandy = {
  "id": "dandys-world", "name": "Dandy's World", "emoji": "🌼", "color": "#F59E0B", "cover": DW_COVER,
  "description": "Jogo de terror cooperativo no Roblox, da BlushCrunch Studio. Você joga como um Toon, conserta máquinas e foge dos Twisteds dentro do Gardenview Center.",
  "categories": [
    {
      "id": "codigos", "title": "Códigos e Como Resgatar", "emoji": "🎁",
      "description": "Códigos atuais, expirados e o passo a passo para resgatar.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "text", "title": "O que são os códigos?",
         "body": "Códigos são palavras liberadas pelos desenvolvedores do Dandy's World (BlushCrunch Studio). Ao digitar um código válido dentro do jogo, você ganha recompensas grátis — normalmente Ichor, a moeda do jogo. Já existiram códigos que davam skin."},
        {"type": "text", "title": "Para que serve a recompensa?",
         "body": "O Ichor é usado na Dandy's Store para comprar novos Toons (personagens) e Trinkets (acessórios que ajudam na partida). Ou seja: códigos ajudam você a liberar coisas novas mais rápido."},
        {"type": "notice", "title": "", "tone": "warning",
         "body": "Os códigos podem expirar a qualquer momento e diferenciam maiúsculas de minúsculas. Digite exatamente como aparece aqui. Este app nunca inventa códigos: só listamos o que foi divulgado."},
        {"type": "codes", "title": "Códigos ativos",
         "codes": [
           {"code": "ICHOR", "reward": "50 Ichor", "status": "active",
            "note": "Reportado como ativo por vários sites especializados até abril de 2026. Se não funcionar, ele pode ter expirado."},
           {"code": "RESEARCHMOBILE", "reward": "Skin Research-mobile do Rodger", "status": "unverified",
            "note": "Segundo a Dandy's World Wiki, o código da skin Research-mobile volta todo 1º de abril. Fora dessa data pode não funcionar."}
         ]},
        {"type": "steps", "title": "Como resgatar",
         "items": [
           "Abra o Dandy's World no Roblox.",
           "Espere o jogo carregar e fique no lobby (antes de entrar no elevador).",
           "No lado esquerdo da tela, toque no botão \"USE CODE\" (fica na coluna de botões, abaixo de Twisteds).",
           "Digite ou cole o código na caixa de texto.",
           "Toque em \"USE\" para receber a recompensa."
         ]},
        {"type": "codes", "title": "Códigos expirados (não funcionam mais)",
         "codes": [
           {"code": c, "reward": "", "status": "expired", "note": ""} for c in
           ["ONETHOUSAND", "TENMILLION", "FIFTYMILLION", "HUNDREDMILLION", "2HUNDREDMILLION",
            "1BILLION", "300K", "SKINTICKET", "EASTER2025", "APRIL1", "FESTIVEGIFT"]
         ]},
        {"type": "text", "title": "Onde encontrar novos códigos?",
         "body": "Novos códigos costumam sair em grandes atualizações, marcos de visitas e datas comemorativas. As fontes oficiais são o Discord da BlushCrunch Studio, o grupo BlushCrunch Studio no Roblox e a própria página do jogo no Roblox. Neste app, toque no botão de atualizar (↻) na tela de jogos para baixar a lista mais recente."},
        {"type": "links", "title": "Links úteis",
         "links": [
           {"label": "Dandy's World no Roblox (busca oficial)", "url": ROBLOX_SEARCH, "official": True},
           {"label": "Lista de códigos — Roblox Den", "url": "https://robloxden.com/game-codes/dandy-s-world", "official": False},
           {"label": "Lista de códigos — PC Gamer (inglês)", "url": "https://www.pcgamer.com/games/roblox/dandys-world-codes", "official": False}
         ]}
      ]
    },
    {
      "id": "toons", "title": "Toons", "emoji": "🧸",
      "description": "Todos os personagens com foto, preço, requisitos, Maestria, skins e mini tutorial.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "text", "title": "", "body": "Toque em um Toon para ver quanto custa, os requisitos, as missões de Maestria, todas as skins e um mini tutorial animado de como conseguir."},
        {"type": "toons", "title": "Toons comuns e principais", "toons": [t for t in TOONS if t["name"] not in EV]},
        {"type": "toons", "title": "Toons de evento", "toons": [t for t in TOONS if t["name"] in EV]},
        CREDIT
      ]
    },
    {
      "id": "skins", "title": "Skins", "emoji": "👕",
      "description": "Skins Douradas de Natal, Vintage e todas as outras.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "text", "title": "", "body": "Skins mudam só a aparência do Toon, não deixam ele mais forte. Para ver TODAS as skins de um Toon específico, abra a categoria Toons e toque no personagem."},
        {"type": "gallery", "title": "🎄 Skins Douradas de Natal",
         "images": [{"image": sk["image"], "caption": sk["name"], "subtitle": t["name"] + " · missões do evento de Natal (Holiday Quests)"}
                    for t, sk in skins_where(lambda t, sk: sk["category"] == "Dourada de Natal")]},
        {"type": "notice", "title": "", "tone": "warning", "body": "As Douradas de Natal saem das Holiday Quests: missões do evento de Natal feitas jogando com o Toon. Só dá para conseguir enquanto o evento estiver ativo."},
        {"type": "gallery", "title": "📼 Skins Vintage (recompensa da Maestria)",
         "images": [{"image": t["vintageImage"], "caption": "Vintage " + t["name"], "subtitle": "Complete a Maestria"} for t in TOONS if t["vintageImage"]]},
        {"type": "gallery", "title": "✨ Outras skins Douradas",
         "images": [{"image": sk["image"], "caption": sk["name"], "subtitle": t["name"] + " · " + sk["howTo"]}
                    for t, sk in skins_where(lambda t, sk: sk["category"].startswith("Dourada") and sk["category"] != "Dourada de Natal")]},
        CREDIT
      ]
    },
    {
      "id": "como-conseguir-skins", "title": "Como Conseguir Skins", "emoji": "🧩",
      "description": "Todas as formas de liberar skins.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "items", "title": "Formas de conseguir",
         "items": [
           {"name": "Maestria → Vintage", "tags": ["Grátis"], "description": "Complete todas as missões de Maestria de um Toon e ganhe a skin Vintage dele. Existe para todo Toon.", "fields": {}},
           {"name": "Dyle's Store", "tags": ["600 Ichor"], "description": "No lobby, abra a Dyle's Store, aba Skins, e compre por 600 Ichor.", "fields": {}},
           {"name": "Lojas de evento", "tags": ["Tempo limitado"], "description": "Natal (loja da Bobette, Ornaments), Páscoa (loja da Bassie, Baskets) e Halloween (loja do Gourdy, Pumpkins). Só durante cada evento. Skins com rastro animado custam 1.200 Ichor + 1.200 da moeda do evento.", "fields": {}},
           {"name": "Skins Douradas", "tags": ["Missões"], "description": "Completando missões de evento (como as Holiday Quests de Natal) ou do Toon da Semana com o Toon certo.", "fields": {}},
           {"name": "Pacotes cosméticos (Robux)", "tags": ["Pago"], "description": "Gamepasses com skins temáticas, 40 Robux por Toon do pacote.", "fields": {}},
           {"name": "Calendário do Advento", "tags": ["Natal"], "description": "Algumas skins vêm como prêmio do Calendário do Advento no evento de Natal.", "fields": {}},
           {"name": "Códigos", "tags": ["Raro"], "description": "A skin Research-mobile do Rodger vem de código, que segundo a wiki volta todo 1º de abril.", "fields": {}}
         ]},
        CREDIT
      ]
    },
    {
      "id": "maestria", "title": "Maestria", "emoji": "🏅",
      "description": "Missões de cada Toon e a skin Vintage de recompensa.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "text", "title": "O que é Maestria?",
         "body": "Cada Toon tem 6 missões de Maestria (Mastery), como terminar máquinas ou usar a habilidade várias vezes. Completando todas, você ganha a skin Vintage daquele Toon. Alguns Toons só podem ser comprados depois de completar a Maestria de outro (ex.: Looey pede a Maestria completa da Poppy)."},
        {"type": "items", "title": "Maestria de cada Toon (toque para ver as missões)",
         "items": [{"name": t["name"], "image": t["vintageImage"], "tags": ["Vintage " + t["name"]],
                    "description": "",
                    "fields": {"Missões": "\n".join(f"{i+1}. {q}" for i, q in enumerate(t["mastery"])), "Recompensa": "Skin Vintage " + t["name"]}}
                   for t in TOONS]},
        CREDIT
      ]
    },
    {
      "id": "vintage", "title": "Vintage", "emoji": "📼",
      "description": "O que significa e todas as skins Vintage.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "text", "title": "O que significa Vintage?",
         "body": "Vintage é a skin que você ganha ao completar a Maestria de um Toon. Ela mostra o personagem em preto e branco, como um desenho antigo, com pupilas em formato de \"fatia de torta\". Ter uma Vintage mostra que você dominou aquele Toon."},
        {"type": "gallery", "title": "Todas as skins Vintage",
         "images": [{"image": t["vintageImage"], "caption": "Vintage " + t["name"], "subtitle": ""} for t in TOONS if t["vintageImage"]]},
        CREDIT
      ]
    },
    {
      "id": "termos", "title": "Termos do Jogo", "emoji": "📖",
      "description": "Dicionário para iniciantes.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "items", "title": "",
         "items": [
           {"name": "Toon", "tags": [], "description": "Os personagens que você controla. Cada Toon tem atributos e uma habilidade própria.", "fields": {}},
           {"name": "Twisted", "tags": [], "description": "As versões monstruosas dos Toons que perseguem os jogadores durante a partida.", "fields": {}},
           {"name": "Ichor", "tags": ["Moeda"], "description": "A moeda do jogo. Serve para comprar Toons e Trinkets na Dandy's Store.", "fields": {}},
           {"name": "Dandy's Store", "tags": [], "description": "A loja do Dandy, onde você compra Toons e Trinkets com Ichor.", "fields": {}},
           {"name": "Trinkets", "tags": [], "description": "Acessórios que dão vantagens durante a partida.", "fields": {}},
           {"name": "Máquinas", "tags": [], "description": "Objetivos que os jogadores precisam completar para avançar.", "fields": {}},
           {"name": "Research (Pesquisa)", "tags": [], "description": "Progresso de pesquisa sobre os Twisteds, obtido pegando Research Capsules. Alguns Toons exigem pesquisa para serem liberados.", "fields": {}},
           {"name": "Maestria (Mastery)", "tags": [], "description": "Missões de cada Toon. Completar dá a skin Vintage.", "fields": {}},
           {"name": "Gardenview Center", "tags": [], "description": "O lugar assombrado onde o jogo acontece.", "fields": {}}
         ]}
      ]
    },
    {
      "id": "guias", "title": "Guias por Nível", "emoji": "🧭",
      "description": "Do iniciante ao profissional.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "levels", "title": "Escolha seu nível",
         "levels": [
           {"level": "🟢 Fácil / Iniciante", "tips": [
             "Resgate os códigos ativos antes de jogar para ganhar Ichor extra.",
             "Foque em completar as máquinas: é o principal objetivo da partida.",
             "Aprenda o nome e a habilidade do seu Toon antes de tentar missões difíceis.",
             "Use o Ichor para comprar um Toon novo ou Trinkets na Dandy's Store."
           ]},
           {"level": "🟡 Intermediário", "tips": [
             "Comece as missões de Maestria do Toon que você mais usa para ganhar a skin Vintage.",
             "Pegue as Research Capsules: a pesquisa libera novos Toons."
           ]},
           {"level": "🟠 Avançado", "tips": [
             "Planeje a Maestria em ordem: alguns Toons só liberam depois da Maestria de outro (ex.: Looey pede a da Poppy; Blot pede a de Looey e Yatta). (Dandy's World Wiki)",
             "Acompanhe o Toon da Semana: ele tem missões próprias que dão skins Douradas. (Dandy's World Wiki / Parkourdisaster2005)"]},
           {"level": "🔴 Profissional", "tips": [
             "Criadores focados em partidas longas mostram como chegar a andares altos (Parkourdisaster2005 registra recorde pessoal no andar 154). (CreatorDB)",
             "Acompanhe o Discord oficial da BlushCrunch Studio para saber de códigos e eventos assim que saem. (games.gg)"]}
         ]}
      ]
    },
    {"id": "criadores", "title": "Criadores de Conteúdo", "emoji": "📺", "description": "Canais e comunidades para aprender.",
     "status": "available", "updatedAt": TODAY,
     "sections": [
       {"type": "items", "title": "Criadores e comunidades", "items": [
         {"name": "Parkourdisaster2005", "tags": ["YouTube", "Lives"], "description": "Canal com partidas de andares altos e conteúdo do Toon da Semana; recorde pessoal no andar 154.", "fields": {"Fonte da indicação": "CreatorDB"}},
         {"name": "BlushCrunch Studio (oficial)", "tags": ["Discord", "Roblox"], "description": "Estúdio criador do jogo. O Discord oficial anuncia códigos e eventos primeiro.", "fields": {"Fonte da indicação": "games.gg"}},
         {"name": "Dandy's World Wiki", "tags": ["Wiki"], "description": "Wiki da comunidade com todos os Toons, skins e missões (fonte dos dados deste app).", "fields": {"Fonte da indicação": "Fandom"}}]},
       {"type": "notice", "title": "", "tone": "info", "body": "Os links abrem a busca no YouTube e no TikTok, assim nunca levam a um perfil errado ou apagado."},
       {"type": "links", "title": "Ver vídeos", "links": [
         {"label": "Parkourdisaster2005 no YouTube", "url": "https://www.youtube.com/results?search_query=Parkourdisaster2005+Dandy%27s+World", "official": False},
         {"label": "Dandy's World no TikTok", "url": "https://www.tiktok.com/search?q=Dandy%27s%20World%20dicas", "official": False},
         {"label": "Dandy's World dicas no YouTube", "url": "https://www.youtube.com/results?search_query=Dandy%27s+World+dicas", "official": False},
         {"label": "Dandy's World Wiki", "url": "https://dandys-world-robloxhorror.fandom.com/wiki/Dandy%27s_World_Wiki", "official": False}]}]},
    {
      "id": "atualizacoes", "title": "Atualizações", "emoji": "🛠️",
      "description": "Histórico de atualizações do jogo.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "items", "title": "",
         "items": [
           {"name": "20/12/2025 — Bobette and Coal Quests", "tags": ["Natal"], "description": "Trouxe as Holiday Quests, missões de Natal com skins Douradas como recompensa.", "fields": {}},
           {"name": "06/12/2024 — Connie e Looey", "tags": ["Novos Toons"], "description": "Adicionou os Toons Connie e Looey.", "fields": {}}
         ]},
        {"type": "notice", "title": "", "tone": "info", "body": "Novas atualizações serão adicionadas aqui pela atualização de conteúdo do app."}
      ]
    },
    soon("novidades", "Novidades", "📰", "Últimas notícias do jogo."),
    soon("eventos", "Eventos", "🎉", "Eventos ativos e futuros."),
    soon("semana", "Coisas da Semana", "📅", "O que está rolando nesta semana."),
    {
      "id": "info", "title": "Informações Gerais", "emoji": "ℹ️",
      "description": "Sobre o jogo e links oficiais.",
      "status": "available", "updatedAt": TODAY,
      "sections": [
        {"type": "text", "title": "Sobre o jogo",
         "body": "Dandy's World é um jogo de terror cooperativo (estilo \"mascot horror\") do Roblox, criado pela BlushCrunch Studio. Os jogadores escolhem um Toon, completam máquinas e tentam escapar dos Twisteds no Gardenview Center."},
        {"type": "links", "title": "Links oficiais",
         "links": [{"label": "Dandy's World no Roblox (busca oficial)", "url": ROBLOX_SEARCH, "official": True}]}
      ]
    }
  ]
}
dump(os.path.join(G, "dandys-world.json"), dandy)

def standard(game_id, name, emoji, color, desc, cats, extra_links):
    return {"id": game_id, "name": name, "emoji": emoji, "color": color, "description": desc,
            "categories": cats + [{"id": "info", "title": "Links Oficiais", "emoji": "🔗", "description": "Fontes oficiais do jogo.",
                                   "status": "available", "updatedAt": TODAY,
                                   "sections": [{"type": "links", "title": "", "links": extra_links}]}]}





