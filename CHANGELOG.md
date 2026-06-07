# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/)
(`MAJOR.MINOR.PATCH`, com sufixos de pré-lançamento `-alpha.N` / `-beta.N`).

## [Unreleased]

_Sem mudanças não lançadas._

## [1.0.0-alpha.2] - 2026-06-07

Segundo alpha — badge de sync por desenho, toolbar flutuante, e a base de
qualidade (testes + CI).

### Added
- **Badge de nuvem por desenho** nas galerias My draws e Favorites: mostra o
  status de sincronização com o Google Photos de cada desenho — **âmbar**
  (pendente/na fila), **vermelho** (falhou) ou **verde** (sincronizado). Some
  quando o desenho nunca foi baixado ou o usuário está deslogado.
- Vínculo fila↔desenho: itens da `SyncQueue` carregam o `drawingId`, e um
  conjunto persistente de ids já sincronizados (`synced_ids.json`) mantém o
  badge correto após o upload (que remove o item da fila).
- `DrawingViewModel.syncStatusFor(id)` e `commitCurrentDrawingForDownload()`
  (o Download no canvas agora salva o desenho em My draws para vincular o badge).
- Ícones `IconCloudDone` / `IconCloudUpload` / `IconCloudOff` e o componente
  `CloudStatusBadge`.
- **Infraestrutura de testes unitários**: JUnit4 + Robolectric + coroutines-test.
  Primeira leva de testes (10) cobrindo persistência (`DrawingStore`,
  `SyncQueue`), navegação da galeria, expiração do Trash e `syncStatusFor`.
  Rodar com `./gradlew :app:testDebugUnitTest`.
- **CI (GitHub Actions)**: workflow `tests` roda os testes unitários em todo
  push na `main` e em pull requests.
- **Seam de teste do uploader**: interface `PhotoUploader` + `UploadResult`
  (top-level); `GooglePhotosUploader` agora a implementa. `DrawingViewModel`
  recebe o uploader e o `ioDispatcher` por injeção (`@JvmOverloads`, defaults de
  produção). Permite testar o processamento da fila de sync com um fake, sem
  rede/auth/device. +3 testes (sucesso limpa a fila e marca synced; falha mantém
  e marca failed; needs-consent para o lote).

### Changed
- **Toolbar do canvas** virou um **botão flutuante único e arrastável**: por
  padrão fica recolhido (um círculo com a cor atual), pode ser posicionado em
  qualquer lugar da tela e mantém a posição. **Tap** expande no arco de 3 botões
  (espessura · cor · ações); **tap fora** recolhe. Libera a tela pequena e tira o
  toolbar de cima do desenho. Arraste fluido (delta por evento, posição lida na
  fase de layout). Ícone de espessura agora é uma **linha horizontal** (reflete o
  pincel atual). Posição em `DrawingViewModel.fabOffset`.

## [1.0.0-alpha.1] - 2026-06-06

Primeiro lançamento público (**alpha**) — app de desenho Wear OS completo, com
login Google e **sincronização para o Google Photos**.

### Added
- **Sincronização com o Google Photos** (logado): ao tocar **Download**, o
  desenho é salvo na galeria local **e** enviado para o **Google Photos do
  próprio usuário** (escopo `photoslibrary.appendonly`), aparecendo no app Fotos
  do celular. Deslogado = só local (nada sai do dispositivo).
- `GooglePhotosUploader`: upload via REST (`/v1/uploads` →
  `/v1/mediaItems:batchCreate`) usando `GoogleAuthUtil` + `HttpURLConnection` +
  `org.json` (sem dependência nova).
- `SyncQueue`: fila offline **persistente** (PNGs em disco + índice JSON em
  `filesDir`) — sobrevive a restart e funciona offline. Estados PENDING /
  UPLOADING / SYNCED / FAILED.
- **Auto-retry** do sync: processa a fila no init do app, no login e ao tocar
  Download (para no 1º erro de auth).
