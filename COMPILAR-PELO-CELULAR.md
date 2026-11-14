# iOS Experience AI v0.2 — compilar pelo celular

## Antes de começar
Esta é uma versão de desenvolvimento. O próprio README do projeto informa que ela ainda não foi compilada nem testada em aparelho. O projeto não inclui Gradle Wrapper; por isso, este ZIP inclui uma rotina de compilação no GitHub Actions.

## Passos
1. Baixe e extraia este ZIP no POCO F7.
2. Entre em https://github.com pelo navegador e crie um repositório novo (pode ser privado).
3. Envie o CONTEÚDO da pasta `IOSExperienceAI` para a raiz do repositório. Inclua a pasta oculta `.github` e seu arquivo `workflows/android.yml`.
4. Abra a aba **Actions** do repositório. Habilite GitHub Actions se for solicitado.
5. Escolha **Build APK** e toque em **Run workflow**.
6. Aguarde. Se a execução ficar verde, abra a execução e baixe o artefato `IOSExperienceAI-v0.2-debug`.
7. Extraia o artefato e localize `app-debug.apk`.
8. Abra o APK no próprio POCO F7 e siga a confirmação de instalação do Android.

## Se a compilação falhar
Abra a execução vermelha em Actions, copie o trecho final do log de erro e envie para análise. Não instale APKs de fontes desconhecidas que não sejam o artefato da sua própria compilação.

## Limitações conhecidas desta versão
- O projeto implementa um launcher, não substitui o sistema HyperOS.
- A tela de bloqueio é uma tela alternativa dentro do app; não substitui a tela segura do Android.
- A Dynamic Island aparece dentro do launcher, não por cima de outros aplicativos.
- Wi-Fi, Bluetooth e modo avião abrem os painéis oficiais do Android em vez de alternar diretamente.
- A IA disponível é baseada em regras locais, não é um modelo generativo; integração remota está marcada como não implementada.
- O código ainda precisa passar pela compilação e por testes reais no POCO F7.
