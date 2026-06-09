# WatchDraw — Field notes (08/jun/2026): gravando o vídeo e vencendo a verificação OAuth do Google

> Fonte para NotebookLM / rascunho do artigo no Medium. Relato detalhado de uma
> sessão de trabalho real: do "vamos gravar o vídeo" até a verificação OAuth
> **submetida e em análise** — passando por duas reprovações de branding e uma
> migração de domínio. Inclui comandos, decisões e armadilhas.

## Contexto rápido (para quem chega agora)
- **WatchDraw** é um app de desenho **standalone para Wear OS** (Galaxy Watch 4+),
  em Kotlin + Jetpack Compose (Wear Compose Material3).
- Uma das features é **sincronizar os desenhos com o Google Photos do próprio
  usuário**: ao tocar em *Download*, o app envia aquela imagem para a biblioteca
  do usuário usando o escopo **`photoslibrary.appendonly`**.
- Esse escopo é **sensível** (não "restrito"). Para liberar o uso ao **público
  geral** (fora dos até 100 *test users*), o Google exige **verificação OAuth**:
  tela de consentimento + domínio + justificativa + **vídeo de demonstração**.
- Importante: por ser **sensível e não restrito**, **não** exige avaliação de
  segurança CASA. O upload é direto device→Google Photos, sem servidor próprio.

## O objetivo da sessão
"Vamos gravar o vídeo." Era o último item técnico que faltava (A4) para submeter
a verificação. O que parecia 20 minutos virou uma jornada de ~2h com duas
reprovações reais — e um aprendizado valioso sobre o que o Google realmente checa.

---

## Parte 1 — Gravando o vídeo de demonstração

### A pegadinha do consentimento
O vídeo precisa mostrar a **tela de permissão aparecendo** e sendo concedida. Mas
se você já concedeu o escopo antes (e eu já tinha, de tanto testar o sync), o
Google **pula** a tela de permissão. Solução: **revogar o acesso antes de gravar**
em `myaccount.google.com/permissions` → WatchDraw → Remover acesso, e dar logout
no app. Aí o consentimento reaparece do zero.

### Capturando a tela do relógio (sem app extra)
Wear OS tem `screenrecord` embutido. Via ADB:
```
adb -t <id> shell screenrecord --time-limit 180 --bit-rate 8000000 /sdcard/oauth-demo.mp4
# parar limpo (finaliza o mp4):
adb -t <id> shell pkill -INT screenrecord
adb -t <id> pull /sdcard/oauth-demo.mp4 ~/Desktop/
```
Detalhe importante: **mandar SIGINT** (`pkill -INT`) em vez de matar o processo —
é o que finaliza/escreve o cabeçalho do mp4 corretamente.

### O roteiro (o que o revisor precisa ver)
1. App aberto, nome **WatchDraw** visível (igual à consent screen).
2. Settings → **Sign in with Google**.
3. Seletor de conta → **tela de permissão** ("Add to your Google Photos library")
   — pausar ~2s aqui, é o frame que valida tudo.
4. **Permitir**.
5. Desenhar → **Download** (uso real do escopo).
6. (Em outro clipe) abrir o **Google Photos** e mostrar o desenho que chegou lá.

### A tela do relógio não filma o celular
O `screenrecord` do relógio captura só o relógio. O passo 6 (desenho aparecendo
no Google Photos) acontece no **celular**. Então gravei um **segundo clipe** no
Galaxy S24, também por `adb screenrecord` (transport_id diferente — sempre
re-detectar com `adb devices -l`, os IDs mudam).

