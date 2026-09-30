package br.com.convite.usecase;

import br.com.convite.entrypoint.api.model.LoginResponse;

public interface RenovarSessaoUseCase {
    LoginResponse executar(String refreshToken, String deviceInfo);
}
