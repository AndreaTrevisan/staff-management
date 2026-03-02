package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import it.atrevisan.staffmanagement.model.User;

@Route("home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Check login
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        if (user == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        add(new H1("Welcome " + user.getUsername()));
    }
}