### Juntando os dois clipes (formatos diferentes)
- Relógio: **438×438** (quadrado), ~2,5 fps.
- Celular: **1080×2340** (retrato), ~21 fps.
Sem Homebrew na máquina, baixei um **ffmpeg estático** avulso (evermeet.cx) e
montei um vídeo único, colocando o quadrado do relógio centralizado num fundo
preto do tamanho do celular:
```
ffmpeg -i relogio.mp4 -i celular.mp4 -filter_complex \
"[0:v]scale=1080:1080:force_original_aspect_ratio=decrease,pad=1080:2340:(ow-iw)/2:(oh-ih)/2,fps=30,setsar=1[w];\
 [1:v]scale=1080:2340:force_original_aspect_ratio=decrease,pad=1080:2340:(ow-iw)/2:(oh-ih)/2,fps=30,setsar=1[p];\
 [w][p]concat=n=2:v=1:a=0[out]" -map "[out]" -c:v libx264 -crf 20 -pix_fmt yuv420p final.mp4
```
Resultado: 1:09, ~5 MB. Também aparei 1:12 de tempo morto do começo
(`ffmpeg -ss 00:01:12 -i ...`).

### Upload
YouTube como **Não listado** (NÃO privado — privado o revisor não acessa). Virou
um "Short" por ser vertical e curto; **irrelevante** para a verificação. Link:
`https://youtube.com/shorts/le0TvygR1JQ`.

**Lição:** teste o link numa janela anônima para garantir que abre.

---

## Parte 2 — A reprovação (dupla) do branding

Ao abrir a Central de verificação, o Google mostrou os problemas de uma **tentativa
anterior**. Duas reprovações:

### Problema 1 — o logo "não identifica a marca"
A mensagem: *"Seu logotipo não identifica sua marca e identidade de forma exclusiva
/ não falsifique a de outra marca."* A descoberta desconfortável: o logo reprovado
**era o ícone real do app** — um "swoosh"/onda colorida em arco-íris num fundo
preto. Genérico demais: não dizia "app de desenho" e lembrava logos de outras
marcas (tipo apps de IA/Notion). **Não dava para re-enviar o mesmo arquivo.**

### Problema 2 — a página inicial "não está registrada para você"
A home page do OAuth estava em `https://guconstantino.github.io/GalaxyWatchDrawing/`.
O Google reprovou: *"não está registrado para você."* Mesmo com a meta tag de
verificação no ar e o property verificado no Search Console.

---

## Parte 3 — Resolvendo o logo (sem designer, sem IA de imagem)

Sem ferramentas de imagem instaladas (nem Homebrew), usei o que o macOS já tem:
- **SVG escrito à mão** (vetor, nítido em qualquer tamanho).
- **`qlmanage`** (QuickLook) para rasterizar SVG→PNG:
  `qlmanage -t -s 1024 -o . logo.svg`.
- **`sips`** para gerar os tamanhos (512, 120).

Conceito escolhido: **um lápis desenhando um traço colorido** sobre fundo navy.
Mantém a "alma" colorida do ícone antigo, mas o lápis torna **inconfundível que é
um app de desenho** — atacando exatamente o motivo da reprovação. Distintivo,
escala bem, e não parece outra marca. Guardei o **SVG fonte** no repo
(`docs/store-assets/icon-source.svg`) para edições futuras.

**Lição:** o logo de marca precisa **comunicar o que o app faz** e ser único.
"Bonito mas genérico" é reprovado.

---

## Parte 4 — Resolvendo o domínio: a armadilha do `github.io`

A causa raiz: **`github.io` é um "public suffix"**. O domínio *registrável* é
`github.io` — e ele é da **GitHub**, não minha. Para a etapa de **branding de
produção**, o Google checa posse no nível do domínio registrável. Por isso, por
mais que o Search Console diga "verificado" (via meta tag, URL-prefix), o branding
**reprova** a home page. **Não tem contorno** ficando no github.io.

### A solução: subdomínio próprio
Eu já tinha `gustavoconstantino.com` (GoDaddy). Plano:
1. **GoDaddy → DNS:** `CNAME` `watchdraw` → `guconstantino.github.io`.
2. **Repo:** `docs/CNAME` = `watchdraw.gustavoconstantino.com` (commit + push) —
   o GitHub Pages serve o mesmo conteúdo no domínio próprio.
