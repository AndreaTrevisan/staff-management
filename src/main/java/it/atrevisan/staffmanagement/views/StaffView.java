package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.PersonService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

@Route(value = "staff", layout = MainLayout.class)
public class StaffView extends BasicLoggedInView {

    private final PersonService personService;

    private final Grid<PersonDTO> grid = new Grid<>(PersonDTO.class,false);

    public StaffView(PersonService personService) {

        this.personService = personService;

        setSizeFull();

        Button createBtn = new Button("Create Person", e -> openCreateDialog());

        grid.addComponentColumn(this::buildActions)
                .setHeader("Actions")
                .setAutoWidth(true);

        grid.addColumn(PersonDTO::getDocumentId)
                .setHeader("Document ID")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getName)
                .setHeader("Name")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getSurname)
                .setHeader("Surname")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getBirthDate)
                .setHeader("Birth Date")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getEmail)
                .setHeader("Email")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getUsername)
                .setHeader("User")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getCreatedBy)
                .setHeader("Created By")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getCreatedTime)
                .setHeader("Create Time")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getUpdatedBy)
                .setHeader("Last Updated By")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(PersonDTO::getUpdatedTime)
                .setHeader("Last Update Time")
                .setSortable(true)
                .setAutoWidth(true);

        grid.setSizeFull();

        refreshGrid();

        add(createBtn, grid);
        expand(grid);
    }

    private void refreshGrid(){
        grid.setItems(personService.getAllStaff());
    }

    private HorizontalLayout buildActions(PersonDTO person){

        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        edit.getElement().setProperty("title", "Edit");
        edit.addClickListener(e -> openEditDialog(person));

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY
        );
        delete.getElement().setProperty("title", "Delete");

        delete.addClickListener(e -> {
            personService.deletePerson(person.getDocumentId());
            NotificationService.showSuccess("Person deleted");
            refreshGrid();
        });

        return new HorizontalLayout(edit, delete);
    }

    private void openCreateDialog(){

        Dialog dialog = buildDialog(null);

        dialog.open();
    }

    private void openEditDialog(PersonDTO person){

        Dialog dialog = buildDialog(person);

        dialog.open();
    }

    private Dialog buildDialog(PersonDTO person){

        Dialog dialog = new Dialog();

        TextField document = new TextField("Document");
        TextField name = new TextField("Name");
        TextField surname = new TextField("Surname");
        DatePicker birthDate = new DatePicker("Birth date");
        TextField email = new TextField("Email");
        TextField phone = new TextField("Phone");
        TextField address = new TextField("Address");

        Checkbox createUserFlag = new Checkbox("Create User");
        Checkbox deleteUserFlag = new Checkbox("Delete User");

        Binder<PersonDTO> binder = new Binder<>(PersonDTO.class);

        /*
         * BINDINGS + VALIDATIONS
         */
        binder.forField(name)
                .asRequired("Name is required")
                .bind(PersonDTO::getName, PersonDTO::setName);

        binder.forField(surname)
                .asRequired("Surname is required")
                .bind(PersonDTO::getSurname, PersonDTO::setSurname);

        binder.forField(document)
                .asRequired("Document is required")
                .bind(PersonDTO::getDocumentId, PersonDTO::setDocumentId);

        binder.bind(birthDate, PersonDTO::getBirthDate, PersonDTO::setBirthDate);
        binder.bind(email, PersonDTO::getEmail, PersonDTO::setEmail);
        binder.bind(phone, PersonDTO::getPhone, PersonDTO::setPhone);
        binder.bind(address, PersonDTO::getAddress, PersonDTO::setAddress);

        /*
         * LOAD DATA
         */
        PersonDTO dto = person != null ? person : new PersonDTO();
        binder.setBean(dto);

        boolean isEdit = person != null;
        boolean hasUser = isEdit && person.getUsername() != null;

        if(isEdit){
            document.setEnabled(false);
            name.setEnabled(false);
            surname.setEnabled(false);
        }

        createUserFlag.setVisible(!hasUser);
        deleteUserFlag.setVisible(hasUser);

        /*
         * SAVE BUTTON
         */
        Button save = new Button("Save", e -> {

            if(!binder.validate().isOk()){
                return;
            }

            PersonDTO bean = binder.getBean();

            boolean createUser = createUserFlag.isVisible() && createUserFlag.getValue();
            boolean deleteUser = deleteUserFlag.isVisible() && deleteUserFlag.getValue();

            personService.savePerson(bean, createUser, deleteUser);
            NotificationService.showSuccess("Person saved");
            dialog.close();
            refreshGrid();
        });

        VerticalLayout layout = new VerticalLayout(
                name,
                surname,
                birthDate,
                document,
                email,
                phone,
                address,
                createUserFlag,
                deleteUserFlag,
                save
        );

        dialog.add(layout);

        return dialog;
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN, Roles.HR};
    }

}
