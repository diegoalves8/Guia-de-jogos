# 🎮 Guia de Jogos

Aplicativo Android que reúne **códigos, dicas, guias, skins, termos e novidades** de vários jogos — começando pelo **Dandy's World**.

Feito em **Kotlin + Jetpack Compose (Material 3)**, com layout adaptável para **celulares e tablets** (testado para telas como o Galaxy Tab S6 Lite, em retrato e paisagem).

---

## 📲 Como pegar o APK (sem instalar nada no PC)

O repositório já gera o APK sozinho pelo **GitHub Actions** a cada `push` na `main`.

1. Abra a aba **Actions** do repositório.
2. Clique na execução mais recente de **Build APK** (com ✅ verde).
3. Em **Artifacts**, baixe **GuiaDeJogos-apk** (um `.zip`).
4. Dentro dele estão:
   - `GuiaDeJogos.apk` → versão normal (use esta)
   - `GuiaDeJogos-debug.apk` → versão de testes
5. Passe o APK para o tablet/celular e abra. Se o Android pedir, permita **"Instalar apps desconhecidos"** para o app que você usou para abrir o arquivo (Arquivos, Chrome etc.).

**Releases:** criando uma tag (ex.: `git tag v1.0.0 && git push --tags`), o APK também aparece na aba **Releases**, mais fácil de baixar direto no tablet.

## 🛠️ Build local (opcional)

Requisitos: Android Studio (Ladybug ou mais novo) ou JDK 17 + Android SDK 35.

```bash
./gradlew assembleRelease
# APK em: app/build/outputs/apk/release/app-release.apk
```

Ou no Android Studio: **File → Open** → pasta do projeto → **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

> O release é assinado com a chave de debug para poder ser instalado direto. Para publicar na Play Store, crie uma chave própria (**Build → Generate Signed App Bundle / APK**) e configure `signingConfigs` em `app/build.gradle.kts`.

---

## 🧭 Navegação

```
GUIA DE JOGOS (tela inicial)
   ↓ ENTRAR
Escolha o jogo  (Dandy's World, Brawl Stars, Free Fire, Murder Mystery…)
   ↓
Página do jogo  (categorias em botões grandes)
   ↓
Conteúdo da categoria  (ex.: Códigos e Como Resgatar)
```

Toda tela interna tem botão **Voltar** (e o voltar do Android também funciona).

## 🗂️ Estrutura do projeto

```
app/src/main/
├── AndroidManifest.xml
├── assets/content/              ← TODO o conteúdo do app (JSON)
│   ├── games.json               ← lista de jogos
│   └── games/
│       ├── dandys-world.json
│       ├── brawl-stars.json
│       ├── free-fire.json
│       └── murder-mystery.json
├── java/com/guiadejogos/app/
│   ├── MainActivity.kt
│   ├── GuiaDeJogosApp.kt        ← cria dependências compartilhadas
│   ├── config/AppConfig.kt      ← configurações (URL de conteúdo, larguras)
│   ├── data/
│   │   ├── model/Models.kt      ← jogos, categorias, blocos de conteúdo
│   │   └── repository/          ← leitura dos JSON + atualização online
│   └── ui/
│       ├── theme/               ← cores e tipografia
│       ├── components/          ← cards, cabeçalhos, blocos de conteúdo
│       ├── screens/             ← Home, Jogos, Jogo, Categoria
│       └── navigation/          ← rotas
└── res/                         ← ícone, tema, strings
docs/
├── ESTRUTURA_CONTEUDO.md        ← formato dos JSON
└── ATUALIZAR_CONTEUDO.md        ← atualizar sem gerar APK novo
```

## ➕ Adicionar conteúdo (sem mexer em código)

- **Novo código:** edite `games/dandys-world.json` → categoria `codigos` → bloco `codes`.
- **Nova categoria:** adicione um objeto em `categories` do jogo.
- **Novo jogo:** crie `games/<id>.json` e registre em `games.json`.
- Sempre que mudar conteúdo, **aumente `contentVersion`** em `games.json`.

Detalhes e exemplos: [`docs/ESTRUTURA_CONTEUDO.md`](docs/ESTRUTURA_CONTEUDO.md).

## 🔄 Atualização de conteúdo pela internet

O app vem com o conteúdo embutido e funciona offline. O botão **↻** na tela de jogos baixa uma versão mais nova dos JSON, **sem precisar instalar outro APK**. Veja [`docs/ATUALIZAR_CONTEUDO.md`](docs/ATUALIZAR_CONTEUDO.md) (importante: com o repositório **privado**, essa função precisa de um endereço público para os JSON).

## 📌 Regras de conteúdo

- **Nada é inventado.** Códigos e informações vêm de fontes oficiais ou de sites especializados reconhecidos, e cada categoria mostra a data da última revisão.
- Códigos têm status: **ATIVO**, **EXPIRADO** ou **NÃO VERIFICADO**.
- Links são marcados como **OFICIAL** ou **COMUNIDADE**.

## 🚀 Próximos passos (a arquitetura já suporta)

Busca · Favoritos · Notícias · Imagens e vídeos · Notificações de novos códigos · Conteúdo completo de Brawl Stars, Free Fire e Murder Mystery.
