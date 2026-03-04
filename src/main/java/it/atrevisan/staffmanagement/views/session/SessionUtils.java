package it.atrevisan.staffmanagement.views.session;

import com.vaadin.flow.server.VaadinSession;
import it.atrevisan.staffmanagement.dto.UserDTO;

public class SessionUtils {

    private static final String USER_KEY = "user";

    public static void setSessionUser(UserDTO user){
        VaadinSession.getCurrent().setAttribute(USER_KEY, user);
    }

    public static UserDTO getSessionUser(){
        return (UserDTO) VaadinSession.getCurrent().getAttribute(USER_KEY);
    }
}
