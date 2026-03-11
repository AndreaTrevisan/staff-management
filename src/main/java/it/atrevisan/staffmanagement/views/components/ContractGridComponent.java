package it.atrevisan.staffmanagement.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.enums.JobRole;
import it.atrevisan.staffmanagement.service.ContractService;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

import java.util.List;

public class ContractGridComponent extends VerticalLayout {

    private final ContractService contractService;

    private final Grid<ContractDTO> grid = new Grid<>(ContractDTO.class, false);
    private final TextField personFilter = new TextField();
    private String personDocumentId;

    public ContractGridComponent(ContractService contractService, boolean viewOnly) {

        this.contractService = contractService;

        setSizeFull();

        if(!viewOnly) {
            personFilter.setPlaceholder("Filter by person document ID...");
            personFilter.setClearButtonVisible(true);
            personFilter.addValueChangeListener(e -> refresh());

            Button createBtn = new Button("Create Contract", e -> openDialog(null));

            HorizontalLayout topBar = new HorizontalLayout(personFilter, createBtn);
            topBar.setWidthFull();
            topBar.expand(personFilter);

            add(topBar);

            grid.addComponentColumn(this::buildActions)
                    .setHeader("Actions")
                    .setAutoWidth(true);
        }

        grid.addColumn(ContractDTO::getPersonDocumentId)
                .setHeader("Person")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ContractDTO::getStartDate)
                .setHeader("Start Date")
                .setAutoWidth(true);

        grid.addColumn(ContractDTO::getEndDate)
                .setHeader("End Date")
                .setAutoWidth(true);

        grid.addColumn(ContractDTO::getJobRole)
                .setHeader("Job Role")
                .setAutoWidth(true);

        grid.addColumn(ContractDTO::getLicenseExpiry)
                .setHeader("License Expiry")
                .setAutoWidth(true);

        grid.setSizeFull();

        add(grid);
        expand(grid);
    }

    public void setPerson(String documentId) {
        this.personDocumentId = documentId;
        refresh();
    }

    public void refresh() {

        if(personDocumentId != null){
            grid.setItems(contractService.getContractsByPerson(personDocumentId));
            return;
        }

        String filter = personFilter.getValue();

        List<ContractDTO> contracts = contractService.getAllContracts();

        if (filter != null && !filter.isEmpty()) {
            contracts.removeIf(c ->
                    !c.getPersonDocumentId()
                            .toLowerCase()
                            .contains(filter.toLowerCase()));
        }

        grid.setItems(contracts);
    }

    private HorizontalLayout buildActions(ContractDTO contract) {

        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        edit.getElement().setProperty("title", "Edit");
        edit.addClickListener(e -> openDialog(contract));

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY
        );
        delete.getElement().setProperty("title", "Delete");

        delete.addClickListener(e -> {
            contractService.deleteContract(contract.getId());
            NotificationService.showSuccess("Contract deleted");
            refresh();
        });

        return new HorizontalLayout(edit, delete);
    }

    private void openDialog(ContractDTO contract) {

        Dialog dialog = new Dialog();

        TextField personId = new TextField("Person Document ID");

        if(personDocumentId != null){
            personId.setValue(personDocumentId);
            personId.setEnabled(false);
        }

        DatePicker startDate = new DatePicker("Start Date");
        DatePicker endDate = new DatePicker("End Date");
        DatePicker licenseExpiry = new DatePicker("License Expiry");

        // Enum select for job role
        com.vaadin.flow.component.combobox.ComboBox<JobRole> jobRoleCombo = new com.vaadin.flow.component.combobox.ComboBox<>("Job Role");
        jobRoleCombo.setItems(JobRole.values());

        if (contract != null) {
            personId.setValue(contract.getPersonDocumentId());
            startDate.setValue(contract.getStartDate());
            endDate.setValue(contract.getEndDate());
            licenseExpiry.setValue(contract.getLicenseExpiry());
            jobRoleCombo.setValue(contract.getJobRole());

            personId.setEnabled(false); // cannot change person for existing contract
        }

        Button save = new Button("Save", e -> {

            ContractDTO dto = ContractDTO.builder()
                    .personDocumentId(personId.getValue())
                    .startDate(startDate.getValue())
                    .endDate(endDate.getValue())
                    .licenseExpiry(licenseExpiry.getValue())
                    .jobRole(jobRoleCombo.getValue())
                    .build();

            if (contract == null) {
                contractService.createContract(dto.getPersonDocumentId(), dto);
            } else {
                dto.setId(contract.getId());
                contractService.updateContract(contract.getId(), dto);
            }

            NotificationService.showSuccess("Contract saved");

            dialog.close();
            refresh();
        });

        dialog.add(new VerticalLayout(personId, startDate, endDate, licenseExpiry, jobRoleCombo, save));
        dialog.open();
    }
}