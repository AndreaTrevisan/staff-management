package it.atrevisan.staffmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class UpdatableDTO {
    protected LocalDateTime createdTime;
    protected LocalDateTime updatedTime;
    protected String createdBy;
    protected String updatedBy;
}
