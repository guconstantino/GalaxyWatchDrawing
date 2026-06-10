# WatchDraw — desenho no Galaxy Watch

App de **desenho standalone** para **Wear OS** (Galaxy Watch 4+), escrito em
**Kotlin + Jetpack Compose** (Wear Compose Material3). Desenhe direto no mostrador
redondo do relógio — sem precisar do celular — guarde numa galeria, favorite,
exporte como imagem e, opcionalmente, **sincronize seus desenhos com o seu próprio
Google Photos**.

| | |
|---|---|
| **Package** | `com.guconstantino.watchdraw` |
| **Versão** | `1.0.0-alpha.3` (versionCode 3) |
| **Plataforma** | Wear OS — Galaxy Watch 4/5/6/7/8 (minSdk 30) |
| **Distribuição** | Google Play Store |
| **Site / Política** | https://watchdraw.gustavoconstantino.com |

> A Galaxy Store foi descartada (o canal Wear dela é China-only e o app usa GMS).
> Detalhes em [`CONTRIBUTING.md`](CONTRIBUTING.md).

---

## ✨ Funcionalidades

### Desenho
- **Canvas** com traços suaves e precisos: **spline Catmull-Rom** (a linha passa
  pelos pontos reais do toque), **filtro 1€ (One-Euro)** para suavizar o input e
  **samples sub-frame** (`historical`) para capturar o caminho entre frames.
- **Paleta de 6 cores** + **3 espessuras** de pincel.
- **Botão de ferramenta flutuante e arrastável**: um único botão expansível no
  canvas (cor / espessura / ações), que pode ser reposicionado com o dedo.
- **Undo / redo por gestos** de dois dedos, com **retorno háptico**.
- **Exportar:** salva o desenho como **PNG** (galeria local) e permite
  **compartilhar**. A exportação usa a mesma geometria da tela (WYSIWYG).

### Galerias e organização
- **My draws** — galeria dos desenhos salvos (ordenada por data; limite de 100,
  descarta o mais antigo).
- **Favorites** — marque os desenhos preferidos.
- **Trash** — lixeira com **restauração**; itens expiram em **30 dias**.
- Persistência em **JSON no `filesDir`** (sem banco; usa `org.json`).

### Conta Google + sincronização (opcional)
- **Sign in with Google** (`play-services-auth`) nas Configurações; tela de Profile
  com foto, nome e e-mail.
- **Sync com o Google Photos do próprio usuário** (escopo sensível
  `photoslibrary.appendonly`): ao tocar **Download** logado, o desenho é enviado
  para a biblioteca do usuário e aparece no celular. **O app só adiciona imagens
  que ele cria** — nunca lê/lista a biblioteca existente. Sem servidor próprio.
- **Deslogado = só local** (nada sai do relógio).
- **Fila offline persistente** (`SyncQueue`): PNGs em disco + índice JSON;
  sobrevive a restart, com **auto-retry** e um botão **"Sync Now"** (contagem,
  spinner durante o envio, desabilitado quando não há pendências).
- **Badge de nuvem por desenho** nas galerias (status de sync).

---

## 🛠️ Stack

| Item | Versão |
|---|---|
| Linguagem | Kotlin 2.2.10 |
| UI | Jetpack Compose · Wear Compose Material3 (`1.0.0-alpha24`) · Wear Compose 1.4.0 |
| AGP | 9.2.1 |
| Gradle (wrapper) | 9.4.1 |
| compileSdk / targetSdk | 35 |
| minSdk | 30 (Wear OS 4) |
| Auth | play-services-auth 21.2.0 · Google Photos REST (`HttpURLConnection` + `org.json`) |
| Imagens | Coil 2.7.0 |
| Testes | JUnit 4 · Robolectric 4.14.1 · kotlinx-coroutines-test 1.8.1 |
| JDK | 11 (toolchain) — build roda com o JBR do Android Studio |

---

## 📦 Flavors (importante)

O projeto tem **dois product flavors** do **mesmo app** (mesmo `applicationId`,
então login/sync funcionam nos dois):

- **`watch`** — o produto real (publicado na Play Store).
- **`phone`** — um **harness dev-only**: roda a **mesma UI do relógio** dentro de
  um mostrador redondo redimensionável (zoom 150–400dp) num celular, com um botão
  que simula o "voltar" físico. Serve para iterar a UI rápido. **Nunca é publicado.**

Por isso as tasks do Gradle são por flavor (ex.: `assembleWatchDebug`, não
`assembleDebug`).

---

## 🚀 Build & Run

1. **Android Studio** (Ladybug+ / 2024.2+). O JDK embutido (JBR) roda o Gradle.
2. `File → Open` na pasta do projeto e aguarde o **Gradle sync** (baixa o Gradle
   9.4.1 e as dependências no primeiro open).
