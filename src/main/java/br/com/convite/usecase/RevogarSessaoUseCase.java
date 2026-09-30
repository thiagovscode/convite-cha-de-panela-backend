package br.com.convite.usecase;

public interface RevogarSessaoUseCase {
    void revogarPorRefreshToken(String refreshToken);
    void revogarTodasDoUsuario(String username);
}
