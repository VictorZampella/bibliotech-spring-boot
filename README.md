# BiblioTech — API de gerenciamento de biblioteca

API REST desenvolvida com Spring Boot para administrar leitores, autores, categorias e livros. O projeto oferece operações de cadastro, consulta, atualização e remoção, além de documentação interativa para explorar os endpoints.

**Autor:** Victor de Curtis  
**Website:** [meu-portfolio-five-neon.vercel.app](https://meu-portfolio-five-neon.vercel.app/)  
**Contato:** [victorcurtiss@outlook.com](mailto:victorcurtiss@outlook.com)

## Sumário

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Requisitos](#requisitos)
- [Como executar](#como-executar)
- [Documentação Swagger](#documentação-swagger)
- [Endpoints](#endpoints)
- [Dados e validações](#dados-e-validações)
- [Códigos HTTP](#códigos-http)
- [Licença](#licença)

## Funcionalidades

- Cadastro, consulta, atualização e remoção de leitores.
- Cadastro, consulta, atualização e remoção de autores.
- Cadastro, consulta, atualização e remoção de categorias.
- Cadastro, consulta, atualização e remoção de livros.
- Pesquisa de livros por palavra-chave.
- Validação de campos com Bean Validation.
- Consulta de endereço por CEP usando a API ViaCEP durante o cadastro/atualização de leitores.
- Documentação da API gerada com OpenAPI e Swagger UI.
- Respostas navegáveis com links HATEOAS e paginação.
- Cadastro idempotente por `X-Idempotency-Key`.
- Papéis de acesso, planos e limites de requisições por IP e por chave.
- Consulta de livro nas versões 1 e 2 por meio do cabeçalho `X-API-Version`.

## Tecnologias

- Java 17
- Spring Boot 3.1.4
- Spring Web
- Spring Data JPA / Hibernate
- Spring Validation
- Spring Cloud OpenFeign
- Springdoc OpenAPI / Swagger UI
- H2 para desenvolvimento
- PostgreSQL para configuração de produção
- Maven e Lombok

## Requisitos

- JDK 17 ou superior
- Conexão com a internet para consultar o ViaCEP
- Para o perfil de produção: PostgreSQL configurado e acessível

## Como executar

### Desenvolvimento com H2

Na raiz do projeto, execute:

**Windows (PowerShell ou Prompt de Comando):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux ou macOS:**

```bash
./mvnw spring-boot:run
```

A aplicação usa o perfil `dev` por padrão e configura um banco H2 em memória. Os dados desse banco são temporários e podem ser apagados quando a aplicação for encerrada.

O console H2, quando habilitado, fica disponível em [http://localhost:8080/h2-console](http://localhost:8080/h2-console).

### Empacotar e executar

```bash
./mvnw clean package
java -jar target/LivrariaDigital-0.0.1-SNAPSHOT.jar
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

### Perfil PostgreSQL

O perfil `prod` requer um PostgreSQL configurado. Antes de executá-lo, ajuste `src/main/resources/application-prod.properties` para os dados do seu ambiente. Não publique senhas ou credenciais reais no repositório.

Para selecionar outro perfil:

**PowerShell:**

```powershell
$env:APP_PROFILE="prod"
.\mvnw.cmd spring-boot:run
```

**Linux ou macOS:**

```bash
APP_PROFILE=prod ./mvnw spring-boot:run
```

## Documentação Swagger

Com a aplicação em execução, acesse:

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Especificação OpenAPI em JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

No Swagger UI, expanda um grupo, clique em **Try it out**, preencha os parâmetros e o corpo JSON quando necessário e clique em **Execute**. Os IDs dos registros são UUIDs.

## Acesso e recursos avançados

O cadastro público em `POST /api/usuarios` cria leitores como **MEMBRO** no plano **BASE**. Papéis e planos são definidos pelo servidor; enviar `papel` ou `plano` no JSON não concede acesso.

- **MEMBRO BASE:** pode consultar livros, autores, categorias e leitores; não pode cadastrar, atualizar ou remover registros. O cadastro público de leitores continua disponível em `POST /api/usuarios`.
- **MEMBRO PREMIUM:** pode consultar, cadastrar, atualizar e remover livros, autores, categorias e leitores, inclusive apagar leitores.
- **ADMIN:** pode administrar todos os recursos, independentemente do plano.
- **BASE:** até 30 requisições por minuto por chave de API.
- **PREMIUM:** até 100 requisições por minuto por chave de API.
- **Limite por IP:** até 120 requisições por minuto, aplicado junto ao limite da chave quando ela é enviada.

Ao exceder um limite, a API responde `429 Too Many Requests`, informa quando tentar novamente no cabeçalho `Retry-After` e mostra os limites/saldos em `X-Rate-Limit-IP-*` e `X-Rate-Limit-Key-*`. As contagens ficam na memória do processo: reiniciar a aplicação zera os contadores e várias instâncias não compartilham a contagem.

Para autorizar uma conta no Render, cadastre o leitor, copie o `usuarioId` retornado e adicione no serviço as variáveis de ambiente `BIBLIOTECH_ADMIN_USER_IDS` (UUIDs de administradores) e, se necessário, `BIBLIOTECH_PREMIUM_USER_IDS` (UUIDs com plano Premium). Para mais de uma conta, separe os UUIDs por vírgula. Faça login novamente após alterar as variáveis. No ambiente local, o leitor `leitor.demo1@example.com` com senha `BiblioDemo123` é Admin; `leitor.demo3@example.com` tem plano Premium.

O papel **ADMIN** dá acesso completo. Para leitores com papel **MEMBRO**, o plano **BASE** permite apenas consultas e o plano **PREMIUM** permite também cadastrar, atualizar e remover registros. O cadastro público de leitores em `POST /api/usuarios` é a exceção: não exige chave e cria uma conta no plano BASE.

Depois do login em `POST /api/auth/login`, copie a chave exibida uma única vez e use o botão **Authorize** do Swagger ou envie `X-API-Key` no pedido. Uma chave inválida retorna `401`; uma conta sem permissão para a operação retorna `403`. Para revogar a chave, envie-a em `POST /api/auth/logout`.

O `POST` de livros, autores, categorias e leitores usa `X-Idempotency-Key`: gere uma chave única para cada nova criação e reutilize a mesma chave somente ao repetir aquela criação. Assim, uma repetição acidental devolve o cadastro já criado em vez de duplicá-lo.

O `GET /api/livros/{id}` usa a versão 1 por padrão. Para a versão 2, envie `X-API-Version: 2`; o livro continua sendo o mesmo, mas autor e categoria são apresentados de forma resumida. Envie `X-API-Version: 1` para pedir explicitamente a representação original.

O campo opcional `quantidadeExemplares` aceita zero ou valores positivos. Se for omitido, assume zero; valores negativos são rejeitados com `400`.

No perfil de produção, `schema-prod.sql` adiciona os campos novos com valores padrão compatíveis com os registros existentes e cria uma restrição no PostgreSQL para impedir exemplares negativos. No perfil de desenvolvimento, o H2 recebe a mesma proteção ao criar as tabelas.

## Endpoints

A base local é `http://localhost:8080`.

| Recurso | Método | Rota | Descrição |
|---|---|---|---|
| Leitores | GET | `/api/usuarios` | Lista os leitores cadastrados |
| Leitores | GET | `/api/usuarios/{id}` | Consulta um leitor pelo UUID |
| Leitores | POST | `/api/usuarios` | Cadastra um leitor |
| Leitores | PUT | `/api/usuarios/{id}` | Atualiza um leitor existente |
| Leitores | DELETE | `/api/usuarios/{id}` | Remove um leitor |
| Autores | GET | `/api/autores` | Lista os autores cadastrados |
| Autores | GET | `/api/autores/{id}` | Consulta um autor pelo UUID |
| Autores | POST | `/api/autores` | Cadastra um autor |
| Autores | PUT | `/api/autores/{id}` | Atualiza um autor existente |
| Autores | DELETE | `/api/autores/{id}` | Remove um autor |
| Categorias | GET | `/api/categorias` | Lista as categorias cadastradas |
| Categorias | GET | `/api/categorias/{id}` | Consulta uma categoria pelo UUID |
| Categorias | POST | `/api/categorias` | Cadastra uma categoria |
| Categorias | PUT | `/api/categorias/{id}` | Atualiza uma categoria existente |
| Categorias | DELETE | `/api/categorias/{id}` | Remove uma categoria |
| Livros | GET | `/api/livros` | Lista os livros cadastrados |
| Livros | GET | `/api/livros/{id}` | Consulta um livro pelo UUID |
| Livros | GET | `/api/livros/pesquisa?termo=palavra` | Pesquisa livros por palavra-chave |
| Livros | POST | `/api/livros` | Cadastra um livro |
| Livros | PUT | `/api/livros/{id}` | Atualiza um livro existente |
| Livros | DELETE | `/api/livros/{id}` | Remove um livro |

### Exemplo: cadastrar uma categoria

```http
POST /api/categorias
Content-Type: application/json
```

```json
{
  "nome": "Ficção científica"
}
```

### Exemplo: atualizar uma categoria

```http
PUT /api/categorias/SEU_UUID
Content-Type: application/json
```

```json
{
  "nome": "Fantasia"
}
```

## Dados e validações

Os corpos das requisições devem ser enviados em JSON. O identificador é um UUID gerado pela aplicação; em atualizações e consultas por ID, informe o UUID na rota.

- **Leitor:** nome, e-mail, telefone, senha, data de nascimento e endereço com CEP. O serviço consulta o ViaCEP para obter os dados do endereço.
- **Autor:** nome, data de nascimento, biografia, URL da foto e nacionalidade.
- **Categoria:** nome.
- **Livro:** título, autor, descrições curta e longa, URL da capa, preço, categoria, quantidade de páginas, quantidade de exemplares (opcional) e ano/data de publicação.

As datas de nascimento de autores e a data de publicação de livros são documentadas no formato `yyyy-MM-dd`. Campos obrigatórios e regras de formato são validados pela API; erros de validação retornam detalhes em JSON.

## Códigos HTTP

Os códigos abaixo refletem a implementação atual dos controllers:

- `200 OK`: consulta bem-sucedida, login ou atualização de cadastro existente por PUT.
- `201 Created`: cadastro criado por POST ou criação por PUT quando o UUID ainda não existe.
- `204 No Content`: remoção executada com sucesso.
- `400 Bad Request`: dados enviados inválidos.
- `401 Unauthorized`: chave ausente/inválida em rota protegida ou credenciais de login incorretas.
- `403 Forbidden`: chave válida sem plano ou papel autorizado para a operação.
- `404 Not Found`: registro não encontrado em consultas por ID ou ao tentar atualizar um registro inexistente.
- `409 Conflict`: conflito de regra de negócio, como remoção de leitor não encontrado.
- `429 Too Many Requests`: limite por IP ou por chave excedido; aguarde o valor de `Retry-After`.
- `500 Internal Server Error`: erro inesperado no servidor.

**Comportamento do PUT:** se o UUID já existir, atualiza o cadastro e responde `200`; se não existir, cria o registro com o UUID informado e responde `201`.

## Licença

Distribuído sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para ver os termos.

