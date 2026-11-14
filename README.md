# iOS Experience AI v0.2
Launcher Android (Kotlin + Compose) inspirado no iOS, para POCO F7 / HyperOS. Sem root.
## Compilar e instalar
1. Android Studio (Ladybug+), JDK 17. Abra esta pasta; aguarde o Sync (gera o Gradle wrapper).
2. `./gradlew :core-data:test :feature-ai:test :feature-launcher:test` (testes JVM).
3. Rode o módulo `app` no POCO F7 (depuração USB). Depois: Configurações → Apps → Apps padrão → Tela inicial.
## Gestos
- Deslizar para baixo na borda superior ESQUERDA: notificações; DIREITA: central de controle.
- Botão “Buscar”: pesquisa + atalhos (Configurações, Assistente, Tela de bloqueio, Multitarefa, Papel de parede).
- Toque longo + arrastar ícone: reordenar; soltar no centro de outro: pasta. Toque longo no dock: trocar app.
- Página à esquerda da primeira: “Hoje” (widgets).
## Status
Escrito sem acesso a compilador/SDK: **não foi compilado nem testado em aparelho.** Veja docs/LIMITATIONS.md.

## v0.3-A — fundação visual (não compilada localmente)
Mudanças: `IOSSurface` (materiais Thin/Regular/Thick), `SquircleShape`, wallpapers próprios, tipografia, `iosPress`, dock em vidro, barra de status do app (opcional), restyle de Ajustes. Veja `CHANGES.md`.
- **Release/medição**: `gradle :app:assembleRelease` (precisa de keystore própria) e meça com Perfetto (ui.perfetto.dev). Nenhum fps foi medido por mim.
- **HyperOS**: Início automático + bateria "Sem restrições" + "Permitir configurações restritas" (Info do app → ⋮) para o serviço de notificações.
- **Perfil**: Ajustes → Desempenho. Econômico = sem cópia do fundo, sem sombra, 60 Hz.
