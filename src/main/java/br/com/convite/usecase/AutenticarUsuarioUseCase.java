package br.com.convite.usecase;

import br.com.convite.entrypoint.api.model.LoginResponse;

public interface AutenticarUsuarioUseCase {
    String executar(String username, String password);
    LoginResponse autenticarCompleto(String username, String password, String deviceInfo);
}
