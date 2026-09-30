package br.com.convite.entrypoint.api;

import br.com.convite.entrypoint.api.model.LoginRequest;
import br.com.convite.entrypoint.api.model.LoginResponse;
import br.com.convite.entrypoint.api.model.LogoutRequest;
import br.com.convite.entrypoint.api.model.RefreshTokenRequest;
import br.com.convite.usecase.AutenticarUsuarioUseCase;
import br.com.convite.usecase.RenovarSessaoUseCase;
import br.com.convite.usecase.RevogarSessaoUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final RenovarSessaoUseCase renovarSessaoUseCase;
    private final RevogarSessaoUseCase revogarSessaoUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {

        String deviceInfo = extrairDeviceInfo(servletRequest);
        LoginResponse response = autenticarUsuarioUseCase.autenticarCompleto(
                request.getUsername(), request.getPassword(), deviceInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest) {

        String deviceInfo = extrairDeviceInfo(servletRequest);
        LoginResponse response = renovarSessaoUseCase.executar(request.getRefreshToken(), deviceInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) LogoutRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            revogarSessaoUseCase.revogarPorRefreshToken(request.getRefreshToken());
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout efetuado com sucesso."
        ));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            revogarSessaoUseCase.revogarTodasDoUsuario(authentication.getName());
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Todas as sessões foram encerradas com sucesso."
        ));
    }

    private String extrairDeviceInfo(HttpServletRequest request) {
        if (request == null) return null;
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return (userAgent != null ? userAgent : "") + " | IP: " + (ip != null ? ip : "");
    }
}
