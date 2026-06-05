# GalaxyWatchDrawing

App de **desenho na tela** para **Galaxy Watch / Wear OS**, escrito em **Kotlin +
Jetpack Compose** (Wear Compose Material3). A UI segue o
[M3 Wear OS Apps Design Kit](https://www.figma.com/community) (Figma).

Desenhe com o dedo no mostrador redondo, escolha cor e espessura, desfaça/refaça
com gestos de dois dedos e salve ou compartilhe o resultado.

---

## ✨ Funcionalidades

- **Canvas de desenho** com traços suaves (curvas de Bézier), cor e espessura
  configuráveis.
- **Controles em arco** na parte de baixo, acompanhando o bezel redondo:
  - **Espessura** (botão esquerdo) — 3 opções: fino / médio / grosso.
  - **Cor** (botão central, destacado) — mostra a cor atual.
  - **Ações** (botão direito) — menu 2×2.
- **Menus modais centralizados** sobre o canvas (não telas cheias), com **scrim**
  e **vibração háptica sutil** ao abrir.
- **Menu de ações (2×2):**
  - 🗑️ **Limpar** — abre confirmação "Clear Canvas".
  - ⬇️ **Download** — salva o desenho como **PNG na galeria** (`Pictures/WatchDraw`).
  - 🤍 **Coração** — alterna preenchido/outline (placeholder, sem ação ainda).
  - ✕ **Fechar**.
- **Gestos multitoque:**
  - Toque com **dois dedos** → **desfazer** (1 vibração curta).
  - **Toque duplo** com dois dedos → **refazer** (2 vibrações rápidas).
- **Paleta fixa de 6 cores** (vermelho, laranja, teal, verde, magenta, branco) —
  exatamente as do Color Selector do design.

> Os ícones do menu de ações são desenhados a partir dos **SVGs exatos do Figma**
> via `PathParser`, mantendo fidelidade ao protótipo.

---

## 🛠️ Stack

| Item | Versão |
|---|---|
| Linguagem | Kotlin 2.2.10 |
| UI | Jetpack Compose · Wear Compose Material3 |
| AGP | 9.2.1 |
| Gradle | 9.4.1 (wrapper) |
| compileSdk / targetSdk | 35 |
| minSdk | 30 (Wear OS 4) |
| JDK | 11 (toolchain) — build roda com o JBR do Android Studio (21) |

---

## 🚀 Build & Run

1. **Android Studio** (Ladybug+ / 2024.2+). O JDK embutido (JBR 21) é usado para
   rodar o Gradle.
2. `File → Open` e selecione a pasta do projeto. Aguarde o **Gradle sync** (baixa
   o Gradle 9.4.1 e as dependências no primeiro open).
3. **Parear o Galaxy Watch:**
   - No relógio: *Configurações → Opções do desenvolvedor → Depuração sem fio*.
   - No Studio: *Pair Devices Using Wi-Fi* → parear via QR/código.
4. Selecione o device + módulo **app** e clique em **▶ Run**.

### Linha de comando

```bash
./gradlew :app:assembleDebug
```

> No Windows, se o Gradle pegar um JDK 8 do `PATH`, aponte para o JBR do Studio:
> `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:assembleDebug`

> ⚠️ O **espelhamento da tela do watch** falha no Android Studio
> (`No video encoder for video/x-vnd.on2.vp8`, exit code 30). É limitação do
> Wear OS, **não** é erro do projeto — teste olhando direto para o relógio.

---

## 📁 Estrutura

```
app/src/main/java/com/guconstantino/watchdraw/
├── MainActivity.kt                  # host Compose
├── data/
│   ├── DrawingModels.kt             # DrawnPath, paleta de cores, espessuras, AppScreen
│   └── DrawingViewModel.kt          # estado: paths, cor, espessura, undo/redo, favorite
└── presentation/
    ├── DrawingCanvas.kt             # canvas + controles em arco + gestos
    ├── Overlays.kt                  # menus modais (espessura, cor, ações, confirmar)
    ├── MenuIcons.kt                 # ícones vetoriais (SVG exato via PathParser)
    ├── DrawingExport.kt             # rasteriza o desenho → salvar/compartilhar PNG
    ├── Haptics.kt                   # vibrações (undo/redo)
    ├── DesignTokens.kt              # cores do protótipo Figma
    └── theme/Theme.kt
```

---

## 🎨 Design

Baseado no **M3 Wear OS Apps Design Kit (Community)** no Figma. Tokens principais:
`surface-container` `#332E3C`, `primary-container` `#4D3D76`,
`primary` `#E9DDFF`, laranja `#FF9914`.

Paleta de desenho (Color Selector):
`#F21B3F` · `#FF9914` · `#08BDBD` · `#29BF12` · `#FF14B1` · `#FFFFFF`.

---

## 📋 Changelog

Veja [CHANGELOG.md](CHANGELOG.md).
