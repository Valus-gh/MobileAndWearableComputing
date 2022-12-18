package ch.disappointment.WalkoutCompanion;

public enum NotificationChannels {
    DEFAULT_NOTIFICATION_CHANNEL("walkout-companion.DEFAULT_NOTIFICATION_CHANNEL");

    private final String value;

    NotificationChannels(String id){
        this.value = id;
    }
    
    public String getValue() {
        return value;
    }
}
