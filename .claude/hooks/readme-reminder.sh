#!/usr/bin/env bash
# PreToolUse hook (matcher: Bash, if: Bash(git commit*)).
# Quando um `git commit` inclui mudanças de código/build (app/, gradle) mas o
# README.md NÃO está no stage, bloqueia o commit com um lembrete para atualizar
# o README — mantendo-o em sincronia com features/stack/estrutura.
#
# Escape: inclua [skip-readme] na mensagem do commit quando o README realmente
# não precisar mudar. Commits que não tocam código/build não disparam nada.
#
# Observação: `git commit -a` faz o stage no momento do commit, então este hook
# (que olha `git diff --cached`) não o intercepta — prefira `git add` explícito.

input="$(cat)"
cmd="$(printf '%s' "$input" | python3 -c 'import sys,json; print(json.load(sys.stdin).get("tool_input",{}).get("command",""))' 2>/dev/null)"

# Só age em git commit.
case "$cmd" in
  *"git commit"*) : ;;
  *) exit 0 ;;
esac

# Bypass explícito.
case "$cmd" in
  *"[skip-readme]"*) exit 0 ;;
esac

staged="$(git diff --cached --name-only 2>/dev/null || true)"

# README já está no stage → ok.
printf '%s\n' "$staged" | grep -qxF 'README.md' && exit 0

# Mudou código/build sem README → lembrete (bloqueia).
if printf '%s\n' "$staged" | grep -qE '^app/|\.gradle(\.kts)?$|libs\.versions\.toml$'; then
  cat <<'JSON'
{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"Mudancas em codigo/build (app/, gradle) estao no stage, mas o README.md nao. Atualize o README.md para refletir features/stack/estrutura e adicione-o ao commit; depois re-commite. Se o README realmente nao precisar mudar, inclua [skip-readme] na mensagem do commit."}}
JSON
fi
exit 0
