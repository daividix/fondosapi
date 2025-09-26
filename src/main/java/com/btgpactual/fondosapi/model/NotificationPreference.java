package com.btgpactual.fondosapi.model;

public class NotificationPreference {
    
    private String notificationType; // "EMAIL" or "SMS"

    public NotificationPreference() {}

    public NotificationPreference(String type) { 
        this.notificationType = type; 
    }

    public String getNotificationType() { 
        return notificationType; 
    }

    public void setNotificationType(String notificationType) { 
        this.notificationType = notificationType; 
    }
}
