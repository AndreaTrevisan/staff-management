package it.atrevisan.staffmanagement.views.config;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class MenuItem {
    private final Set<String> roles;
    private final Class<? extends BasicLoggedInView> viewClass;
    private final String label;
    private VaadinIcon icon;

    protected RouterLink buildRouterLink(){
        com.vaadin.flow.component.icon.Icon i = icon.create();
        i.getStyle().set("margin-right", "10px");

        RouterLink link = new RouterLink("", viewClass);

        HorizontalLayout content = new HorizontalLayout(i, new Span(label));
        content.setAlignItems(FlexComponent.Alignment.CENTER);

        link.add(content);

        return styleMenuItem(link);
    }

    private RouterLink styleMenuItem(RouterLink link) {

        link.getStyle()
                .set("padding", "10px 16px")
                .set("border-radius", "6px")
                .set("cursor", "pointer");

        link.getStyle().set("margin-bottom", "4px");

        return link;
    }

    private boolean canAccess(Set<String> userRoles){
        for(String role : userRoles){
            if(roles.contains(role)){
                return true;
            }
        }
        return false;
    }

    protected boolean canAccess(UserDTO user){
        if(user == null) return false;
        return canAccess(user.getRoles());
    }
}
