package world.horosho.tgbot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import world.horosho.tgbot.http.BasicServer;
import world.horosho.tgbot.http.telegram.TelegramScheduler;
import world.horosho.tgbot.manager.BotManager;
import world.horosho.tgbot.manager.BotMngrProperties;
import world.horosho.tgbot.services.LoggerService;

import java.io.IOException;
import java.util.logging.Level;

public class TgbotApplication {

	public static void main(String[] args) throws TelegramApiException, IOException {
		//start bot
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot(new BotManager(new BotMngrProperties()));

		//start web server
		new BasicServer(9090);

		//schedule task for broadcasting messages in separate thread
		TelegramScheduler.scheduleTask(9, 0, 0);

		LoggerService.log("Bot started on port 9090", Level.INFO);
	}

}
