package br.com.victordecurtis.livrariadigital.assembler;

import br.com.victordecurtis.livrariadigital.controller.AutorController;
import br.com.victordecurtis.livrariadigital.model.Autor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AutorModelAssembler implements RepresentationModelAssembler<Autor, EntityModel<Autor>> {

    @Override
    public EntityModel<Autor> toModel(Autor autor) {
        var id = autor.getId();
        return EntityModel.of(autor,
                linkTo(methodOn(AutorController.class).consultarAutor(id)).withSelfRel(),
                linkTo(methodOn(AutorController.class).criarOuAtualizarAutor(id, null)).withRel("update"),
                linkTo(methodOn(AutorController.class).removerAutor(id)).withRel("delete"),
                linkTo(methodOn(AutorController.class).listarAutores(null)).withRel("autores"));
    }
}
