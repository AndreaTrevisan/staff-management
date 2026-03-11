package it.atrevisan.staffmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserDTO extends UpdatableDTO {
    private String username;
    private String password;
    private boolean enabled;
    private Set<String> roles;
    private String personDocumentId;
}
