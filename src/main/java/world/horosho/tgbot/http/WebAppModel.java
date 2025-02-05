package world.horosho.tgbot.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class WebAppModel {
    private String state;
    private String corporation;
    private Boolean special;
    private Integer timeLeft;

    private Boolean adminDelete;
    private String deleteIssuer;
    private String number;
    private String author;

    private String category;
    private String admin;
    private String link;
    private String exState;

    private Boolean returned;
    private String moveIssuer;
    private Boolean flag;
    private String headAdminName;

    private String lastMessageRole;
    private String room;
    private String issuer;
    private String timeSent;

    private List<Admin> adminList;
    private List<User> userCreator;
    private List<Room> roomData;

    private String company;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    // Геттеры и сеттеры
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCorporation() {
        return corporation;
    }

    public void setCorporation(String corporation) {
        this.corporation = corporation;
    }

    public Boolean getSpecial() {
        return special;
    }

    public void setSpecial(Boolean special) {
        this.special = special;
    }

    public Integer getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(Integer timeLeft) {
        this.timeLeft = timeLeft;
    }

    public Boolean getAdminDelete() {
        return adminDelete;
    }

    public void setAdminDelete(Boolean adminDelete) {
        this.adminDelete = adminDelete;
    }

    public String getDeleteIssuer() {
        return deleteIssuer;
    }

    public void setDeleteIssuer(String deleteIssuer) {
        this.deleteIssuer = deleteIssuer;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getExState() {
        return exState;
    }

    public void setExState(String exState) {
        this.exState = exState;
    }

    public Boolean getReturned() {
        return returned;
    }

    public void setReturned(Boolean returned) {
        this.returned = returned;
    }

    public String getMoveIssuer() {
        return moveIssuer;
    }

    public void setMoveIssuer(String moveIssuer) {
        this.moveIssuer = moveIssuer;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getHeadAdminName() {
        return headAdminName;
    }

    public void setHeadAdminName(String headAdminName) {
        this.headAdminName = headAdminName;
    }

    public String getLastMessageRole() {
        return lastMessageRole;
    }

    public void setLastMessageRole(String lastMessageRole) {
        this.lastMessageRole = lastMessageRole;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public List<Admin> getAdminList() {
        return adminList;
    }

    public void setAdminList(List<Admin> adminList) {
        this.adminList = adminList;
    }

    public List<User> getUserCreator() {
        return userCreator;
    }

    public void setUserCreator(List<User> userCreator) {
        this.userCreator = userCreator;
    }

    public List<Room> getRoomData() {
        return roomData;
    }

    public void setRoomData(List<Room> roomData) {
        this.roomData = roomData;
    }

    @Override
    public String toString() {
        return "BotTypes{" +
                "state='" + state + '\'' +
                ", corporation='" + corporation + '\'' +
                ", special=" + special +
                ", timeLeft=" + timeLeft +
                ", adminDelete=" + adminDelete +
                ", deleteIssuer='" + deleteIssuer + '\'' +
                ", number='" + number + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", admin='" + admin + '\'' +
                ", link='" + link + '\'' +
                ", exState='" + exState + '\'' +
                ", returned=" + returned +
                ", moveIssuer='" + moveIssuer + '\'' +
                ", flag=" + flag +
                ", headAdminName='" + headAdminName + '\'' +
                ", lastMessageRole='" + lastMessageRole + '\'' +
                ", room='" + room + '\'' +
                ", issuer='" + issuer + '\'' +
                ", timeSent='" + timeSent + '\'' +
                ", adminList=" + adminList +
                ", userCreator=" + userCreator +
                ", roomData=" + roomData +
                '}';
    }
    public static class Admin{
        private String adminmessages;
        private Integer id;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getAdminmessages() {
            return adminmessages;
        }

        public void setAdminmessages(String adminmessages) {
            this.adminmessages = adminmessages;
        }

        private String number;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

    }

    public static class User{
        private String credentials;
        private String number;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        private Integer id;

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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Room{
        private String corporation;
        private String credentials;
        private String category;

        public String getCorporation() {
            return corporation;
        }

        public void setCorporation(String corporation) {
            this.corporation = corporation;
        }

        public String getCredentials() {
            return credentials;
        }

        public void setCredentials(String credentials) {
            this.credentials = credentials;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}