- **Botão "Sync Now"** funcional no Profile: contagem de pendentes, **spinner**
  durante o upload (não-clicável), estado **desabilitado** quando vazio, e linha
  de status (`Syncing N/M`, `All synced ✓`, `N synced · M failed`).
- `downloadDrawing()`: helper central do Download (salva local + enfileira sync),
  usado nos 3 pontos (Canvas, My draws, Favorites).
- `AuthManager`: escopo `photoslibrary.appendonly` + `account()` /
  `hasPhotosScope()`.
- Documentos de projeto: `docs/roadmap-sync-monetization.md` (plano de sync +
  monetização), `CLAUDE.md` (instruções/hábitos do agente), `CONTRIBUTING.md`
  (processo, versionamento, releases) e o relatório vivo no Notion.
- Preparação para a **Play Store**: assinatura de release via **upload key**
  (`signingConfigs.release` lê `keystore.properties`, ambos os segredos —
  `keystore.properties` e `*.jks` — gitignored). Gera `.aab` assinado com
  `:app:bundleRelease`.
- **Política de Privacidade** (`docs/privacy-policy.html`, PT-BR) para publicar
  via GitHub Pages — exigida pelo Google por causa do login com conta.
- Skill `instalacao`: nova seção "Publicar na Play Store" (upload key, AAB,
  SHA-1 de upload + App Signing, URL da política, form factor Wear OS).
- **Settings** com **Login com Google** e **Reset All**. O botão é o oficial
  "Sign in with Google" (pill branca + logo G de 4 cores desenhado via vetor),
  conforme as diretrizes de marca do Google. Login via `play-services-auth`
  (`GoogleSignIn`), com o seletor de conta nativo do relógio. A sessão é
  persistida pelo Play services (`getLastSignedInAccount`). Quando logado, o
  Settings abre o **Profile**: foto (carregada com Coil), "Hello, {nome}",
  e-mail, **Sync Now** (sincroniza com o Google Photos — ver seção de sync),
  **Reset All** e **Logout**. **Reset All** abre a confirmação **"Caution / This action is
  irreversible"** (Cancel / Delete All); ao confirmar, apaga My draws, Favorites
  e Trash (memória + JSON) e mostra **"All files have been removed."**.
- `AuthManager` (camada de dados): encapsula `GoogleSignInOptions` com
  `requestIdToken(default_web_client_id)`, login, perfil e logout.
- `UserProfile` (nome, e-mail, foto) exposto pelo `DrawingViewModel`.
- Telas `SettingsScreen`, `ProfileScreen`, `ResetConfirmScreen`,
  `ResetSuccessScreen` e o componente `GoogleSignInButton`.
- Integração Firebase: plugin `google-services` + `app/google-services.json`,
  permissão `INTERNET`, regras ProGuard para o Play services auth.
- Dependências: `com.google.android.gms:play-services-auth` 21.2.0,
  `io.coil-kt:coil-compose` 2.7.0.
- **My draws** (galeria de desenhos): os desenhos do usuário são salvos
  automaticamente ao sair do Draw pelo **X** (novo, ou atualiza se veio de Edit)
  e persistidos em disco (JSON em `filesDir`, via `DrawingStore`). A galeria
  mostra um desenho por vez com uma **coroa segmentada** no bezel (1 segmento por
  desenho, atual destacado). Navegação: **girar o dedo** (horário = próximo,
  anti-horário = anterior) **ou arrastar na vertical** (cima = próximo, baixo =
  anterior). Controles: **Delete** (move para a lixeira), **Download** (salva PNG
  na galeria) e **Edit** (reabre para editar). **Voltar** (gesto de back ou botão
  físico) retorna à Home. Limite de **100 desenhos** (descarta o mais antigo). O
  `DrawingViewModel` virou `AndroidViewModel` para persistência.
- **Tela Trash** (lixeira): galeria de desenhos deletados com navegação idêntica
  à My draws (girar/arrastar com tick háptico). Controles: **Delete permanente**
  (abre `DeleteConfirmMenu`), **Restore** (devolve para My draws) e **Edit**
  (reabre para editar como novo desenho). Desenhos expiram automaticamente após
  **30 dias** na lixeira (limpeza automática no `init` do ViewModel).
