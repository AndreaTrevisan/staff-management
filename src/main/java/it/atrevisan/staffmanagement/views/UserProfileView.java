package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.PersonService;
import it.atrevisan.staffmanagement.service.UserService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;
import it.atrevisan.staffmanagement.views.session.SessionUtils;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

import java.util.Optional;

@Route(value = Routes.PROFILE, layout = MainLayout.class)
public class UserProfileView extends BasicLoggedInView {

    private final UserService userService;
    private final PersonService personService;

    public UserProfileView(UserService userService, PersonService personService) {
        this.userService = userService;
        this.personService = personService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);

        removeAll();

        Optional<UserDTO> currentUserOpt = getCurrentUser();
        if (!currentUserOpt.isPresent()) {
            add(new H1("No user logged in"));
            return;
        }

        UserDTO currentUser = currentUserOpt.get();
        add(new H1("Profile for " + currentUser.getUsername()));

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);

        // Colonna cambio password
        VerticalLayout passwordColumn = new VerticalLayout();
        passwordColumn.setWidth("45%");
        passwordColumn.setSpacing(true);
        passwordColumn.setPadding(true);
        passwordColumn.add(new H2("Change Password"));
        passwordColumn.add(passwordChangeForm(currentUser));

        mainLayout.add(passwordColumn);

        // Colonna info personali
        if (currentUser.getPersonDocumentId() != null) {
            VerticalLayout personalColumn = new VerticalLayout();
            personalColumn.setWidth("45%");
            personalColumn.setSpacing(true);
            personalColumn.setPadding(true);
            personalColumn.add(new H2("Personal Information"));
            VerticalLayout personalForm = personalInfoForm(currentUser);
            if (personalForm != null) {
                personalColumn.add(personalForm);
            }
            mainLayout.add(personalColumn);
        }

        add(mainLayout);
    }

    private VerticalLayout personalInfoForm(UserDTO currentUser) {
        if (currentUser.getPersonDocumentId() == null) return null;

        String docId = currentUser.getPersonDocumentId();
        Optional<PersonDTO> personOpt = personService.getPerson(docId);
        if (!personOpt.isPresent()) return null;

        PersonDTO person = personOpt.get();
        Binder<PersonDTO> binder = new Binder<>(PersonDTO.class);

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);

        TextField name = new TextField("Name");
        TextField surname = new TextField("Surname");
        TextField documentIdField = new TextField("Document ID");
        TextField email = new TextField("Email");
        TextField phone = new TextField("Phone");
        TextField address = new TextField("Address");

        // non modificabili
        name.setValue(person.getName());
        surname.setValue(person.getSurname());
        documentIdField.setValue(person.getDocumentId());
        name.setEnabled(false);
        surname.setEnabled(false);
        documentIdField.setEnabled(false);

        // bind
        binder.forField(email).bind(PersonDTO::getEmail, PersonDTO::setEmail);
        binder.forField(phone).bind(PersonDTO::getPhone, PersonDTO::setPhone);
        binder.forField(address).bind(PersonDTO::getAddress, PersonDTO::setAddress);
        binder.setBean(person);

        Button saveBtn = new Button("Save Personal Info", e -> {
            try {
                binder.validate();
                personService.updatePerson(binder.getBean());
                NotificationService.showSuccess("Personal info updated");
            } catch (Exception ex) {
                NotificationService.showError("Error updating personal info: " + ex.getMessage());
            }
        });

        formLayout.add(name, surname, documentIdField, email, phone, address, saveBtn);
        return formLayout;
    }

    private VerticalLayout passwordChangeForm(UserDTO currentUser) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        PasswordField oldPassword = new PasswordField("Old Password");
        PasswordField newPassword = new PasswordField("New Password");
        PasswordField confirmPassword = new PasswordField("Confirm Password");

        Button changePasswordBtn = new Button("Change Password", e -> {
            String oldPwd = oldPassword.getValue();
            String newPwd = newPassword.getValue();
            String confirmPwd = confirmPassword.getValue();

            if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                NotificationService.showWarning("All password fields are required");
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                NotificationService.showWarning("New password and confirm password do not match");
                return;
            }

            try {
                userService.updatePassword(currentUser.getUsername(), oldPwd, newPwd);
                NotificationService.showSuccess("Password updated successfully");
                oldPassword.clear();
                newPassword.clear();
                confirmPassword.clear();
            } catch (Exception ex) {
                NotificationService.showError("Error updating password: " + ex.getMessage());
            }
        });

        layout.add(oldPassword, newPassword, confirmPassword, changePasswordBtn);
        return layout;
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return Roles.values();
    }
}