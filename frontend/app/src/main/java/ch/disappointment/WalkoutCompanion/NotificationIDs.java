package ch.disappointment.WalkoutCompanion;

public enum NotificationIDs {
    PERSISTENT_SERVICE(1);
    private int value;

    private NotificationIDs(int val){
        value = val;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "NotificationIDs{" +
                "value=" + value +
                '}';
    }
}