3. **Parear o Galaxy Watch:** no relógio, *Configurações → Opções do desenvolvedor
   → Depuração sem fio*; no Studio, *Pair Devices Using Wi-Fi*.
4. Selecione o device + a variante **`watchDebug`** e clique em **▶ Run**.

### Linha de comando

```bash
# JAVA_HOME (terminal sem Java): use o JBR do Android Studio
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# App real (flavor watch)
./gradlew :app:assembleWatchDebug        # APK debug
./gradlew :app:assembleWatchRelease      # APK release (assinado, requer keystore)
./gradlew :app:bundleWatchRelease        # AAB para a Play Store

# Harness de celular (dev-only)
./gradlew :app:assemblePhoneRelease

# Testes unitários (e o que a CI roda)
./gradlew :app:testWatchDebugUnitTest
```

> Release assinado precisa de `keystore.properties` + `upload-keystore.jks` na
> raiz (ambos **gitignored**; restaurar do backup).

> ⚠️ O **espelhamento da tela do watch** pode falhar no Android Studio
> (`No video encoder for video/x-vnd.on2.vp8`). É limitação do Wear OS — teste
> olhando direto para o relógio, ou use o **flavor `phone`** para iterar a UI.

---

## ✅ Testes & CI

- **Filosofia:** núcleo funcional testável — lógica de sync, persistência e
  geometria cobertas por testes JVM rápidos; UI fica para o teste manual no relógio.
- **Rodar:** `./gradlew :app:testWatchDebugUnitTest` (relatório em
  `app/build/reports/tests/`).
- **Cobertura atual** (`app/src/test/...`): `DrawingStoreTest`, `SyncQueueTest`,
  `SyncProcessingTest`, `DrawingViewModelTest`, `OneEuroFilterTest`,
  `StrokeGeometryTest`. O seam `PhotoUploader` permite testar a fila de sync sem rede.
- **CI:** GitHub Actions (`.github/workflows/tests.yml`) roda os testes em todo
  push na `main` e PRs. **CI verde é pré-requisito de merge.**

---

## 📁 Estrutura

```
app/src/main/java/com/guconstantino/watchdraw/
├── MainActivity.kt                  # host Compose (launcher do flavor watch)
├── data/
│   ├── DrawingModels.kt             # DrawnPath, Drawing, paleta, espessuras, AppScreen, SyncState
│   ├── DrawingViewModel.kt          # estado central: canvas, undo/redo, galerias, conta, fila de sync
│   ├── DrawingStore.kt              # persistência JSON dos desenhos
│   ├── AuthManager.kt               # wrapper do Google Sign-In (email, profile, photoslibrary.appendonly)
│   ├── PhotoUploader.kt             # interface (seam) de upload + UploadResult
│   ├── GooglePhotosUploader.kt      # upload REST p/ Google Photos (uploads + mediaItems:batchCreate)
│   ├── SyncQueue.kt                 # fila offline persistente (PNG em disco + índice JSON)
│   └── OneEuroFilter.kt             # filtro 1€ para suavizar o traço
└── presentation/
    ├── DrawingCanvas.kt             # canvas + botão de ferramenta flutuante + gestos
    ├── StrokeGeometry.kt            # Catmull-Rom (curva que passa pelos pontos)
    ├── DrawingExport.kt             # rasteriza → salvar/compartilhar PNG + enfileira sync
    ├── HomeScreen / MyDrawsScreen / FavoritesScreen / TrashScreen / SettingsScreen
    ├── Overlays.kt                  # menus modais (espessura, cor, ações, confirmar)
    ├── SyncBadge.kt                 # badge de status de sync por desenho
    ├── MenuIcons.kt                 # ícones vetoriais (SVG exato via PathParser)
    ├── GoogleSignInButton.kt · Haptics.kt · DesignTokens.kt
    └── theme/Theme.kt

app/src/watch/   # manifesto do app real (uses-feature watch + MainActivity launcher)
app/src/phone/   # harness dev-only (PhonePreviewActivity)
app/src/test/    # testes JUnit + Robolectric
```

---

## 🎨 Design

Baseado no **M3 Wear OS Apps Design Kit (Community)** no Figma.

Paleta de desenho (Color Selector):
`#F21B3F` · `#FF9914` · `#08BDBD` · `#29BF12` · `#FF14B1` · `#FFFFFF`.

---

## 📚 Documentos

| Documento | Para quê |
|---|---|
| [`CHANGELOG.md`](CHANGELOG.md) | Histórico de mudanças por versão |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Versionamento, branches, release, build |
| [`CLAUDE.md`](CLAUDE.md) | Hábitos do projeto (CHANGELOG, SemVer, relatório) |
| [`docs/`](docs/) | Política de privacidade, OAuth/Data Safety, ficha da loja, roadmap |
