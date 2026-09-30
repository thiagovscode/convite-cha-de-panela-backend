package br.com.convite.usecase.impl;

import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.GerarCodigoConviteUnicoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GerarCodigoConviteUnicoUseCaseImpl implements GerarCodigoConviteUnicoUseCase {

    private final ConviteGateway conviteGateway;

    private static final String CODIGO_ALPHABET = "23456789abcdefghjkmnpqrstuvwxyz";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String executar() {
        for (int tentativa = 0; tentativa < 30; tentativa++) {
            StringBuilder sb = new StringBuilder("tn-");
            for (int i = 0; i < 6; i++) {
                sb.append(CODIGO_ALPHABET.charAt(SECURE_RANDOM.nextInt(CODIGO_ALPHABET.length())));
            }
            String gerado = sb.toString();
            if (conviteGateway.buscarPorCodigo(gerado).isEmpty()) {
                return gerado;
            }
        }
        return "tn-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
