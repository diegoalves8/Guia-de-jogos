/* Guia de Jogos — versão web (PWA). Criado por Gustavo Bezerra.
 * Lê o mesmo conteúdo JSON do app Android (pasta content/) e reproduz as mesmas telas.
 * Rotas (hash): #/home #/games #/favorites #/news #/settings #/search
 *               #/game/<id> #/game/<id>/c/<categoria> #/game/<id>/c/<categoria>/t/<personagem>
 */
"use strict";

// ---------------------------------------------------------------- utilidades
const $app = document.getElementById("app");
const $tabbar = document.getElementById("tabbar");
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const nl2br = (s) => esc(s).replace(/\n/g, "<br>");
const fmtDate = (d) => (d || "").split("-").reverse().join("/");
const C = { primary: "#3D8BFF", secondary: "#8B7CF6", success: "#5CC593", accent: "#E8B75C", danger: "#E5737A", text2: "#8792A6" };
const CHART = ["#3D8BFF", "#8B7CF6", "#3CC6C2", "#5CC593", "#E8B75C", "#6FA8FF"];
const pill = (t, c = C.text2) => `<span class="pill" style="--c:${c}">${esc(t)}</span>`;
const isWide = () => innerWidth >= 760;

const ICON = {
  back: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>',
  search: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>',
  settings: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94a7.07 7.07 0 0 0 0-1.88l2.03-1.58a.5.5 0 0 0 .12-.64l-1.92-3.32a.5.5 0 0 0-.61-.22l-2.39.96a7.03 7.03 0 0 0-1.62-.94l-.36-2.54a.5.5 0 0 0-.5-.42h-3.84a.5.5 0 0 0-.5.42l-.36 2.54c-.59.24-1.13.56-1.62.94l-2.39-.96a.5.5 0 0 0-.61.22L2.74 8.84a.5.5 0 0 0 .12.64l2.03 1.58a7.07 7.07 0 0 0 0 1.88l-2.03 1.58a.5.5 0 0 0-.12.64l1.92 3.32c.13.22.39.3.61.22l2.39-.96c.49.38 1.03.7 1.62.94l.36 2.54c.05.24.25.42.5.42h3.84c.25 0 .45-.18.5-.42l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.48 0 .61-.22l1.92-3.32a.5.5 0 0 0-.12-.64l-2.03-1.58zM12 15.6A3.6 3.6 0 1 1 12 8.4a3.6 3.6 0 0 1 0 7.2z"/></svg>',
  home: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>',
  games: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M21.58 16.09l-1.09-7.66A3.996 3.996 0 0 0 16.53 5H7.47C5.48 5 3.79 6.46 3.51 8.43l-1.09 7.66A2.545 2.545 0 0 0 4.94 19c.68 0 1.32-.27 1.8-.75L9 16h6l2.25 2.25c.48.48 1.13.75 1.8.75 1.56 0 2.75-1.38 2.53-2.91zM11 11H9v2H8v-2H6v-1h2V8h1v2h2v1zm4-1c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm2 3c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1z"/></svg>',
  heart: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>',
  heartO: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>',
  news: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M23 12l-2.44-2.78.34-3.68-3.61-.82-1.89-3.18L12 3 8.6 1.54 6.71 4.72l-3.61.81.34 3.68L1 12l2.44 2.78-.34 3.69 3.61.82 1.89 3.18L12 21l3.4 1.46 1.89-3.18 3.61-.82-.34-3.68L23 12zm-10 5h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>',
  open: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 19H5V5h7V3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z"/></svg>',
  copy: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M16 1H4a2 2 0 0 0-2 2v14h2V3h12V1zm3 4H8a2 2 0 0 0-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7a2 2 0 0 0-2-2zm0 16H8V7h11v14z"/></svg>',
  chev: '<svg class="chev" viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z"/></svg>',
  right: '<svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>',
  play: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>',
  pause: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>',
  prev: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 6h2v12H6zm3.5 6l8.5 6V6z"/></svg>',
  next: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/></svg>',
  replay: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z"/></svg>',
  video: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z"/></svg>',
  note: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 3v10.55A4 4 0 1 0 14 17V7h4V3h-6z"/></svg>',
  book: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M21 5c-1.11-.35-2.33-.5-3.5-.5-1.95 0-4.05.4-5.5 1.5-1.45-1.1-3.55-1.5-5.5-1.5S2.45 4.9 1 6v14.65c0 .25.25.5.5.5.1 0 .15-.05.25-.05C3.1 20.45 5.05 20 6.5 20c1.95 0 4.05.4 5.5 1.5 1.35-.85 3.8-1.5 5.5-1.5 1.65 0 3.35.3 4.75 1.05.1.05.15.05.25.05.25 0 .5-.25.5-.5V6c-.6-.45-1.25-.75-2-1zm0 13.5c-1.1-.35-2.3-.5-3.5-.5-1.7 0-4.15.65-5.5 1.5V8c1.35-.85 3.8-1.5 5.5-1.5 1.2 0 2.4.15 3.5.5v11.5z"/></svg>',
  download: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>',
  refresh: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M17.65 6.35A7.958 7.958 0 0 0 12 4a8 8 0 1 0 7.73 10h-2.08A5.99 5.99 0 0 1 12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>',
};

function toast(msg) {
  const t = document.getElementById("toast");
  t.textContent = msg; t.classList.add("show");
  clearTimeout(toast._t); toast._t = setTimeout(() => t.classList.remove("show"), 2200);
}
function copy(text) {
  (navigator.clipboard?.writeText(text) || Promise.reject()).then(() => toast(`Código ${text} copiado!`)).catch(() => {
    const ta = document.createElement("textarea"); ta.value = text; document.body.appendChild(ta); ta.select();
    try { document.execCommand("copy"); toast(`Código ${text} copiado!`); } catch (_) {} ta.remove();
  });
}
// Imagens com carregamento elegante (aparecem com fade) e sem referer (as wikis permitem assim).
const img = (src, alt = "", cls = "") => src ? `<img class="${cls}" src="${esc(src)}" alt="${esc(alt)}" loading="lazy" decoding="async" referrerpolicy="no-referrer" onload="this.classList.add('ok')" onerror="this.style.visibility='hidden'">` : "";

// ---------------------------------------------------------------- dados
const DB = { index: null, games: {} };
async function loadIndex(force = false) {
  if (DB.index && !force) return DB.index;
  const r = await fetch("content/games.json", { cache: force ? "reload" : "no-cache" });
  DB.index = await r.json();
  return DB.index;
}
async function loadGame(id, force = false) {
  if (DB.games[id] && !force) return DB.games[id];
  const idx = await loadIndex();
  const g = idx.games.find((x) => x.id === id);
  if (!g) throw new Error("Jogo não encontrado");
  const r = await fetch("content/" + g.file, { cache: force ? "reload" : "no-cache" });
  DB.games[id] = await r.json();
  return DB.games[id];
}
const allGames = async () => { const idx = await loadIndex(); return Promise.all(idx.games.map((g) => loadGame(g.id).then((d) => [g, d]))); };

// ---------------------------------------------------------------- favoritos (no navegador)
const Fav = {
  read() { try { return JSON.parse(localStorage.getItem("gj-fav") || "{}"); } catch (_) { return {}; } },
  has(k) { return !!this.read()[k]; },
  toggle(f) { const all = this.read(); if (all[f.key]) delete all[f.key]; else all[f.key] = f; try { localStorage.setItem("gj-fav", JSON.stringify(all)); } catch (_) {} return !!all[f.key]; },
  list() { return Object.values(this.read()).sort((a, b) => a.title.localeCompare(b.title)); },
};

