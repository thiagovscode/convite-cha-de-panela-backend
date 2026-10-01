package br.com.convite.usecase.impl;

import br.com.convite.domain.*;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.ConfirmarRsvpCasamentoUseCase;
import br.com.convite.usecase.ProcessarConfirmacaoRsvpCasamentoUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class ProcessarConfirmacaoRsvpCasamentoUseCaseImpl implements ProcessarConfirmacaoRsvpCasamentoUseCase {

    public static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    private final ConviteGateway conviteGateway;
    private final ParticipanteCerimoniaGateway participanteCerimoniaGateway;
    private final FornecedorGateway fornecedorGateway;
    private final ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase;
    private final br.com.convite.gateway.ConfiguracaoEventoGateway configuracaoEventoGateway;
    private final Clock clock;

    @Autowired
    public ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
            ConviteGateway conviteGateway,
            ParticipanteCerimoniaGateway participanteCerimoniaGateway,
            FornecedorGateway fornecedorGateway,
            ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase,
            br.com.convite.gateway.ConfiguracaoEventoGateway configuracaoEventoGateway
    ) {
        this(conviteGateway, participanteCerimoniaGateway, fornecedorGateway, confirmarRsvpCasamentoUseCase, configuracaoEventoGateway, Clock.system(FUSO_HORARIO));
    }

    public ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
            ConviteGateway conviteGateway,
            ParticipanteCerimoniaGateway participanteCerimoniaGateway,
            FornecedorGateway fornecedorGateway,
            ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase
    ) {
        this(conviteGateway, participanteCerimoniaGateway, fornecedorGateway, confirmarRsvpCasamentoUseCase, null, Clock.system(FUSO_HORARIO));
    }

    public ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
            ConviteGateway conviteGateway,
            ParticipanteCerimoniaGateway participanteCerimoniaGateway,
            FornecedorGateway fornecedorGateway,
            ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase,
            Clock clock
    ) {
        this(conviteGateway, participanteCerimoniaGateway, fornecedorGateway, confirmarRsvpCasamentoUseCase, null, clock);
    }

    public ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
            ConviteGateway conviteGateway,
            ParticipanteCerimoniaGateway participanteCerimoniaGateway,
            FornecedorGateway fornecedorGateway,
            ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase,
            br.com.convite.gateway.ConfiguracaoEventoGateway configuracaoEventoGateway,
            Clock clock
    ) {
        this.conviteGateway = conviteGateway;
        this.participanteCerimoniaGateway = participanteCerimoniaGateway;
        this.fornecedorGateway = fornecedorGateway;
        this.confirmarRsvpCasamentoUseCase = confirmarRsvpCasamentoUseCase;
        this.configuracaoEventoGateway = configuracaoEventoGateway;
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
        LocalDateTime prazoEfetivo = (configuracaoEventoGateway != null)
                ? configuracaoEventoGateway.buscarPrazoRsvp().orElse(null)
                : null;

        if (prazoEfetivo != null && agora.isAfter(prazoEfetivo)) {
            String formatado = prazoEfetivo.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            throw new RegraDeNegocioException("O prazo para confirmação de presença encerrou em " + formatado + ". Por favor, entre em contato diretamente com os noivos.");
        }

        if (codigoConvite == null || codigoConvite.isBlank()) {
            throw new RegraDeNegocioException("A confirmação de presença é restrita à lista oficial. Informe um código de convite válido.");
        }

        Convite convite = conviteGateway.buscarPorCodigo(codigoConvite.trim())
                .or(() -> conviteGateway.buscarPorCodigoOuId(codigoConvite.trim()))
                .orElseThrow(() -> new ConviteNaoEncontradoException(codigoConvite));

        // REGRA: Após o primeiro registro de RSVP (CONFIRMADO ou RECUSADO), nenhuma alteração pública é permitida.
        // O painel administrativo pode continuar alterando via /api/admin/** (requer ADMIN).
        if ("CONFIRMADO".equalsIgnoreCase(convite.getStatus()) || "RECUSADO".equalsIgnoreCase(convite.getStatus())) {
            throw new RegraDeNegocioException(
                "Sua resposta já foi registrada e não pode ser alterada. " +
                "Para qualquer ajuste, entre em contato diretamente com os noivos."
            );
        }

        List<AcompanhanteCasamento> acompFinal = acompanhantes != null ? new ArrayList<>(acompanhantes) : new ArrayList<>();
        if (!Boolean.TRUE.equals(presenca)) {
            if (acompFinal.isEmpty() && convite.getMembros() != null) {
                for (MembroConvite m : convite.getMembros()) {
                    if (nome == null || !nome.equalsIgnoreCase(m.getNome())) {
                        acompFinal.add(AcompanhanteCasamento.builder()
                                .nome(m.getNome())
                                .criancaAte6Anos(m.getCriancaAte6Anos())
                                .build());
                    }
                }
            }
        }

        RsvpCasamento rsvp = RsvpCasamento.builder()
                .codigoConvite(convite.getCodigo())
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
                    String respondenteClean = rsvp.getNome() != null ? limparNomeParaComparacao(rsvp.getNome()) : "";

                    boolean confirmado;
                    if (m.getId() != null && !idsConfirmados.isEmpty()) {
                        // Confirma se o ID estiver nos selecionados ou se o nome bater com o contato respondente
                        confirmado = idsConfirmados.contains(m.getId().toString()) || respondenteClean.equals(mClean) || nomesConfirmados.contains(mClean);
                    } else {
                        confirmado = nomesConfirmados.contains(mClean) || respondenteClean.equals(mClean);
                    }
                    m.setConfirmadoRsvp(confirmado);

                    if (m.getId() != null && mapaCriancaPorId.containsKey(m.getId().toString())) {
                        m.setCriancaAte6Anos(mapaCriancaPorId.get(m.getId().toString()));
                    } else if (mapaCriancaPorId.isEmpty() && mapaCriancaPorNome.containsKey(mClean)) {
                        m.setCriancaAte6Anos(mapaCriancaPorNome.get(mClean));
                    }
                }
            }
        }

        conviteGateway.salvar(convite);

        try {
            sincronizarParticipantesNoRsvp(convite, nomesConfirmados, vai);
        } catch (Exception e) {
            log.warn("Erro não impeditivo ao sincronizar participantes do cortejo para convite {}: {}", convite.getCodigo(), e.getMessage());
        }

        try {
            sincronizarFornecedoresNoRsvp(nomesConfirmados, vai);
        } catch (Exception e) {
            log.warn("Erro não impeditivo ao sincronizar fornecedores para convite {}: {}", convite.getCodigo(), e.getMessage());
        }
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
                    String vinculoFinal = "Casal";
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
