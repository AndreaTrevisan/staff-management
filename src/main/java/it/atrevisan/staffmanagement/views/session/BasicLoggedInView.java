package it.atrevisan.staffmanagement.views.session;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.views.config.Routes;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BasicLoggedInView extends VerticalLayout implements BeforeEnterObserver {

    protected abstract Roles[] getAllowedRoles();

    protected Optional<UserDTO> getCurrentUser() {
        return Optional.ofNullable(SessionUtils.getSessionUser());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if(!isUserAllowed()){
            event.forwardTo(Routes.LOGIN); // redirect
        }

    }

    private boolean isUserAllowed() {
        if(!getCurrentUser().isPresent()) return false;
        for(String role : getCurrentUser().get().getRoles()){
            if(!Arrays.stream(getAllowedRoles())
                    .map(Enum::name)
                    .filter(r-> r.equals(role))
                    .collect(Collectors.toSet()).isEmpty()
            ) {
                return true;
            }
        }

        return false;
    }
}
