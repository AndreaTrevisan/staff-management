package it.atrevisan.staffmanagement.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.enums.AbsenceReason;
import it.atrevisan.staffmanagement.service.AbsenceService;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

import java.util.List;

public class AbsenceGridComponent extends VerticalLayout {

    private final AbsenceService absenceService;

    private final Grid<AbsenceDTO> grid = new Grid<>(AbsenceDTO.class, false);
    private final TextField personFilter = new TextField();

    private String personDocumentId;

    public AbsenceGridComponent(AbsenceService absenceService, boolean viewOnly) {

        this.absenceService = absenceService;

        setSizeFull();

        if(!viewOnly){

            personFilter.setPlaceholder("Filter by person document ID...");
            personFilter.setClearButtonVisible(true);
            personFilter.addValueChangeListener(e -> refresh());

            Button createBtn = new Button("Create Absence", e -> openDialog(null));

            HorizontalLayout topBar = new HorizontalLayout(personFilter, createBtn);
            topBar.setWidthFull();
            topBar.expand(personFilter);

            add(topBar);

            grid.addComponentColumn(this::buildActions)
                    .setHeader("Actions")
                    .setAutoWidth(true);
        }

        grid.addColumn(AbsenceDTO::getPersonDocumentId)
                .setHeader("Person")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(AbsenceDTO::getDate)
                .setHeader("Date")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(AbsenceDTO::getReason)
                .setHeader("Reason")
                .setAutoWidth(true)
                .setSortable(true);

        grid.setSizeFull();

        add(grid);
        expand(grid);
    }

    public void setPerson(String documentId) {
        this.personDocumentId = documentId;
        refresh();
    }

    public void refresh(){

        if(personDocumentId != null){
            grid.setItems(absenceService.getAbsencesByPerson(personDocumentId));
            return;
        }

        String filter = personFilter.getValue();

        List<AbsenceDTO> absences = absenceService.getAllAbsences();

        if(filter != null && !filter.isEmpty()){
            absences.removeIf(a ->
                    !a.getPersonDocumentId()
                            .toLowerCase()
                            .contains(filter.toLowerCase()));
        }

        grid.setItems(absences);
    }

    private HorizontalLayout buildActions(AbsenceDTO absence){

        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        edit.addClickListener(e -> openDialog(absence));

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY
        );

        delete.addClickListener(e -> {
            absenceService.deleteAbsence(absence.getId());
            NotificationService.showSuccess("Absence deleted");
            refresh();
        });

        return new HorizontalLayout(edit, delete);
    }

    private void openDialog(AbsenceDTO absence){

        Dialog dialog = new Dialog();

        TextField personId = new TextField("Person Document ID");

        if(personDocumentId != null){
            personId.setValue(personDocumentId);
            personId.setEnabled(false);
        }

        DatePicker date = new DatePicker("Date");

        ComboBox<AbsenceReason> reason =
                new ComboBox<>("Reason");
        reason.setItems(AbsenceReason.values());

        if(absence != null){
            personId.setValue(absence.getPersonDocumentId());
            date.setValue(absence.getDate());
            reason.setValue(absence.getReason());
            personId.setEnabled(false);
        }

        Button save = new Button("Save", e -> {

            AbsenceDTO dto = AbsenceDTO.builder()
                    .personDocumentId(personId.getValue())
                    .date(date.getValue())
                    .reason(reason.getValue())
                    .build();

            if(absence == null){
                absenceService.createAbsence(dto.getPersonDocumentId(), dto.getDate(), dto.getReason());
            } else {
                dto.setId(absence.getId());
                absenceService.updateAbsence(absence.getId(), dto);
            }

            NotificationService.showSuccess("Absence saved");

            dialog.close();
            refresh();
        });

        dialog.add(new VerticalLayout(personId, date, reason, save));
        dialog.open();
    }
}