# WatchDraw — instruções do projeto

App de desenho **standalone para Wear OS** (Galaxy Watch 4+). Kotlin + Jetpack
Compose (Wear Compose Material3). Package `com.guconstantino.watchdraw`.

## Hábitos obrigatórios

1. **CHANGELOG sempre.** Toda mudança relevante entra em `## [Unreleased]` no
   `CHANGELOG.md` (grupos Added/Changed/Fixed/Removed). No lançamento, mover para
   `[<versão>] - data`.
2. **Versionamento SemVer `MAJOR.MINOR.PATCH`** com pré-lançamentos
   `-alpha.N` / `-beta.N`. A tag é `v<versão>` e o `versionName` deve bater.
   Detalhes e processo de release em `CONTRIBUTING.md`.
3. **Relatório do Notion** (abaixo) atualizado a cada fase/merge na `main`.
4. **README sincronizado.** Ao commitar mudanças de **código/build** (`app/`,
   gradle), atualize o `README.md` para refletir features/stack/estrutura. Um hook
   (`.claude/hooks/readme-reminder.sh`, configurado em `.claude/settings.json`)
   **bloqueia o `git commit`** se houver código no stage sem o `README.md` junto.
   Quando o README realmente não precisar mudar, inclua **`[skip-readme]`** na
   mensagem do commit para liberar.

## Hábito: manter o relatório do Notion atualizado

Existe uma página de relatório do projeto no Notion:

- **Página:** WatchDraw — Relatório do Projeto (Wear OS)
- **ID:** `378158667997811e8e5bdac2b32bf436`
- **URL:** https://app.notion.com/p/378158667997811e8e5bdac2b32bf436

**Regra:** sempre que uma fase de trabalho for concluída e/ou um **merge na `main`**
acontecer, **atualize essa página** via Notion MCP (`notion-update-page` / `fetch`
para reler o conteúdo atual). Atualize: linha do tempo (novos commits/fases),
status atual, aprendizados/erros novos e próximos passos.

Antes de atualizar, leia o histórico real: `git log --oneline` e o estado do
working tree, para o relatório refletir os fatos.

> Observação: esta atualização exige o Notion MCP (não dá para automatizar por
> hook de shell). O agente deve fazê-la como parte do fluxo ao concluir fases.

## Build / instalação (notas úteis)

- Terminal sem Java: use o JDK do Android Studio →
  `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`
- Release assinado: precisa de `keystore.properties` + `upload-keystore.jks` na
  raiz (ambos gitignored; restaurar do backup zip).
- Instalar no Galaxy Watch real por cima (preserva dados) só funciona com APK de
  **release** (mesma assinatura): `adb -t <transport_id> install -r <apk>`.
- Padrão de trabalho: cada feature numa branch → testar no relógio real →
  merge `--no-ff` na main.

## Distribuição

- Canal oficial: **Google Play Store**. Galaxy Store foi descartada (canal Wear
  é China-only e o app usa GMS).
