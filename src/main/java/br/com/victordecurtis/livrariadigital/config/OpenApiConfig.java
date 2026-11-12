package br.com.victordecurtis.livrariadigital.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@OpenAPIDefinition(
        info = @Info(
                title = "BiblioTech",
                version = "1.0",
                description = """
                        ## Sobre a BiblioTech

                        API REST para organizar os leitores e o catálogo de uma biblioteca. Permite cadastrar, consultar, pesquisar, atualizar e remover livros, autores e categorias. No cadastro de leitores, o endereço pode ser preenchido a partir do CEP usando o ViaCEP.

                        ## Como começar

                        Para cadastrar um livro, primeiro cadastre o autor em **POST `/api/autores`** e a categoria em **POST `/api/categorias`**. Copie o UUID retornado em cada resposta e informe-os nos campos `autor.id` e `categoria.id` do **POST `/api/livros`**. Um mesmo autor ou categoria pode ser usado em vários livros.

                        Os identificadores são UUIDs gerados pela API. As listagens aceitam paginação: `page` começa em 0, `size` define quantos itens aparecem por página e `sort` permite ordenar os resultados. O **PUT** cria um registro quando o UUID ainda não existe (201) ou atualiza quando ele já existe (200).

                        Para experimentar as rotas, abra uma operação, clique em **Try it out**, preencha os dados e selecione **Execute**. As respostas e mensagens de validação aparecem logo abaixo.

                        Para remover um cadastro, primeiro faça login em **POST `/api/auth/login`** com o e-mail e a senha de um leitor. Copie a chave retornada e use **Authorize** no Swagger para informar o valor de `X-API-Key`; essa chave é exigida nos **DELETE**. Use **POST `/api/auth/logout`** para revogá-la.

                        Cada IP pode fazer até **20 requisições por minuto** às rotas da API. Ao ultrapassar o limite, a resposta é **429 Too Many Requests**; aguarde o tempo informado no cabeçalho `Retry-After` antes de tentar novamente.
                        """,
                contact = @Contact(
                        name = "Victor de Curtis",
                        email = "victorcurtiss@outlook.com",
                        url = "https://meu-portfolio-five-neon.vercel.app/"
                ),
                termsOfService = "/termos-de-uso.html"
        ),
        servers = @Server(
                description = "BiblioTech",
                url = "/"
        ))
@SecurityScheme(name = "ApiKeyAuth", type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER, paramName = "X-API-Key", description = "Chave emitida por POST /api/auth/login; necessária nas operações DELETE.")
public class OpenApiConfig {
}