// ---------------------------------------------------------------- filtros da Home
const FILTERS = ["Todos os jogos", "Mais populares", "Terror", "Aventura", "Simulação", "Estratégia", "Roblox", "Novidades"];
function applyFilter(games, f) {
  if (f === "Todos os jogos") return games;
  if (f === "Mais populares") return games.filter((g) => (g.badges || []).includes("Popular"));
  if (f === "Novidades") return games.filter((g) => (g.badges || []).some((b) => b.startsWith("Novo")));
  return games.filter((g) => (g.genres || []).includes(f));
}

// ---------------------------------------------------------------- componentes
const topbar = (title, back = true, actions = "") => `
  <header class="topbar"><div class="inner">
    ${back ? `<button class="icon-btn" onclick="goBack()" aria-label="Voltar">${ICON.back}</button>` : `<span style="width:56px"></span>`}
    <h1>${esc(title)}</h1>${actions || `<span style="width:56px"></span>`}
  </div></header>`;
const gameIcon = (g, size = 64) => `<div class="game-icon gi" style="width:${size}px;height:${size}px">${g.cover ? img(g.cover, g.name, "ok") : esc(g.emoji)}</div>`;
const badges = (g) => (g.badges || []).map((b) => pill(b, b.startsWith("Novo") ? C.success : C.secondary)).join("");
const chips = (opts, sel, attr) => `<div class="chips">${opts.map((o) => `<button class="chip ${o === sel ? "on" : ""}" data-${attr}="${esc(o)}">${esc(o)}</button>`).join("")}</div>`;
const sectionHead = (title, sub = "", action = "", href = "") => `
  <div class="section-head"><div class="grow"><h2>${esc(title)}</h2>${sub ? `<div class="muted small">${esc(sub)}</div>` : ""}</div>
  ${action ? `<a class="link-btn" href="${href}">${esc(action)}</a>` : ""}</div>`;
const empty = (e, t, m) => `<div class="empty"><div class="e">${e}</div><h3>${esc(t)}</h3><div class="muted">${esc(m)}</div></div>`;
const media = (image, title, { badge = "", detail = "", href = "", tall = false, fav = null } = {}) => { if (fav) FAVDATA[fav.key] = fav; return `
  <${href ? `a href="${href}"` : "div"} class="card media press" style="position:relative">${fav ? favHeart(fav.key) : ""}
    ${image ? `<div class="img ${tall ? "tall" : ""}">${img(image, title)}</div>` : ""}
    <div class="body ${image ? "" : "noimg"}"><h3 class="clamp2">${esc(title)}</h3>${badge ? `<div>${pill(badge, C.secondary)}</div>` : ""}${detail ? `<div class="muted small clamp3">${esc(detail)}</div>` : ""}</div>
  </${href ? "a" : "div"}>`; };
const favHeart = (key, extra = "") => `<button class="fav-btn ${Fav.has(key) ? "on" : ""}" data-fav="${esc(key)}" ${extra} aria-label="Favoritar">${Fav.has(key) ? ICON.heart : ICON.heartO}</button>`;
const gameFav = (g) => ({ key: "game:" + g.id, title: g.name, subtitle: g.developer, image: g.cover, route: `#/game/${g.id}`, type: "game" });
const gameCard = (g) => `
  <div class="card game-card press" data-href="#/game/${g.id}" role="link" tabindex="0">
    ${favHeart("game:" + g.id, `data-game="${g.id}"`)}
    <div class="art-box">${img(g.cover, g.name, "ok")}</div>
    <div class="gc-body">
      <div class="gc-top">${gameIcon(g, 44)}<div class="grow"><h3 class="ellipsis">${esc(g.name)}</h3>
        <div class="muted small ellipsis">${esc([g.developer, g.stats?.characters ? `${g.stats.characters} ${g.stats.label}` : ""].filter(Boolean).join(" · "))}</div></div></div>
      <div class="muted small clamp2 gc-desc">${esc(g.subtitle)}</div>
      <div class="gc-foot"><div class="pills grow" style="gap:6px">${(g.genres || []).slice(0, 1).map((x) => pill(x, C.secondary)).join("")}${g.platform && !(g.genres || []).includes(g.platform) ? pill(g.platform, C.primary) : ""}</div>
        <span class="go">${ICON.right}</span></div>
    </div>
  </div>`;

