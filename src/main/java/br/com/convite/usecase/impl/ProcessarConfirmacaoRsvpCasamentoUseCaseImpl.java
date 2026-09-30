package br.com.convite.usecase.impl;

import br.com.convite.domain.*;
import br.com.convite.exception.ConfirmacaoDuplicadaException;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.ConfirmarRsvpCasamentoUseCase;
import br.com.convite.usecase.ProcessarConfirmacaoRsvpCasamentoUseCase;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ProcessarConfirmacaoRsvpCasamentoUseCaseImpl implements ProcessarConfirmacaoRsvpCasamentoUseCase {

    public static final LocalDateTime PRAZO_LIMITE_RSVP = LocalDateTime.of(2026, 12, 23, 23, 59, 59);
    public static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    private final ConviteGateway conviteGateway;
    private final ParticipanteCerimoniaGateway participanteCerimoniaGateway;
    private final FornecedorGateway fornecedorGateway;
    private final ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase;
    private final Clock clock;

    public ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
            ConviteGateway conviteGateway,
            ParticipanteCerimoniaGateway participanteCerimoniaGateway,
            FornecedorGateway fornecedorGateway,
            ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase
    ) {
        this(conviteGateway, participanteCerimoniaGateway, fornecedorGateway, confirmarRsvpCasamentoUseCase, Clock.system(FUSO_HORARIO));
    }

    public ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
            ConviteGateway conviteGateway,
            ParticipanteCerimoniaGateway participanteCerimoniaGateway,
            FornecedorGateway fornecedorGateway,
            ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase,
            Clock clock
    ) {
        this.conviteGateway = conviteGateway;
        this.participanteCerimoniaGateway = participanteCerimoniaGateway;
        this.fornecedorGateway = fornecedorGateway;
        this.confirmarRsvpCasamentoUseCase = confirmarRsvpCasamentoUseCase;
        this.clock = clock != null ? clock : Clock.system(FUSO_HORARIO);
    }

    @Override
    public ResultadoProcessamentoRsvp executar(
            String codigoConvite,
            String nome,
            String telefone,
            String email,
            Boolean presenca,
            List<AcompanhanteCasamento> acompanhantes,
            String observacao
    ) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (agora.isAfter(PRAZO_LIMITE_RSVP)) {
            throw new RegraDeNegocioException("O prazo para confirmação ou alteração de presença encerrou em 23/12/2026. Por favor, entre em contato diretamente com os noivos.");
        }

        if (codigoConvite == null || codigoConvite.isBlank()) {
            throw new RegraDeNegocioException("A confirmação de presença é restrita à lista oficial. Informe um código de convite válido.");
        }

        Convite convite = conviteGateway.buscarPorCodigo(codigoConvite.trim())
                .orElseThrow(() -> new ConviteNaoEncontradoException(codigoConvite));

        if ("CONFIRMADO".equalsIgnoreCase(convite.getStatus())) {
            String telExistente = convite.getTelefone() != null ? convite.getTelefone().replaceAll("\\D", "") : "";
            String telNovo = telefone != null ? telefone.replaceAll("\\D", "") : "";
            if (!telExistente.isEmpty() && !telNovo.isEmpty() && !telExistente.equals(telNovo)) {
                throw new ConfirmacaoDuplicadaException();
            }
        }

        List<AcompanhanteCasamento> acompFinal = acompanhantes != null ? new ArrayList<>(acompanhantes) : new ArrayList<>();
        if (!Boolean.TRUE.equals(presenca)) {
            if (acompFinal.isEmpty() && convite.getMembros() != null) {
                for (MembroConvite m : convite.getMembros()) {
                    if (!Boolean.TRUE.equals(m.getTitular()) && (nome == null || !nome.equalsIgnoreCase(m.getNome()))) {
                        acompFinal.add(AcompanhanteCasamento.builder()
                                .nome(m.getNome())
                                .criancaAte6Anos(m.getCriancaAte6Anos())
                                .build());
                    }
                }
            }
        }

        RsvpCasamento rsvp = RsvpCasamento.builder()
                .nome(nome != null ? nome.trim() : null)
                .telefone(telefone != null ? telefone.trim() : null)
                .email(email != null && !email.isBlank() ? email.trim() : null)
                .presenca(presenca)
                .acompanhantes(acompFinal)
                .observacao(observacao != null && !observacao.isBlank() ? observacao.trim() : null)
                .build();

        RsvpCasamento salvo = confirmarRsvpCasamentoUseCase.executar(rsvp);

        sincronizarConvitePreDefinido(convite, rsvp);

        if (Boolean.TRUE.equals(salvo.getPresenca())) {
            int total = 1 + (salvo.getAcompanhantes() != null ? salvo.getAcompanhantes().size() : 0);
            long criancas = (salvo.getAcompanhantes() != null)
                    ? salvo.getAcompanhantes().stream().filter(a -> Boolean.TRUE.equals(a.getCriancaAte6Anos())).count()
                    : 0;
            long adultos = total - criancas;

            return new ResultadoProcessamentoRsvp(
                    true,
                    "Presenca confirmada com sucesso!",
                    total,
                    adultos,
                    criancas
            );
        } else {
            return new ResultadoProcessamentoRsvp(
                    false,
                    "Resposta registrada com sucesso.",
                    0,
                    0,
                    0
            );
        }
    }

    private void sincronizarConvitePreDefinido(Convite convite, RsvpCasamento rsvp) {
        boolean vai = Boolean.TRUE.equals(rsvp.getPresenca());
        convite.setStatus(vai ? "CONFIRMADO" : "RECUSADO");
        convite.setDataConfirmacao(LocalDateTime.now());
        convite.setUpdatedAt(LocalDateTime.now());

        Set<String> idsConfirmados = new HashSet<>();
        Set<String> nomesConfirmados = new HashSet<>();
        Map<String, Boolean> mapaCriancaPorId = new HashMap<>();
        Map<String, Boolean> mapaCriancaPorNome = new HashMap<>();

        if (vai) {
            if (rsvp.getNome() != null) {
                nomesConfirmados.add(limparNomeParaComparacao(rsvp.getNome()));
            }
            if (rsvp.getAcompanhantes() != null) {
                for (AcompanhanteCasamento a : rsvp.getAcompanhantes()) {
                    if (a.getId() != null && !a.getId().isBlank()) {
                        idsConfirmados.add(a.getId());
                        if (a.getCriancaAte6Anos() != null) {
                            mapaCriancaPorId.put(a.getId(), a.getCriancaAte6Anos());
                        }
                    }
                    if (a.getNome() != null) {
                        String clean = limparNomeParaComparacao(a.getNome());
                        nomesConfirmados.add(clean);
                        if (a.getCriancaAte6Anos() != null) {
                            mapaCriancaPorNome.put(clean, a.getCriancaAte6Anos());
                        }
                    }
                }
            }
        }

        if (convite.getMembros() != null) {
            for (MembroConvite m : convite.getMembros()) {
                if (!vai) {
                    m.setConfirmadoRsvp(false);
                } else {
                    String mClean = limparNomeParaComparacao(m.getNome());
                    String titularClean = rsvp.getNome() != null ? limparNomeParaComparacao(rsvp.getNome()) : "";

                    boolean confirmado;
                    if (m.getId() != null && !m.getId().isBlank() && !idsConfirmados.isEmpty()) {
                        // Confirma se o ID estiver nos selecionados ou se o nome bater com o contato principal
                        confirmado = idsConfirmados.contains(m.getId()) || titularClean.equals(mClean) || nomesConfirmados.contains(mClean);
                    } else {
                        confirmado = nomesConfirmados.contains(mClean) || titularClean.equals(mClean);
                    }
                    m.setConfirmadoRsvp(confirmado);

                    if (m.getId() != null && mapaCriancaPorId.containsKey(m.getId())) {
                        m.setCriancaAte6Anos(mapaCriancaPorId.get(m.getId()));
                    } else if (mapaCriancaPorId.isEmpty() && mapaCriancaPorNome.containsKey(mClean)) {
                        m.setCriancaAte6Anos(mapaCriancaPorNome.get(mClean));
                    }
                }
            }
        }

        conviteGateway.salvar(convite);

        sincronizarParticipantesNoRsvp(convite, nomesConfirmados, vai);
        sincronizarFornecedoresNoRsvp(nomesConfirmados, vai);
    }

    private void sincronizarParticipantesNoRsvp(Convite convite, Set<String> nomesConfirmados, boolean vai) {
        List<ParticipanteCerimonia> todos = participanteCerimoniaGateway.listarTodos();
        for (ParticipanteCerimonia p : todos) {
            boolean mesmoCodigo = p.getCodigoConvite() != null && p.getCodigoConvite().equalsIgnoreCase(convite.getCodigo());
            String pClean = limparNomeParaComparacao(p.getNome());
            boolean nomeConfirmado = nomesConfirmados.contains(pClean);

            if (mesmoCodigo || nomeConfirmado) {
                p.setConfirmadoRsvp(vai && nomeConfirmado);
                if (p.getCodigoConvite() == null && convite.getCodigo() != null) {
                    p.setCodigoConvite(convite.getCodigo());
                }
                if (p.getTelefone() == null && convite.getTelefone() != null) {
                    p.setTelefone(convite.getTelefone());
                }
                p.setUpdatedAt(LocalDateTime.now());
                participanteCerimoniaGateway.salvar(p);
            }
        }

        if (convite.getMembros() != null) {
            for (MembroConvite m : convite.getMembros()) {
                if (m.getPapel() != null && !m.getPapel().isBlank()) {
                    boolean confirmado = Boolean.TRUE.equals(m.getConfirmadoRsvp());
                    String vinculoFinal = m.getVinculo() != null && !m.getVinculo().isBlank() ? m.getVinculo() : "Noivo";
                    var partOpt = participanteCerimoniaGateway.buscarPorNome(m.getNome().trim());
                    var part = partOpt.orElseGet(() -> ParticipanteCerimonia.builder()
                            .nome(m.getNome().trim())
                            .papel(m.getPapel())
                            .vinculo(vinculoFinal)
                            .codigoConvite(convite.getCodigo())
                            .telefone(convite.getTelefone())
                            .createdAt(LocalDateTime.now())
                            .build());

                    part.setPapel(m.getPapel());
                    part.setVinculo(vinculoFinal);
                    part.setConfirmadoRsvp(confirmado);
                    part.setCodigoConvite(convite.getCodigo());
                    part.setUpdatedAt(LocalDateTime.now());
                    participanteCerimoniaGateway.salvar(part);
                }
            }
        }
    }

    private void sincronizarFornecedoresNoRsvp(Set<String> nomesConfirmados, boolean vai) {
        if (nomesConfirmados.isEmpty()) return;
        List<Fornecedor> todos = fornecedorGateway.listarTodos();
        for (Fornecedor f : todos) {
            boolean alterou = false;
            if (f.getEquipe() != null) {
                for (MembroEquipeFornecedor m : f.getEquipe()) {
                    if (m.getNome() != null && nomesConfirmados.contains(normalizarTexto(m.getNome()))) {
                        m.setPresente(vai);
                        alterou = true;
                    }
                }
            }
            if (alterou) {
                f.setUpdatedAt(LocalDateTime.now());
                fornecedorGateway.salvar(f);
            }
        }
    }

    private String limparNomeParaComparacao(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("[^a-z0-9]", "");
    }

    private String normalizarTexto(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
