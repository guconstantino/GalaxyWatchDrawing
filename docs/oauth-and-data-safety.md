# Verificação OAuth (Google) + Data Safety (Play Console)

Materiais e passo a passo para liberar a **sincronização com o Google Photos**
ao público geral. Necessário porque o app usa o escopo **sensível**
`photoslibrary.appendonly`.

> **Classificação:** escopo **sensível** (não restrito) → **NÃO** exige avaliação
> de segurança (CASA). O upload é direto device→Google Photos, sem servidor
> próprio. Verificação = consent screen + domínio + justificativa + vídeo.

> **Quando isso é obrigatório:** só para usuários **fora** da lista de test users.
> Enquanto em modo *Testing*, até 100 test users usam o sync sem verificação.
> Para um alpha fechado, dá para adiar; para público geral, é obrigatório.

---

## Parte A — Verificação OAuth

Tudo no projeto **watchdraw-gus** em https://console.cloud.google.com/auth (Google Auth Platform).

### A1. Tela de consentimento (Branding)
- **App name:** `WatchDraw`
- **User support email:** `gustavohconst@gmail.com` (conta dona do projeto Cloud)
- **App logo:** `docs/store-assets/oauth-logo-120.png` — **logo novo** (lápis
  desenhando um traço colorido). O swoosh antigo foi **reprovado** pelo Google
  ("não identifica a marca de forma exclusiva / parece outra marca"). Fonte
  vetorial: `docs/store-assets/icon-source.svg`. ✅ corrigido.
- **App home page:** `https://watchdraw.gustavoconstantino.com/`
  ⚠️ O `github.io` foi **reprovado no branding** (public suffix → o Google não
  aceita como "registrado para você"). Migramos para subdomínio próprio.
- **Privacy policy:** `https://watchdraw.gustavoconstantino.com/privacy-policy.html`
- **Authorized domains:** `watchdraw-gus.firebaseapp.com` (auto, Firebase) +
  **`gustavoconstantino.com`** (domínio próprio, verificável por DNS).
- **Developer contact email:** o mesmo

### A2. Verificação de domínio — ⚠️ REFEITO via domínio próprio
> **github.io não serve para branding.** A verificação URL-prefix do
> `guconstantino.github.io` foi feita e até aparece "verificada" no Search
> Console, **mas o branding do Google reprova** com "a página inicial não está
> registrada para você" — porque o domínio *registrável* (`github.io`) é da
> GitHub. Solução: **subdomínio próprio** `watchdraw.gustavoconstantino.com`
> (GoDaddy), servido pelo mesmo GitHub Pages via `docs/CNAME`, e o domínio
> registrável `gustavoconstantino.com` verificado por **DNS TXT** no Search
> Console (posse real). Aí o branding passa.
>
> Setup do subdomínio:
> 1. GoDaddy → DNS → `CNAME` `watchdraw` → `guconstantino.github.io`.
> 2. Repo: `docs/CNAME` = `watchdraw.gustavoconstantino.com` (commitado).
> 3. GitHub → Settings → Pages → custom domain + Enforce HTTPS.
> 4. Search Console → adicionar **Domínio** `gustavoconstantino.com` → TXT no GoDaddy.
> 5. Branding (A1) → trocar home/política/authorized domains → re-verificar.

### A2-legado. Verificação via github.io (não basta para branding)
O Google exige que a conta dona do projeto **comprove posse** do domínio dos links
acima, via **Google Search Console**.

- Nossa página fica em `guconstantino.github.io/GalaxyWatchDrawing/` (GitHub Pages).
- **Caminho A (tentar primeiro):** no Search Console, adicionar propriedade
  **URL-prefix** `https://guconstantino.github.io/GalaxyWatchDrawing/` e verificar
  pelo método **meta tag** — colamos a `<meta name="google-site-verification" ...>`
  no `docs/index.html`. Usar a **mesma conta Google** que é Owner do projeto Cloud.
- **Caminho B (fallback, mais robusto):** registrar um **domínio próprio** barato,
  apontar para o GitHub Pages, hospedar a política nele e verificar por DNS (TXT).
  Mais confiável para o campo "Authorized domains", mas tem custo.

> Sem a verificação de propriedade concluída, o Google **não aprova** a verificação.

### A3. Justificativa do escopo (colar no campo "How will the scopes be used")
```
WatchDraw is a standalone Wear OS (Galaxy Watch) drawing app. When the user taps
"Download" on a drawing, the app uploads that single image to the user's own
Google Photos library using the photoslibrary.appendonly scope, so the user can
view and share their drawings on their phone.

The app only adds new media that it created; it never reads, lists, or browses the
user's existing Google Photos. There is no narrower scope that allows uploading
images to Google Photos, so appendonly is the minimum required.

Data flow: the image goes directly from the watch to the user's own Google Photos
account over HTTPS. We do not operate any server; we never receive or store the
images or the user's photos.
```

### A4. Vídeo de demonstração (YouTube, "Unlisted") — ✅ FEITO
**Link:** https://youtube.com/shorts/le0TvygR1JQ (Não listado)

> Gravado no relógio real (consent + Permitir + Download via `adb screenrecord`) e
> no S24 (desenho aparecendo no Google Photos), juntados num arquivo só com ffmpeg.
> Virou um "Short" (vertical/curto) — irrelevante para a verificação.

