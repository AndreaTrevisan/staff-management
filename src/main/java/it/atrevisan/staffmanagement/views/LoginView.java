package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import it.atrevisan.staffmanagement.repository.UserRepository;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

@Route("login")
@PageTitle("Login")
public class LoginView extends VerticalLayout {

    private final UserService userService;

    public LoginView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Login");

        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        Button loginButton = new Button("Log in", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            User user = userService.findByUsername(username).orElse(null);
            if (user != null && user.isEnabled() &&
                    BCrypt.checkpw(password, user.getPassword())) {

                VaadinSession.getCurrent().setAttribute("user", user);
                getUI().ifPresent(ui -> ui.navigate("home"));

            } else {
                Notification.show("Wrong username or password", 3000, Notification.Position.MIDDLE);
            }
        });

        add(title, usernameField, passwordField, loginButton);
    }
}