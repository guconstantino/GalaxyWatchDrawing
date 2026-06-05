# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added
- **Tela Home** (Figma node 144969:299): menu vertical rolável com 5 botões
  (New draw, My draws, Favorites, Trash, Settings), ícones a partir dos SVGs
  exatos do Figma. O app agora **inicia na Home**. Por enquanto só **New draw**
  está funcional — abre um desenho novo (limpa o canvas → `viewModel.newDrawing()`);
  os demais são placeholders para implementar feature a feature.
- Navegação Home↔Draw: no menu de ações, o **X volta para a Home** e **tocar
  fora (scrim) volta para o desenho**.
- Skill `instalacao` (`.claude/skills/instalacao`) com guia de setup do repo,
  bibliotecas necessárias e manutenção de changelog/releases.
- Este `CHANGELOG.md`.
- Menu de ações com exportação do desenho: **Compartilhar** (gera PNG e abre o
  share sheet via `FileProvider`) e **Salvar na galeria** (MediaStore,
  `Pictures/WatchDraw`). Rasterização do canvas em `DrawingExport.kt`.
- Gestos multitoque no canvas: toque com dois dedos = **desfazer**, toque duplo
  com dois dedos = **refazer** (pilha de redo no `DrawingViewModel`).
- Vibração háptica sutil ao abrir qualquer menu (via `LocalHapticFeedback`,
  respeita as configurações de háptico do sistema; sem permissão extra).
- Feedback háptico nos gestos de dois dedos: **um tick curto** ao desfazer e
  **dois ticks rápidos** ao refazer (toque duplo). Usa `Vibrator`/waveform
  (`Haptics.kt`); requer permissão `VIBRATE`.

### Changed
- Gradle wrapper atualizado de 8.9 para 9.4.1 para compatibilizar com as flags
  `--jvm-version`/`--jvm-vendor` que o Android Studio mais novo passa para a
  task `updateDaemonJvm` (corrige "Unknown command-line option '--jvm-vendor'"
  no sync).
- AGP 8.5.2 → 9.2.1 e Kotlin 2.0.0 → 2.2.10 (via AGP Upgrade Assistant), que
  também adicionou o foojay-resolver (`settings.gradle.kts`) e flags de
  compatibilidade em `gradle.properties`.
- Barra de ferramentas do canvas refeita para ser fiel ao protótipo Figma
  (M3 Wear OS Apps Design Kit): os 3 botões agora seguem um arco voltado para
  baixo acompanhando o bezel redondo, em círculos `surface-container` (#332E3C)
  com alvo de toque de 48dp. Botão esquerdo = espessura, botão central
  (destaque) = cor atual como disco colorido, botão direito = ações (3 pontos).
- Os menus (espessura, cor, ações, confirmar limpeza) agora abrem como **modais
  centralizados sobre o canvas** (card `surface-card` + scrim), em vez de telas
  cheias separadas — fiel ao fluxo do protótipo. Tamanhos dos cards ajustados
  para caber no mostrador redondo.
- Menu de ações refeito conforme o node Figma 145007:371: card
  `surface-container`, grade 2×2 de botões `primary-container` (#4D3D76, círculo
  32dp / toque 48dp), ícones desenhados a partir dos SVGs exatos do Figma via
  `PathParser`. Itens: Limpar (abre confirmação), Download (salva o PNG na
  galeria), Coração (apenas alterna preenchido/outline, sem ação ainda) e
  Fechar. Compartilhar removido do menu.
- Seletor de espessura conforme o node Figma 145007:387 (tema cinza): **3
  opções** (grosso/médio/fino) em linhas tocáveis de 32dp **sem fill** (planas,
  fiéis ao tema cinza), só com a linha de preview (24×8/4/2dp, #E2E2E2) dentro de
  um card `surface-container`. `StrokeWidths` reduzido para 3 valores.
- UI migrada para o **tema cinza (neutro)** do design kit, substituindo o roxo
  do tema padrão: `surface-container` #332E3C → **#303131**, `primary-container`
  #4D3D76 → **#454747**, linha do seletor de espessura #E9DDFF → **#E2E2E2**, e a
  pill "Clear Canvas" deixou de ter tom roxo. Afeta menus, botões de ação e o
  seletor de espessura.
- Paleta de desenho fixada nas **6 cores exatas** do Color Selector do Figma
  (node 145007:380): vermelho #F21B3F, laranja #FF9914, teal #08BDBD, verde
  #29BF12, magenta #FF14B1, branco #FFFFFF. Não é possível desenhar com nenhuma
  cor fora dessa paleta. `ColorMenu` alinhado ao box model do node (card
  `surface-container`, grade 2×3, círculos 24dp, gap 6, padding 12, radius 26).

## [0.1.0] - 2026-06-05

### Added
- App de desenho na tela para Galaxy Watch 8 (Wear OS 4) em Kotlin + Jetpack
  Compose.
- Canvas de desenho com curvas suaves (bezier), cor e espessura configuráveis.
- Seletor de cores (8 cores) e seletor de espessura de traço (4 tamanhos).
- Tela de ações: desfazer (undo) e limpar canvas com confirmação.

### Changed
- Substituídas APIs instáveis do Wear Compose (`SwipeToDismissBox`,
  `material.icons`, `CompactButtonDefaults`) por primitivos estáveis
  (`Box` clicável + `Text`/símbolos), corrigindo a compilação.