- **Tela Favorites** (favoritos): galeria de desenhos marcados como favoritos,
  idêntica à My draws na navegação (girar/arrastar com tick háptico). Controles
  em arco de 4 botões: **Trash** (envia para lixeira), **Desmarcar** (coração
  preenchido → remove dos favoritos e move o desenho de volta para My draws com
  feedback `hapticSuccess` e overlay "Unfavorited"), **Download** e **Edit**.
- `DeleteConfirmMenu`: modal de confirmação para exclusão permanente de um item
  da lixeira (card vermelho + botão X para cancelar).
- Novos ícones em `MenuIcons.kt`: `IconDeletePermanent` (lixeira), `IconRestore`
  (seta circular de restaurar), `IconX` (X de fechar) e `IconEdit` (lápis).
- Novas funções hápticas em `Haptics.kt`: `hapticScrollTick` (tick curto/leve
  para navegação na galeria), `hapticSuccess` (pulso duplo para restore/clear) e
  `hapticWarning` (pulso longo para ações de delete).
- **Tela Home** (Figma node 144969:299): menu vertical rolável com 5 botões
  (New draw, My draws, Favorites, Trash, Settings), ícones a partir dos SVGs
  exatos do Figma. O app agora **inicia na Home**. **New draw**, **My draws**,
  **Favorites** e **Trash** são funcionais; Settings exibe Toast de placeholder.
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
- **Política de Privacidade** e `docs/index.html` traduzidos de PT-BR para
  **inglês**, com nova seção cobrindo o upload para o Google Photos (escopo
  `appendonly`, dados no Photos do próprio usuário, fila offline, deslogado não
  envia nada).
- `downloadDrawing()` passou a **enfileirar** o upload via `DrawingViewModel`
  (com persistência) em vez de subir direto — robusto a offline.
- `versionName` para `1.0.0-alpha.1`.
- **Ícone do app** atualizado: a "onda" agora usa o gradiente colorido da paleta
  (vermelho→laranja→verde→azul) sobre fundo preto, no lugar do traço branco.
  Vetorial (gradiente no `strokeColor`), aplicado via adaptive icon — vale para
  Galaxy Watch e Wear OS round. Combina com o ícone e o feature graphic da loja.
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
- Paleta de cores atualizada com as **6 cores corretas** do Color Selector do
  Figma (node 145007:380, grid 2×3, da esquerda para direita por linha):
  Vermelho `#DA0505`, Azul `#128AE6`, Laranja-avermelhado `#ED3F1C`,
  Verde `#14AA60`, Laranja `#FF9914`, Branco/Cinza `#E6E6E6`.
  (Substitui a versão anterior com teal, magenta e branco puro.)
- `renderDrawingBitmap` refatorado como `renderPathsBitmap(paths, size)` para
  ser reutilizável nas galerias My draws e Trash sem depender do ViewModel.
- `drawSmoothPath` alterado de `private` para `internal` para ser acessível
  em `MyDrawsScreen` e `TrashScreen`.
- Botões **My draws**, **Favorites** e **Trash** na Home agora funcionais;
  exibem Toast informativo quando a lista está vazia.
- `Drawing` agora tem campo `isFavorite: Boolean` (padrão `false`); persistido
  em `my_draws.json`. Favoritos e desenhos regulares compartilham o mesmo arquivo.
- My draws agora exibe apenas desenhos **não-favoritos**, ordenados por
  `createdAt` decrescente (mais recente primeiro).
- My draws ganha botão de **favoritar** (coração vazio) no arco de controles,
  agora com 4 botões: Trash | Favoritar | Download | Edit. Ao favoritar, o
  desenho sai de My draws e aparece em Favorites.
- Fechar o menu de ações pelo **X** agora salva o desenho automaticamente antes
  de retornar à Home (via `exitToHome()`).
- Vibração háptica `hapticWarning` adicionada ao confirmar limpeza do canvas.

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
