package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;

@Route(value = Routes.CALENDAR, layout = MainLayout.class)
public class CalendarView extends BasicLoggedInView {

    public CalendarView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        removeAll();
        if(getCurrentUser().isPresent()) {
            add(new H1("Calendar"));
        }
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN, Roles.HR};
    }

}
