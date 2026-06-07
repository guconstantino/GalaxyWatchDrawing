# Roadmap — Sincronização (Google Photos) + Monetização

> Documento de planejamento. Decisões tomadas em conversa, ainda **não implementadas**.
> Use como referência para implementar a feature de sync e a estratégia de monetização.

---

## Contexto e decisões de fundo

- **Distribuição:** o app é Wear OS (Galaxy Watch 4+). O canal oficial é a **Google Play Store**.
  A **Galaxy Store** foi descartada: o canal "Android Watch" dela é efetivamente
  **China-only**, e como o app usa **GMS** (Google Sign-In + Firebase), ele não roda na
  China — os dois se anulam.
- **Objetivo da feature:** levar os desenhos do relógio para o celular do usuário, para
  ele usar e compartilhar de lá.
- **Solução escolhida:** upload para o **Google Photos** do usuário (via escopo
  `photoslibrary.appendonly`), reaproveitando o login Google que já existe no app.

### Por que Google Photos (verificado na doc oficial, jun/2026)

- O escopo **`photoslibrary.appendonly` continua válido** para upload (NÃO foi removido).
- As remoções de 31/03/2025 atingiram só os escopos de **leitura** da biblioteca inteira
  (`photoslibrary.readonly`, `.sharing`, `photoslibrary`) — que não precisamos.
- Doc confirma: *"A media item is always added to the user's library."* → a imagem entra
  na biblioteca real do Google Photos e **aparece no app Fotos do celular** (cross-device).
- Bônus: dá para **criar um álbum** ("WatchDraw") e agrupar os desenhos.

### Ressalvas importantes

1. **"Aparece no Google Photos", não na Samsung Gallery local.** Para a maioria isso é a
   galeria do dia a dia, mas quem usa só a Samsung Gallery veria as imagens abrindo o Photos.
2. **Verificação OAuth do Google (hurdle de release):** `appendonly` é escopo *sensível*.
   Para público geral, exige passar pela **verificação OAuth** (tela de consentimento +
   revisão). Sem isso → limitado a ~100 usuários de teste. Processo leva dias a semanas;
   **planejar antes do lançamento da feature.**

---

## Parte 1 — Comportamento por estado de login

| Estado | Ação do "Download" | Ganho |
|---|---|---|
| **Deslogado** | Salva só na galeria local (como hoje) | App 100% funcional sem conta — mantém "sem cadastro" |
| **Logado** | Salva local **+** sobe pro Google Photos | Aparece no celular pra usar/compartilhar |

- **Privacidade:** nada sai do dispositivo a menos que o usuário esteja **logado E** tenha
  tocado em **Download**. Fácil de declarar.
- Observação: a "galeria local do relógio" é pouco usada na prática; o valor real do
  deslogado continua sendo **My draws** + **share**. O sync é o grande ganho da versão logada
  (e o gancho natural de monetização).

---

## Parte 2 — Fluxo de sync (usuário logado)

### Gatilhos do sync automático (camadas)

| Gatilho | Quando | Por quê |
|---|---|---|
| Ao tocar **Download** | Imediato | Intenção explícita — tenta subir na hora |
| Ao **recuperar conexão** | Wi-Fi/internet volta | Esvazia a fila offline |
| Ao **abrir o app** | Launch | Reenvia pendências de sessões anteriores |
| Ao **carregar/Wi-Fi** (opcional) | Background | Rede de segurança |

> Regra de ouro: relógio quase sempre sem Wi-Fi e com bateria curta. Trate upload como
> *"tentar; se falhar, enfileirar"* — nunca *"tem que dar certo agora"*.

### Fila offline (estados por item)

```
PENDING   → na fila, ainda não tentou
UPLOADING → enviando agora
SYNCED    → confirmado no Google Photos
FAILED    → falhou (sem rede, token expirado, etc.)
```

- Download → adiciona como `PENDING` e dispara tentativa.
- Sem rede → fica `PENDING`, sem erro assustador.
- Falha → `FAILED` + **retry com backoff** quando a rede voltar (não martelar).
- Sucesso → `SYNCED`, sai da fila.

### Estados na UI

**Por desenho (My draws)** — ícone discreto:
- nuvem com check = sincronizado
- nuvem com seta = na fila/enviando
- nuvem com "!" = falhou (toque pra retry)
- sem ícone = nunca baixado/sincronizado

**Botão "Sync Now" (Settings/Profile)** — estados:
- Normal: "Sync Now" (+ "X pending" se houver fila)
- Tocado: "Syncing… (2/5)" com progresso
- Concluído: "All synced ✓"
- Sem rede: "No connection — will sync later"
- Deslogado: escondido/desabilitado

> O botão é **override manual**, não uso constante. O automático faz o trabalho; o botão
> força reenvio do que ficou na fila ou pula a espera.

### Edge cases a decidir

1. **Token expirado** → re-autenticar silenciosamente; se falhar, `FAILED` + avisar uma vez.
2. **Mesma imagem 2x** → evitar duplicata (guardar ID local "já sincronizado").
3. **Baixou deslogado, logou depois** → só novos sincronizam automático; "Sync Now" oferece
   subir os locais pendentes (mantém o automático previsível).
