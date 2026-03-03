package it.atrevisan.staffmanagement.views.session;

import com.vaadin.flow.server.VaadinSession;
import it.atrevisan.staffmanagement.model.User;

public class SessionUtils {

    private static final String USER_KEY = "user";

    public static void setSessionUser(User user){
        VaadinSession.getCurrent().setAttribute(USER_KEY, user);
    }

    public static User getSessionUser(){
        return (User) VaadinSession.getCurrent().getAttribute(USER_KEY);
    }
}
