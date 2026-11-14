# Limitações conhecidas (Android 15 / HyperOS 2 — POCO F7)
## Não implementado / adiado
- **Widgets de terceiros (AppWidgetHost)**: v0.2 tem widgets nativos próprios (relógio/agenda, bateria, notificações, mídia).
- **IA remota**: só existe o provedor local por regras (não é modelo generativo). A chave de API é guardada cifrada, mas nenhum provedor a usa.
- **Organizar por categorias / personalizar widgets por IA**: o assistente responde que ainda não existe.
- **Arrastar entre páginas**: reordenar/criar pasta funciona dentro da página.
- **Papel de parede com desfoque real do fundo (backdrop blur)**: `Modifier.blur` desfoca o conteúdo do app (grade) quando um painel abre, não o wallpaper. `GlassCard` usa translucidez + borda.
- **Ícones temáticos/monocromáticos**: usa os ícones adaptativos dos apps.
## Impossível para apps comuns (por desenho do Android)
- Substituir a tela de bloqueio segura: a “Tela de bloqueio” é uma tela alternativa dentro do app e não bloqueia o aparelho.
- Ligar/desligar Wi-Fi, Bluetooth e modo avião: os botões mostram o estado real e abrem o painel/configuração do sistema.
- Prévias e lista de recentes do sistema: Multitarefa mostra só apps abertos por este launcher.
- Ilha dinâmica como overlay global: v0.2 a desenha dentro do app (não aparece sobre outros apps). Overlay exigiria `SYSTEM_ALERT_WINDOW` e é v0.3. Atividades de apps de terceiros (ex.: delivery) não são acessíveis; só mídia, notificações e o timer interno.
- Timer: não toca alarme se o app for encerrado (sem serviço em primeiro plano).
## A validar no aparelho (não testado)
- Brilho: faixa de `SCREEN_BRIGHTNESS` no HyperOS pode ser maior que 0–255.
- Notification Listener: o HyperOS pode exigir “Permitir configurações restritas” (Info do app → ⋮) por ser APK instalado fora da loja, além de Início automático e bateria “Sem restrições” para o serviço não ser encerrado.
- Ícones de status brancos sobre telas claras (Configurações em tema claro).
- 120 Hz: `preferredRefreshRate` é só um pedido; o sistema decide.

# Visual (v0.3-A)
- **Vidro**: o "fundo" do vidro é uma cópia saturada (180%) do wallpaper PRÓPRIO do app, alinhada à janela — não é blur gaussiano. Com o wallpaper do sistema (opção em Ajustes) o app não consegue ler o fundo; o vidro vira só tinta translúcida + borda.
- **Barra de status**: a do sistema não é controlável. A barra do app (experimental, Ajustes) esconde a do sistema; deslizar da borda superior a revela. Sinal de operadora não é exibido (exige READ_PHONE_STATE). Telas ainda não reservam o espaço dela.
- **Ilha dinâmica**: continua dentro do app e ligada por padrão; o pedido é ficar desligada e opcional (pendente).
- **Overscroll**: brilho/stretch do Android removidos; o "bounce" elástico (rubber band) NÃO foi implementado.
- Sem ripple Material nos componentes próprios; componentes Material3 restantes (Slider, AlertDialog, TextButton) ainda mostram o estilo Material.
