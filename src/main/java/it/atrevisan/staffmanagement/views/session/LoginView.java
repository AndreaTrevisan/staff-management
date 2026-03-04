package it.atrevisan.staffmanagement.views.session;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.service.UserService;
import it.atrevisan.staffmanagement.views.config.Routes;

@Route(Routes.LOGIN)
@PageTitle("Login")
public class LoginView extends VerticalLayout {

    public LoginView(UserService userService) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Login");

        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        Button loginButton = new Button("Log in", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (userService.checkUser(username, password)) {
                UserDTO user = userService.findByUsername(username).orElse(null);
                SessionUtils.setSessionUser(user);
                getUI().ifPresent(ui -> ui.navigate("home"));

            } else {
                Notification.show("Wrong username or password", 3000, Notification.Position.MIDDLE);
            }
        });

        add(title, usernameField, passwordField, loginButton);
    }
}