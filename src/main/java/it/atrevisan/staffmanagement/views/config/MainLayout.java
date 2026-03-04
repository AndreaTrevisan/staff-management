package it.atrevisan.staffmanagement.views.config;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.views.session.SessionUtils;

import java.util.Arrays;

public class MainLayout extends AppLayout {

    public MainLayout() {

        // =========================
        // HEADER (top bar)
        // =========================

        DrawerToggle toggle = new DrawerToggle();
        H2 title = new H2("Staff Management");
        title.getStyle().set("margin", "0");

        Button logout = new Button("Logout", e -> {
            VaadinSession.getCurrent().close();
            getUI().ifPresent(ui -> ui.navigate(Routes.LOGIN));
        });

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Span spacer = new Span();
        header.expand(spacer);

        header.add(toggle, title, spacer, logout);

        addToNavbar(header);

        // =========================
        // SIDEBAR
        // =========================

        VerticalLayout menu = new VerticalLayout();
        menu.setSizeFull();
        menu.setPadding(false);
        menu.setSpacing(false);

        VerticalLayout links = new VerticalLayout();
        links.setPadding(false);
        links.setSpacing(false);
        links.setWidthFull();

        UserDTO user = SessionUtils.getSessionUser();

        // -------------------------
        // AUTHORIZED MENU ITEMS
        // -------------------------
        Arrays.stream(MenuConfig.items)
                .filter(item -> item.canAccess(user))
                .forEach(item -> links.add(item.buildRouterLink()));

        // -------------------------
        // USER PROFILE LINK
        // -------------------------

        VerticalLayout bottomSection = new VerticalLayout();
        bottomSection.setPadding(true);
        bottomSection.setSpacing(false);
        bottomSection.setWidthFull();

        Hr separator = new Hr();

        bottomSection.add(separator, MenuConfig.USER_PROFILE_ITEM.buildRouterLink());

        // PRINCIPAL LAYOUT
        menu.add(links);
        menu.expand(links);
        menu.add(bottomSection);

        addToDrawer(menu);
    }




}