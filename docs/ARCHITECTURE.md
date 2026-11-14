# iOS Experience AI v0.2 — Arquitetura
```
app ─────────► feature-launcher ──┐
 │ (HomeScreen, Settings,         ├─► core-data  (settings, layout, chaves, ícones, agenda)
 │  TodayPage)  feature-notifications ─┤
 ├────────────► feature-control-center ─┤      ▲ lógica pura testável em JVM
 ├────────────► feature-lockscreen      ├─► core-design (tokens, tema, GlassCard, PillSlider…)
 └────────────► feature-ai ─────────────┘
```
- Dependências só para baixo; `feature-*` não dependem do `app`. Exceções deliberadas: lockscreen → notifications/control-center (reusa cartão e lanterna).
- **Ilha dinâmica** vive em `feature-notifications` (consome mídia/notificações). **Multitarefa** e **pesquisa** vivem em `feature-launcher`. **Widgets** (página “Hoje”) em `app/ui/TodayPage`. Serão separados em módulos próprios se crescerem.
- Estado: `Graph` (injeção manual) → `SettingsStore`/`LayoutStore` (SharedPreferences) → `StateFlow` → Compose. Sem Hilt/Room/DataStore ainda: não há necessidade que os justifique.
- Tela inicial: `LayoutLogic` (puro) decide reordenar/criar pasta; `HomePage` só captura o gesto.
- Papel de parede: tema `Theme.Material.Wallpaper` (`windowShowWallpaper`) — o Compose desenha sobre o wallpaper real do sistema, sem permissão.
- Camadas sobre o wallpaper forçam tema escuro (rótulos brancos); Configurações/Assistente usam o tema escolhido.
- Perfis de desempenho: ECONOMIC (sem blur, 60 Hz), BALANCED, ADVANCED (blur maior, 120 Hz solicitado via `preferredRefreshRate`).
