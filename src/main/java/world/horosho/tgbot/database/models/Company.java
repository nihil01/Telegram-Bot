package world.horosho.tgbot.database.models;

public class Company {

    private String name;
    private Long telegram_group_id;

    public Company(String name, Long telegram_group_id) {
        this.name = name;
        this.telegram_group_id = telegram_group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTelegram_group_id() {
        return telegram_group_id;
    }

    public void setTelegram_group_id(Long telegram_group_id) {
        this.telegram_group_id = telegram_group_id;
    }
}
