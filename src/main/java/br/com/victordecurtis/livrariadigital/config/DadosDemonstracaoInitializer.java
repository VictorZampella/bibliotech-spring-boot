package br.com.victordecurtis.livrariadigital.config;

import br.com.victordecurtis.livrariadigital.model.Autor;
import br.com.victordecurtis.livrariadigital.model.Categoria;
import br.com.victordecurtis.livrariadigital.model.Endereco;
import br.com.victordecurtis.livrariadigital.model.Livro;
import br.com.victordecurtis.livrariadigital.model.Usuario;
import br.com.victordecurtis.livrariadigital.repository.AutorRepository;
import br.com.victordecurtis.livrariadigital.repository.CategoriaRepository;
import br.com.victordecurtis.livrariadigital.repository.EnderecoRepository;
import br.com.victordecurtis.livrariadigital.repository.LivroRepository;
import br.com.victordecurtis.livrariadigital.repository.UsuarioRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/** Popula o H2 de demonstração com exemplos para facilitar os testes pelo Swagger. */
@Component
@Profile("dev")
public class DadosDemonstracaoInitializer implements CommandLineRunner {
    private static final String EMAIL_DOMAIN = "@example.com";
    private static final String SENHA_DEMO = "BiblioDemo123";

    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final EnderecoRepository enderecoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DadosDemonstracaoInitializer(AutorRepository autorRepository,
                                        CategoriaRepository categoriaRepository,
                                        EnderecoRepository enderecoRepository,
                                        LivroRepository livroRepository,
                                        UsuarioRepository usuarioRepository,
                                        PasswordEncoder passwordEncoder) {
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.enderecoRepository = enderecoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        criarLeitoresDeDemonstracao();
        criarLivrosDeDemonstracao();
    }

    private void criarLeitoresDeDemonstracao() {
        Endereco endereco = enderecoRepository.findById("01001000").orElseGet(() -> {
            Endereco novo = new Endereco();
            novo.setCep("01001000");
            novo.setLogradouro("Praça da Sé");
            novo.setBairro("Sé");
            novo.setLocalidade("São Paulo");
            novo.setUf("SP");
            novo.setIbge("3550308");
            novo.setDdd("11");
            return enderecoRepository.save(novo);
        });

        criarLeitor("Leitor Demonstração 1", "leitor.demo1" + EMAIL_DOMAIN, "(11) 93000-0001", "1995-05-10", endereco);
        criarLeitor("Leitor Demonstração 2", "leitor.demo2" + EMAIL_DOMAIN, "(11) 93000-0002", "1990-08-20", endereco);
        criarLeitor("Leitor Demonstração 3", "leitor.demo3" + EMAIL_DOMAIN, "(11) 93000-0003", "2000-02-15", endereco);
    }

    private void criarLeitor(String nome, String email, String telefone, String nascimento, Endereco endereco) {
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setSenha(passwordEncoder.encode(SENHA_DEMO));
        usuario.setDataNascimento(data(nascimento));
        usuario.setEndereco(endereco);
        usuarioRepository.save(usuario);
    }

    private void criarLivrosDeDemonstracao() {
        adicionarLivro("A Hora da Estrela", "Clarice Lispector", "Drama",
                "A trajetória de Macabéa pelas ruas do Rio de Janeiro.",
                "Macabéa é uma jovem nordestina que vive no Rio de Janeiro e enfrenta a rotina com sonhos simples. A narrativa acompanha seus encontros, desejos e descobertas.",
                "https://example.com/capas/a-hora-da-estrela.jpg", "34.90", 88, "1977-10-26");
        adicionarLivro("1984", "George Orwell", "Ficção Científica",
                "Um homem questiona o poder em uma sociedade vigiada.",
                "Em uma sociedade controlada pelo Grande Irmão, Winston Smith tenta preservar sua liberdade de pensamento e compreender a verdade por trás das regras do regime.",
                "https://example.com/capas/1984.jpg", "39.90", 328, "1949-06-08");
        adicionarLivro("Harry Potter e a Pedra Filosofal", "J.K. Rowling", "Fantasia",
                "Um jovem descobre um mundo de magia e amizade.",
                "Ao completar onze anos, Harry Potter descobre que é um bruxo e começa seus estudos em Hogwarts, onde faz amigos e conhece os mistérios de seu passado.",
                "https://example.com/capas/harry-potter.jpg", "49.90", 264, "1997-06-26");
        adicionarLivro("Assassinato no Expresso do Oriente", "Agatha Christie", "Policial",
                "Um crime inesperado desafia a investigação de Poirot.",
                "Durante uma viagem de trem interrompida pela neve, Hercule Poirot investiga um assassinato e reúne pistas entre os passageiros do Expresso do Oriente.",
                "https://example.com/capas/expresso-do-oriente.jpg", "42.50", 256, "1934-01-01");
        adicionarLivro("O Alienista", "Machado de Assis", "Conto",
                "Um médico investiga os limites entre razão e loucura.",
                "Na cidade de Itaguaí, o médico Simão Bacamarte funda a Casa Verde e passa a estudar o comportamento dos moradores, provocando dúvidas sobre a própria ciência.",
                "https://example.com/capas/o-alienista.jpg", "24.90", 96, "1882-01-01");
    }

    private void adicionarLivro(String titulo, String nomeAutor, String nomeCategoria,
                                String descricaoCurta, String descricaoLonga, String urlCapa,
                                String preco, int paginas, String publicacao) {
        boolean jaExiste = livroRepository.findAll().stream()
                .anyMatch(livro -> livro.getTitulo().equalsIgnoreCase(titulo));
        if (jaExiste) {
            return;
        }

        Autor autor = autorRepository.findAll().stream()
                .filter(item -> item.getNome().equalsIgnoreCase(nomeAutor))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Autor de demonstração não encontrado: " + nomeAutor));
        Categoria categoria = categoriaRepository.findAll().stream()
                .filter(item -> item.getNome().toLowerCase(Locale.ROOT).equals(nomeCategoria.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Categoria de demonstração não encontrada: " + nomeCategoria));

        Livro livro = new Livro();
        livro.setId(UUID.randomUUID());
        livro.setTitulo(titulo);
        livro.setAutor(autor);
        livro.setDescricaoCurta(descricaoCurta);
        livro.setDescricaoLonga(descricaoLonga);
        livro.setUrlCapa(urlCapa);
        livro.setPreco(new BigDecimal(preco));
        livro.setCategoria(categoria);
        livro.setQuantidadePaginas(paginas);
        livro.setAnoPublicacao(data(publicacao));
        livroRepository.save(livro);
    }

    private Date data(String isoDate) {
        return Date.from(LocalDate.parse(isoDate).atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
