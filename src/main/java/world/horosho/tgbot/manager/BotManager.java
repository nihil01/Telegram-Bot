package world.horosho.tgbot.manager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import world.horosho.tgbot.database.models.User;
import world.horosho.tgbot.services.LoggerService;
import world.horosho.tgbot.services.ResultStatus;
import world.horosho.tgbot.services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


public class BotManager extends TelegramLongPollingBot {
    private final BotMngrProperties props;
    private final Map<Long, Boolean> waitingForPassword = new HashMap<>();
    private HelperContract helperMethods = new HelperMethods();

    public BotManager(BotMngrProperties props) {
        this.props = props;
    }

    @Override
    public void onUpdateReceived(Update update) {
        //Dialog processors for user input
        if (update.hasMessage() && update.getMessage().isCommand()){
            String messageText = update.getMessage().getText();
            long userID = update.getMessage().getFrom().getId();

            try{
                if (messageText.startsWith("/start")){
                    execute(helperMethods.startDiscussion(userID, messageText));
                }else if (messageText.equalsIgnoreCase("/password_reset")){
                    execute(helperMethods.prepareRecoverDialog(userID));
                }else if (messageText.equalsIgnoreCase("/my_company")){
                    execute(helperMethods.prepareCompanyInfoDialog(userID));
                }else if (messageText.equalsIgnoreCase("/company_discussion_link")){
                    execute(helperMethods.prepareCompanyDiscussionLinkDialog(userID));
                }else if (messageText.startsWith("/get_group_id")){
                    if (update.getMessage().isGroupMessage()){
                        execute(helperMethods.saveGroupID(
                                update.getMessage().getChat().getTitle(), update.getMessage().getChatId())
                        );
                    }
                }
            } catch (TelegramApiException e) {
                LoggerService.log("BotManager manager warn: SQL QUERY" + e.getMessage(), Level.WARNING);
            }

        } else if (update.hasCallbackQuery()){
            //here are command processors
            int messageID = update.getCallbackQuery().getMessage().getMessageId();
            long userID = update.getCallbackQuery().getFrom().getId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            SendMessage sm = new SendMessage();
            sm.setChatId(chatId);

            try{
                switch (callbackData) {
                    case "PASSWORD_RESET_YES": {
                        HashMap<ResultStatus, User> user = UserService.getUser(userID);

                        if (user.containsKey(ResultStatus.FAILURE)) {
                            sm.setText("İD-niz tapılmadı!");
                            execute(sm);
                            return;
                        }

                        sm.setText("Zəhmət olmasa, yeni parolu daxil edin:");
                        execute(sm);
                        waitingForPassword.put(userID, true);
                        break;
                    }

                    case "PASSWORD_RESET_NO": {
                        sm.setText("Əməliyyat imtina olunub!");
                        execute(sm);
                        break;
                    }
                }

                EditMessageReplyMarkup emrp = EditMessageReplyMarkup.builder()
                    .chatId(chatId)
                    .messageId(messageID)
                    .replyMarkup(null)
                    .build();

                execute(emrp);
            }catch (Exception e){
                LoggerService.log("BotManager manager warn: SQL QUERY" + e.getMessage(), Level.WARNING);
            }
        }else{
            //default processor, i.e. /password_reset field input ..
            String messageText = update.getMessage().getText();
            long userID = update.getMessage().getFrom().getId();
            long chatID = update.getMessage().getChatId();

            if (waitingForPassword.getOrDefault(userID, false)) {
                waitingForPassword.remove(userID);
                try {
                    execute(helperMethods.prepareRecoverInput(chatID, userID, messageText));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                if (!update.getMessage().isGroupMessage())execute(helperMethods.notifyUserAboutCommands(userID));
            } catch (TelegramApiException e) {
                LoggerService.log("No update" + e.getMessage(), Level.WARNING);
                throw new RuntimeException(e);
            }
        }
    }



    @Override
    public String getBotToken() {
        System.out.println("Bot Token: " + this.props.getBotToken());
        return this.props.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return "Orkhan Bot";
    }

}
