package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.ContractService;
import it.atrevisan.staffmanagement.views.components.ContractGridComponent;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;

@Route(value = Routes.CONTRACTS, layout = MainLayout.class)
public class ContractView extends BasicLoggedInView  {

    public ContractView(ContractService contractService) {
        setSizeFull();
        ContractGridComponent contractGridComponent = new ContractGridComponent(contractService, false);
        contractGridComponent.refresh();
        add(contractGridComponent);
        expand(contractGridComponent);
    }


    @Override
    protected Roles[] getAllowedRoles() {
        return new Roles[]{Roles.ADMIN, Roles.HR};
    }
}