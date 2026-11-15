package br.com.victordecurtis.livrariadigital.assembler;

import br.com.victordecurtis.livrariadigital.controller.AutorController;
import br.com.victordecurtis.livrariadigital.controller.CategoriaController;
import br.com.victordecurtis.livrariadigital.controller.LivroController;
import br.com.victordecurtis.livrariadigital.dto.LivroVersao2Response;
import br.com.victordecurtis.livrariadigital.model.Livro;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LivroModelAssembler implements RepresentationModelAssembler<Livro, EntityModel<Livro>> {

    @Override
    public EntityModel<Livro> toModel(Livro livro) {
        return EntityModel.of(livro, linksDoLivro(livro));
    }

    public EntityModel<LivroVersao2Response> toModelVersao2(Livro livro) {
        return EntityModel.of(LivroVersao2Response.de(livro), linksDoLivro(livro));
    }

    private Link[] linksDoLivro(Livro livro) {
        var id = livro.getId();
        return new Link[]{
                linkTo(methodOn(LivroController.class).consultarLivro(id, null)).withSelfRel(),
                linkTo(methodOn(LivroController.class).criarOuAtualizarLivro(id, null)).withRel("update"),
                linkTo(methodOn(LivroController.class).removerLivro(id)).withRel("delete"),
                linkTo(methodOn(LivroController.class).listarLivros(null)).withRel("livros"),
                linkTo(methodOn(AutorController.class).consultarAutor(livro.getAutor().getId())).withRel("autor"),
                linkTo(methodOn(CategoriaController.class).consultarCategoria(livro.getCategoria().getId())).withRel("categoria")
        };
    }
}
