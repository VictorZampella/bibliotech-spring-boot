package br.com.victordecurtis.livrariadigital.assembler;

import br.com.victordecurtis.livrariadigital.controller.UsuarioController;
import br.com.victordecurtis.livrariadigital.model.Usuario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>> {

    @Override
    public EntityModel<Usuario> toModel(Usuario usuario) {
        var id = usuario.getId();
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).consultarLeitor(id)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).criarOuAtualizarLeitor(id, null)).withRel("update"),
                linkTo(methodOn(UsuarioController.class).removerLeitor(id)).withRel("delete"),
                linkTo(methodOn(UsuarioController.class).listarLeitores(null)).withRel("leitores"));
    }
}
