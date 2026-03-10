package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.model.Contract;

public class ContractMapper {

    private ContractMapper(){}

    public static ContractDTO toDTO(Contract contract) {
        return ContractDTO.builder()
                .id(contract.getId())
                .personDocumentId(contract.getPerson() != null ? contract.getPerson().getDocumentId() : null)
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .jobRole(contract.getJobRole())
                .licenseExpiry(contract.getLicenseExpiry())
                .createdTime(contract.getCreatedTime())
                .createdBy(contract.getCreatedBy())
                .updatedTime(contract.getUpdatedTime())
                .updatedBy(contract.getUpdatedBy())
                .build();
    }

    public static Contract toEntity(ContractDTO dto) {
        return Contract.builder()
                .id(dto.getId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .jobRole(dto.getJobRole())
                .licenseExpiry(dto.getLicenseExpiry())
                .build();
    }

    public static void updateEntity(ContractDTO dto, Contract entity) {
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setJobRole(dto.getJobRole());
        entity.setLicenseExpiry(dto.getLicenseExpiry());
    }
}