4. **Usuário deleta no app** → não mexe no que já foi pro Photos (é a galeria dele agora).

### Feedback "pra onde foi" (UX)

- Deslogado: "Saved to gallery"
- Logado: "Sent to Google Photos"
- (sem isso, o usuário não sabe onde o desenho parou)

### Upsell de login (sem ser chato)

- Ao baixar deslogado: nudge único e dispensável — "Sign in to send drawings to your phone."
  Não repetir toda vez.

---

## Parte 3 — Monetização honesta e lucrativa

### Realidade do mercado

Wear OS é mercado **pequeno**: o gargalo é **volume**, não conversão. Estratégia: **conversão
alta de poucos engajados, sem irritar** (reviews pesam muito num nicho pequeno).
Plataformas ficam com **15–30%**.

### Opções (melhor → pior para este caso)

#### 🥇 1. Freemium + "Pro" (pagamento ÚNICO via IAP) — RECOMENDADO
App grátis e totalmente usável; um pagamento único libera o Pro.

**O que vai no Pro** (sync é a âncora):
- ☁️ **Sync pro Google Photos** ← gancho principal
- 🎨 Mais paletas / packs de pincéis
- 📐 Exportar em resolução maior / sem marca d'água
- 🗂️ Álbuns organizados no Photos
- ♾️ Limite maior de desenhos salvos (free = X, Pro = ilimitado)

**Por quê:** honesto (grátis entrega valor real), **custo recorrente zero** (usa o Photos do
*próprio usuário*), e o sync é benefício que se sente.
**Preço sugerido:** US$ 2,99–4,99 único.

#### 🥈 2. Packs cosméticos (IAPs pequenos)
Paletas/pincéis/fundos opcionais e estéticos. Receita repetível, mas exige criar conteúdo.

#### 🥉 3. Tip jar / "Buy me a coffee"
Apoio voluntário no Settings. 100% honesto, receita baixa. Complemento, não estratégia.

#### ⚠️ 4. Assinatura — evitar (por ora)
Como usa o Photos do usuário (sem servidor seu), **não há custo recorrente** → cobrar
assinatura seria difícil de justificar eticamente. Só faria sentido com features server-side
próprias (galeria web, storage próprio, colaboração).

#### ❌ 5. Anúncios — NÃO
Tela de relógio: péssima UX, payout irrisório. Mata o charme. Descartado.

### Princípios "honestos"

- Grátis **genuinamente útil**; Pro é conveniência/extra, não resgate de refém.
- **Zero venda de dados** (usar como argumento de marketing).
- Preço transparente, sem trial-que-vira-cobrança.
- Sem nag agressivo (no máximo um lembrete sutil no momento certo).

### Roadmap de monetização

1. **Lançar grátis** (construir base + reviews) ← estágio atual
2. Adicionar **sync + Pro unlock** como primeira monetização
3. **Packs cosméticos** depois, se houver tração

---

## Pré-requisitos técnicos do sync (checklist)

- [x] Adicionar escopo `photoslibrary.appendonly` ao Google Sign-In existente
- [ ] Iniciar **verificação OAuth** do Google (sensível) — fazer cedo, leva tempo
- [x] Implementar fila offline persistente (`SyncQueue`: PNGs em disco + índice JSON)
- [x] Implementar gatilhos de auto-sync (download, login, launch)
- [x] Botão "Sync Now" com override manual (contagem + spinner + estado desabilitado)
- [ ] Estados de UI **por desenho** (ícone de nuvem) — exige vínculo fila↔desenho
- [ ] Evitar duplicatas (não subir 2x a mesma imagem)
- [ ] (Opcional) criar álbum "WatchDraw" no Photos
- [x] **Atualizar Política de Privacidade**: declara upload pro Google Photos do usuário
- [ ] **Atualizar Data Safety** (Play/Galaxy Console): declarar o upload pro Photos

---

## Status (jun/2026)

**Já no `main`:**
- Sync para Google Photos no Download (logado) — testado em Galaxy Watch real
- Fila offline persistente + auto-retry (init/login/download)
- Botão "Sync Now" (contagem, spinner durante upload, desabilitado quando vazio)
- Política de privacidade atualizada (PT→EN + cláusula do Google Photos)

**Falta:**
- 🔴 Verificação OAuth (gargalo de calendário) — atualizar Data Safety, gravar vídeo, submeter
- 🟡 Ícone de nuvem por desenho + dedup
- 🟢 Trava de Pro + IAP (pagamento único)

---

## Próximos passos em aberto (para retomar)

- Definir a **linha de corte free vs. Pro** (o que maximiza conversão sem irritar).
- Implementar **ícone de nuvem por desenho** (vínculo fila↔desenho + set de já-sincronizados).
- Planejar o **processo de verificação OAuth** passo a passo.
- **Data Safety** (console): com o sync, "Photos and videos" agora também é **enviado**
  (shared = No; collected/uploaded = Yes, App functionality).
