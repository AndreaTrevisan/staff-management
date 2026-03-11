package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.AbsenceService;
import it.atrevisan.staffmanagement.views.components.AbsenceGridComponent;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;

@Route(value = Routes.ABSENCES, layout = MainLayout.class)
public class AbsenceView extends BasicLoggedInView {

    public AbsenceView(AbsenceService absenceService){

        setSizeFull();

        AbsenceGridComponent grid =
                new AbsenceGridComponent(absenceService,false);

        grid.refresh();

        add(grid);
        expand(grid);
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN, Roles.HR};
    }
}
