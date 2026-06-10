#!/usr/bin/env bash
# PreToolUse hook (matcher: Bash, if: Bash(git commit*)).
# Quando um `git commit` inclui mudanças de código/build (app/, gradle) mas o
# README.md NÃO está no stage, emite um AVISO NÃO-BLOQUEANTE lembrando de manter
# o README em sincronia com features/stack/estrutura. O commit segue normalmente.
#
# Para silenciar o aviso quando o README não precisar mudar, inclua [skip-readme]
# na mensagem do commit. Commits que não tocam código/build não disparam nada.
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

# Mudou código/build sem README → AVISO não-bloqueante (commit segue).
if printf '%s\n' "$staged" | grep -qE '^app/|\.gradle(\.kts)?$|libs\.versions\.toml$'; then
  cat <<'JSON'
{"systemMessage":"⚠️ README: codigo/build mudou (app/, gradle) mas o README.md nao esta no commit. Considere atualizar o README.md para refletir features/stack/estrutura (ou use [skip-readme] para silenciar).","hookSpecificOutput":{"hookEventName":"PreToolUse","additionalContext":"Lembrete: este commit altera codigo/build sem o README.md no stage. Avalie se features/stack/estrutura mudaram e, se sim, atualize o README.md (commit separado ou amend). Nao e bloqueante."}}
JSON
fi
exit 0
