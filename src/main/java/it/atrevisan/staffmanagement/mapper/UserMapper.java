package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.model.User;

import java.util.Optional;

public class UserMapper {
    private UserMapper(){}

    public static UserDTO map(User user){
        UserDTO dto =  UserDTO.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .createdTime(user.getCreatedTime())
                .createdBy(user.getCreatedBy())
                .updatedTime(user.getUpdatedTime())
                .updatedBy(user.getUpdatedBy())
                .build();

        if(user.getPerson() != null && user.getPerson().getDocumentId() != null){
            dto.setPersonDocumentId(user.getPerson().getDocumentId());
        }

        return dto;
    }
}
