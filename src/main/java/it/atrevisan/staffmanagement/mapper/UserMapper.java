package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.model.User;

public class UserMapper {
    private UserMapper(){}

    public static UserDTO map(User user){
        return UserDTO.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .createdTime(user.getCreatedTime())
                .createdBy(user.getCreatedBy())
                .updatedTime(user.getUpdatedTime())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}