// ---------------------------------------------------------------- blocos de conteúdo
function renderSection(s, ctx) {
  const box = (inner) => `<div class="card pad block">${s.title ? `<h3 class="t">${esc(s.title)}</h3>` : ""}${inner}</div>`;
  switch (s.type) {
    case "text": return box(`<div>${nl2br(s.body)}</div>`);
    case "notice": return box(`<div class="notice" style="--c:${s.tone === "warning" ? C.accent : s.tone === "success" ? C.success : C.primary}">${nl2br(s.body)}</div>`);
    case "steps": return box(`<div class="steps">${(s.items || []).map((t, i) => `<div class="step"><div class="num">${i + 1}</div><div style="padding-top:5px">${esc(t)}</div></div>`).join("")}</div>`);
    case "codes": return box(!(s.codes || []).length ? `<div class="muted">Nenhum código cadastrado no momento.</div>` : `<div class="stack" style="gap:10px">${s.codes.map((c) => {
      const st = c.status === "active" ? ["Ativo", C.success] : c.status === "expired" ? ["Expirado", C.text2] : ["Não verificado", C.accent];
      return `<div class="code ${c.status === "expired" ? "exp" : ""}"><div class="grow stack" style="gap:4px"><div class="row"><span class="c">${esc(c.code)}</span>${pill(st[0], st[1])}</div>
        ${c.reward ? `<div class="muted small">🎁 ${esc(c.reward)}</div>` : ""}${c.note ? `<div class="muted small">${esc(c.note)}</div>` : ""}</div>
        ${c.status !== "expired" ? `<button class="icon-btn" style="color:${C.primary}" data-copy="${esc(c.code)}" aria-label="Copiar">${ICON.copy}</button>` : ""}</div>`; }).join("")}</div>`);
    case "items": return box(`<div class="stack" style="gap:10px">${(s.items || []).map((it) => {
      const f = Object.entries(it.fields || {}); const exp = f.length > 0;
      return `<div class="item ${exp ? "exp" : ""}"><div class="head" ${exp ? 'data-toggle="1"' : ""}>${it.image ? `<img class="thumb ok" src="${esc(it.image)}" loading="lazy" referrerpolicy="no-referrer" alt="">` : ""}
        <div class="grow stack" style="gap:6px"><strong style="font-weight:500">${esc(it.name)}</strong>${(it.tags || []).length ? `<div class="pills">${it.tags.map((t) => pill(t, C.secondary)).join("")}</div>` : ""}</div>${exp ? ICON.chev : ""}</div>
        ${it.description ? `<div style="margin-top:8px">${nl2br(it.description)}</div>` : ""}
        ${exp ? `<div class="fields">${f.map(([k, v]) => `<div><div class="label">${esc(k)}</div><div>${/^https?:\/\//.test(v) ? `<a href="${esc(v)}" target="_blank" rel="noopener" style="color:var(--primary);word-break:break-all">${esc(v)}</a>` : nl2br(v)}</div></div>`).join("")}</div>` : ""}</div>`; }).join("")}</div>`);
    case "levels": return box(`<div class="stack" style="gap:10px">${(s.levels || []).map((l) => `<div class="level" data-toggle="1"><div class="row"><strong class="grow" style="font-weight:500">${esc(l.level)}</strong>${pill(`${l.tips.length} dicas`)}${ICON.chev}</div>
      <ul>${l.tips.length ? l.tips.map((t) => `<li>${esc(t)}</li>`).join("") : "<li class='muted'>Dicas deste nível em breve.</li>"}</ul></div>`).join("")}</div>`);
    case "links": return box(`<div class="stack" style="gap:10px">${(s.links || []).map((l) => `<a class="lnk press" href="${esc(l.url)}" target="_blank" rel="noopener"><div class="grow stack" style="gap:4px"><strong style="font-weight:500">${esc(l.label)}</strong>
      <div>${pill(l.official ? "Oficial" : "Comunidade", l.official ? C.success : C.text2)}</div></div><span style="color:${C.primary}">${ICON.open}</span></a>`).join("")}</div>`);
    case "creators": return box(`<div class="stack" style="gap:12px">${(s.creators || []).map((c) => `<div class="creator"><div class="row"><div class="avatar">${esc(c.name[0] || "?")}</div>
      <div class="grow stack" style="gap:4px"><strong style="font-size:20px;font-weight:600">${esc(c.name)}</strong>${c.country ? `<div>${pill(c.country, c.country.includes("Brasil") ? C.success : C.text2)}</div>` : ""}</div></div>
      ${c.about ? `<div class="muted">${esc(c.about)}</div>` : ""}
      <div class="pills">${c.youtube ? `<a class="btn btn-secondary" href="${esc(c.youtube)}" target="_blank" rel="noopener">${ICON.video} YouTube</a>` : ""}${c.tiktok ? `<a class="btn btn-secondary" href="${esc(c.tiktok)}" target="_blank" rel="noopener">${ICON.note} TikTok</a>` : ""}</div>
      ${c.source ? `<div class="muted small">Fonte: ${esc(c.source)}</div>` : ""}</div>`).join("")}</div>`);
    case "gallery": return `<div class="stack">${s.title ? `<h2 class="section-title">${esc(s.title)}</h2>` : ""}<div class="grid-media">${(s.images || []).map((g) => media(g.image, g.caption, { detail: g.subtitle, tall: true })).join("")}</div></div>`;
    case "toons": return `<div class="stack">${s.title ? `<h2 class="section-title">${esc(s.title)}</h2>` : ""}<div class="grid-media">${(s.toons || []).map((t) => media(t.image, t.name, { badge: (t.kind || "").split(" · ")[0], detail: t.cost, href: `#/game/${ctx.game}/c/${ctx.cat}/t/${t.id}` })).join("")}</div></div>`;
    default: return "";
  }
}
// Interações dos blocos (expandir itens, copiar códigos)
const FAVDATA = {}; // dados de favoritos registrados pelas telas (chave -> objeto)
document.addEventListener("click", (e) => {
  const fv = e.target.closest("[data-fav]");
  if (fv) {
    e.preventDefault(); e.stopPropagation();
    const key = fv.dataset.fav; let data = FAVDATA[key];
    if (!data && fv.dataset.game && DB.index) data = gameFav(DB.index.games.find((g) => g.id === fv.dataset.game));
    if (!data) return;
    const on = Fav.toggle(data);
    document.querySelectorAll(`[data-fav="${CSS.escape(key)}"]`).forEach((b) => { b.classList.toggle("on", on); b.innerHTML = on ? ICON.heart : ICON.heartO; });
    toast(on ? "Adicionado aos favoritos" : "Removido dos favoritos"); return;
  }
  const card = e.target.closest("[data-href]"); if (card && !e.target.closest("a,button")) { location.hash = card.dataset.href; return; }
  const cp = e.target.closest("[data-copy]"); if (cp) { e.preventDefault(); copy(cp.dataset.copy); return; }
  const tg = e.target.closest("[data-toggle]"); if (tg) { (tg.classList.contains("level") ? tg : tg.closest(".item")).classList.toggle("open"); }
});

document.addEventListener("keydown", (e) => { if (e.key === "Enter" && e.target.dataset?.href) location.hash = e.target.dataset.href; });

// ---------------------------------------------------------------- telas
const TABS = [["home", "Início", ICON.home], ["games", "Jogos", ICON.games], ["favorites", "Favoritos", ICON.heart], ["news", "Novidades", ICON.news], ["settings", "Configurações", ICON.settings]];
function setTabs(active) {
  const show = TABS.some((t) => t[0] === active);
  $tabbar.hidden = !show; document.body.classList.toggle("no-tabs", !show);
  if (show) $tabbar.innerHTML = `<div class="tabs">${TABS.map(([r, l, ic]) => `<a class="tab ${r === active ? "on" : ""}" href="#/${r}"><span class="ind">${ic}</span><span class="l">${l}</span></a>`).join("")}</div>`;
}
const NAV = { last: 0 };
function parentRoute() {
  const p = location.hash.replace(/^#\/?/, "").split("/").filter(Boolean);
  if (p[0] === "game" && p.length > 2) return "#/" + p.slice(0, p.length - 2).join("/");
  return "#/home";
}
function goBack() { if ((history.state?.d || 0) > 0) history.back(); else { NAV.replacing = true; location.replace(parentRoute()); } }
window.goBack = goBack;

const state = { homeFilter: "Todos os jogos", gamesFilter: "Todos os jogos", gamesQuery: "" };

function welcome() {
  setTabs(null);
  $app.innerHTML = `<div class="welcome"><div class="box fade"><div class="logo">🎮</div><h1>GUIA DE JOGOS</h1>
    <div class="muted" style="font-size:18px">Códigos, dicas, guias, skins, novidades e informações dos seus jogos favoritos — tudo em um só lugar.</div>
    <button class="enter" id="enter">ENTRAR</button><div class="muted small" style="margin-top:16px">Versão web · Criado por Gustavo Bezerra</div></div></div>`;
  document.getElementById("enter").onclick = () => { try { sessionStorage.setItem("gj-in", "1"); } catch (_) {} NAV.replacing = true; location.replace("#/home"); };
}

async function home() {
  setTabs("home");
  const idx = await loadIndex();
  const featured = idx.games.filter((g) => g.cover);
  const favs = Fav.list();
  const week = (idx.weeklyNews?.items || []).slice(0, 3);
  const byId = Object.fromEntries(idx.games.map((g) => [g.id, g]));
  const quick = [["#/games", "Jogos", ICON.games, `${idx.games.length} guias`], ["#/favorites", "Favoritos", ICON.heart, favs.length ? `${favs.length} salvos` : "Seus salvos"],
    ["#/news", "Novidades", ICON.news, "Semana e mês"], ["#/search", "Pesquisar", ICON.search, "Tudo do guia"]];
  $app.innerHTML = `<div class="wrap stack page" style="gap:22px">
    <div class="home-head"><div class="logo">🎮</div><span class="brand">Guia de Jogos</span>
      <a class="search press grow" href="#/search">${ICON.search}<span class="ellipsis">Pesquisar jogos, personagens, skins...</span></a>
      <a class="icon-btn" href="#/settings" aria-label="Configurações" style="color:var(--text2)">${ICON.settings}</a></div>
    <div><div class="banner" id="banner">${featured.map((g, k) => `
      <div class="slide ${k === 0 ? "on" : ""}" data-href="#/game/${g.id}">
        ${img(g.cover, "", "art ok")}<div class="shade"></div>
        <div class="txt">${pill("★ Em destaque", C.primary)}<h2>${esc(g.name)}</h2>
          <div class="meta">${esc([(g.genres || []).join(" · "), g.developer].filter(Boolean).join("  •  "))}</div>
          <div class="desc clamp2">${esc(g.subtitle || g.longDescription)}</div>
          <div style="margin-top:6px"><a class="btn btn-primary" href="#/game/${g.id}">${ICON.book} Ver guia</a></div></div>
      </div>`).join("")}</div>
      <div class="banner-dots" id="dots">${featured.map((_, k) => `<span class="dot ${k === 0 ? "on" : ""}" data-dot="${k}"></span>`).join("")}</div></div>
    <div class="quick">${quick.map(([h, l, ic, sub]) => `<a class="card press" href="${h}"><span class="qi">${ic}</span><div class="grow"><strong>${l}</strong><div class="muted small ellipsis">${sub}</div></div></a>`).join("")}</div>
    ${favs.length ? `<div class="stack">${sectionHead("Seus favoritos", "", "Ver todos", "#/favorites")}<div class="hscroll">${favs.map((f) => `<div style="width:148px">${media(f.image, f.title, { detail: f.subtitle, href: f.route })}</div>`).join("")}</div></div>` : ""}
    <div class="stack" style="gap:16px">${sectionHead("Jogos", "Escolha uma categoria e toque em um jogo", "Ver catálogo", "#/games")}<div id="hchips"></div><div id="hlist"></div></div>
    ${week.length ? `<div class="stack">${sectionHead("Novidades da semana", idx.weeklyNews.period || "", "Ver todas", "#/news")}
      <div class="news-mini">${week.map((n) => `<a class="card press" href="#/news">${byId[n.game] ? `<div>${pill(byId[n.game].name, C.secondary)}</div>` : ""}<h3 class="clamp2">${esc(n.title)}</h3><div class="muted small clamp2">${esc(n.body)}</div><div class="muted small">${fmtDate(n.date)}</div></a>`).join("")}</div></div>` : ""}
  </div>`;
  // Carrossel: troca suave a cada 6 s, toque nos pontos ou arraste para o lado
  let i = 0; const slides = [...document.querySelectorAll("#banner .slide")], dots = [...document.querySelectorAll("#dots .dot")];
  const show = (k) => { i = (k + slides.length) % slides.length; slides.forEach((x, n) => x.classList.toggle("on", n === i)); dots.forEach((x, n) => x.classList.toggle("on", n === i)); };
  const restart = () => { clearInterval(home._t); home._t = setInterval(() => { if (!document.getElementById("banner")) return clearInterval(home._t); show(i + 1); }, 6000); };
  dots.forEach((d) => (d.onclick = () => { show(+d.dataset.dot); restart(); }));
  const bn = document.getElementById("banner"); let x0 = null;
  bn.addEventListener("pointerdown", (e) => (x0 = e.clientX));
  bn.addEventListener("pointerup", (e) => { if (x0 != null && Math.abs(e.clientX - x0) > 50) { show(i + (e.clientX < x0 ? 1 : -1)); restart(); bn._swiped = true; setTimeout(() => (bn._swiped = false), 50); } x0 = null; });
  bn.addEventListener("click", (e) => { if (bn._swiped) e.stopPropagation(); }, true);
  restart();
  const renderList = () => {
    document.getElementById("hchips").innerHTML = chips(FILTERS, state.homeFilter, "hf");
    const list = applyFilter(idx.games, state.homeFilter);
    const el = document.getElementById("hlist");
    el.innerHTML = list.length ? `<div class="grid-games fade">${list.map(gameCard).join("")}</div>` : empty("🔎", "Nenhum jogo aqui ainda", "Ainda não temos jogos nesta categoria. Novos jogos chegam pelas atualizações.");
    document.querySelectorAll("[data-hf]").forEach((c) => (c.onclick = () => { state.homeFilter = c.dataset.hf; renderList(); }));
  };
  renderList();
}

async function games() {
  setTabs("games");
  const idx = await loadIndex();
  $app.innerHTML = `${topbar("Jogos", false)}<div class="wrap stack page">
    <label class="search">${ICON.search}<input id="gq" placeholder="Pesquisar no catálogo" value="${esc(state.gamesQuery)}"></label>
    <div id="gchips"></div><div id="glist" class="stack" style="gap:12px"></div></div>`;
  const render = () => {
    document.getElementById("gchips").innerHTML = chips(FILTERS, state.gamesFilter, "gf");
    const q = state.gamesQuery.toLowerCase();
    const list = applyFilter(idx.games, state.gamesFilter).filter((g) => !q || [g.name, g.developer, g.subtitle, (g.genres || []).join()].join(" ").toLowerCase().includes(q));
    document.getElementById("glist").innerHTML = list.length ? list.map((g) => `<a class="card game-row press" href="#/game/${g.id}">${gameIcon(g, 60)}
      <div class="grow"><strong style="font-weight:500">${esc(g.name)}</strong><div class="muted small ellipsis">${esc([g.developer, (g.genres || []).join(" · ")].filter(Boolean).join(" • "))}</div></div>
      ${g.stats?.characters ? pill(`${g.stats.characters} ${g.stats.label}`, C.primary) : ""}${favHeart("game:" + g.id, `data-game="${g.id}" style="position:static;background:none"`)}<span class="muted">${ICON.right}</span></a>`).join("") : empty("🔎", "Nada encontrado", "Tente outra palavra ou outra categoria.");
    document.querySelectorAll("[data-gf]").forEach((c) => (c.onclick = () => { state.gamesFilter = c.dataset.gf; render(); }));
  };
  document.getElementById("gq").oninput = (e) => { state.gamesQuery = e.target.value; render(); };
  render();
}

// Agrupa as seções do guia para a página do jogo ficar organizada (funciona para qualquer jogo novo).
const SECTION_GROUPS = [
  ["Comece por aqui", ["info", "codigos", "guias", "dicas", "iniciante", "como-jogar"]],
  ["Coleção", ["toons", "brawlers", "personagens", "skins", "douradas", "maestria", "vintage", "como-conseguir-skins", "pets", "armas", "frutas", "estilos", "espadas", "racas", "papeis", "ancient", "godly", "como-desbloquear"]],
  ["Novidades e eventos", ["semana", "novidades", "atualizacoes", "eventos"]],
  ["Comunidade e termos", ["criadores", "termos", "links"]],
];
function groupCategories(cats) {
  const used = new Set(); const out = [];
  SECTION_GROUPS.forEach(([title, ids]) => {
    const list = cats.filter((c) => !used.has(c.id) && ids.some((k) => c.id === k || c.id.startsWith(k)));
    list.forEach((c) => used.add(c.id)); if (list.length) out.push([title, list]);
  });
  const rest = cats.filter((c) => !used.has(c.id)); if (rest.length) out.push(["Mais seções", rest]);
  return out;
}

async function gamePage(id) {
  setTabs(null);
  const idx = await loadIndex(); const s = idx.games.find((g) => g.id === id); const g = await loadGame(id);
  const favKey = "game:" + id; FAVDATA[favKey] = gameFav(s);
  const toonsCat = g.categories.find((c) => c.sections.some((x) => x.type === "toons"));
  const preview = toonsCat ? toonsCat.sections.filter((x) => x.type === "toons").flatMap((x) => x.toons).slice(0, 14) : [];
  const codes = g.categories.flatMap((c) => c.sections.filter((x) => x.type === "codes").flatMap((x) => x.codes));
  const activeCodes = codes.filter((c) => c.status === "active").length;
  const codesCat = g.categories.find((c) => c.sections.some((x) => x.type === "codes"));
  // Destaque da semana (ex.: Toon da Semana e skins Douradas no Dandy's World)
  const weekCat = g.categories.find((c) => ["douradas", "semana"].includes(c.id) && c.sections.length);
  const weekNote = weekCat?.sections.find((x) => x.type === "notice" || x.type === "text");
  const favBtn = () => Fav.has(favKey) ? `${ICON.heart} Nos favoritos` : `${ICON.heartO} Favoritar`;
  const tile = (c) => { const soon = c.status === "coming_soon" && !c.sections.length;
    const tag = c.id === codesCat?.id && activeCodes ? pill(`${activeCodes} ativos`, C.success) : soon ? pill("Em breve") : "";
    return `<a class="card tile press" href="#/game/${id}/c/${c.id}" style="${soon ? "opacity:.6" : ""}"><span class="tag">${tag}</span><div class="ico">${esc(c.emoji)}</div><strong style="font-weight:500">${esc(c.title)}</strong><div class="muted small clamp2">${esc(soon ? "Em preparação" : c.description)}</div></a>`; };
  $app.innerHTML = `${topbar(s.name)}<div class="wrap stack page" style="gap:20px">
    <div class="hero">${img(s.cover, "", "art ok")}<div class="shade"></div>
      <div class="info">${gameIcon(s, 80)}<div class="stack" style="gap:4px"><h2 style="font-size:clamp(24px,3vw,30px)">${esc(s.name)}</h2>
      <div style="color:var(--primary);font-weight:500">${esc(s.developer)}</div><div class="pills">${(s.genres || []).map((x) => pill(x, C.secondary)).join("")}${s.platform && !(s.genres || []).includes(s.platform) ? pill(s.platform, C.primary) : ""}${badges(s)}</div></div></div></div>
    <div class="pills" style="gap:12px">${s.storeUrl ? `<a class="btn btn-primary" href="${esc(s.storeUrl)}" target="_blank" rel="noopener">${ICON.open} ${esc(s.storeLabel || "Abrir o jogo")}</a>` : ""}
      <button class="btn btn-secondary" id="fav">${favBtn()}</button>
      ${codesCat ? `<a class="btn btn-secondary" href="#/game/${id}/c/${codesCat.id}">🎁 Códigos${activeCodes ? ` (${activeCodes})` : ""}</a>` : ""}</div>
    <div class="stats-row">
      <div class="card stat"><b>${s.stats?.characters || 0}</b><span>${esc(s.stats?.label || "personagens")}</span></div>
      <div class="card stat"><b>${s.stats?.skins || 0}</b><span>skins e itens</span></div>
      <div class="card stat"><b>${g.categories.length}</b><span>seções do guia</span></div>
      <div class="card stat"><b style="color:${activeCodes ? C.success : "inherit"}">${activeCodes}</b><span>códigos ativos</span></div></div>
    ${weekNote ? `<a class="card week press" href="#/game/${id}/c/${weekCat.id}"><span class="wi">${esc(weekCat.emoji || "⭐")}</span><div class="grow stack" style="gap:4px"><strong style="font-size:18px;font-weight:600">${esc(weekCat.title)}</strong><div class="muted clamp3">${esc(weekNote.body || "")}</div></div><span style="color:var(--primary)">${ICON.right}</span></a>` : ""}
    ${preview.length ? `<div class="stack">${sectionHead(toonsCat.title, toonsCat.description, "Ver tudo", `#/game/${id}/c/${toonsCat.id}`)}
      <div class="hscroll">${preview.map((t) => `<div style="width:150px">${media(t.image, t.name, { badge: (t.kind || "").split(" · ")[0], href: `#/game/${id}/c/${toonsCat.id}/t/${t.id}` })}</div>`).join("")}</div></div>` : ""}
    ${sectionHead("Seções do guia", `${g.categories.length} seções`)}
    ${groupCategories(g.categories).map(([title, list]) => `<div class="group-title">${esc(title)}</div><div class="tiles">${list.map(tile).join("")}</div>`).join("")}
    <div class="two-col">
      <div class="card pad stack" style="gap:10px"><h3 style="font-size:20px">Sobre o jogo</h3><div class="muted">${esc(s.longDescription || g.description)}</div>
        ${(s.credits || []).length ? `<h3 style="font-size:17px;margin-top:6px">Quem criou</h3>${s.credits.map((c) => `<div class="muted">• ${esc(c)}</div>`).join("")}` : ""}</div>
      <div class="card pad stack" style="gap:12px"><h3 style="font-size:20px">📱 Compatibilidade</h3><div class="muted">${esc(s.compatibility || s.platform)}</div>
        <div class="muted small">Este guia funciona no celular, no tablet, no computador e na multimídia do carro.</div></div>
    </div>
  </div>`;
  document.getElementById("fav").onclick = (e) => {
    const on = Fav.toggle(FAVDATA[favKey]);
    e.currentTarget.innerHTML = favBtn(); toast(on ? "Adicionado aos favoritos" : "Removido dos favoritos");
  };
}

async function categoryPage(id, catId) {
  setTabs(null);
  const g = await loadGame(id); const c = g.categories.find((x) => x.id === catId);
  if (!c) throw new Error("Seção não encontrada");
  const collection = c.sections.some((x) => x.type === "toons" || x.type === "gallery");
  const groups = c.sections.filter((x) => (x.type === "toons" || x.type === "gallery") && x.title);
  let q = "", grp = "Todos";
  const gk = `guide:${id}/${catId}`; const idxG = (await loadIndex()).games.find((x) => x.id === id);
  FAVDATA[gk] = { key: gk, title: c.title, subtitle: idxG?.name || "", image: idxG?.cover || "", route: `#/game/${id}/c/${catId}`, type: "guide" };
  $app.innerHTML = `${topbar(c.title, true, favHeart(gk, 'style="position:static;background:none;width:56px;height:56px"'))}<div class="wrap stack page ${collection ? "" : "narrow"}" style="gap:20px">
    <div class="card row" style="padding:18px 22px;gap:18px"><div class="tile" style="padding:0;min-height:0"><div class="ico" style="width:56px;height:56px">${esc(c.emoji)}</div></div>
      <div class="grow"><h2 style="font-size:22px">${esc(c.title)}</h2><div class="muted small clamp2">${esc(c.description)}</div></div></div>
    ${collection ? `<label class="search">${ICON.search}<input id="cq" placeholder="Pesquisar em ${esc(c.title.toLowerCase())}"></label>${groups.length > 1 ? `<div id="cchips"></div>` : ""}` : ""}
    <div id="csec" class="stack" style="gap:20px"></div>
    ${c.updatedAt ? `<div class="muted small">Última revisão: ${esc(fmtDate(c.updatedAt))}</div>` : ""}</div>`;
  const render = () => {
    if (groups.length > 1) { document.getElementById("cchips").innerHTML = chips(["Todos", ...groups.map((x) => x.title)], grp, "cg");
      document.querySelectorAll("[data-cg]").forEach((b) => (b.onclick = () => { grp = b.dataset.cg; render(); })); }
    const ql = q.toLowerCase();
    const list = c.sections.map((s) => {
      if (s.type === "toons" || s.type === "gallery") {
        if (grp !== "Todos" && s.title !== grp) return null;
        if (s.type === "toons") { const t = s.toons.filter((x) => !ql || x.name.toLowerCase().includes(ql) || (x.kind || "").toLowerCase().includes(ql)); return t.length ? { ...s, toons: t } : null; }
        const im = s.images.filter((x) => !ql || x.caption.toLowerCase().includes(ql) || (x.subtitle || "").toLowerCase().includes(ql)); return im.length ? { ...s, images: im } : null;
      }
      return grp === "Todos" && !ql ? s : null;
    }).filter(Boolean);
    const el = document.getElementById("csec");
    el.innerHTML = !list.length ? (c.sections.length ? empty("🔎", "Nada encontrado", "Tente outra palavra ou outro filtro.") : empty("🚧", "Em preparação", "Esta seção ainda está sendo preparada.")) : list.map((s) => renderSection(s, { game: id, cat: catId })).join("");
    el.classList.remove("fade"); void el.offsetWidth; el.classList.add("fade");
  };
  const inp = document.getElementById("cq"); if (inp) inp.oninput = (e) => { q = e.target.value; render(); };
  render();
}

async function toonPage(id, catId, toonId) {
  setTabs(null);
  const g = await loadGame(id);
  const t = g.categories.find((c) => c.id === catId)?.sections.filter((s) => s.type === "toons").flatMap((s) => s.toons).find((x) => x.id === toonId);
  if (!t) throw new Error("Personagem não encontrado");
  const favKey = `char:${id}/${catId}/${toonId}`;
  const tabs = ["Visão geral", ...(t.tutorial?.length ? ["Como conseguir"] : []), ...(t.mastery?.length ? ["Maestria"] : []), ...(t.skins?.length ? [`Skins (${t.skins.length})`] : [])];
  let tab = tabs[0];
  const heartBtn = () => `<button class="icon-btn" id="tfav" aria-label="Favoritar" style="color:${Fav.has(favKey) ? C.danger : "inherit"}">${Fav.has(favKey) ? ICON.heart : ICON.heartO}</button>`;
  $app.innerHTML = `${topbar(t.name, true, heartBtn())}<div class="wrap page"><div class="char-layout">
    <div class="stack" style="gap:18px"><div class="char-img">${img(t.image, t.name, "ok")}</div>
      <h2 style="font-size:30px">${esc(t.name)}</h2>${t.kind ? `<div>${pill(t.kind, C.secondary)}</div>` : ""}
      ${t.cost ? `<div style="color:${C.accent};font-weight:500">${esc(t.cost)}</div>` : ""}
      ${t.videoUrl ? `<div><a class="btn btn-secondary" href="${esc(t.videoUrl)}" target="_blank" rel="noopener">${ICON.video} Ver vídeos</a></div>` : ""}</div>
    <div class="stack"><div id="ttabs"></div><div id="tbody"></div></div></div></div>`;
  const onFav = () => {
    const on = Fav.toggle({ key: favKey, title: t.name, subtitle: t.kind, image: t.image, route: `#/game/${id}/c/${catId}/t/${toonId}`, type: "character" });
    document.getElementById("tfav").outerHTML = heartBtn(); document.getElementById("tfav").onclick = onFav; toast(on ? "Adicionado aos favoritos" : "Removido dos favoritos");
  };
  document.getElementById("tfav").onclick = onFav;
  const card = (title, inner) => `<div class="card pad stack" style="gap:12px"><h3 style="font-size:20px">${esc(title)}</h3>${inner}</div>`;
  const bullets = (arr) => arr.map((x) => `<div class="bullet">${esc(x)}</div>`).join("");
  const render = () => {
    document.getElementById("ttabs").innerHTML = chips(tabs, tab, "tt");
    document.querySelectorAll("[data-tt]").forEach((b) => (b.onclick = () => { tab = b.dataset.tt; render(); }));
    const el = document.getElementById("tbody"); stopTutorial();
    if (tab === "Visão geral") {
      el.innerHTML = `<div class="stack fade">${card("Como desbloquear", `<div class="label">Custo</div><div>${esc(t.cost || "—")}</div><div class="label">Requisitos</div>${t.requirements?.length ? bullets(t.requirements) : `<div class="muted">Nenhum além do custo.</div>`}`)}
        ${(t.infoBlocks || []).filter((b) => b.items?.length).map((b) => card(b.title, bullets(b.items))).join("")}
        <div class="muted small">Dados e imagens: wikis da comunidade (Fandom, CC BY-SA). Marcas e personagens pertencem aos seus donos.</div></div>`;
    } else if (tab === "Como conseguir") {
      el.innerHTML = `<div class="stack fade"><div class="tutorial" id="tut"></div>${t.videoUrl ? `<a class="btn btn-primary btn-block" href="${esc(t.videoUrl)}" target="_blank" rel="noopener">${ICON.video} Ver vídeos no YouTube</a>` : ""}</div>`;
      startTutorial(t.tutorial);
    } else if (tab === "Maestria") {
      el.innerHTML = `<div class="fade">${card("Missões de Maestria", `<div class="row" style="align-items:flex-start;gap:22px;flex-wrap:wrap">
        ${t.vintageImage ? `<img src="${esc(t.vintageImage)}" referrerpolicy="no-referrer" alt="Vintage ${esc(t.name)}" style="width:170px;height:230px;object-fit:contain;background:var(--surface-high);border-radius:16px">` : ""}
        <div class="grow stack" style="gap:10px;min-width:240px">${t.mastery.map((m, i) => `<div class="step"><div class="num">${i + 1}</div><div style="padding-top:5px">${esc(m)}</div></div>`).join("")}
        ${t.vintageImage ? `<div>${pill(`🎁 Recompensa: skin Vintage ${t.name}`, C.success)}</div>` : ""}</div></div>`)}</div>`;
    } else {
      el.innerHTML = `<div class="grid-media fade">${t.skins.map((s) => media(s.image, s.name, { badge: s.category, detail: s.howTo, tall: true, fav: { key: `skin:${id}/${toonId}/${s.name}`, title: s.name, subtitle: `${t.name} · ${s.category || "Skin"}`, image: s.image, route: `#/game/${id}/c/${catId}/t/${toonId}`, type: "skin" } })).join("")}</div>`;
    }
  };
  render();
}

// "Mini vídeo" do tutorial: passos que avançam sozinhos com barra de progresso.
let tutTimer = null;
function stopTutorial() { if (tutTimer) cancelAnimationFrame(tutTimer); tutTimer = null; }
function startTutorial(steps) {
  const box = document.getElementById("tut"); if (!box || !steps?.length) return;
  const DUR = 5500; let i = 0, t0 = performance.now(), elapsed = 0, playing = true;
  const draw = () => {
    const s = steps[i];
    box.innerHTML = `<div class="bars">${steps.map((_, k) => `<span><i style="width:${k < i ? 100 : 0}%"></i></span>`).join("")}</div>
      <div class="tut-body fade">${s.image ? `<img src="${esc(s.image)}" referrerpolicy="no-referrer" alt="" onerror="this.style.visibility='hidden'">` : ""}<div class="stack" style="gap:6px"><h3 style="font-size:22px">${esc(s.title)}</h3><div class="muted">${nl2br(s.text)}</div></div></div>
      <div class="ctrl"><button class="icon-btn" id="tp">${ICON.prev}</button><button class="play" id="tg">${playing ? ICON.pause : (i === steps.length - 1 && elapsed >= DUR ? ICON.replay : ICON.play)}</button><button class="icon-btn" id="tn">${ICON.next}</button></div>`;
    box.querySelector("#tp").onclick = () => go(i - 1);
    box.querySelector("#tn").onclick = () => go(i + 1);
    box.querySelector("#tg").onclick = () => { if (!playing && i === steps.length - 1 && elapsed >= DUR) { go(0); playing = true; } else { playing = !playing; t0 = performance.now() - elapsed; } draw(); };
  };
  const go = (k) => { i = Math.max(0, Math.min(steps.length - 1, k)); elapsed = 0; t0 = performance.now(); draw(); };
  const tick = (now) => {
    if (!document.getElementById("tut")) return stopTutorial();
    if (playing) {
      elapsed = now - t0;
      const bar = box.querySelectorAll(".bars i")[i]; if (bar) bar.style.width = Math.min(100, (elapsed / DUR) * 100) + "%";
      if (elapsed >= DUR) { if (i < steps.length - 1) go(i + 1); else { playing = false; elapsed = DUR; draw(); } }
    }
    tutTimer = requestAnimationFrame(tick);
  };
  draw(); tutTimer = requestAnimationFrame(tick);
}

function favorites() {
  setTabs("favorites");
  const all = Fav.list();
  const groups = [["game", "Jogos"], ["character", "Personagens e Toons"], ["skin", "Skins"], ["guide", "Guias e seções"]];
  all.forEach((f) => (FAVDATA[f.key] = f));
  $app.innerHTML = `${topbar("Favoritos", false)}<div class="wrap stack page">${!all.length ? empty("♡", "Nenhum favorito ainda", "Toque no coração em um jogo, personagem, skin ou guia para guardar aqui. Fica salvo mesmo se você fechar o app.") :
    groups.map(([tp, label]) => { const l = all.filter((f) => (f.type || "game") === tp); if (!l.length) return "";
      if (tp === "game" || tp === "guide") return `${sectionHead(label, `${l.length} salvos`)}<div class="stack" style="gap:12px">${l.map((f) => `<a class="card game-row press" href="${f.route}"><div class="game-icon" style="width:56px;height:56px">${img(f.image, "", "ok")}</div><div class="grow"><strong style="font-weight:500">${esc(f.title)}</strong><div class="muted small">${esc(f.subtitle)}</div></div>${favHeart(f.key, 'style="position:static;background:none"')}</a>`).join("")}</div>`;
      return `${sectionHead(label, `${l.length} salvos`)}<div class="grid-media">${l.map((f) => media(f.image, f.title, { detail: f.subtitle, href: f.route, fav: f })).join("")}</div>`; }).join("")}</div>`;
}

async function news() {
  setTabs("news");
  const idx = await loadIndex(); const pairs = await allGames(); const byId = Object.fromEntries(idx.games.map((g) => [g.id, g]));
  const codes = []; pairs.forEach(([g, d]) => d.categories.forEach((c) => c.sections.filter((s) => s.type === "codes").forEach((s) => s.codes.filter((x) => x.status === "active").forEach((x) => codes.push([g, x])))));
  const tabs = ["Da semana", "Do mês", "Códigos ativos", "Do app"]; let tab = news._tab || tabs[0];
  $app.innerHTML = `${topbar("Novidades", false)}<div class="wrap narrow stack page"><div id="ntabs"></div><div id="nbody" class="stack"></div></div>`;
  const newsCard = (n) => { const g = byId[n.game]; return `<${n.url ? `a href="${esc(n.url)}" target="_blank" rel="noopener"` : `a href="#/game/${n.game}"`} class="card pad press row" style="align-items:flex-start;gap:16px">
    ${g ? gameIcon(g, 56) : ""}<div class="grow stack" style="gap:6px"><div class="row">${g ? pill(g.name, C.secondary) : ""}<span class="muted small">${fmtDate(n.date)}</span></div>
    <h3 style="font-size:20px">${esc(n.title)}</h3><div class="muted">${esc(n.body)}</div><div class="small" style="color:${C.primary}">Fonte: ${esc(n.source)}${n.url ? " · toque para abrir" : ""}</div></div></a>`; };
  const render = () => {
    document.getElementById("ntabs").innerHTML = chips(tabs, tab, "nt");
    document.querySelectorAll("[data-nt]").forEach((b) => (b.onclick = () => { tab = news._tab = b.dataset.nt; render(); }));
    const el = document.getElementById("nbody"); let h = "";
    if (tab === "Da semana" || tab === "Do mês") {
      const p = tab === "Da semana" ? idx.weeklyNews : idx.monthlyNews;
      h = sectionHead(tab === "Da semana" ? "Notícias da semana" : "Notícias do mês", p?.period || "") + ((p?.items || []).map(newsCard).join("") || `<div class="muted">Ainda não há notícias neste período.</div>`);
    } else if (tab === "Códigos ativos") {
      h = sectionHead("Códigos ativos agora", "Toque para copiar") + (codes.map(([g, c]) => `<div class="card game-row press" data-copy="${esc(c.code)}">${gameIcon(g, 52)}<div class="grow"><div class="code" style="background:none;padding:0;min-height:0"><span class="c">${esc(c.code)}</span></div><div class="muted small">${esc(g.name)} · ${esc(c.reward)}</div></div>${pill("Ativo", C.success)}<span style="color:${C.primary}">${ICON.copy}</span></div>`).join("") || `<div class="muted">Nenhum código ativo confirmado no momento.</div>`);
    } else {
      h = sectionHead("Novidades do app") + (idx.news || []).map((n) => `<div class="card pad row" style="align-items:flex-start;gap:16px"><span class="dot on" style="width:10px;height:10px;margin-top:8px"></span><div class="stack" style="gap:4px"><div class="row"><strong style="font-weight:500">${esc(n.title)}</strong><span class="muted small">${fmtDate(n.date)}</span></div><div class="muted">${esc(n.body)}</div></div></div>`).join("");
    }
    el.innerHTML = `<div class="stack fade">${h}</div>`;
  };
  render();
}

async function settings() {
  setTabs("settings");
  const idx = await loadIndex();
  const total = idx.games.reduce((a, g) => a + (g.stats?.skins || 0), 0);
  const maxC = Math.max(...idx.games.map((g) => g.stats?.characters || 0), 1);
  const slices = idx.games.filter((g) => g.stats?.skins > 0);
  let acc = 0; const R = 70, L = 2 * Math.PI * R;
  const donut = slices.map((g, i) => { const frac = g.stats.skins / total; const seg = `<circle r="${R}" cx="85" cy="85" fill="none" stroke="${CHART[idx.games.indexOf(g) % CHART.length]}" stroke-width="26" stroke-dasharray="${Math.max(frac * L - 3, 1)} ${L}" stroke-dashoffset="${-acc * L}" transform="rotate(-90 85 85)"/>`; acc += frac; return seg; }).join("");
  const sec = (title, sub, inner) => `<div class="card pad stack" style="gap:14px"><div><h3 style="font-size:20px">${esc(title)}</h3><div class="muted small">${esc(sub)}</div></div>${inner}</div>`;
  $app.innerHTML = `${topbar("Configurações", false)}<div class="wrap narrow stack page">
    ${sec("Conteúdo", "Códigos, skins, personagens e notícias", `<div class="muted small">Revisado em ${fmtDate(idx.updatedAt)} · versão de conteúdo ${idx.contentVersion}</div><button class="btn btn-primary btn-block" id="refresh">${ICON.refresh} Atualizar conteúdo</button>`)}
    ${sec("App Android", "Para celular, tablet e multimídia do carro", `<a class="btn btn-secondary btn-block" href="https://github.com/diegoalves8/Guia-de-jogos/releases/latest/download/GuiaDeJogos.apk">${ICON.download} Baixar o APK mais recente</a>`)}
    ${sec("Instalar a versão web", "Use como um app, até sem internet", `<div class="muted">No Chrome do celular: menu ⋮ → <strong>Adicionar à tela inicial</strong>. No computador: ícone de instalar na barra de endereço.</div>${window._installEvt ? `<button class="btn btn-secondary btn-block" id="install">${ICON.download} Instalar agora</button>` : ""}`)}
    ${sec("Conteúdo do guia", `${idx.games.length} jogos`, `<strong style="font-weight:500">Personagens por jogo</strong>
      ${idx.games.map((g, i) => `<div class="bar-row"><div class="row"><span class="grow">${esc(g.name)}</span><strong style="color:${CHART[i % CHART.length]}">${g.stats?.characters || 0}</strong></div><div class="track"><div class="fill" data-w="${((g.stats?.characters || 0) / maxC * 100).toFixed(1)}" style="background:${CHART[i % CHART.length]}"></div></div></div>`).join("")}
      <strong style="font-weight:500;margin-top:8px">Skins e itens</strong>
      <div class="row" style="gap:24px;flex-wrap:wrap"><svg width="170" height="170" viewBox="0 0 170 170"><circle r="${R}" cx="85" cy="85" fill="none" stroke="#242C3D" stroke-width="26"/>${donut}<text x="85" y="84" text-anchor="middle" fill="#F5F7FA" font-size="28" font-weight="700">${total}</text><text x="85" y="106" text-anchor="middle" fill="#A5AFBF" font-size="14">no total</text></svg>
      <div class="stack grow" style="gap:10px;min-width:200px">${slices.map((g) => `<div class="legend"><i style="background:${CHART[idx.games.indexOf(g) % CHART.length]}"></i><span class="grow">${esc(g.name)}</span><strong class="muted">${Math.round(g.stats.skins / total * 100)}%</strong></div>`).join("")}</div></div>`)}
    ${sec("Sobre", "Guia de Jogos · versão web", `<strong style="font-weight:500">Aplicativo criado por Gustavo Bezerra.</strong>
      <strong style="font-weight:500">Quem criou cada jogo</strong>${idx.games.map((g) => `<div class="muted small">• ${esc(g.name)}: ${esc((g.credits || []).join(" "))}</div>`).join("")}
      <div class="muted small">Dados e imagens vêm de wikis da comunidade (Fandom, licença CC BY-SA) e de fontes oficiais. Marcas e personagens pertencem aos seus donos. Este app não é oficial de nenhum dos jogos.</div>`)}
  </div>`;
  requestAnimationFrame(() => document.querySelectorAll(".fill").forEach((f) => (f.style.width = f.dataset.w + "%")));
  document.getElementById("refresh").onclick = async (e) => {
    e.currentTarget.disabled = true; e.currentTarget.textContent = "Atualizando…";
    const before = idx.contentVersion; DB.games = {};
    try { const n = await loadIndex(true); toast(n.contentVersion > before ? "Conteúdo atualizado!" : "Você já tem o conteúdo mais recente."); settings(); }
    catch (_) { toast("Sem conexão. Tente de novo."); settings(); }
  };
  const ins = document.getElementById("install"); if (ins) ins.onclick = () => window._installEvt.prompt();
}

async function search() {
  setTabs(null);
  $app.innerHTML = `${topbar("Pesquisar")}<div class="wrap stack page"><label class="search">${ICON.search}<input id="sq" placeholder="Pesquisar jogos, personagens, skins..." autofocus></label><div id="sres"></div></div>`;
  const pairs = await allGames(); const hits = [];
  pairs.forEach(([g, d]) => {
    hits.push({ type: "Jogos", title: g.name, sub: [g.developer, (g.genres || []).join(" · ")].join(" • "), image: g.cover, href: `#/game/${g.id}` });
    d.categories.forEach((c) => {
      hits.push({ type: "Seções", title: c.title, sub: g.name, image: "", emoji: c.emoji, href: `#/game/${g.id}/c/${c.id}` });
      c.sections.forEach((s) => {
        if (s.type === "toons") s.toons.forEach((t) => hits.push({ type: "Personagens", title: t.name, sub: `${g.name} · ${t.kind}`, image: t.image, href: `#/game/${g.id}/c/${c.id}/t/${t.id}` }));
        if (s.type === "gallery") s.images.forEach((im) => hits.push({ type: "Skins e itens", title: im.caption, sub: g.name, image: im.image, href: `#/game/${g.id}/c/${c.id}` }));
        if (s.type === "toons") s.toons.forEach((t) => (t.skins || []).forEach((sk) => hits.push({ type: "Skins e itens", title: sk.name, sub: `${g.name} · ${t.name}`, image: sk.image, href: `#/game/${g.id}/c/${c.id}/t/${t.id}` })));
        if (s.type === "codes") s.codes.forEach((cd) => hits.push({ type: "Códigos", title: cd.code, sub: `${g.name} · ${cd.status === "active" ? "Ativo" : cd.status === "expired" ? "Expirado" : "Não verificado"}${cd.reward ? " · " + cd.reward : ""}`, emoji: "🎁", href: `#/game/${g.id}/c/${c.id}` }));
        if (s.type === "items") s.items.forEach((it) => hits.push({ type: c.id === "termos" ? "Termos" : "Guias e dicas", title: it.name, sub: `${g.name} · ${c.title}`, image: it.image || "", emoji: c.emoji, href: `#/game/${g.id}/c/${c.id}` }));
        if (s.type === "levels") s.levels.forEach((l) => hits.push({ type: "Guias e dicas", title: `${c.title}: ${l.level}`, sub: g.name, emoji: c.emoji, href: `#/game/${g.id}/c/${c.id}` }));
        if (s.type === "creators") s.creators.forEach((cr) => hits.push({ type: "Criadores", title: cr.name, sub: `${g.name} · ${cr.country || ""}`, emoji: "📺", href: `#/game/${g.id}/c/${c.id}` }));
      });
    });
  });
  { const seen = new Set(); for (let k = hits.length - 1; k >= 0; k--) { const h = hits[k], id = h.type + "|" + h.title + "|" + (h.image || h.sub); if (seen.has(id)) hits.splice(k, 1); else seen.add(id); } }
  const inp = document.getElementById("sq"); inp.value = search._q || ""; inp.focus();
  const render = () => {
    const q = (search._q = inp.value).trim().toLowerCase(); const el = document.getElementById("sres");
    if (q.length < 2) { el.innerHTML = empty("🔍", "O que você procura?", 'Digite pelo menos 2 letras. Ex.: "Shelly", "Vintage", "Dragon", "códigos".'); return; }
    const res = hits.filter((h) => h.title.toLowerCase().includes(q) || h.sub.toLowerCase().includes(q)).slice(0, 160);
    if (!res.length) { el.innerHTML = empty("🤔", "Nada encontrado", "Tente outro nome ou confira a ortografia."); return; }
    el.innerHTML = ["Jogos", "Personagens", "Skins e itens", "Códigos", "Guias e dicas", "Termos", "Criadores", "Seções"].map((tp) => { const l = res.filter((h) => h.type === tp); if (!l.length) return "";
      const cards = tp === "Personagens" || tp === "Skins e itens";
      return `<div class="stack" style="margin-bottom:24px">${sectionHead(tp, `${l.length} resultados`)}${cards ? `<div class="grid-media">${l.map((h) => media(h.image, h.title, { detail: h.sub, href: h.href })).join("")}</div>`
        : `<div class="stack" style="gap:12px">${l.map((h) => `<a class="card game-row press" href="${h.href}"><div class="game-icon" style="width:52px;height:52px">${h.image ? img(h.image, "", "ok") : esc(h.emoji || "🎮")}</div><div class="grow"><strong style="font-weight:500">${esc(h.title)}</strong><div class="muted small">${esc(h.sub)}</div></div>${pill({ "Seções": "Seção", "Jogos": "Jogo", "Códigos": "Código", "Termos": "Termo", "Criadores": "Criador" }[tp] || "Guia")}</a>`).join("")}</div>`}</div>`; }).join("");
  };
  inp.oninput = render; render();
}

// ---------------------------------------------------------------- roteador
async function router() {
  stopTutorial();
  // Cada entrada do histórico guarda sua profundidade: o "voltar" do app nunca sai do site.
  if (history.state?.d == null) history.replaceState({ d: !router._ran ? 0 : NAV.replacing ? NAV.last : NAV.last + 1 }, "");
  NAV.last = history.state.d; NAV.replacing = false; router._ran = true;
  const parts = location.hash.replace(/^#\/?/, "").split("/").filter(Boolean);
  let entered = false; try { entered = sessionStorage.getItem("gj-in") === "1"; } catch (_) { entered = true; }
  if (!entered && !parts.length) return welcome();
  if (!parts.length) { NAV.replacing = true; location.replace("#/home"); return; }
  $app.innerHTML = `<div class="spinner"></div>`;
  scrollTo(0, 0);
  try {
    const [p0, p1, p2, p3, p4, p5] = parts;
    if (p0 === "home") await home();
    else if (p0 === "games") await games();
    else if (p0 === "favorites") favorites();
    else if (p0 === "news") await news();
    else if (p0 === "settings") await settings();
    else if (p0 === "search") await search();
    else if (p0 === "game" && p1 && p2 === "c" && p3 && p4 === "t" && p5) await toonPage(p1, p3, p5);
    else if (p0 === "game" && p1 && p2 === "c" && p3) await categoryPage(p1, p3);
    else if (p0 === "game" && p1) await gamePage(p1);
    else { NAV.replacing = true; location.replace("#/home"); }
  } catch (err) {
    setTabs(null);
    $app.innerHTML = `${topbar("Ops")}<div class="wrap">${empty("⚠️", "Algo deu errado", err.message || "Erro ao carregar")}<div style="text-align:center"><a class="btn btn-secondary" href="#/home">Voltar ao início</a></div></div>`;
  }
}
addEventListener("hashchange", router);
addEventListener("beforeinstallprompt", (e) => { e.preventDefault(); window._installEvt = e; });
if ("serviceWorker" in navigator) addEventListener("load", () => navigator.serviceWorker.register("sw.js").catch(() => {}));
router();
