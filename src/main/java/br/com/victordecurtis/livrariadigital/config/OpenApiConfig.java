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

                        ### Links e versões

                        As respostas de consulta, cadastro, atualização e listagem incluem links `_links` para navegar pela API. Em um livro, por exemplo, `self` abre o próprio livro, `update` aponta para a rota de atualização, `delete` indica a rota de remoção, e `autor` e `categoria` levam aos cadastros relacionados. Nas listas, os links `first`, `prev`, `self`, `next` e `last` ajudam a percorrer as páginas.

                        A consulta **GET `/api/livros/{id}`** aceita o cabeçalho `X-API-Version`. Sem esse cabeçalho, a API usa a versão **1**, que mantém autor e categoria como objetos completos. Com `X-API-Version: 2`, retorna uma representação resumida com `autorNome` e `categoriaNome`. As duas versões consultam o mesmo livro; muda apenas o formato da resposta.

                        ### Como acessar operações administrativas

                        1. Cadastre um leitor em **POST `/api/usuarios`** ou use uma conta existente.
                        2. Faça login em **POST `/api/auth/login`**. A resposta informa o papel da conta e mostra a chave `apiKey` uma única vez.
                        3. Para operações administrativas, a conta precisa ser **ADMIN**. No ambiente local, use o leitor de demonstração `leitor.demo1@example.com` com a senha `BiblioDemo123`. No Render, cadastre um leitor, copie o `usuarioId` retornado e configure esse UUID em `BIBLIOTECH_ADMIN_USER_IDS`; depois faça login novamente.
                        4. Clique em **Authorize** no topo da página, cole a chave no campo `X-API-Key` e confirme. Use então as rotas permitidas para o papel da conta.

                        O cadeado indica que a operação exige uma chave válida e um papel autorizado; basta informar a chave uma vez no botão **Authorize**. O Swagger pode exibir **Authorized** assim que o texto é salvo, mas isso não confirma que a chave é válida. Na execução, uma chave ausente ou inválida resulta em **401 (Não autorizado)**; uma chave válida de **MEMBRO** em uma rota administrativa resulta em **403 (Acesso proibido)**.

                        Para encerrar o acesso, execute **POST `/api/auth/logout`** com a chave atual. Depois do logout, faça login novamente para obter outra chave.

                        ### Papéis, planos e limites

                        Todo novo leitor recebe o papel **MEMBRO** e o plano **BASE**. Membros podem consultar livros; somente usuários **ADMIN** podem cadastrar, atualizar, remover ou acessar os demais recursos administrativos. O servidor valida o papel em cada requisição; enviar `papel` ou `plano` no corpo não altera essas permissões.

                        Os papéis e planos são independentes. O plano **BASE** permite 30 requisições por minuto por chave; o **PREMIUM**, 100. Além disso, cada IP tem limite de 120 requisições por minuto. Ao exceder qualquer limite, a API responde **429** com `Retry-After`; os cabeçalhos `X-Rate-Limit-IP-*` e `X-Rate-Limit-Key-*` mostram o limite e o saldo de cada controle.

                        Para promover uma conta com segurança, configure o UUID retornado no login em `BIBLIOTECH_ADMIN_USER_IDS` no ambiente do servidor. Para conceder o plano Premium, use `BIBLIOTECH_PREMIUM_USER_IDS`. Aceitam-se vários UUIDs separados por vírgula. O cliente não pode alterar esses valores no corpo de cadastro.

                        Livros aceitam `quantidadeExemplares` igual a zero ou maior; valores negativos retornam **400 (Requisição inválida)**. O campo é opcional para manter compatibilidade com cadastros antigos e assume zero quando omitido.

                        O controle de limite usa memória do processo: ele reinicia junto com a aplicação e não compartilha contagens entre múltiplas instâncias. Para uma demonstração em uma instância, isso mantém a configuração simples; ambientes com escala horizontal precisam de armazenamento compartilhado.
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
        in = SecuritySchemeIn.HEADER, paramName = "X-API-Key", description = "Cole aqui a apiKey retornada por POST /api/auth/login. As rotas administrativas exigem uma chave de usuário ADMIN; usuários MEMBRO podem consultar somente livros. O Swagger salva a chave, mas a API valida o papel em cada requisição.")
public class OpenApiConfig {

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

