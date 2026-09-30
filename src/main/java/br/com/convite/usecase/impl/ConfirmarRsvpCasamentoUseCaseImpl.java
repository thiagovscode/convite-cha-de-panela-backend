package br.com.convite.usecase.impl;

import br.com.convite.domain.AcompanhanteCasamento;
import br.com.convite.domain.RsvpCasamento;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.RsvpCasamentoGateway;
import br.com.convite.usecase.ConfirmarRsvpCasamentoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmarRsvpCasamentoUseCaseImpl implements ConfirmarRsvpCasamentoUseCase {

    private static final int MAX_ACOMPANHANTES = 20;

    private final RsvpCasamentoGateway gateway;

    @Override
    public RsvpCasamento executar(RsvpCasamento rsvp) {
        validar(rsvp);
        normalizarTelefone(rsvp);
        atribuirIdsAcompanhantes(rsvp);
        return gateway.salvarOuAtualizar(rsvp);
    }

    private void validar(RsvpCasamento rsvp) {
        if (rsvp.getNome() == null || rsvp.getNome().isBlank()) {
            throw new RegraDeNegocioException("O nome e obrigatorio.");
        }
        if (rsvp.getNome().length() > 150) {
            throw new RegraDeNegocioException("O nome nao pode ter mais de 150 caracteres.");
        }
        if (rsvp.getTelefone() == null || rsvp.getTelefone().isBlank()) {
            throw new RegraDeNegocioException("O telefone e obrigatorio.");
        }
        if (rsvp.getPresenca() == null) {
            throw new RegraDeNegocioException("A confirmacao de presenca e obrigatoria.");
        }
        if (rsvp.getEmail() != null && !rsvp.getEmail().isBlank()) {
            if (!rsvp.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new RegraDeNegocioException("O e-mail informado nao e valido.");
            }
        }
        if (rsvp.getObservacao() != null && rsvp.getObservacao().length() > 500) {
            throw new RegraDeNegocioException("A observacao nao pode ter mais de 500 caracteres.");
        }

        List<AcompanhanteCasamento> acompanhantes = rsvp.getAcompanhantes();
        if (acompanhantes != null) {
            if (acompanhantes.size() > MAX_ACOMPANHANTES) {
                throw new RegraDeNegocioException("Numero maximo de acompanhantes e " + MAX_ACOMPANHANTES + ".");
            }
            for (int i = 0; i < acompanhantes.size(); i++) {
                AcompanhanteCasamento a = acompanhantes.get(i);
                if (a.getNome() == null || a.getNome().isBlank()) {
                    throw new RegraDeNegocioException("O nome do acompanhante " + (i + 1) + " e obrigatorio.");
                }
                if (a.getNome().length() > 150) {
                    throw new RegraDeNegocioException("O nome do acompanhante " + (i + 1) + " nao pode ter mais de 150 caracteres.");
                }
            }
        }
    }

    private void normalizarTelefone(RsvpCasamento rsvp) {
        String telNormalizado = rsvp.getTelefone().replaceAll("[^0-9]", "");
        if (telNormalizado.length() < 10 || telNormalizado.length() > 15) {
            throw new RegraDeNegocioException("Telefone invalido. Informe apenas os digitos (10 a 15 digitos).");
        }
        rsvp.setTelefone(telNormalizado);
    }

    private void atribuirIdsAcompanhantes(RsvpCasamento rsvp) {
        if (rsvp.getAcompanhantes() == null) return;
        rsvp.getAcompanhantes().forEach(a -> {
            if (a.getId() == null || a.getId().isBlank()) {
                a.setId(UUID.randomUUID().toString());
            }
        });
    }
}