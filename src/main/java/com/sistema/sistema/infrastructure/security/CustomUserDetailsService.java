package com.sistema.sistema.infrastructure.security;


import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Roles del usuario (UserRole)
        if (u.getRoles() != null) {
            u.getRoles().forEach(userRole -> {
                if (userRole == null || Boolean.TRUE.equals(userRole.getDeleted())) return; // ignorar eliminados

                Role role = userRole.getRole();
                if (role != null && Boolean.TRUE.equals(role.getActive())) {
                    // Agregar rol como autoridad
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

                    // Permisos del rol (filtrar eliminados)
                    if (role.getPermissions() != null) {
                        role.getPermissions().forEach(permission -> {
                            if (permission != null && permission.getName() != null) {
                                authorities.add(new SimpleGrantedAuthority(permission.getName()));
                            }
                        });
                    }
                }
            });
        }

        return new XsUserDetails(
                u.getId(),
                u.getUsername(),
                u.getPassword(),
                new ArrayList<>(authorities),
                u.getActive()
        );
    }


}
