# Processo & organização — WatchDraw

Guia de como o projeto é versionado, lançado e mantido. Vale para qualquer pessoa
(ou agente) trabalhando no repositório.

## Versionamento (SemVer)

Seguimos **Versionamento Semântico**: `MAJOR.MINOR.PATCH`.

- **MAJOR** — mudanças incompatíveis / reescritas grandes.
- **MINOR** — novas features compatíveis.
- **PATCH** — correções de bug compatíveis.

**Pré-lançamentos** usam sufixo: `-alpha.N` → `-beta.N` → versão estável.

Exemplos: `1.0.0-alpha.1`, `1.0.0-alpha.2`, `1.0.0-beta.1`, `1.0.0`.

Regras:
- A **tag** do Git/GitHub é `v<versão>` (ex.: `v1.0.0-alpha.1`).
- O `versionName` em `app/build.gradle.kts` **deve bater** com a tag (sem o `v`).
- O `versionCode` (inteiro) **incrementa a cada build** enviado a uma loja —
  nunca repete nem regride.

## Disciplina de CHANGELOG

`CHANGELOG.md` segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).

- **Sempre** registre mudanças relevantes na seção `## [Unreleased]` enquanto
  trabalha (grupos `Added` / `Changed` / `Fixed` / `Removed`).
- No lançamento, renomeie `[Unreleased]` para `[<versão>] - AAAA-MM-DD` e crie
  uma nova `[Unreleased]` vazia no topo.

## Fluxo de branches

1. Cada feature/fix numa **branch** (`feature/...`, `fix/...`).
2. Compilar e **testar no Galaxy Watch real** antes do merge.
3. **Merge `--no-ff`** na `main` (preserva o histórico da feature).
4. Apagar a branch (local + remoto) depois do merge.

Commits seguem prefixos semânticos: `feat:`, `fix:`, `docs:`, `chore:`,
`refactor:`. Mensagens de commit terminam com a linha de co-autoria do agente.

## Processo de release

1. Garantir `main` verde (compila; testado no relógio).
2. Atualizar `CHANGELOG.md` (mover `[Unreleased]` → `[<versão>] - data`).
3. Atualizar `versionName` (e `versionCode` se for para loja).
4. Commit + push na `main`.
5. Criar a tag e a release no GitHub:
   ```bash
   gh release create v<versão> --target main \
     --title "WatchDraw v<versão> — <subtítulo>" \
     --notes-file <arquivo de notas> \
     [--prerelease]            # usar em alpha/beta
   ```
   - Alphas/betas **sempre** com `--prerelease`.
   - Opcional: anexar o APK de release como asset para sideload/teste.
6. Atualizar o **relatório do Notion** (ver `CLAUDE.md`).

## Build & assinatura

- Terminal sem Java: usar o JDK do Android Studio →
  `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`
- Release assinado precisa de `keystore.properties` + `upload-keystore.jks` na
  raiz (ambos **gitignored**; restaurar do backup). Gera APK com
  `:app:assembleRelease` e AAB com `:app:bundleRelease`.
- Instalar no Galaxy Watch real por cima (preserva dados) só funciona com APK de
  **release** (mesma assinatura): `adb -t <transport_id> install -r <apk>`.

## Distribuição

- Canal oficial: **Google Play Store**.
- A **Galaxy Store** não é viável para apps Wear OS (canal Wear é China-only e o
  app usa GMS). Ver detalhes no relatório do Notion.

## Documentos vivos

| Documento | Para quê |
|---|---|
| `CHANGELOG.md` | Histórico de mudanças por versão |
| `CONTRIBUTING.md` | Este guia de processo |
| `CLAUDE.md` | Instruções/hábitos do agente (inclui atualizar o Notion) |
| `docs/roadmap-sync-monetization.md` | Plano de sync + monetização |
| Notion | Relatório vivo do projeto (visão executiva) |
