"""Coloca Evomon, Jujutsu Shenanigans e Ninja Tycoon no índice (games.json) e adiciona as notícias deles.
Rodar depois de build_roblox.py:  python3 tools/add_roblox_games.py <pasta-roblox-data>
"""
import json, os, sys

SRC = sys.argv[1] if len(sys.argv) > 1 else "/home/claude/rd"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "assets", "content")
S = json.load(open(os.path.join(SRC, "summary.json"), encoding="utf-8"))
gj = json.load(open(os.path.join(ROOT, "games.json"), encoding="utf-8"))

ROBLOX_COMPAT = "Jogo do Roblox: Android, iOS, PC, Xbox e PlayStation (precisa do app Roblox)."
NEW = [
    {"id": "evomon", "name": "Evomon", "subtitle": "Capture, treine e evolua monstrinhos", "platform": "Roblox", "emoji": "🐾", "color": "#38BDF8",
     "status": "available", "file": "games/evomon.json", "cover": S["evomon"]["cover"],
     "stats": {"characters": S["evomon"]["characters"], "skins": 0, "label": "Evomons"}, "developer": "Evomon Devs", "genres": ["Aventura", "Roblox"],
     "badges": ["Popular", "Novo no app"],
     "longDescription": "Jogo de capturar monstrinhos no Roblox, no estilo Pokémon. Você explora ilhas, captura Evomons de 17 elementos, treina, evolui, monta neles e enfrenta dungeons com amigos. Tem variantes raras Shiny e Sparkle.",
     "storeUrl": "https://www.roblox.com/games/134381727982611/Evomon", "storeLabel": "Abrir no Roblox", "compatibility": ROBLOX_COMPAT,
     "credits": ["Desenvolvido e publicado pelo grupo Evomon Devs no Roblox.", "Lançado para todos em 17/06/2026."]},
    {"id": "jujutsu-shenanigans", "name": "Jujutsu Shenanigans", "subtitle": "Luta battlegrounds de Jujutsu Kaisen", "platform": "Roblox", "emoji": "👊", "color": "#EF4444",
     "status": "available", "file": "games/jujutsu-shenanigans.json", "cover": S["jujutsu-shenanigans"]["cover"],
     "stats": {"characters": S["jujutsu-shenanigans"]["characters"], "skins": S["jujutsu-shenanigans"]["skins"], "label": "Personagens"}, "developer": "Tze",
     "genres": ["Ação", "Roblox"], "badges": ["Popular", "Novo no app"],
     "longDescription": "Jogo de luta do Roblox inspirado em Jujutsu Kaisen. Você escolhe um personagem, luta com 4 golpes e um especial e, com a barra cheia, Desperta para ganhar golpes novos. Venceu o prêmio de Melhor Experiência de Ação do Roblox (RIA 2026).",
     "storeUrl": "https://www.roblox.com/games/9391468976/Jujutsu-Shenanigans", "storeLabel": "Abrir no Roblox", "compatibility": ROBLOX_COMPAT,
     "credits": ["Criado por Tze (@AlsoNot_Tze) e publicado pelo grupo Jujutsu Shenanigans! no Roblox.", "Inspirado no anime e mangá Jujutsu Kaisen."]},
    {"id": "ninja-tycoon", "name": "Ninja Tycoon", "subtitle": "Base ninja, jutsus e transformações", "platform": "Roblox", "emoji": "🥷", "color": "#F97316",
     "status": "available", "file": "games/ninja-tycoon.json", "cover": S["ninja-tycoon"]["cover"],
     "stats": {"characters": S["ninja-tycoon"]["characters"], "skins": S["ninja-tycoon"]["skins"], "label": "Transformações"}, "developer": "Coldjason",
     "genres": ["Simulação", "Roblox"], "badges": ["Novo no app"],
     "longDescription": "Tycoon de ninjas do Roblox inspirado em Naruto. Você escolhe um clã, monta sua base com droppers que geram dinheiro, compra armas e jutsus, enfrenta chefes e usa transformações como Modo Sábio e Modo Baryon.",
     "storeUrl": "https://www.roblox.com/games/221718525/Ninja-Tycoon", "storeLabel": "Abrir no Roblox", "compatibility": ROBLOX_COMPAT,
     "credits": ["Criado por Coldjason, com AzuriteStorm na construção e modelagem.", "No Roblox desde fevereiro de 2015 (antes chamado Naruto Tycoon)."]},
]
ids = {g["id"] for g in NEW}
gj["games"] = [g for g in gj["games"] if g["id"] not in ids] + NEW


