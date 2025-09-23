package com.sistema.sistema.infrastructure.security;


import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Roles como authorities con prefijo ROLE_
        if (u.getRoles() != null) {
            authorities.addAll(
                    u.getRoles().stream()
                            .map(Role::getName)
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .toList()
            );
        }

        // Permisos como authorities sin prefijo
        if (u.getPermissions() != null) {
            authorities.addAll(
                    u.getPermissions().stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList()
            );
        }


        return new XsUserDetails(
                u.getId(),
                u.getUsername(),
                u.getPassword(),
                authorities,
                u.getActive()
        );
    }



}