Roteiro usado (gravar com o app já funcionando no relógio/emulador):
1. Abrir o app; mostrar o nome/branding "WatchDraw" (igual à consent screen).
2. Settings → tocar **"Sign in with Google"**.
3. Mostrar o seletor de conta e a **tela de consentimento** exibindo a permissão
   do Photos ("Add to your Google Photos library").
4. **Conceder** a permissão.
5. Mostrar o **uso do escopo**: desenhar → **Download** → o app envia ao Google Photos.
6. Abrir o app **Google Photos** e mostrar o desenho que apareceu lá.
7. (Narração/legenda) "The app only adds images it creates; it never reads the
   user's library."

Subir no YouTube como **Unlisted** e colar o link no campo do formulário.

### A5. Submeter — ✅ SUBMETIDO (08/jun/2026)
Cloud Console → **Verification Center** (Central de verificação) → preenchido
(justificativa A3 + vídeo A4) + questionário (uso pessoal/interno/dev = **Não**;
plug-in SMTP WordPress = **Não**; 2 caixas de confirmação marcadas) →
**Submit for verification**.

> **Status:** "Data access status: o acesso aos dados do seu app **está em
> análise**." Branding ✅ verificado e publicado.
> **Prazo oficial: revisão de 4 a 6 semanas; primeiro e-mail da equipe em 3–5
> dias** (no contato `hello.gustavoconstantino@gmail.com`). A tela de permissão
> aprovada continua em uso; nada quebra durante a análise.
> Itens revisados: página inicial, política, funcionalidade, diretrizes de marca,
> acesso a dados, escopos mínimos. Acompanhar em *Ver progresso da verificação*.

---

## Parte B — Data Safety (Play Console)

Em Play Console → App → **Política → Segurança dos dados**. Responder de acordo
com o comportamento real:

**Coleta/compartilhamento:** "Sim, o app coleta/compartilha dados".

| Tipo de dado | Coletado | Compartilhado | Finalidade | Obrigatório? |
|---|---|---|---|---|
| Personal info → **Name** | Sim | **Não** | App functionality, Account management | Opcional |
| Personal info → **Email address** | Sim | **Não** | App functionality, Account management | Opcional |
| Photos and videos → **Photos** | Sim | **Não** | App functionality | Opcional |

> "Photos" = a foto de perfil do Google **e** os desenhos que o usuário exporta
> (enviados ao Google Photos **do próprio usuário**). "Compartilhado = Não" porque
> não repassamos a terceiros; vai para a conta do próprio usuário.

**Práticas de segurança:**
- Dados **criptografados em trânsito**: **Sim** (HTTPS).
- Usuário pode **solicitar exclusão**: **Sim** (Logout, Reset All no app, e excluir
  no próprio Google Photos).

**Tudo o mais** (localização, contatos, mensagens, arquivos, etc.): **Não coletado**.

> Importante: o Data Safety deve **bater** com a Política de Privacidade (que já
> cobre o upload pro Google Photos).

---

## Progresso (jun/2026)
- [x] A1 — Branding (logo NOVO distintivo, links no domínio próprio, contato) —
      **verificado e publicado**
- [x] A2 — domínio próprio `gustavoconstantino.com` verificado por **DNS** no
      Search Console (o `github.io` foi reprovado: public suffix)
- [x] A3 — justificativa do escopo colada na Central de verificação
- [x] A4 — vídeo publicado: https://youtube.com/shorts/le0TvygR1JQ (Unlisted)
- [x] A5 — **submetido em 08/jun/2026** → status "em análise" (aguardando Google)
- [ ] Parte B — Data Safety no Play Console (pode fazer a qualquer momento)

## Ordem sugerida
1. ~~Consent screen (A1) + logo~~ ✅
2. ~~Verificação de domínio (A2 — domínio próprio por DNS)~~ ✅
3. ~~Gravar o vídeo (A4)~~ ✅
4. ~~A5 — justificativa + vídeo + questionário → submeter~~ ✅ (em análise)
5. **Aguardar a análise do Google** (acompanhar em *Ver progresso da verificação*).
6. Preencher o Data Safety (Parte B) no Play Console — pode ser feito em paralelo.

## Aprendizados desta rodada (jun/2026)
- **github.io não passa no branding de produção:** é *public suffix*; mesmo
  verificado por meta tag no Search Console, o Google reprova a home page
  ("não registrada para você"). Solução: subdomínio próprio + verificação DNS.
- **Logo precisa ser distintivo:** um swoosh genérico é reprovado por "não
  identificar a marca / parecer outra marca". Logo temático (lápis) passou.
- **Verificação DNS automática do GoDaddy** (via Search Console) é instantânea —
  não precisa copiar TXT à mão.
- **Escopo sensível ≠ restrito:** `photoslibrary.appendonly` é **sensível**, então
  **não** exige CASA (a caixa do questionário é só um reconhecimento condicional).
- **Home page NÃO pode redirecionar para a política:** o `index.html` era um
  `<meta refresh>` para `privacy-policy.html`. Na análise (10/jun) o Google
  reprovou o item "Requisitos da política de privacidade" com *"a URL da política
  é a mesma da página inicial"*. Correção: transformar a home numa **landing page
  real** (descreve o app, com link — não redirect — para a política). Home e
  política precisam ser **páginas distintas com conteúdo próprio**.
