package br.com.convite.gateway;

import br.com.convite.domain.Convite;

import java.util.List;
import java.util.Optional;

public interface ConviteGateway {
    List<Convite> listarTodos();
    Optional<Convite> buscarPorCodigo(String codigo);
    Optional<Convite> buscarPorId(String id);
    /** Busca convite combinando Código + ID para máxima assertividade */
    Optional<Convite> buscarPorIdECodigo(String id, String codigo);
    Optional<Convite> buscarPorCodigoEId(String codigo, String id);
    /** Busca convite por Código ou por ID (tenta código primeiro, fallback para ID) */
    Optional<Convite> buscarPorCodigoOuId(String termo);
    List<Convite> buscarPorTermo(String termo);
    /** Busca convites que possuem um membro com exatamente o nome informado */
    List<Convite> buscarPorNomeMembro(String nomeMembro);
    /** Busca assertiva de convidado específico: pelo CÓDIGO do convite + ID do convidado (membro) */
    Optional<br.com.convite.domain.MembroConvite> buscarConvidadoPorCodigoConviteEId(String codigoConvite, String membroId);
    Optional<br.com.convite.domain.MembroConvite> buscarConvidadoPorConviteIdEConvidadoId(String conviteIdOuCodigo, String membroId);
    Convite salvar(Convite convite);
    void excluir(Convite convite);
    long contarTotal();
}
