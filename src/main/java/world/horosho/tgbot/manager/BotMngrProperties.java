package world.horosho.tgbot.manager;

import io.github.cdimascio.dotenv.Dotenv;

public class BotMngrProperties {
    Dotenv dotenv = Dotenv.configure().load();

    public String getBotToken() {
        return dotenv.get("token");
    }

}
