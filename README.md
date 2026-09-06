# Elo Verde Admin Mobile

Aplicativo Android administrativo da Chácara Elo Verde.

Login persistente, reservas Firestore, WhatsApp, calendário, métricas e navegação inferior.

Adicione o `google-services.json` do projeto Firebase em `app/` antes de executar.

## Releases automáticos

O workflow `.github/workflows/release.yml` cria um GitHub Release e empacota APK/AAB quando há commits desde a última tag semântica:

- `release: ...` incrementa **major** (ex.: `v1.2.3` → `v2.0.0`)
- `feat: ...` incrementa **minor**
- `bugfix: ...` incrementa **patch**
- outros tipos (`chore:`, `docs:`, etc.) não publicam uma nova versão

As tags seguem o formato `vMAJOR.MINOR.PATCH`. Configure o secret de repositório `GOOGLE_SERVICES_JSON` com o conteúdo completo do arquivo Firebase para permitir o build no GitHub Actions. O pacote atual é gerado sem assinatura de produção; configure uma chave de assinatura antes de distribuir pela Play Store.
