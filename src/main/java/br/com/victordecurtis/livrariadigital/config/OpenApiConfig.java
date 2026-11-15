package br.com.victordecurtis.livrariadigital.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
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

                        Para testar uma operação, abra-a e clique em **Try it out** (testar). Preencha os campos e clique em **Execute** (executar). A resposta e eventuais mensagens de validação aparecem logo abaixo.

                        Para remover um cadastro, primeiro faça login em **POST `/api/auth/login`** com o e-mail e a senha de um leitor. Copie a chave retornada e clique no cadeado **Authorize** (autorizar) para informar o valor de `X-API-Key`; essa chave é exigida nas operações **DELETE**. Use **POST `/api/auth/logout`** para revogá-la.

                        Cada endereço IP pode fazer até **20 requisições por minuto**. Ao ultrapassar esse limite, a API responde com o código **429 (Muitas requisições)**. Aguarde o tempo informado no cabeçalho `Retry-After` antes de tentar novamente.
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

    /**
     * Substitui as descrições automáticas em inglês do Pageable e das respostas
     * padrão por orientações em português, mais fáceis de entender no Swagger.
     */
    @Bean
    public OperationCustomizer descricoesSwaggerEmPortugues() {
        return (operation, handlerMethod) -> {
            traduzirParametrosDePaginacao(operation);
            traduzirRespostasPadrao(operation);
            return operation;
        };
    }

    private void traduzirParametrosDePaginacao(Operation operation) {
        if (operation.getParameters() == null) {
            return;
        }

        Map<String, String> descricoes = Map.of(
                "page", "Número da página que deseja consultar. A contagem começa em 0: a página 0 é a primeira.",
                "size", "Quantidade máxima de registros exibidos por página. Exemplo: 10.",
                "sort", "Ordena os resultados. Informe o campo e a direção separados por vírgula: asc para crescente ou desc para decrescente. Exemplo: nome,asc."
        );

        for (Parameter parameter : operation.getParameters()) {
            String descricao = descricoes.get(parameter.getName());
            if (descricao != null) {
                parameter.setDescription(descricao);
            }
        }
    }

    private void traduzirRespostasPadrao(Operation operation) {
        if (operation.getResponses() == null) {
            return;
        }

        Map<String, String> descricoes = Map.of(
                "Bad Request", "Requisição inválida. Confira os dados enviados.",
                "Unauthorized", "Acesso não autorizado. Informe uma chave X-API-Key válida.",
                "Not Found", "O registro solicitado não foi encontrado.",
                "Conflict", "Os dados entram em conflito com um cadastro existente.",
                "Too Many Requests", "Limite de requisições atingido. Aguarde o tempo indicado em Retry-After.",
                "Internal Server Error", "Erro interno do servidor. Tente novamente mais tarde."
        );

        operation.getResponses().values().forEach(response -> {
            String descricaoEmPortugues = descricoes.get(response.getDescription());
            if (descricaoEmPortugues != null) {
                response.setDescription(descricaoEmPortugues);
            }
        });
    }
}

