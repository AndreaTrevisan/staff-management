package it.atrevisan.staffmanagement.views.utils;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UiExceptionHandler implements ErrorHandler {

    @Override
    public void error(ErrorEvent event) {

        Throwable throwable = unwrap(event.getThrowable());

        if (throwable instanceof IllegalArgumentException) {
            NotificationService.showWarning(throwable.getMessage());
        }
        else if (throwable instanceof IllegalStateException) {
            NotificationService.showError(throwable.getMessage());
        }
        else {
            NotificationService.showError("Unexpected error occurred: "+throwable.getMessage());
            log.error("UI error", throwable);
        }
    }

    private Throwable unwrap(Throwable throwable) {
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }
}