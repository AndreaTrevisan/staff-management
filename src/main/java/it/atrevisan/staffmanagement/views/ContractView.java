package it.atrevisan.staffmanagement.views;

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
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.enums.JobRole;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.ContractService;
import it.atrevisan.staffmanagement.service.PersonService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

import java.util.List;

@Route(value = Routes.CONTRACTS, layout = MainLayout.class)
public class ContractView extends BasicLoggedInView {

    private final ContractService contractService;
    private final PersonService personService;

    private Grid<ContractDTO> grid = new Grid<>(ContractDTO.class, false);
    private TextField personFilter = new TextField();

    public ContractView(ContractService contractService, PersonService personService) {
        this.contractService = contractService;
        this.personService = personService;

        setSizeFull();

        // =========================
        // TOP BAR (Filtri + Create)
        // =========================

        personFilter.setPlaceholder("Filter by person document ID...");
        personFilter.setClearButtonVisible(true);
        personFilter.addValueChangeListener(e -> refreshGrid());

        Button createBtn = new Button("Create Contract", e -> openDialog(null));

        HorizontalLayout topBar = new HorizontalLayout(personFilter, createBtn);
        topBar.setWidthFull();
        topBar.expand(personFilter);

        // =========================
        // GRID
        // =========================

        grid.addComponentColumn(this::buildActionsColumn)
                .setHeader("Actions")
                .setAutoWidth(true);

        grid.addColumn(ContractDTO::getPersonDocumentId)
                .setHeader("Person")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ContractDTO::getStartDate)
                .setHeader("Start Date")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ContractDTO::getEndDate)
                .setHeader("End Date")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ContractDTO::getJobRole)
                .setHeader("Job Role")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ContractDTO::getLicenseExpiry)
                .setHeader("License Expiry")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ContractDTO::getCreatedBy)
                .setHeader("Created By")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(ContractDTO::getCreatedBy);

        grid.addColumn(ContractDTO::getCreatedTime)
                .setHeader("Create Time")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(ContractDTO::getCreatedTime);

        grid.addColumn(ContractDTO::getUpdatedBy)
                .setHeader("Last Updated By")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(ContractDTO::getUpdatedBy);

        grid.addColumn(ContractDTO::getUpdatedTime)
                .setHeader("Last Update Time")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(ContractDTO::getUpdatedTime);



        grid.setSizeFull();

        refreshGrid();

        add(topBar, grid);
        expand(grid);
    }

    private void refreshGrid() {
        String filter = personFilter.getValue();
        List<ContractDTO> contracts = contractService.getAllContracts();

        if (filter != null && !filter.isEmpty()) {
            contracts.removeIf(c -> !c.getPersonDocumentId().toLowerCase().contains(filter.toLowerCase()));
        }

        grid.setItems(contracts);
    }

    private HorizontalLayout buildActionsColumn(ContractDTO contract) {
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
            refreshGrid();
        });

        return new HorizontalLayout(edit, delete);
    }

    private void openDialog(ContractDTO contract) {

        Dialog dialog = new Dialog();

        TextField personId = new TextField("Person Document ID");
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
            refreshGrid();
        });

        dialog.add(new VerticalLayout(personId, startDate, endDate, licenseExpiry, jobRoleCombo, save));
        dialog.open();
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN, Roles.HR};
    }
}