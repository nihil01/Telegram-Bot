package world.horosho.tgbot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import world.horosho.tgbot.manager.BotManager;
import world.horosho.tgbot.manager.BotMngrProperties;

public class TgbotApplication {

	public static void main(String[] args) throws TelegramApiException {

		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot(new BotManager(new BotMngrProperties()));
	}

}
