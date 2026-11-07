import json, re, sys
W = "/home/claude/wiki"
idx = json.load(open(f"{W}/images.json"))
IMG = idx["images"]
TOONS = ["Boxten","Poppy","Shrimpo","Toodles","Finn","Tisha","Cosmo","Looey","Rodger","Brightney","Brusha",
 "Teagan","Flutter","Scraps","Goob","Connie","Gigi","Razzle & Dazzle","Yatta","Glisten","Blot","Squirm",
 "Waxwell","Pebble","Shelly","Sprout","Vee","Astro","Gourdy","Eclipse","Soulvester","Ribecca","Bobette",
 "Coal","Ginger","Rudie","Bassie","Cocoa","Flyte","Eggson"]

def clean(s):
    s = re.sub(r"<ref[^>]*>.*?</ref>|<ref[^>]*/>", "", s, flags=re.S)
    s = re.sub(r"\{\{(?:CI|TI|AI|HT|Type|Skin)\|([^|}]+)[^}]*\}\}", r"\1", s)
    s = re.sub(r"\{\{Robux\|(\d+)\}\}", r"\1 Robux", s)
    s = re.sub(r"\{\{Small\|([^}]*)\}\}", r"\1", s)
    s = re.sub(r"\[\[(?:[^|\]]*\|)?([^\]]+)\]\]", r"\1", s)
    s = re.sub(r"\{\{[^{}]*\}\}", "", s)
    s = re.sub(r"<[^>]+>", " ", s)
    s = s.replace("'''", "").replace("''", "")
    return re.sub(r"\s+", " ", s).strip()

def field(text, name):
    m = re.search(r"^\|" + name + r"\s*=(.*?)(?=^\||^\}\})", text, flags=re.M | re.S)
    return m.group(1).strip() if m else ""

def mastery(text):
    m = re.search(r"\{\{Mastery\|(.*?)\}\}\s*$", text, flags=re.M)
    if not m: return []
    body = m.group(1)
    # split on top-level pipes
    parts, depth, cur = [], 0, ""
    i = 0
    while i < len(body):
        if body.startswith("{{", i) or body.startswith("[[", i): depth += 1; cur += body[i:i+2]; i += 2; continue
        if body.startswith("}}", i) or body.startswith("]]", i): depth -= 1; cur += body[i:i+2]; i += 2; continue
        if body[i] == "|" and depth == 0: parts.append(cur); cur = ""; i += 1; continue
        cur += body[i]; i += 1
    parts.append(cur)
    return [clean(p) for p in parts if clean(p) and "=" not in p[:12]]

out = {}
for t in TOONS:
    f = idx["pages"][t]["file"]
    text = open(f"{W}/{f}", encoding="utf-8").read()
    reqs = []
    for n in range(1, 6):
        v = field(text, f"requirement_{n}")
        if v: reqs.append(clean(v))
    skins = [clean(x) for x in re.findall(r"^\*(.+)$", field(text, "skins"), flags=re.M)]
    gal = re.findall(r"^([^|\n]+\.(?:png|gif|jpg|webp))\|(Render|Full Render|Icon)$", field(text, "image1"), flags=re.M | re.I)
    out[t] = {"reqs": reqs, "skins": skins, "mastery": mastery(text),
              "release": clean(field(text, "release_date")),
              "images": {k: v.strip() for v, k in gal}}
json.dump(out, open("/home/claude/scripts/toons_raw.json", "w"), indent=1, ensure_ascii=False)

# Skins from Toon Skins page
text = open(f"{W}/raw/Toon_Skins.wikitext", encoding="utf-8").read().split("\n")
cat = None; sub = None; first_gallery_done = False; in_gal = False; skins = []
for line in text:
    m = re.match(r"^\|-\|(.+?)=\s*$", line)
    if m: cat = m.group(1).strip(); sub = None; first_gallery_done = False; continue
    m = re.match(r"^\{\{#tag:tabber\|(.+?)=\s*$", line) or re.match(r"^([A-Z][^=|{}]+)=\s*$", line)
    if m:
        lbl = m.group(1).strip()
        if lbl == "Idle": first_gallery_done = False
        elif lbl in ("Blinking","Hurt","Ability","Blackout","Concept Art","Other","Trails","Hurt (Low HP)","Low Health"): first_gallery_done = True
        else: sub = lbl; first_gallery_done = False
        continue
    if line.startswith("<gallery"): in_gal = True; continue
    if line.startswith("</gallery"):
        in_gal = False; first_gallery_done = True; continue
    if in_gal and not first_gallery_done:
        m = re.match(r"^(.+?\.(?:png|gif|jpg|webp))\|\{\{Skin\|([^|}]+)\|toon=\[\[([^\]|]+)(?:\|[^\]]*)?\]\]\}\}\s*$", line, flags=re.I)
        if m:
            skins.append({"file": m.group(1).strip(), "skin": m.group(2).strip(), "toon": m.group(3).strip(), "cat": cat, "sub": sub})
json.dump(skins, open("/home/claude/scripts/skins_raw.json", "w"), indent=1, ensure_ascii=False)
from collections import Counter
print(Counter((s["cat"], s["sub"]) for s in skins))
miss = [s["file"] for s in skins if s["file"] not in IMG and s["file"].replace(" ","_") not in IMG]
print("skins", len(skins), "missing img", len(miss), miss[:5])
for t,v in out.items():
    if not v["mastery"] or not v["reqs"]: print("WARN", t, len(v["mastery"]), v["reqs"])
    for k,fn in v["images"].items():
        if fn not in IMG: print("noimg", t, k, fn)
