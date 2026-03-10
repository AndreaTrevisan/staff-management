package it.atrevisan.staffmanagement.views.utils;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class NotificationService {

    private NotificationService(){}

    public static void showSuccess(String message){
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static void showError(String message){
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public static void showWarning(String message){
        Notification notification = Notification.show(message, 3500, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}