package world.horosho.tgbot.database.models;

public class UserData {
    private String credentials;
    private int day;
    private String discussion_uid;
    private String number;

    public UserData(int day, String discussion_uid, String credentials, String number) {
        this.number = number;
        this.discussion_uid = discussion_uid;
        this.credentials = credentials;
        this.day = day;
    }

    public void setDiscussion_uid(String discussion_uid) {
        this.discussion_uid = discussion_uid;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getDiscussion_uid() {
        return discussion_uid;
    }

    public String getCredentials() {
        return credentials;
    }

    public int getDay() {
        return day;
    }
}
