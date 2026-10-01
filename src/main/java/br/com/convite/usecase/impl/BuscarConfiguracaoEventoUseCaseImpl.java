package br.com.convite.usecase.impl;

import br.com.convite.entrypoint.api.model.ConfiguracaoEventoResponse;
import br.com.convite.gateway.ConfiguracaoEventoGateway;
import br.com.convite.usecase.BuscarConfiguracaoEventoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BuscarConfiguracaoEventoUseCaseImpl implements BuscarConfiguracaoEventoUseCase {

    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter FORMATTER_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PT_BR);
    private static final DateTimeFormatter FORMATTER_EXTENSO = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_PT_BR);

    private final ConfiguracaoEventoGateway configuracaoEventoGateway;

    @Override
    public ConfiguracaoEventoResponse executar() {
        LocalDateTime prazo = configuracaoEventoGateway.buscarPrazoRsvp().orElse(null);
        LocalDateTime agora = LocalDateTime.now();
        boolean expirado = prazo != null && agora.isAfter(prazo);

        return ConfiguracaoEventoResponse.builder()
                .prazoRsvp(prazo)
                .prazoRsvpFormatado(prazo != null ? prazo.format(FORMATTER_DATA) : null)
                .prazoRsvpExtenso(prazo != null ? prazo.format(FORMATTER_EXTENSO) : null)
                .expirado(expirado)
                .build();
    }
}
