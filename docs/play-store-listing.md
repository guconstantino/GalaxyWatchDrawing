# Ficha da Play Store — WatchDraw

Textos prontos para colar no **Google Play Console → Presença na loja → Ficha
principal da loja**. Há versões em **Português (Brasil)** e **English (US)** —
adicione os dois idiomas em *Configurações → Traduções da ficha* para mais
alcance.

> Limites do Play Console: **Título** 30 caracteres · **Descrição curta** 80 ·
> **Descrição completa** 4000 · **Notas da versão** 500.

---

## 🇧🇷 Português (Brasil)

### Título (≤30)
```
WatchDraw: Desenhe no Relógio
```
*(29 caracteres)*

### Descrição curta (≤80)
```
Desenhe direto no seu relógio. Cores, traços e galeria, sem precisar do celular.
```
*(80 caracteres)*

### Descrição completa (≤4000)
```
WatchDraw transforma o seu Galaxy Watch numa tela de desenho de bolso. Abra o
app e desenhe direto no pulso — sem precisar pegar o celular.

Feito sob medida para a tela redonda do Wear OS, com gestos rápidos e retorno
tátil em cada ação.

RECURSOS
• Tela de desenho com traços suaves e fluidos
• Paleta de cores selecionada e três espessuras de pincel
• Desfazer e refazer com gestos de dois dedos
• Galeria "My draws" para guardar suas criações
• Favoritos para marcar os desenhos que você mais gosta
• Lixeira com restauração (itens expiram em 30 dias)
• Exporte como imagem: salve na galeria ou compartilhe
• Retorno tátil (vibração) sutil para uma experiência precisa

CONTA GOOGLE (OPCIONAL)
• Entre com sua Conta Google nas Configurações para personalizar seu perfil
• A sincronização na nuvem está a caminho

PRIVACIDADE
• Seus desenhos ficam armazenados localmente no relógio
• Não vendemos nem compartilhamos seus dados
• A opção "Reset All" apaga tudo a qualquer momento

WatchDraw é leve, rápido e pensado para quem quer rabiscar uma ideia, deixar um
recado ou só se divertir — tudo na ponta do pulso.
```

### Notas desta versão (≤500)
```
Primeira versão do WatchDraw!
• Tela de desenho com cores e espessuras
• Galeria, Favoritos e Lixeira
• Exportar e compartilhar como imagem
• Login com Google nas Configurações
• Gestos de desfazer/refazer com retorno tátil
```

---

## 🇺🇸 English (US)

### Title (≤30)
```
WatchDraw: Draw on Your Watch
```
*(29 characters)*

### Short description (≤80)
```
Draw right on your watch. Colors, brushes and a gallery — no phone needed.
```
*(73 characters)*

### Full description (≤4000)
```
WatchDraw turns your Galaxy Watch into a pocket-sized drawing canvas. Open the
app and sketch right on your wrist — no need to reach for your phone.

Built for the round Wear OS display, with quick gestures and haptic feedback on
every action.

FEATURES
• Drawing canvas with smooth, fluid strokes
• Curated color palette and three brush sizes
• Undo and redo with two-finger gestures
• "My draws" gallery to keep your creations
• Favorites to star the drawings you love most
• Trash with restore (items expire after 30 days)
• Export as an image: save to your gallery or share
• Subtle haptic feedback for a precise experience

GOOGLE ACCOUNT (OPTIONAL)
• Sign in with your Google Account in Settings to personalize your profile
• Cloud sync is on the way

PRIVACY
• Your drawings are stored locally on your watch
• We don't sell or share your data
• "Reset All" wipes everything anytime

WatchDraw is lightweight, fast, and made for anyone who wants to jot down an
idea, leave a note, or just have fun — right at their fingertips.
```

### Release notes (≤500)
```
First release of WatchDraw!
• Drawing canvas with colors and brush sizes
• Gallery, Favorites and Trash
• Export and share as an image
• Sign in with Google in Settings
• Undo/redo gestures with haptic feedback
```

---

## Metadados da ficha

| Campo | Valor sugerido |
|---|---|
| **Categoria** | Arte e design (Art & Design) |
| **Tags** | desenho, arte, criatividade, wear os |
| **E-mail de contato** | hello.gustavoconstantino@gmail.com |
| **Política de Privacidade** | https://guconstantino.github.io/GalaxyWatchDrawing/privacy-policy.html |
| **Site (opcional)** | https://github.com/guconstantino/GalaxyWatchDrawing |
| **Público-alvo** | 13+ (não direcionado a crianças) |
| **Anúncios** | Não contém anúncios |
| **Compras no app** | Não |

---

## Assets gráficos necessários (você precisa criar/exportar)

O Play Console **exige** estes arquivos para publicar. Dá para gerar a partir do
ícone e de capturas do app no relógio:

| Asset | Tamanho | Obrigatório |
|---|---|---|
| **Ícone do app** | 512 × 512 px, PNG 32-bit | ✅ |
| **Gráfico de destaque** (feature graphic) | 1024 × 500 px | ✅ |
| **Capturas Wear OS** | 1:1 (ex.: 384×384) ou 9:16, mín. 1, recomendado 4–8 | ✅ (pelo menos 1 de Wear) |

**Capturas já geradas** em `docs/screenshots/` (1152×1152, 1:1 — emulador Wear OS
round). É só subir estas no Play Console:

| Arquivo | Tela |
|---|---|
| `01-home.png` | Menu principal (New draw, My draws…) |
| `02-canvas.png` | Tela de desenho com cores + barra de ferramentas |
| `03-colorpicker.png` | Seletor de cores (paleta) |
| `04-actions.png` | Menu de ações (limpar, exportar, favoritar, fechar) |
| `05-settings-login.png` | Configurações com login Google + Reset All |

---

## Checklist antes de "Enviar para revisão"

- [ ] App criado com **form factor Wear OS** marcado
- [ ] `app-release.aab` enviado numa trilha (sugestão: começar por **Teste interno**)
- [ ] **3º SHA-1** (App Signing) adicionado no Firebase + `google-services.json` rebaixado
- [ ] URL da Política de Privacidade colada
- [ ] Ícone 512×512 + feature graphic 1024×500 + ≥1 captura de Wear OS
- [ ] Questionário de **classificação de conteúdo** respondido
- [ ] Seção **Segurança dos dados** preenchida (coletamos: nome, e-mail, foto —
      via login Google; tudo opcional; sem compartilhamento)
- [ ] Países/regiões de distribuição selecionados
- [ ] Preço definido como **Gratuito**
