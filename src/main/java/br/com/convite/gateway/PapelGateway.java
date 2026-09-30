package br.com.convite.gateway;

import br.com.convite.domain.PapelParticipante;

import java.util.List;
import java.util.Optional;

public interface PapelGateway {
    List<PapelParticipante> listarTodos();
    Optional<PapelParticipante> buscarPorId(String id);
    Optional<PapelParticipante> buscarPorNome(String nome);
    PapelParticipante salvar(PapelParticipante papel);
    void excluir(String id);
    boolean existePorNome(String nome);
}
