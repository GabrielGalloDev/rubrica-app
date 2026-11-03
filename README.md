# Rubrica

Aplicativo Android nativo para envio e assinatura de documentos PDF, desenvolvido para a LetsSign no Challenge FIAP (Sprint 1).

Integrantes: Gabriel Gallo das Virgens (RM566880) e Luiz Fernando Lucio Vilela (RM566626).

- **Admin**: importa um PDF, escolhe e classifica os signatários (contratante, contratado ou testemunha), posiciona os campos de assinatura, revisa, envia e acompanha o andamento.
- **Signatário**: vê os documentos pendentes e os já assinados, abre o PDF, desenha a assinatura e salva. O status do registro é atualizado na hora.

## Stack

| Camada | Tecnologia |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Arquitetura | MVVM + Repository, injeção de dependências manual (`AppContainer`) |
| Banco local | **Room** (usuários, documentos, signatários, campos e eventos) |
| Arquivos | PDFs e rubricas no sistema de arquivos do app (`files/documents/{id}`), com o caminho salvo no Room |
| Remoto (opcional) | **Firebase** Auth + **Realtime Database** (dados e arquivos em Base64), atrás da interface `RemoteDataSource` |
| PDF | `PdfRenderer` (visualização) + **PDFBox-Android** (gera o PDF assinado e a trilha de auditoria) |

## Arquitetura

```
br.com.fiap.letssign
├── components/    componentes Compose reutilizáveis (botões, campos, cartões, PDF, assinatura…)
├── database/      Room: AppDatabase, dao/, entity/ e mapeamentos entidade ↔ modelo
├── factory/       AppContainer (injeção de dependências manual) e LocalAppContainer
├── firebase/      FirebaseProvider, DatabaseSchema, conversão de snapshots e FirebaseErrorMapper
├── model/         modelos de domínio, AppError, UiState e ValidationIssue
├── navigation/    LetsSignApp (NavHost) e LetsSignDestination (rotas)
├── repository/    repositórios, preferências, arquivos e RemoteDataSource ← interface
│                  └─ RealtimeRemoteDataSource (dá para trocar por uma API REST/Retrofit)
├── ui/screens/    uma tela por arquivo
├── ui/theme/      Color, Type, Shape, Dimens e Theme
├── ui/viewmodel/  ViewModels com StateFlow<UiState> e factory(container)
└── utils/         validadores, mensagens de erro, idioma, datas e PDF
```

- O **Room é a fonte de verdade** das telas (Flows). O Firebase sincroniza os dados entre aparelhos.
- Sem o `google-services.json`, o app roda em **modo local**: tudo fica no aparelho e existe um admin de demonstração (`admin@letssign.com` / `admin123`).
- Os erros são tipados (`AppError`) e convertidos em texto só na tela, por `AppErrorMessages`.

## Idiomas

- Todos os textos ficam em `res/values/strings.xml` (inglês, padrão) e `res/values-pt-rBR/strings.xml` (português).
- O idioma segue o do aparelho e pode ser trocado no app em **Configurações** (ícone de engrenagem no login e na tela inicial), junto com o tema claro ou escuro. No Android 13+ usa o idioma por app do sistema (`locales_config.xml`).
- Datas, plurais e a trilha de auditoria do PDF final também seguem o idioma escolhido.
- Para adicionar um idioma: crie `values-<código>/strings.xml`, inclua o código em `locales_config.xml` e em `AppLanguage`.

## Testes

```bash
./gradlew testDebugUnitTest
```

Cobrem validadores, regras de envio do documento, cálculo de status e o esquema do Realtime Database.

## Regras de negócio implementadas

- O documento só pode ser enviado com **pelo menos 1 signatário** (e pelo menos 1 obrigatório).
- Todo campo fica associado a um participante, e todo participante precisa de pelo menos um campo.
- Os campos ficam **dentro dos limites da página**: o arrasto e o redimensionamento são limitados, e há uma nova validação antes do envio.
- A posição e o tamanho dos campos são salvos **em relação ao tamanho da página (0..1)**, então o posicionamento se mantém em qualquer tela ou aparelho.
- O PDF é validado ao ser importado. O app avisa quando o arquivo não é PDF, está vazio, tem mais de 7 MB, está corrompido ou protegido por senha.
- Um documento enviado não pode ser alterado. Para mudar algo, o admin usa **"Alterar (gerar nova versão)"**, que cria a v2 como um novo fluxo e mantém a v1.
- O documento só fica **concluído** quando todos os signatários **obrigatórios** assinam.
- O **reenvio não duplica nada**: os ids são gerados no cliente e a gravação é idempotente (`set` com o mesmo id).
- **Isolamento de dados**: cada admin só vê os próprios signatários e documentos, e cada signatário só vê os documentos enviados a ele. Isso vale nas consultas do Room e nas regras do Realtime Database (`database.rules.json`).

## Como compilar

Requisitos: Android Studio (JDK 17+), Android SDK 37.

```bash
./gradlew assembleDebug      # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # app/build/outputs/apk/release/app-release.apk (minificado, assinado com a chave de debug)
```

## Ativando o Firebase

1. Crie um projeto em <https://console.firebase.google.com>.
2. Adicione um app **Android** com o pacote `br.com.fiap`.
3. Baixe o `google-services.json` e coloque em `app/google-services.json`.
4. No console:
   - **Authentication**: ative o provedor *E-mail/senha*.
   - **Realtime Database**: crie o banco.
   - Não precisa de Cloud Storage. Os PDFs ficam no aparelho e sincronizam em Base64 pelo Realtime Database. Por isso o limite é de 7 MB por PDF, já que o banco aceita até 10 MB por valor.
5. Publique as regras de segurança deste repositório (`database.rules.json`). Cole o conteúdo na aba *Regras* do Realtime Database e clique em *Publicar*.
6. Recompile. O plugin `google-services` só é aplicado quando o arquivo existe.

No primeiro acesso, use **"Sou empresa — criar conta de administrador"** e depois cadastre os signatários.

### Estrutura no Realtime Database

```
users/{uid}                    nome, e-mail, telefone, perfil, createdBy
documents/{docId}              título, mensagem, versão, status, datas,
                               signerUserIds{uid: true}, signers[], fields[], events[]
userDocuments/{uid}/{docId}    índice dos documentos recebidos por cada signatário
files/{docId}/original_pdf     PDF original (Base64)
files/{docId}/signatures/{id}_png
files/{docId}/final_pdf        PDF final assinado
```

## Fluxo para o vídeo de demonstração

1. Entrar como admin e cadastrar dois signatários.
2. Em "Novo documento": título, PDF, signatários com a classificação de cada um, campos arrastados sobre as linhas de assinatura, revisão e envio.
3. Abrir o acompanhamento: tudo pendente.
4. Sair, entrar como signatário 1, abrir o documento, desenhar a assinatura, salvar e ver o PDF.
5. Entrar como signatário 2 e assinar. O documento passa a **Concluído**.
6. Entrar como admin: status, histórico com datas e horários, **documento final** com a trilha de auditoria, compartilhamento e nova versão.
