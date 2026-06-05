# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added
- Skill `instalacao` (`.claude/skills/instalacao`) com guia de setup do repo,
  bibliotecas necessárias e manutenção de changelog/releases.
- Este `CHANGELOG.md`.

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
