package br.com.convite.gateway;

import br.com.convite.domain.RsvpCasamento;
import java.util.List;
import java.util.Optional;

public interface RsvpCasamentoGateway {
    RsvpCasamento salvarOuAtualizar(RsvpCasamento rsvp);
    List<RsvpCasamento> listarTodos();
    Optional<RsvpCasamento> buscarPorTelefone(String telefone);
    Optional<RsvpCasamento> buscarPorCodigoConvite(String codigoConvite);
    void deletar(String id);
    void deletarPorCodigoConvite(String codigoConvite);
}