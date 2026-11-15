package br.com.victordecurtis.livrariadigital.assembler;

import br.com.victordecurtis.livrariadigital.controller.CategoriaController;
import br.com.victordecurtis.livrariadigital.model.Categoria;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaModelAssembler implements RepresentationModelAssembler<Categoria, EntityModel<Categoria>> {

    @Override
    public EntityModel<Categoria> toModel(Categoria categoria) {
        var id = categoria.getId();
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).consultarCategoria(id)).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).criarOuAtualizarCategoria(id, null)).withRel("update"),
                linkTo(methodOn(CategoriaController.class).removerCategoria(id)).withRel("delete"),
                linkTo(methodOn(CategoriaController.class).listarCategorias(null)).withRel("categorias"));
    }
}
