package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.SincronizarPresencaFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SincronizarPresencaFornecedorUseCaseImpl implements SincronizarPresencaFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public void executar(MembroConvite membro, boolean presente, LocalDateTime agora) {
        if (membro == null || membro.getNome() == null) return;
        String nomeNorm = normalizar(membro.getNome());

        List<Fornecedor> fornecedores = fornecedorGateway.listarTodos();
        for (Fornecedor f : fornecedores) {
            boolean alterou = false;
            if (f.getEquipe() != null) {
                for (MembroEquipeFornecedor mEquipe : f.getEquipe()) {
                    String mNorm = normalizar(mEquipe.getNome());
                    if (mNorm.equalsIgnoreCase(nomeNorm) || mNorm.contains(nomeNorm) || nomeNorm.contains(mNorm)) {
                        mEquipe.setPresente(presente);
                        mEquipe.setDataHoraEntrada(presente ? agora : null);
                        alterou = true;
                    }
                }
            }
            if (alterou) {
                f.setUpdatedAt(agora);
                fornecedorGateway.salvar(f);
            }
        }
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
