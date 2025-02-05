package world.horosho.tgbot.database.models;

public class DiscussionData {
    private final String corporation;
    private final String category;

    public DiscussionData(String corporation, String category) {
        this.corporation = corporation;
        this.category = category;
    }

    public String getCorporation() {
        return corporation;
    }

    public String getCategory() {
        return category;
    }
}