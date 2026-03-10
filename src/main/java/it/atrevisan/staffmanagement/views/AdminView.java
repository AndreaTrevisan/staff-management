package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.UserService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = Routes.ADMIN, layout = MainLayout.class)
public class AdminView extends BasicLoggedInView {

    private final UserService userService;

    private final Grid<UserDTO> grid = new Grid<>(UserDTO.class, false);
    private final TextField usernameFilter = new TextField();

    public AdminView(UserService userService) {
        this.userService = userService;

        setSizeFull();

        // =========================
        // TOP BAR (Filter + Create)
        // =========================

        usernameFilter.setPlaceholder("Filter by username...");
        usernameFilter.setClearButtonVisible(true);
        usernameFilter.addValueChangeListener(e -> refreshGrid());

        Button createBtn = new Button("New User", e -> openCreateDialog());

        HorizontalLayout topBar = new HorizontalLayout(usernameFilter, createBtn);
        topBar.setWidthFull();
        topBar.expand(usernameFilter);

        // =========================
        // GRID
        // =========================

        grid.addComponentColumn(this::buildActionsColumn)
                .setHeader("Actions")
                .setAutoWidth(true);

        grid.addColumn(UserDTO::getUsername)
                .setHeader("Username")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(UserDTO::getUsername);

        grid.addColumn(UserDTO::isEnabled)
                .setHeader("Enabled")
                .setAutoWidth(true);

        grid.addComponentColumn(user -> {
            HorizontalLayout badges = new HorizontalLayout();
            badges.setPadding(false);
            badges.setSpacing(true);

            user.getRoles().forEach(role -> {
                Span badge = new Span(role);
                badge.getElement().getThemeList().add("badge");
                badges.add(badge);
            });

            return badges;
        }).setHeader("Roles");

        grid.addColumn(UserDTO::getCreatedBy)
                .setHeader("Created By")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(UserDTO::getCreatedBy);

        grid.addColumn(UserDTO::getCreatedTime)
                .setHeader("Create Time")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(UserDTO::getCreatedTime);

        grid.addColumn(UserDTO::getUpdatedBy)
                .setHeader("Last Updated By")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(UserDTO::getUpdatedBy);

        grid.addColumn(UserDTO::getUpdatedTime)
                .setHeader("Last Update Time")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(UserDTO::getUpdatedTime);

        grid.setSizeFull();

        refreshGrid();

        add(topBar, grid);
        expand(grid);
    }

    private void refreshGrid() {
        String filter = usernameFilter.getValue();
        List<UserDTO> users = userService.getAllUsers();

        if (filter != null && !filter.isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getUsername().toLowerCase()
                            .contains(filter.toLowerCase()))
                    .collect(Collectors.toList());
        }

        grid.setItems(users);
    }

    private HorizontalLayout buildActionsColumn(UserDTO user) {

        Button pwdBtn = new Button(new Icon(VaadinIcon.KEY));
        pwdBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        pwdBtn.getElement().setProperty("title", "Change password");
        pwdBtn.addClickListener(e -> openChangePasswordDialog(user));

        Button rolesBtn = new Button(new Icon(VaadinIcon.USERS));
        rolesBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        rolesBtn.getElement().setProperty("title", "Edit roles");
        rolesBtn.addClickListener(e -> openRolesDialog(user));

        VaadinIcon toggleIcon = user.isEnabled()
                ? VaadinIcon.LOCK
                : VaadinIcon.UNLOCK;

        Button toggleBtn = new Button(new Icon(toggleIcon));
        toggleBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        toggleBtn.getElement().setProperty("title",
                user.isEnabled() ? "Disable user" : "Enable user");

        toggleBtn.addClickListener(e -> {
            userService.toggleUser(user.getUsername());
            refreshGrid();
        });

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY
        );
        deleteBtn.getElement().setProperty("title", "Delete user");

        deleteBtn.addClickListener(e -> {
            userService.deleteUser(user.getUsername());
            NotificationService.showSuccess("User deleted");
            refreshGrid();
        });

        return new HorizontalLayout(pwdBtn, rolesBtn, toggleBtn, deleteBtn);
    }

    private void openCreateDialog() {

        Dialog dialog = new Dialog();

        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        Checkbox enabled = new Checkbox("Enabled", true);

        // =========================
        // ROLES SELECT
        // =========================

        CheckboxGroup<Roles> rolesGroup = new CheckboxGroup<>();
        rolesGroup.setLabel("Roles");
        rolesGroup.setItems(Roles.values());

        // Default role
        rolesGroup.setValue(Collections.singleton(Roles.STAFF));

        rolesGroup.setItemLabelGenerator(Enum::name);

        // =========================
        // SAVE BUTTON
        // =========================

        Button save = new Button("Save", e -> {

            Set<String> roles = rolesGroup.getValue()
                    .stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet());

            userService.createUser(
                    username.getValue(),
                    password.getValue(),
                    roles,
                    enabled.getValue()
            );

            NotificationService.showSuccess("User saved");

            dialog.close();
            refreshGrid();
        });

        dialog.add(new VerticalLayout(
                username,
                password,
                enabled,
                rolesGroup,
                save
        ));

        dialog.open();
    }

    private void openChangePasswordDialog(UserDTO user) {

        Dialog dialog = new Dialog();
        PasswordField newPassword = new PasswordField("New Password");

        Button save = new Button("Update", e -> {
            userService.updatePassword(user.getUsername(), user.getPassword(), newPassword.getValue());
            NotificationService.showSuccess("Password Updated");
            dialog.close();
            refreshGrid();
        });

        dialog.add(new VerticalLayout(newPassword, save));
        dialog.open();
    }

    private void openRolesDialog(UserDTO user) {

        Dialog dialog = new Dialog();

        CheckboxGroup<Roles> rolesGroup = new CheckboxGroup<>();
        rolesGroup.setLabel("Roles");
        rolesGroup.setItems(Roles.values());
        rolesGroup.setItemLabelGenerator(Enum::name);

        Set<Roles> currentRoles = user.getRoles().stream()
                .map(Roles::valueOf)
                .collect(java.util.stream.Collectors.toSet());

        rolesGroup.setValue(currentRoles);

        Button save = new Button("Save", e -> {

            Set<String> updatedRoles = rolesGroup.getValue()
                    .stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet());

            userService.updateRoles(user.getUsername(), updatedRoles);
            NotificationService.showSuccess("Roles updated");
            dialog.close();
            refreshGrid();
        });

        dialog.add(new VerticalLayout(rolesGroup, save));
        dialog.open();
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN};
    }

}
