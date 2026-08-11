package br.com.convite.entrypoint.api;

import br.com.convite.entrypoint.api.model.LoginRequest;
import br.com.convite.entrypoint.api.model.LoginResponse;
import br.com.convite.usecase.AutenticarUsuarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = autenticarUsuarioUseCase.executar(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
