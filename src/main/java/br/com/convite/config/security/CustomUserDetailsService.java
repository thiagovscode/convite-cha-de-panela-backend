package br.com.convite.config.security;

import br.com.convite.gateway.persistence.UsuarioRepository;
import br.com.convite.gateway.persistence.entity.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        java.util.List<UsuarioEntity> users = usuarioRepository.findAllByUsernameIgnoreCase(username != null ? username.trim() : "");
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }
        UsuarioEntity u = users.get(0);
        String role = u.getRole() != null && !u.getRole().isBlank() ? u.getRole() : "USER";
        String authName = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
        return new User(u.getUsername(), u.getPassword(), java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(authName)));
    }
}
