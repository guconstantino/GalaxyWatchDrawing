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

### A1. Tela de consentimento (Branding) — ✅ FEITO
- **App name:** `WatchDraw`
- **User support email:** `hello.gustavoconstantino@gmail.com`
- **App logo:** `docs/store-assets/oauth-logo-120.png` (120×120, gerado do icon-512).
- **App home page:** `https://guconstantino.github.io/GalaxyWatchDrawing/`
  ⚠️ NÃO usar a URL do github.com — não dá para verificar o github.com como
  domínio autorizado. Usar o github.io (que é nosso e verificado).
- **Privacy policy:** `https://guconstantino.github.io/GalaxyWatchDrawing/privacy-policy.html`
- **Authorized domains:** `watchdraw-gus.firebaseapp.com` (auto, Firebase) +
  **`guconstantino.github.io`** (NÃO `github.io` — é public suffix, rejeitado
  como "domínio inválido").
- **Developer contact email:** o mesmo

### A2. Verificação de domínio — ✅ FEITO (via github.io)
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

### A4. Vídeo de demonstração (YouTube, "Unlisted")
Precisa mostrar o fluxo de consentimento E o uso do escopo. Roteiro (gravar com o
app já funcionando no relógio/emulador):
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

### A5. Submeter
Cloud Console → **Verification Center** (Central de verificação) → preencher tudo
acima → **Submit for verification**. Prazo: dias a algumas semanas.

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
- [x] A2 — domínio `guconstantino.github.io` verificado no Search Console (meta tag)
- [x] A1 — Branding salvo (logo, links, authorized domains, contato)
- [ ] A3 — justificativa do escopo (texto pronto acima; colar em Acesso a dados)
- [ ] A4 — **gravar o vídeo** (YouTube Unlisted) ← próximo passo
- [ ] A5 — mudar para Produção + submeter na Central de verificação
- [ ] Parte B — Data Safety no Play Console (pode fazer a qualquer momento)

## Ordem sugerida
1. ~~Consent screen (A1) + logo~~ ✅
2. ~~Verificação de domínio (A2)~~ ✅
3. Justificativa (A3) + gravar o vídeo (A4).
4. Mudar para Produção e submeter (A5).
5. Preencher o Data Safety (Parte B) no Play Console.
