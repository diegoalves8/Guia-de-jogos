"""Preenche as abas Novidades, Eventos e Coisas da Semana de cada jogo com as notícias
da semana e do mês que já estão em games.json (todas com fonte).

Só mexe em abas vazias ou geradas por este script (marcadas com "auto": true).
Funciona para qualquer jogo novo que tenha categorias com esses ids.
Rodar depois de extra_content.py:  python3 tools/fill_game_news.py
"""
import json, os, re

ROOT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "content")
EVENT = re.compile(r"evento|event|finais|ffws|temporada|halloween|natal", re.I)


def br(d):
    return "/".join(reversed(d.split("-")))


def item(n):
    fields = {"Fonte": n["source"]}
    if n.get("url"):
        fields["Link"] = n["url"]
    return {"name": n["title"], "tags": [br(n["date"])], "description": n["body"], "fields": fields}


def links(news):
    ls = [{"label": f"{n['title']} ({n['source']})", "url": n["url"], "official": False} for n in news if n.get("url")]
    return [{"type": "links", "title": "Fontes", "links": ls}] if ls else []


gj = json.load(open(os.path.join(ROOT, "games.json"), encoding="utf-8"))
week = gj.get("weeklyNews", {})
month = gj.get("monthlyNews", {})
for g in gj["games"]:
    path = os.path.join(ROOT, g["file"])
    d = json.load(open(path, encoding="utf-8"))
    w = sorted([n for n in week.get("items", []) if n["game"] == g["id"]], key=lambda n: n["date"], reverse=True)
    m = sorted([n for n in month.get("items", []) if n["game"] == g["id"]], key=lambda n: n["date"], reverse=True)
    seen, allnews = set(), []
    for n in w + m:
        if n["title"] not in seen:
            seen.add(n["title"])
            allnews.append(n)
    events = [n for n in allnews if EVENT.search(n["title"] + " " + n["body"])]
    plan = {
        "novidades": [{"type": "notice", "title": "", "tone": "info", "body": f"Notícias de {g['name']} reunidas da semana ({week.get('period', '')}) e do mês ({month.get('period', '')})."},
                      {"type": "items", "title": "📰 Últimas notícias", "items": [item(n) for n in allnews]}] + links(allnews) if allnews else [],
        "eventos": [{"type": "items", "title": "🎉 Eventos e datas importantes", "items": [item(n) for n in events]},
                    {"type": "notice", "title": "", "tone": "warning", "body": "Datas de eventos podem mudar. Confira sempre no próprio jogo."}] + links(events) if events else [],
        "semana": [{"type": "items", "title": f"🗓️ {week.get('period', 'Esta semana')}", "items": [item(n) for n in w]}] + links(w) if w else [],
    }
    # Dandy's World: a semana também mostra o Toon da Semana (vem da aba Douradas)
    gold = next((c for c in d["categories"] if c["id"] == "douradas"), None)
    if gold and plan["semana"]:
        tow = next((s for s in gold["sections"] if s["type"] == "items" and "Toon da Semana" in s.get("title", "")), None)
        if tow:
            plan["semana"].insert(0, tow)
    changed = 0
    for c in d["categories"]:
        if c["id"] in plan and plan[c["id"]] and (not c["sections"] or c.get("auto")):
            c["sections"] = plan[c["id"]]
            c["status"] = "available"
            c["auto"] = True
            c["updatedAt"] = gj.get("updatedAt", "")
            c["description"] = {"novidades": "Últimas notícias do jogo, com fonte.", "eventos": "Eventos ativos e próximas datas.",
                                "semana": "O que está acontecendo nesta semana."}[c["id"]]
            changed += 1
    if changed:
        json.dump(d, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(g["id"], "abas preenchidas:", changed)
