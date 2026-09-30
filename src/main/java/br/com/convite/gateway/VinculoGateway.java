package br.com.convite.gateway;

import br.com.convite.domain.VinculoParticipante;

import java.util.List;
import java.util.Optional;

public interface VinculoGateway {
    List<VinculoParticipante> listarTodos();
    Optional<VinculoParticipante> buscarPorId(String id);
    Optional<VinculoParticipante> buscarPorNome(String nome);
    VinculoParticipante salvar(VinculoParticipante vinculo);
    void excluir(String id);
    boolean existePorNome(String nome);
}