def N(game, date, title, body, source, url=""):
    return {"game": game, "date": date, "title": title, "body": body, "source": source, "url": url}


WEEK_ADD = [
    N("evomon", "2026-11-06", "Evomon atualizado: Temporada 4 \"StarUp\"", "O jogo recebeu atualização em 06/11 e está na Temporada 4 (StarUp), com tema de Halloween. O código da temporada é EVOS4LIVE.", "Página do jogo no Roblox · evomon.wiki", "https://www.roblox.com/games/134381727982611/Evomon"),
    N("jujutsu-shenanigans", "2026-11-05", "Jujutsu Shenanigans atualizado com tema de Halloween", "O jogo foi atualizado em 05/11 e está com o evento de Halloween 2026. A última versão com notas na wiki é a V1.83.2.", "Página do jogo no Roblox · Jujutsu Shenanigans Wiki", "https://www.roblox.com/games/9391468976/Jujutsu-Shenanigans"),
    N("ninja-tycoon", "2026-11-05", "Ninja Tycoon chega à versão 4.8", "O Ninja Tycoon foi atualizado para a versão 4.8 em 05/11. As notas completas saem no Discord do criador, Coldjason.", "Página do jogo no Roblox", "https://www.roblox.com/games/221718525/Ninja-Tycoon"),
    N("evomon", "2026-11-03", "Códigos do Evomon continuam valendo", "Nenhum código do Evomon expirou até agora. O próximo sai quando o jogo bater 400 mil likes.", "GamesRadar · página do jogo no Roblox", "https://www.gamesradar.com/games/rpg/evomon-codes/"),
]
MONTH_ADD = [
    N("jujutsu-shenanigans", "2026-10-26", "Novo código RIA2026", "O código RIA2026 dá um emote e comemora o prêmio de Melhor Experiência de Ação do Roblox (RIA 2026).", "Dexerto", "https://www.dexerto.com/roblox/jujutsu-shenanigans-codes-december-2026-3293390"),
    N("evomon", "2026-10-29", "Começa a Temporada 4 do Evomon", "A Temporada 4 começou no fim de outubro, com o código EVOS4LIVE (poções de Trait, Nature e Talent Vector).", "evomon.wiki · GamesRadar", "https://www.evomon.wiki/codes"),
]
for key, add in (("weeklyNews", WEEK_ADD), ("monthlyNews", MONTH_ADD)):
    items = [n for n in gj[key]["items"] if n["game"] not in ids]
    gj[key]["items"] = sorted(items + add, key=lambda n: n["date"], reverse=True)
gj["contentVersion"] = max(gj.get("contentVersion", 0), 8)
gj["news"] = [n for n in gj["news"] if "Evomon" not in n["title"]]
gj["news"].insert(0, {"date": "2026-11-07", "title": "Novos jogos: Evomon, Jujutsu Shenanigans e Ninja Tycoon",
                      "body": "Três jogos do Roblox com códigos, personagens com foto, golpes, guias por nível, termos e novidades. Chegam pelo botão Atualizar conteúdo."})
json.dump(gj, open(os.path.join(ROOT, "games.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=2)
print("jogos:", [g["id"] for g in gj["games"]], "versão", gj["contentVersion"])
