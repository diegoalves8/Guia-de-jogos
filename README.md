# 🎮 Guia de Jogos

Aplicativo Android que reúne **personagens, skins, códigos, guias, dicas e criadores de conteúdo** de **Dandy's World, Brawl Stars, Free Fire, Murder Mystery 2 e Blox Fruits**, com painel e gráficos.

Feito em **Kotlin + Jetpack Compose (Material 3)**, com layout adaptável para **celulares, tablets** (ex.: Galaxy Tab S6 Lite) e **multimídia de carro** (ex.: BYD King, tela giratória de 12,8"), em retrato e paisagem.

---

## 🔄 Atualizar o app por dentro dele (novo)

Toda alteração enviada para a `main` gera automaticamente uma **Release** (`build-<número>`) com o `GuiaDeJogos.apk`.
No app, abra a aba **Atualizar** → **Baixar e instalar**. O Android pede confirmação (e, na primeira vez, permissão para "instalar apps desta fonte").

> ⚠️ O app só enxerga as Releases se o repositório for **público**. Com o repositório privado, a aba Atualizar mostra o aviso e o APK continua disponível pela aba Actions.
>
> 🔑 O app é assinado com uma chave fixa (`keystore/`). Ela precisa ser sempre a mesma para a atualização funcionar — não apague. Se o repositório ficar público, qualquer pessoa poderá ver essa chave; para um app pessoal isso é aceitável, mas para publicar na Play Store crie uma chave nova e guarde em segredo.

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

## 🚗 Instalar na multimídia do BYD

O app roda em Android 8 ou superior, sem Google Play Services e sem bibliotecas nativas, então funciona em centrais BYD DiLink (Android 9+) com instalação de apps liberada.

1. Baixe o `GuiaDeJogos.apk` (passos acima) no celular ou no PC.
2. Copie o APK para um **pendrive** (FAT32 ou exFAT).
3. Conecte o pendrive na USB do carro, abra o **gerenciador de arquivos** da multimídia e toque no APK para instalar.
4. Pelo navegador da multimídia também dá: entre no GitHub logado e baixe o APK direto.

O que muda no carro:
- **Tela giratória:** o layout se ajusta na hora ao girar, sem reiniciar o app.
- **Paisagem (tela larga e baixa):** tela inicial em duas colunas, cabeçalhos compactos e mais cards por linha.
- **Toque:** botões grandes e botão Voltar ampliado.

> ⚠️ Use o app com o carro parado ou pelo passageiro.

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
