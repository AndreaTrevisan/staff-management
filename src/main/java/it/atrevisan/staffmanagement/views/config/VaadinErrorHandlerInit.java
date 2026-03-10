package it.atrevisan.staffmanagement.views.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import it.atrevisan.staffmanagement.views.utils.UiExceptionHandler;
import org.springframework.stereotype.Component;

@Component
public class VaadinErrorHandlerInit implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {

        event.getSource().addSessionInitListener(sessionInitEvent -> {

            sessionInitEvent.getSession()
                    .setErrorHandler(new UiExceptionHandler());

        });

    }
}