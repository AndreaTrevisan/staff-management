package it.atrevisan.staffmanagement.views.config;

import com.vaadin.flow.component.icon.VaadinIcon;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.views.*;

public class MenuConfig {

    protected static final MenuItem HOME_ITEM = new MenuItem(
            Roles.toSet(Roles.values()),
            HomeView.class,
            "Home",
            VaadinIcon.HOME
    );

    protected static final MenuItem ADMIN_ITEM = new MenuItem(
            Roles.toSet(Roles.ADMIN),
            AdminView.class,
            "Admin",
            VaadinIcon.SHIELD
    );

    public static final MenuItem CALENDAR_ITEM = new MenuItem(
            Roles.toSet(Roles.ADMIN, Roles.HR),
            CalendarView.class,
            "Calendar",
            VaadinIcon.CALENDAR
    );

    public static final MenuItem STAFF_ITEM = new MenuItem(
            Roles.toSet(Roles.ADMIN, Roles.HR),
            StaffView.class,
            "Staff",
            VaadinIcon.USERS
    );

    public static final MenuItem USER_PROFILE_ITEM = new MenuItem(
            Roles.toSet(Roles.values()),
            UserProfileView.class,
            "Profile",
            VaadinIcon.USER
    );


    protected static final MenuItem[] items = {
            HOME_ITEM,
            ADMIN_ITEM,
            CALENDAR_ITEM,
            STAFF_ITEM
    };
}
