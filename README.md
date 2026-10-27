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
| Livros | GET | `/api/livros/{busca}` | Pesquisa livros por palavra-chave |
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
- **Livro:** título, autor, descrições curta e longa, URL da capa, preço, categoria, quantidade de páginas e ano/data de publicação.

As datas de nascimento de autores e a data de publicação de livros são documentadas no formato `yyyy-MM-dd`. Campos obrigatórios e regras de formato são validados pela API; erros de validação retornam detalhes em JSON.

## Códigos HTTP

Os códigos abaixo refletem a implementação atual dos controllers:

- `200 OK`: consulta bem-sucedida; também é usado nos PUTs que atualizam registros existentes. Os POSTs de categoria e livro também respondem `200` atualmente.
- `201 Created`: cadastro criado pelos POSTs de leitor e autor.
- `204 No Content`: remoção executada com sucesso.
- `400 Bad Request`: dados enviados inválidos.
- `404 Not Found`: registro não encontrado em consultas por ID ou ao tentar atualizar um registro inexistente.
- `409 Conflict`: conflito de regra de negócio, como remoção de leitor não encontrado.
- `500 Internal Server Error`: erro inesperado no servidor.

**Comportamento do PUT:** nesta versão, o PUT atualiza um registro existente; se o UUID não existir, a resposta é `404`. Ele ainda não cria registros novos (upsert). A paginação também não está implementada nesta versão: as listagens retornam uma lista simples.

## Licença

Distribuído sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para ver os termos.

