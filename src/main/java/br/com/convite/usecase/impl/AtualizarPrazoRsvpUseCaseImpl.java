package br.com.convite.usecase.impl;

import br.com.convite.entrypoint.api.model.ConfiguracaoEventoResponse;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConfiguracaoEventoGateway;
import br.com.convite.usecase.AtualizarPrazoRsvpUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtualizarPrazoRsvpUseCaseImpl implements AtualizarPrazoRsvpUseCase {

    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter FORMATTER_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PT_BR);
    private static final DateTimeFormatter FORMATTER_EXTENSO = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_PT_BR);

    private final ConfiguracaoEventoGateway configuracaoEventoGateway;

    @Override
    public ConfiguracaoEventoResponse executar(LocalDateTime novoPrazo) {
        if (novoPrazo == null) {
            throw new RegraDeNegocioException("O novo prazo de confirmação de presença (RSVP) é obrigatório.");
        }

        configuracaoEventoGateway.salvarPrazoRsvp(novoPrazo);
        log.info("Novo prazo de RSVP configurado com sucesso: {}", novoPrazo);

        LocalDateTime agora = LocalDateTime.now();
        boolean expirado = agora.isAfter(novoPrazo);

        return ConfiguracaoEventoResponse.builder()
                .prazoRsvp(novoPrazo)
                .prazoRsvpFormatado(novoPrazo.format(FORMATTER_DATA))
                .prazoRsvpExtenso(novoPrazo.format(FORMATTER_EXTENSO))
                .expirado(expirado)
                .build();
    }
}
