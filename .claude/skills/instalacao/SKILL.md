---
name: instalacao
description: >-
  Guia de instalação e setup do projeto GalaxyWatchDrawing (app Wear OS de
  desenho). Use SEMPRE que o usuário pedir para "instalar o projeto",
  "configurar o ambiente", "rodar pela primeira vez", "clonar o repo",
  "instalar as dependências/bibliotecas", "configurar em outro computador",
  ou quando perguntar como registrar mudanças / atualizar o changelog /
  criar um release no git. Cobre: clone do repositório, ferramentas e
  bibliotecas necessárias, build/run no Galaxy Watch, e manutenção do
  CHANGELOG.md + GitHub Releases.
---

# Instalação — GalaxyWatchDrawing

App de desenho na tela para **Galaxy Watch 8 (Wear OS 4)**, escrito em
**Kotlin + Jetpack Compose (Wear Compose Material3)**.

Use este guia para: (1) instalar e rodar o projeto do zero, (2) entender as
dependências, (3) registrar mudanças no git (CHANGELOG + Releases).

---

## 1. Clonar o repositório

```bash
git clone https://github.com/guconstantino/GalaxyWatchDrawing.git
cd GalaxyWatchDrawing
```

> Em outro computador, garanta que o `git` tenha credenciais do GitHub. Se o
> push falhar por autenticação, use um Personal Access Token (escopo `repo`)
> ou `gh auth login`.

---

## 2. Ferramentas necessárias (uma vez por máquina)

| Ferramenta | Versão | Observação |
|---|---|---|
| **Android Studio** | Ladybug+ (2024.2+) | Inclui JDK 17 e o Android SDK |
| **JDK** | 17 (bundled) | Vem com o Android Studio; não precisa instalar à parte |
| **Android SDK Platform** | API 35 (compileSdk) | Instale pelo SDK Manager |
| **minSdk** | 30 (Wear OS 4) | — |
| **Gradle** | 8.9 | Baixado automaticamente pelo gradle-wrapper |

Passos:

1. Instale o Android Studio: <https://developer.android.com/studio>
2. `File → Open` → selecione a pasta clonada.
3. Aguarde o **Gradle sync** (baixa Gradle 8.9 + dependências). Pode levar
   alguns minutos no primeiro open.

---

## 3. Bibliotecas do projeto

Declaradas em `gradle/libs.versions.toml` e resolvidas pelo Gradle — **não há
instalação manual**, o sync baixa tudo. Principais:

- `androidx.wear.compose:compose-foundation` — primitivos Wear (canvas, gestos)
- `androidx.wear.compose:compose-material3` — `Text`, tema do relógio
- `androidx.activity:activity-compose` — `setContent`
- `androidx.lifecycle:lifecycle-viewmodel-compose` — `viewModel()`
- `androidx.core:core-ktx`

> **Evite** `androidx.compose.material.icons.*`, `CompactButtonDefaults` e
> `SwipeToDismissBox` — APIs instáveis nesta versão do Wear Compose. O projeto
> usa `Box` clicável + `Text`/símbolos no lugar (ver `presentation/`).

---

## 4. Rodar no Galaxy Watch 8

1. No relógio: **Configurações → Opções do desenvolvedor → Depuração sem fio**
   (ative o Developer Mode tocando 7× no número da build, se necessário).
2. No Android Studio: **Pair Devices Using Wi-Fi** → parear com QR/código.
3. Selecione o device **SM-L320** + módulo **app** no topo.
4. **▶ Run**.

> ⚠️ O **espelhamento da tela do watch falha** no Android Studio
> ("No video encoder for video/x-vnd.on2.vp8" / exit code 30). É limitação do
> Wear OS, **não** é erro do projeto. Teste olhando direto para o relógio — o
> app instala e roda normalmente via ADB.

---

## 5. Registrar mudanças no git

Sempre que terminar uma alteração relevante, faça **as duas coisas**:

### 5a. Atualizar o `CHANGELOG.md`

Segue o padrão [Keep a Changelog](https://keepachangelog.com/). Adicione a
entrada na seção `[Unreleased]` sob a categoria certa (`Added`, `Changed`,
`Fixed`, `Removed`):

```markdown
## [Unreleased]
### Fixed
- Descrição curta da correção
```

Ao publicar uma versão, renomeie `[Unreleased]` para `[x.y.z] - AAAA-MM-DD` e
abra uma nova `[Unreleased]` vazia no topo.

### 5b. Criar um GitHub Release (em versões marcadas)

Use o `gh` CLI. A tag deve casar com a versão do CHANGELOG:

```bash
gh release create v0.1.0 \
  --title "v0.1.0" \
  --notes "Cole aqui as entradas dessa versão do CHANGELOG.md"
```

> Neste ambiente macOS, o `gh` pode precisar do token via env var:
> `GH_TOKEN=$(printf "protocol=https\nhost=github.com\n\n" | git credential fill 2>/dev/null | grep ^password= | cut -d= -f2-) gh release create ...`

### 5c. Commit & push

```bash
git add -A
git commit -m "feat/fix/docs: descrição"
git push origin main
```

Mensagem de commit deve terminar com:
```
Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
```
