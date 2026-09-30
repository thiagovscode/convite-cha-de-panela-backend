package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.CadastrarFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CadastrarFornecedorUseCaseImpl implements CadastrarFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public Fornecedor executar(Fornecedor req) {
        if (req.getEmpresa() == null || req.getEmpresa().trim().isBlank()) {
            throw new RegraDeNegocioException("O nome da empresa é obrigatório.");
        }

        String nome = (req.getNome() != null && !req.getNome().trim().isBlank()) ? req.getNome().trim() : req.getEmpresa().trim();
        String id = (req.getId() != null && !req.getId().isBlank()) ? req.getId().trim() : "fornecedor-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime agora = LocalDateTime.now();

        List<MembroEquipeFornecedor> equipe = new ArrayList<>();
        if (req.getEquipe() != null) {
            for (MembroEquipeFornecedor m : req.getEquipe()) {
                if (m.getNome() != null && !m.getNome().trim().isBlank()) {
                    equipe.add(MembroEquipeFornecedor.builder()
                            .id((m.getId() != null && !m.getId().isBlank()) ? m.getId().trim() : "membro-" + UUID.randomUUID().toString().substring(0, 8))
                            .nome(m.getNome().trim())
                            .funcao((m.getFuncao() != null && !m.getFuncao().trim().isBlank()) ? m.getFuncao().trim() : "Equipe")
                            .presente(Boolean.TRUE.equals(m.getPresente()))
                            .dataHoraEntrada(m.getDataHoraEntrada())
                            .build());
                }
            }
        }

        if (equipe.isEmpty()) {
            equipe.add(MembroEquipeFornecedor.builder()
                    .id("membro-" + UUID.randomUUID().toString().substring(0, 8))
                    .nome(nome)
                    .funcao("Responsável Principal")
                    .presente(false)
                    .build());
        }

        Fornecedor novo = Fornecedor.builder()
                .id(id)
                .nome(nome)
                .responsavel(nome)
                .papel("Fornecedor")
                .categoria(req.getCategoria() != null ? req.getCategoria().trim() : "Geral")
                .servico(req.getServico() != null ? req.getServico().trim() : "Serviço")
                .empresa(req.getEmpresa().trim())
                .telefone(req.getTelefone() != null ? req.getTelefone().trim() : null)
                .horarioPrevisto(req.getHorarioPrevisto() != null ? req.getHorarioPrevisto().trim() : "A definir")
                .instrucaoChegada(req.getInstrucaoChegada() != null ? req.getInstrucaoChegada().trim() : null)
                .chegadaAntecipada(Boolean.TRUE.equals(req.getChegadaAntecipada()))
                .equipe(equipe)
                .createdAt(agora)
                .updatedAt(agora)
                .build();

        return fornecedorGateway.salvar(novo);
    }
}
