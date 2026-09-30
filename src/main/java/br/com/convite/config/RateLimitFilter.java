package br.com.convite.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiter por IP.
 *
 * Limites (por IP, por janela de 1 minuto):
 *   - Endpoints gerais : 60 requisicoes / minuto
 *   - Rota de reservar : 5 requisicoes / minuto  (mais restrito para evitar abuso)
 *   - Rota de rsvp     : 5 requisicoes / minuto
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int JANELA_MS          = 60_000; // 1 minuto
    private static final int LIMITE_GERAL       = 60;
    private static final int LIMITE_RESERVAR    = 5;
    private static final int LIMITE_RSVP        = 5;

    // Estrutura: IP -> [contagem, timestamp inicio da janela]
    private final ConcurrentHashMap<String, long[]> contadores = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1. Bypass completo para requisições autenticadas ou rotas internas da recepção e administração
        String authHeader = request.getHeader("Authorization");
        boolean isAutenticado = (authHeader != null && authHeader.startsWith("Bearer "));
        boolean isRotaOperacional = uri.startsWith("/api/recepcao/") || uri.startsWith("/api/admin/") || uri.equals("/health");

        if (isAutenticado || isRotaOperacional) {
            chain.doFilter(request, response);
            return;
        }

        String ip  = resolverIp(request);
        int limite = resolverLimite(uri);

        if (estaAcimaDolimite(ip + "|" + categoriaUri(uri), limite)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"success\":false,\"error\":\"Muitas requisições. Tente novamente em 1 minuto.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean estaAcimaDolimite(String chave, int limite) {
        long agora = System.currentTimeMillis();

        // Limpeza periódica leve para prevenir vazamento de memória (eviction)
        if (contadores.size() > 300) {
            contadores.entrySet().removeIf(e -> agora - e.getValue()[1] > JANELA_MS * 2);
        }

        long[] estado = contadores.compute(chave, (k, v) -> {
            if (v == null || agora - v[1] > JANELA_MS) {
                // Nova janela
                return new long[]{1, agora};
            }
            v[0]++;
            return v;
        });

        return estado[0] > limite;
    }

    /** Extrai o IP real mesmo atras de proxies/Cloudflare. */
    private String resolverIp(HttpServletRequest request) {
        String cf = request.getHeader("CF-Connecting-IP"); // Cloudflare
        if (cf != null && !cf.isBlank()) return cf;

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private int resolverLimite(String uri) {
        if (uri.contains("/reservar") || uri.contains("/rsvp")) return LIMITE_RESERVAR;
        return LIMITE_GERAL;
    }

    private String categoriaUri(String uri) {
        if (uri.contains("/reservar")) return "reservar";
        if (uri.contains("/rsvp"))     return "rsvp";
        return "geral";
    }
}