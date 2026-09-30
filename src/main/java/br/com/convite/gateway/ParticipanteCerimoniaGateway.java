package br.com.convite.gateway;

import br.com.convite.domain.ParticipanteCerimonia;

import java.util.List;
import java.util.Optional;

public interface ParticipanteCerimoniaGateway {
    List<ParticipanteCerimonia> listarTodos();
    Optional<ParticipanteCerimonia> buscarPorId(String id);
    List<ParticipanteCerimonia> buscarPorCodigoConvite(String codigoConvite);
    Optional<ParticipanteCerimonia> buscarPorNome(String nome);
    ParticipanteCerimonia salvar(ParticipanteCerimonia participante);
    void excluirPorId(String id);
}
