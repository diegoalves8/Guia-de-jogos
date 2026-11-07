# Estrutura do conteúdo (JSON)

Todo o conteúdo fica em `app/src/main/assets/content/`. O app monta as telas a partir desses arquivos — **não é preciso mexer em Kotlin** para adicionar jogos, categorias, códigos, skins ou guias.

## games.json

```json
{
  "contentVersion": 2,
  "updatedAt": "2026-11-10",
  "games": [
    {
      "id": "dandys-world",
      "name": "Dandy's World",
      "subtitle": "Terror cooperativo com Toons",
      "platform": "Roblox",
      "emoji": "🌼",
      "color": "#F59E0B",
      "status": "available",
      "file": "games/dandys-world.json"
    }
  ]
}
```

- `contentVersion`: **aumente a cada mudança** de conteúdo (o app usa isso para saber se há novidade).
- `status`: `available` ou `coming_soon` (mostra o selo "EM BREVE").

## Arquivo de jogo (games/<id>.json)

```json
{
  "id": "dandys-world",
  "name": "Dandy's World",
  "emoji": "🌼",
  "color": "#F59E0B",
  "description": "Texto do cabeçalho",
  "categories": [
    {
      "id": "codigos",
      "title": "Códigos e Como Resgatar",
      "emoji": "🎁",
      "description": "Subtítulo do botão",
      "status": "available",
      "updatedAt": "2026-11-06",
      "sections": [ ... blocos ... ]
    }
  ]
}
```

Cada jogo define **suas próprias categorias**. Os `id` de jogo e categoria não podem se repetir e devem usar só letras minúsculas, números e hífen.

## Tipos de bloco (`sections`)

| type | Uso | Campos |
|---|---|---|
| `text` | Texto livre | `title`, `body` |
| `notice` | Aviso destacado | `body`, `tone` (`info`, `warning`, `success`) |
| `steps` | Passo a passo numerado | `title`, `items: [texto]` |
| `codes` | Códigos com botão copiar | `title`, `codes: [{code, reward, status, note}]` — status: `active`, `expired`, `unverified` |
| `items` | Skins, termos, personagens, eventos… | `title`, `items: [{name, description, tags: [], fields: {"Rótulo": "valor"}}]` |
| `levels` | Guias por nível | `title`, `levels: [{level, tips: []}]` |
| `links` | Links úteis | `title`, `links: [{label, url, official}]` |

Tipos desconhecidos são ignorados — versões antigas do app não quebram quando você usa um tipo novo.

## Exemplos rápidos

**Adicionar um código novo** (no bloco `codes` de "Códigos ativos"):

```json
{ "code": "NOVOCODIGO", "reward": "100 Ichor", "status": "active", "note": "Divulgado no Discord oficial em 10/11/2026." }
```

**Marcar como expirado:** troque `"status": "active"` por `"status": "expired"` (ou mova para o bloco de expirados).

**Nova skin** (bloco `items` da categoria `skins`):

```json
{
  "name": "Nome da Skin",
  "tags": ["Evento", "Tempo limitado"],
  "description": "Resumo.",
  "fields": { "Como conseguir": "...", "Requisitos": "..." }
}
```

## Criar um novo tipo de bloco (precisa de código)

1. `data/model/Models.kt` → nova classe em `Section`.
2. `data/repository/ContentParser.kt` → novo `case` em `parseSection`.
3. `ui/components/SectionViews.kt` → como desenhar.
