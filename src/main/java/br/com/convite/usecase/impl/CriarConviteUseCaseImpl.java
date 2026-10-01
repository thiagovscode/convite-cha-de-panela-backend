package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.ConflitoNegocioException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.CriarConviteUseCase;
import br.com.convite.usecase.DefinirParCortejoUseCase;
import br.com.convite.usecase.GerarCodigoConviteUnicoUseCase;
import br.com.convite.usecase.SincronizarCortejoConviteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriarConviteUseCaseImpl implements CriarConviteUseCase {

    private final ConviteGateway conviteGateway;
    private final GerarCodigoConviteUnicoUseCase gerarCodigoConviteUnicoUseCase;
    private final SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;
    private final DefinirParCortejoUseCase definirParCortejoUseCase;

    @Override
    public Convite executar(Convite dados) {
        if (dados == null) {
            throw new RegraDeNegocioException("Dados do convite não fornecidos.");
        }

        if (dados.getFamilia() == null || dados.getFamilia().trim().isBlank()) {
            throw new RegraDeNegocioException("O nome da família ou do convidado principal é obrigatório.");
        }

        // Define código: auto-gerado ou customizado com validação de unicidade
        String codigo;
        if (dados.getCodigo() == null || dados.getCodigo().trim().isBlank()) {
            codigo = gerarCodigoConviteUnicoUseCase.executar();
        } else {
            codigo = dados.getCodigo().trim().toLowerCase().replaceAll("[^a-z0-9-_]", "");
            if (codigo.length() < 4) {
                throw new RegraDeNegocioException("O código do convite deve ter pelo menos 4 caracteres válidos (letras e números).");
            }
            if (codigo.length() > 40) {
                throw new RegraDeNegocioException("O código do convite não pode ter mais de 40 caracteres.");
            }
            // Na criação, impede conflito se o código já existir
            if (conviteGateway.buscarPorCodigo(codigo).isPresent()) {
                throw new ConflitoNegocioException("Já existe um convite cadastrado com o código '" + codigo + "'. Escolha outro código.");
            }
        }

        List<MembroConvite> membrosProcessados = processarMembrosNovos(dados.getMembros());

        LocalDateTime agora = LocalDateTime.now();
        Convite novoConvite = Convite.builder()
                .codigo(codigo)
                .familia(dados.getFamilia().trim())
                .telefone(dados.getTelefone() != null && !dados.getTelefone().isBlank() ? dados.getTelefone().trim() : null)
                .email(dados.getEmail() != null && !dados.getEmail().isBlank() ? dados.getEmail().trim() : null)
                .observacao(dados.getObservacao() != null && !dados.getObservacao().isBlank() ? dados.getObservacao().trim() : null)
                .status("PENDENTE")
                .createdAt(agora)
                .updatedAt(agora)
                .membros(membrosProcessados)
                .build();

        Convite salvo = conviteGateway.salvar(novoConvite);

        // Sincroniza participantes do cortejo se houver
        sincronizarCortejoConviteUseCase.executar(salvo);

        // Sincroniza pares recíprocos se informados
        if (salvo.getMembros() != null) {
            for (MembroConvite m : salvo.getMembros()) {
                if (m.getPar() != null && !m.getPar().isBlank()) {
                    definirParCortejoUseCase.executar(salvo.getCodigo(), m.getId() != null ? m.getId().toString() : null, m.getNome(), m.getPar());
                }
            }
        }

        return salvo;
    }

    private List<MembroConvite> processarMembrosNovos(List<MembroConvite> membros) {
        if (membros == null || membros.isEmpty()) {
            throw new RegraDeNegocioException("Pelo menos um membro válido deve ser cadastrado.");
        }

        List<MembroConvite> resultado = new ArrayList<>();
        for (MembroConvite m : membros) {
            if (m.getNome() == null || m.getNome().trim().isBlank()) continue;

            resultado.add(MembroConvite.builder()
                    .id(m.getId())
                    .nome(m.getNome().trim())
                    .criancaAte6Anos(Boolean.TRUE.equals(m.getCriancaAte6Anos()))
                    .papel(m.getPapel())
                    .par(m.getPar() != null && !m.getPar().isBlank() ? m.getPar().trim() : null)
                    .participaCortejo(m.getParticipaCortejo())
                    .build());
        }

        if (resultado.isEmpty()) {
            throw new RegraDeNegocioException("Pelo menos um membro válido com nome preenchido deve ser cadastrado.");
        }

        return resultado;
    }
}
