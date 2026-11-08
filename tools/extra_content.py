"""Conteúdo adicional: criadores com links diretos, notícias da semana/mês, créditos, douradas + Toon da Semana."""
import json, os, sys
ROOT = sys.argv[1]
G = os.path.join(ROOT, "games")
TODAY = "2026-11-07"
def load(n): return json.load(open(os.path.join(G, n + ".json"), encoding="utf-8"))
def save(n, d): json.dump(d, open(os.path.join(G, n + ".json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
YT = lambda h: f"https://www.youtube.com/@{h}"
TT = lambda h: f"https://www.tiktok.com/@{h}"
def C(name, country, about, youtube="", tiktok="", source=""):
    return {"name": name, "country": country, "about": about, "youtube": youtube, "tiktok": tiktok, "source": source}
BR, US, UK = "🇧🇷 Brasil", "🇺🇸 EUA", "🇬🇧 Reino Unido"

CREATORS = {
 "dandys-world": [
   C("LimaMosca", BR, "YouTuber com vídeos todos os dias. Fez a live sobre a participação do Dandy's World no evento The Hollow do Roblox (24/10/2026).", YT("LimaMosca"), TT("LimaMosca"), "Dandy's World Wiki · perfil do TikTok"),
   C("MoonyCaroll", BR, "Criadora brasileira de Dandy's World que faz vídeos de gameplays variadas.", YT("moonycaroll"), TT("moonycaroll"), "Perfil do TikTok (21,5 mil seguidores)"),
   C("Parkourdisaster2005", US, "Lives quase todo dia de Dandy's World, partidas de andares altos (recorde no andar 154) e Toon da Semana.", YT("Parkourdisaster2005"), TT("Parkourdisaster2005"), "CreatorDB · perfil do TikTok"),
 ],
 "brawl-stars": [
   C("VTzim", BR, "Jogador profissional brasileiro (ex-QLASH, Finais Mundiais 2021 e 2022) e Super Criador oficial da Supercell desde 2020.", YT("VTzim"), "", "Blog oficial da Supercell · Esports Earnings"),
   C("Vinni Portento", BR, "Criador de Brawl Stars de São Paulo, parceiro oficial da Supercell.", YT("VinniPortento"), TT("VinniPortento"), "Blog da Supercell · perfil do TikTok"),
   C("Bruno Clash", BR, "Narrador de esports e criador com mais de 1 milhão de inscritos no YouTube.", YT("BrunoClash"), TT("BrunoClash"), "Blog da Supercell · perfil do TikTok"),
   C("KairosTime Gaming", US, "Tier lists e análise do meta a cada atualização.", YT("KairosTime"), TT("KairosTime"), "Perfil do TikTok"),
   C("SpenLC", UK, "Primeiro campeão mundial de Brawl Stars (2019); conteúdo competitivo.", YT("SpenLC"), TT("SpenLC"), "Perfil do TikTok"),
 ],
 "free-fire": [
   C("Nobru", BR, "Bruno Goes, um dos maiores nomes do Free Fire no Brasil e cofundador da Fluxo.", "https://www.youtube.com/results?search_query=Nobru+Free+Fire", TT("Nobru"), "Meio & Mensagem · perfil do TikTok (12 mi)"),
   C("Cerol", BR, "Eleito melhor streamer no Prêmio eSports Brasil 2019; cofundador da Fluxo.", YT("Cerol"), TT("Cerol"), "TheTVDB · perfil do TikTok (4,4 mi)"),
   C("El Gato", BR, "Rodrigo El Gato, dono de uma das maiores organizações de esports do Brasil.", YT("ElGato"), TT("ElGato"), "Fragster · perfil do TikTok (3,6 mi)"),
 ],
 "murder-mystery": [
   C("FocusRBX", US, "Canal focado em Murder Mystery 2, com vídeos e shorts.", YT("FocusRBX"), "", "The Famous People"),
 ],
 "blox-fruits": [
   C("Cafazera", BR, "Criador brasileiro focado em Blox Fruits: combos, espadas e bounty.", YT("Cafazera"), TT("Cafazera"), "CreatorDB · perfil do TikTok (319 mil)"),
   C("Gamer Robot (oficial)", US, "Canal oficial do Blox Fruits, da Gamer Robot Inc.: novidades, prévias e códigos.", YT("GamerRobot"), "", "vidIQ (descrição do canal)"),
 ],
}
NOTE = {"type": "notice", "title": "", "tone": "info",
        "body": "Os links abrem direto o canal no YouTube e o perfil no TikTok. Só colocamos link direto quando confirmamos o perfil; nos outros casos o link abre a busca."}

CREDITS = {
 "dandys-world": ["Desenvolvido e publicado pela BlushCrunch Studio no Roblox.", "Idealizado por Qwelver (Roblox Creator Spotlight)."],
 "brawl-stars": ["Desenvolvido e publicado pela Supercell, estúdio da Finlândia."],
 "free-fire": ["Desenvolvido pela 111dots Studio e publicado pela Garena."],
 "murder-mystery": ["Criado por Nikilis, desenvolvedor independente do Roblox. É o único jogo publicado no perfil dele."],
 "blox-fruits": ["Desenvolvido pela Gamer Robot Inc.", "Criado pelos irmãos mygame43 e rip_indra."],
}

for gid, lst in CREATORS.items():
    d = load(gid)
    cat = next((c for c in d["categories"] if c["id"] == "criadores"), None)
    sec = {"type": "creators", "title": "Criadores para acompanhar", "creators": lst}
    if cat is None:
        cat = {"id": "criadores", "title": "Criadores de Conteúdo", "emoji": "📺", "description": "YouTubers e tiktokers.", "status": "available", "updatedAt": TODAY, "sections": []}
        d["categories"].append(cat)
    # mantém as dicas/notas antigas que não são lista de criadores; troca a lista pela nova
    keep = [s for s in cat["sections"] if s.get("type") not in ("items", "links", "creators", "notice")]
    cat["sections"] = [sec, NOTE] + keep
    cat["description"] = "Brasileiros e internacionais, com link direto para o canal."
    cat["updatedAt"] = TODAY
    d["credits"] = CREDITS[gid]
    save(gid, d)

# ---------- Dandy's World: códigos novos + aba de Douradas e Toon da Semana ----------
toons = json.load(open("/home/claude/scripts/toons_final.json"))
d = load("dandys-world")
for c in d["categories"]:
    if c["id"] == "codigos":
        for s in c["sections"]:
            if s.get("type") == "codes" and s["title"] == "Códigos ativos":
                s["codes"] = [
                    {"code": "AD2026", "reward": "Profile Print \"American Dream 2026\"", "status": "active", "note": "Código mais recente, confirmado como ativo pelo GamesRadar em 03/11/2026."},
                    {"code": "ICHOR", "reward": "50 Ichor", "status": "active", "note": "Confirmado como ativo em novembro de 2026 por GamesRadar e UrGameTips."},
                    {"code": "RESEARCHMOBILE", "reward": "Skin Research-mobile do Rodger", "status": "unverified", "note": "Segundo a Dandy's World Wiki, o código volta todo 1º de abril. Fora dessa data pode não funcionar."}]
            if s.get("type") == "codes" and s["title"].startswith("Códigos expirados"):
                names = {x["code"] for x in s["codes"]}
                for code, rw in [("QoM", "Sticker Queens of Mischief (expirou em 06/11/2026)"), ("HAPPYHALLOWEEN", "600 Pumpkins"), ("SPOOKYSEASON", "")]:
                    if code not in names: s["codes"].insert(0, {"code": code, "reward": rw, "status": "expired", "note": ""})
        c["updatedAt"] = TODAY
gold = [(t, s) for t in toons for s in t["skins"] if s["category"].startswith("Dourada")]
astro = next(t for t in toons if t["name"] == "Astro")
dourada_cat = {
  "id": "douradas", "title": "Douradas e Toon da Semana", "emoji": "✨",
  "description": "Todas as skins Douradas e o Toon da Semana atual.", "status": "available", "updatedAt": TODAY,
  "sections": [
    {"type": "items", "title": "⭐ Toon da Semana", "items": [
      {"name": "Astro", "image": astro["image"], "tags": ["06/11 a 13/11/2026"],
       "description": "Esta é a semana do Astro (atualização \"Astro's Week\", versão Alpha 0.30.1, lançada em 06/11/2026).",
       "fields": {"O que é o Toon da Semana": "A cada semana um Toon fica em destaque, com missões próprias. Algumas semanas dão skins Douradas como recompensa.",
                  "Próxima troca": "Sexta-feira, 13/11/2026, às 16h (horário de Brasília)."}}]},
    {"type": "notice", "title": "", "tone": "info", "body": "O Toon da Semana muda toda sexta-feira. Esta aba é atualizada toda semana junto com as notícias."},
    {"type": "gallery", "title": "🏆 Douradas do Toon da Semana",
     "images": [{"image": s["image"], "caption": s["name"], "subtitle": f"{t['name']} · missões do Toon da Semana"} for t, s in gold if s["category"] == "Dourada"]},
    {"type": "gallery", "title": "🎄 Douradas de Natal",
     "images": [{"image": s["image"], "caption": s["name"], "subtitle": f"{t['name']} · Holiday Quests (evento de Natal)"} for t, s in gold if s["category"] == "Dourada de Natal"]},
    {"type": "gallery", "title": "🎃 Douradas de outros eventos",
     "images": [{"image": s["image"], "caption": s["name"], "subtitle": f"{t['name']} · {s['howTo']}"} for t, s in gold if s["category"] not in ("Dourada", "Dourada de Natal")]},
    {"type": "steps", "title": "Como conseguir uma Dourada", "items": [
      "Confira qual é o Toon da Semana (acima) ou se há evento ativo.",
      "Jogue com aquele Toon e abra as missões da semana ou do evento.",
      "Complete todas as missões antes do prazo acabar.",
      "A skin Dourada é liberada na hora, de graça."]},
  ]}
d["categories"] = [c for c in d["categories"] if c["id"] != "douradas"]
idx = next(i for i, c in enumerate(d["categories"]) if c["id"] == "skins")
d["categories"].insert(idx + 1, dourada_cat)
save("dandys-world", d)

# ---------- Blox Fruits: códigos reportados ----------
d = load("blox-fruits")
for c in d["categories"]:
    if c["id"] == "codigos":
        c["sections"] = [
          {"type": "text", "title": "", "body": "Os códigos do Blox Fruits saem no X (Twitter) oficial e no canal Gamer Robot. Para resgatar: no jogo, toque no ícone de presente/Twitter no menu da esquerda, digite o código e confirme."},
          {"type": "codes", "title": "Códigos reportados como ativos", "codes": [
             {"code": "KITT_RESET", "reward": "Reset de atributos grátis", "status": "unverified", "note": "Listado como ativo pelo Beebom em 05/11/2026."},
             {"code": "SUB2GAMERROBOT_EXP1", "reward": "30 minutos de 2x EXP", "status": "unverified", "note": "Listado como ativo pelo Beebom em 05/11/2026."},
             {"code": "BIGNEWS", "reward": "Título \"Big News\"", "status": "unverified", "note": "Listado como ativo por Beebom e G2A em novembro de 2026."}]},
          {"type": "notice", "title": "", "tone": "warning", "body": "Códigos do Blox Fruits expiram sem aviso. Se não funcionar, ele provavelmente já expirou."}]
        c["updatedAt"] = TODAY
save("blox-fruits", d)

# ---------- Notícias da semana e do mês ----------
def N(game, date, title, body, source, url=""):
    return {"game": game, "date": date, "title": title, "body": body, "source": source, "url": url}
WEEK = {"period": "Semana de 01/11 a 07/11/2026", "items": [
  N("dandys-world", "2026-11-06", "Fim do evento de Halloween e começa a semana do Astro", "O evento de Halloween (02/10 a 06/11) acabou e a atualização \"Astro's Week\" (Alpha 0.30.1) removeu o conteúdo do evento. Astro é o Toon da Semana até 13/11.", "Dandy's World Wiki (Changelog)"),
  N("dandys-world", "2026-11-03", "Novo código AD2026", "O código AD2026 dá o Profile Print \"American Dream 2026\". O código QoM expirou em 06/11.", "GamesRadar", "https://www.gamesradar.com/games/survival/dandys-world-codes/"),
  N("dandys-world", "2026-11-03", "Fim da missão The Hollow", "A missão do Dandy's World no evento The Hollow, do Roblox, deixou de ser jogável em 03/11.", "Dandy's World Wiki"),
  N("brawl-stars", "2026-11-03", "Atualização de novembro: Ranqueado renovado", "Agora o Ranqueado exige Brawlers no nível de poder 11 em todos os ranks e 12 Brawlers no Diamante. Também chegaram filtros de Brawlers, sugestão de banimento e chat rápido no draft.", "Supercell (notas oficiais)", "https://supercell.com/en/games/brawlstars/blog/game-updates/release-notes-november-2026/"),
  N("brawl-stars", "2026-11-03", "Novos Brawlers e Hipercargas", "Chegam os Sapos Voltaicos (Lendário, Artilharia) e Rey (Mítico, Atirador), além de novas Hipercargas para Cosmo e Vince e o modo Mega Showdown, com 20 jogadores.", "GearUP Booster · BrawlZone"),
  N("free-fire", "2026-11-06", "Começam as Finais Globais do FFWS", "As Finais Globais do Free Fire World Series começam em 06/11, em Bangcoc (Tailândia), com 24 equipes ao longo de quatro fins de semana.", "Garena (site oficial)", "https://ff.garena.com/pt/article/1607/"),
  N("free-fire", "2026-11-07", "Codiguins de novembro confirmados", "A Garena confirmou 4 itens de codiguin para novembro: Conjunto Pulso Cósmico e Conjunto Aura Cósmica (07 a 19/11), AWM Núcleo Cósmico (14/11 a 10/12) e Invoque a Bandeira! (09/11 a 11/12). Resgate só no site oficial.", "Free Fire Mania (programa FFCP)"),
  N("free-fire", "2026-11-01", "Loja Prime e Passe de novembro", "A Loja Prime de novembro abriu com o Punho Ardente (2.899 diamantes). O Passe de novembro tem o tema Escorpiões Estelares.", "Free Fire Mania"),
  N("blox-fruits", "2026-10-31", "Evento de Halloween com a mutação Hellfire", "O evento de Halloween (Update 30 \"Magnet\") começou em 31/10 e vai até 15/11/2026, às 22h de 14/11 no horário de Brasília. Traz a mutação Hellfire do Gas, caça às abóboras e o Spooky Gacha.", "Timesaver · U7BUY"),
  N("murder-mystery", "2026-11-03", "Halloween retrô continua até 16/11", "O evento de Halloween segue até 16/11. Destaques: a Godly Embersickle (0,2% na caixa), o Ancient Emberbow (nível 30 do passe), a criação do Emberscythe no Caldeirão e as primeiras Chroma Ancients do jogo. Não há código ativo.", "Murder Mystery 2 Wiki · G2A"),
]}
MONTH = {"period": "Novembro de 2026", "items": [
  N("dandys-world", "2026-11-13", "Próxima atualização e troca do Toon da Semana", "A wiki aponta um novo patch (versão Alpha 0.30) e a troca do Toon da Semana para sexta, 13/11. O próximo grande evento esperado é o de Natal.", "Dandy's World Wiki"),
  N("blox-fruits", "2026-11-15", "Fim do evento de Halloween", "O evento termina em 15/11 às 01:00 UTC (22h de 14/11 em Brasília).", "Lista oficial de eventos do Roblox (via Timesaver)"),
  N("murder-mystery", "2026-11-16", "Fim do Halloween 2026", "O evento acaba em 16/11 às 18:00 UTC (15h em Brasília). Depois disso as armas do evento só por troca.", "Lista oficial de eventos do Roblox (via allthings.how)"),
  N("brawl-stars", "2026-11-19", "Nova temporada do Ranqueado", "A próxima temporada do Ranqueado começa em 19/11, já com as novas regras.", "BrawlZone"),
  N("brawl-stars", "2026-11-20", "Finais Mundiais no Japão", "As Finais Mundiais de Brawl Stars acontecem de 20 a 22/11, no Japão, com recompensas para quem assistir pelo site oficial.", "Brawl Stars Dicas"),
  N("free-fire", "2026-11-30", "FFWS durante todo o mês", "As Finais Globais do FFWS seguem por quatro fins de semana em novembro. A Loja e a Loja Prime de novembro vão até 30/11.", "Garena · Free Fire Mania"),
  N("dandys-world", "2026-11-06", "Resumo: Halloween 2026", "Voltaram os Toons Gourdy, Soulvester, Eclipse e Ribecca, com loja de Pumpkins, missões de skins Douradas, a skin Toxic Experiment do Waxwell e a música oficial \"Queens of Mischief\".", "X (Twitter) · Gaming Route"),
]}
gj = json.load(open(os.path.join(ROOT, "games.json"), encoding="utf-8"))
gj["weeklyNews"] = WEEK
gj["monthlyNews"] = MONTH
gj["contentVersion"] = 6
gj["updatedAt"] = TODAY
gj["appCredits"] = {"author": "Gustavo Bezerra", "text": "Aplicativo criado por Gustavo Bezerra."}
for g in gj["games"]:
    g["credits"] = CREDITS[g["id"]]
json.dump(gj, open(os.path.join(ROOT, "games.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=2)
print("ok", len(WEEK["items"]), len(MONTH["items"]), "douradas", len(gold))

# Abas Novidades/Eventos/Semana de cada jogo a partir das notícias acima
import runpy; runpy.run_path(os.path.join(os.path.dirname(__file__), "fill_game_news.py"))
