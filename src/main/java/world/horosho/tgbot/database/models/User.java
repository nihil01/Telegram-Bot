package world.horosho.tgbot.database.models;

public class User {
    private String credentials;
    private String number;
    private String company;
    private Integer privilege_level;
    private String last_seen;

    public User(String credentials, String number, String company) {
        this.credentials = credentials;
        this.number = number;
        this.company = company;
    }

    public User(String credentials, String number, String company, Integer privilege_level, String last_seen) {
        this.credentials = credentials;
        this.number = number;
        this.company = company;
        this.privilege_level = privilege_level;
        this.last_seen = last_seen;
    }

    public Integer getPrivilege_level() {
        return privilege_level;
    }

    public String getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(String last_seen) {
        this.last_seen = last_seen;
    }

    public void setPrivilege_level(int privilege_level) {
        this.privilege_level = privilege_level;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "User{" +
                ", credentials='" + credentials + '\'' +
                ", number='" + number + '\'' +
                ", company='" + company + '\'' +
                '}';
    }
}
