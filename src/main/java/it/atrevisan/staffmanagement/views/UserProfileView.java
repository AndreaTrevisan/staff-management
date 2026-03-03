package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;

@Route(value = Routes.PROFILE, layout = MainLayout.class)
public class UserProfileView extends BasicLoggedInView {

    public UserProfileView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        removeAll();
        if(getCurrentUser().isPresent()) {
            add(new H1("Profile for User " + getCurrentUser().get().getUsername()));
        }
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return Roles.values();
    }

}
