# Atualizar conteúdo sem gerar APK novo

## Como funciona

1. O APK já traz todo o conteúdo (`assets/content`), então o app funciona offline.
2. Na tela **Escolha o jogo**, o botão **↻** baixa `games.json` do servidor configurado.
3. Se o `contentVersion` do servidor for **maior** que o atual, o app baixa e valida todos os arquivos de jogos e salva no aparelho.
4. Se algo der errado (sem internet, JSON inválido), nada é alterado.
5. Quando você instala um APK novo com conteúdo ainda mais recente, o conteúdo embutido volta a valer.

## Endereço do servidor

Fica em `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "REMOTE_CONTENT_BASE_URL",
    "\"https://raw.githubusercontent.com/diegoalves8/Guia-de-jogos/main/app/src/main/assets/content/\"")
```

O app busca `<URL>games.json` e `<URL>games/<id>.json`.

### ⚠️ Repositório privado

O `raw.githubusercontent.com` **só funciona se o repositório for público**. Com o repositório privado, o botão ↻ simplesmente responde "Você já tem o conteúdo mais recente". Opções:

- **Tornar o repositório público** (Settings → General → Change visibility). Mais simples.
- **Publicar só a pasta `content` em outro lugar público** (GitHub Pages de outro repositório, Netlify, Firebase Hosting…) e trocar a URL acima.
- Deixar `""` para desligar a função.

Mudou a URL? Gere um APK novo uma vez; depois disso, conteúdo novo chega sem reinstalar.

## Rotina para publicar novidades

1. Edite os JSON em `app/src/main/assets/content/`.
2. Aumente `contentVersion` em `games.json` e atualize `updatedAt`.
3. Faça commit/push na `main`. O GitHub Actions valida os JSON e gera um APK novo.
4. Nos aparelhos, toque em ↻ para receber o conteúdo.
