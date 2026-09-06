# Elo Verde Admin Mobile

Aplicativo Android administrativo da Chácara Elo Verde.

Login persistente, reservas Firestore, WhatsApp, calendário, métricas e navegação inferior.

Adicione o `google-services.json` do projeto Firebase em `app/` antes de executar. No GitHub Actions, o arquivo é injetado pelo secret `GOOGLE_SERVICES_JSON`.

## Releases automáticos

O workflow `.github/workflows/release.yml` cria um GitHub Release com um APK interno instalável quando há commits desde a última tag semântica:

- `release: ...` incrementa **major** (ex.: `v1.2.3` → `v2.0.0`)
- `feat: ...` incrementa **minor**
- `bugfix: ...` incrementa **patch**
- outros tipos (`chore:`, `docs:`, etc.) não publicam uma nova versão

As tags seguem o formato `vMAJOR.MINOR.PATCH`. O APK interno usa a assinatura de debug criada pelo runner do GitHub. Ele pode ser instalado diretamente, mas uma build futura pode exigir desinstalar a anterior. Para atualizações no lugar, configure depois uma chave interna permanente no workflow.
