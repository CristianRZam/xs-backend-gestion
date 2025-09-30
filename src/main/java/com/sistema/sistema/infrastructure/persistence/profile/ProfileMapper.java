package com.sistema.sistema.infrastructure.persistence.profile;

import com.sistema.sistema.application.dto.response.profile.ProfileDTO;
import com.sistema.sistema.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {

    public ProfileDTO toProfileDTO(User user) {
        if (user == null) {
            return null;
        }

        List<String> roles = null;
        if (user.getRoles() != null) {
            roles = user.getRoles().stream()
                    .filter(ur -> ur.getRole() != null)
                    .map(ur -> ur.getRole().getName())
                    .collect(Collectors.toList());
        }

        return ProfileDTO.builder()
                .idUser(user.getId())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .active(user.getActive())
                .deleted(user.getDeleted())
                .person(user.getPerson())
                .roles(roles)
                .build();
    }
}
