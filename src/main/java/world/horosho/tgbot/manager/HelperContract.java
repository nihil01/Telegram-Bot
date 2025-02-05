package world.horosho.tgbot.manager;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface HelperContract {

    SendMessage startDiscussion(Long user, String message);

    SendMessage prepareRecoverDialog(Long userID);

    SendMessage prepareRecoverInput(Long chatID, Long userID, String messageText);

    SendMessage prepareCompanyInfoDialog(Long userID);

    SendMessage prepareCompanyDiscussionLinkDialog(Long userID);

    SendMessage notifyUserAboutCommands(Long userID);

    SendMessage saveGroupID(String chatTitle, Long groupID);
}
