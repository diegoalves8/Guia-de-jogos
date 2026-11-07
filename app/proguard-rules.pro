# Regras R8 do Guia de Jogos.
# O app não usa reflexão nem serialização automática (o JSON é lido com org.json),
# então as regras padrão do Android já bastam. Mantemos BuildConfig por segurança.
-keep class com.guiadejogos.app.BuildConfig { *; }
