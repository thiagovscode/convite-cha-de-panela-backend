package br.com.convite.gateway;

import br.com.convite.domain.Convite;

import java.util.List;
import java.util.Optional;

public interface ConviteGateway {
    List<Convite> listarTodos();
    Optional<Convite> buscarPorCodigo(String codigo);
    Optional<Convite> buscarPorId(String id);
    List<Convite> buscarPorTermo(String termo);
    Convite salvar(Convite convite);
    void excluir(Convite convite);
    long contarTotal();
}