3. **GitHub Pages:** custom domain + HTTPS (o cert saiu em minutos).
4. **Search Console:** adicionar `gustavoconstantino.com` como propriedade de
   **Domínio** (DNS). O Search Console detectou o GoDaddy e fez a **verificação
   automática** (autoriza o Google a criar o TXT sozinho) — instantâneo, sem
   copiar registro à mão.
5. **OAuth/Branding:** trocar home page e política para o subdomínio; authorized
   domains → `gustavoconstantino.com`.

Em minutos: `https://watchdraw.gustavoconstantino.com/` no ar com HTTPS, DNS
propagado (`dig` mostrando os IPs do GitHub Pages: 185.199.108–111.153).

**Lição:** para verificação OAuth de produção, **tenha um domínio próprio**.
github.io é ótimo para hospedar, mas não serve como "posse" no branding.

---

## Parte 5 — Branding aprovado e submissão

Com **logo novo** + **home page em domínio verificado por DNS**, rodei a
verificação de branding de novo. Passou: *"Sua marca foi verificada."* Detalhe:
o resultado verificado **expira em 7 dias se você não publicar** — então cliquei
em **Publicar branding** → *"verificada e está aparecendo para os usuários."*

Aí o card **Data access** destravou (ele exigia branding publicado antes). No
formulário:
- Justificativa do escopo (texto em inglês explicando o appendonly e o fluxo
  device→Google Photos, sem servidor).
- Link do vídeo.
- **Questionário:** uso pessoal? **Não**. Uso interno? **Não**. Só dev/teste?
  **Não**. Plug-in SMTP do Gmail para WordPress? **Não**. + 2 caixas de
  confirmação (uma menciona CASA para **escopos restritos** — condicional, e o
  nosso é **sensível**, então CASA não se aplica).
- **Enviar para verificação.**

### Resultado
*"O acesso aos dados do seu app está em análise."*
- **Revisão: 4 a 6 semanas.**
- **Primeiro e-mail da Equipe de Confiabilidade e Segurança em 3–5 dias.**
- A tela de permissão aprovada **continua em uso** durante a análise.
- Itens em revisão: página inicial, política de privacidade, funcionalidade do
  app, diretrizes de marca, acesso aos dados, escopos mínimos.

---

## As 5 armadilhas (resumo "quotável" para o artigo)
1. **Revogue o acesso antes de gravar** o vídeo, senão a tela de consentimento
   não aparece e o vídeo é inválido.
2. **Vídeo "Não listado", nunca "Privado"** — o revisor precisa abrir o link.
3. **Logo genérico é reprovado.** Ele tem que dizer o que o app faz e ser único.
4. **`github.io` não passa no branding de produção** (public suffix). Use um
   **domínio próprio** verificado por DNS.
5. **Sensível ≠ restrito.** Escopos sensíveis (como `photoslibrary.appendonly`)
   **não** exigem CASA. Não se assuste com a caixa do questionário.

## Stack/ferramentas usadas hoje (e por quê)
- `adb screenrecord` — capturar relógio e celular reais (autenticidade).
- **ffmpeg estático** (sem Homebrew) — aparar e concatenar vídeos de formatos
  diferentes.
- **SVG + `qlmanage` + `sips`** — desenhar e exportar o logo sem designer/IA.
- **GoDaddy + GitHub Pages + Search Console** — domínio próprio e verificação DNS.

## Estado do projeto após a sessão
- Verificação OAuth: **submetida, em análise** (4–6 semanas).
- Branding (logo + domínio): **verificado e publicado.**
- App segue funcionando para os test users.
- Pendências: **Data Safety** no Play Console; **Play Billing** (Pro) agora que o
  Play Console foi aprovado; opcional trocar o ícone do app no relógio pelo novo.

## Ângulos possíveis para o artigo no Medium
- "Como passei na verificação OAuth do Google para um app de smartwatch — e as 5
  armadilhas que ninguém te conta."
- "Gravando um vídeo de demonstração de OAuth com dois aparelhos reais e ffmpeg."
- "Por que seu `github.io` nunca vai passar no branding do Google (e o que fazer)."
- "Redesenhando um logo reprovado pelo Google sem designer: SVG + QuickLook."
