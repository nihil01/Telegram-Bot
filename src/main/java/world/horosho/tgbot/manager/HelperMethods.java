package world.horosho.tgbot.manager;

import io.github.bucket4j.Bucket;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import world.horosho.tgbot.database.controller.DatabaseController;
import world.horosho.tgbot.database.models.User;
import world.horosho.tgbot.services.LoggerService;
import world.horosho.tgbot.services.RateLimitInitializer;
import world.horosho.tgbot.services.ResultStatus;
import world.horosho.tgbot.services.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelperMethods implements HelperContract{

    private static Bucket bucket = RateLimitInitializer.getLimitBucket(5, 20, Duration.ofMinutes(1));

    @Override
    public SendMessage startDiscussion(Long user, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user);

        String[] splittedQuery = message.split("\\?");
        if (splittedQuery.length == 2){
            int operatingIndex = splittedQuery[1].indexOf("id=");
            try{
                String encodedNumber = operatingIndex != -1 ? splittedQuery[1].substring(3) : null;
                String decodedNumber = new String(Base64.getDecoder().decode(encodedNumber));

                System.out.println(decodedNumber);
                Pattern pattern = Pattern.compile("\\+994(50|51|55|70|71|10|99)[0-9]{7}");
                Matcher matcher = pattern.matcher(decodedNumber);
                System.out.println("decodedNumber: " + decodedNumber);
                System.out.println("salam");
                if (!matcher.find()){
                    System.out.println("Invalid number");
                    sendMessage.setText("Mən şübhələnirəm ki, Siz səhv linki daxil etmisiz .. \uD83E\uDD14\uD83E\uDD14");
                    return sendMessage;
                }

                if (bucket.tryConsume(5)){
                    boolean result = DatabaseController.linkUserToTelegramUUID(user, decodedNumber);
                    if (result) {
                        sendMessage.setText("Afərin! Mənim adım Orxandır. Mən sizin işinizdə yaxın dost olacam!\n" +
                                "Menyudan Siz fərqli kodları seçə bilərsiniz, mən onları icra edərəm. \n" +
                                "Həm də mən Sizi xəbərdar edəcəm (şəxsi söhbətdə və şirkətin müzakirədə) \n" +
                                "Uğurlar \uD83D\uDE3A!");
                    } else {
                        sendMessage.setText("Biz artıq tanış olduq \uD83D\uDE42\u200D↔\uFE0F");
                    }

                }else{
                    sendMessage.setText("Bir az gözləyin ...");
                }

            } catch (IllegalArgumentException e) {
                LoggerService.log("Helper methods: Could nt start discussion" + e.getMessage(), Level.WARNING);
            }
        }else{
            sendMessage.setText("\uD83D\uDE45 Hər vaxtınız xeyir! Zəhmət olmasa, web səhifədə olan kodunu daxil edərsiniz ..");
        }
        return sendMessage;
    }

    @Override
    public SendMessage prepareRecoverDialog(Long userID) {

        InlineKeyboardButton continueBtn = new InlineKeyboardButton();
        continueBtn.setCallbackData("PASSWORD_RESET_YES");
        continueBtn.setText("Davam etmək");

        InlineKeyboardButton cancelBtn = new InlineKeyboardButton();
        cancelBtn.setCallbackData("PASSWORD_RESET_NO");
        cancelBtn.setText("İmtina etmək");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(continueBtn, cancelBtn)));

        return SendMessage.builder()
                .text("Nəzərinizə çatdırmaq istəyirəm ki, bu əməliyyatı" +
                        " <b>bir saat</b> ərzində yalnız bir dəfə icra etmək olar. Davam etmək?")
                .chatId(userID)
                .parseMode("html")
                .replyMarkup(markup)
                .build();
    }

    public SendMessage prepareRecoverInput(Long chatID, Long userID, String messageText) {
        HashMap<ResultStatus, User> users = UserService.getUser(userID);
        try {
            return
                SendMessage.builder().chatId(chatID).text(
                        UserService.recoverPassword(users.get(ResultStatus.SUCCESS), messageText)
                ).build();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SendMessage prepareCompanyInfoDialog(Long userID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userID);
        if (bucket.tryConsume(5)){
            try {
                Map<String, List<User>> users = DatabaseController.getAllUsersInMyCompany(userID);
                String company = users.keySet().iterator().next();
                StringBuilder stringBuilder = new StringBuilder();
                if (!users.isEmpty() && !users.containsKey("error")){
                    stringBuilder.append("⭐ŞİRKƏTİM: %s⭐\n\n".formatted(company));
                    for (User user : users.get(company)){
                        String userLastSeen = "N/A";
                        if (user.getLast_seen() != null){
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                            LocalDateTime ldt = LocalDateTime.parse(user.getLast_seen().substring(0, user.getLast_seen().indexOf("Z")));
                            userLastSeen = ldt.format(formatter);
                        }

                        stringBuilder.append(
                                "\uD83E\uDEAA " + user.getCredentials() + " ☎\uFE0F " + user.getNumber() + "\n"+
                                (user.getPrivilege_level() == 1 ? "\uD83D\uDFE6 İstifadəçi \uD83D\uDFE6" : user.getPrivilege_level() == 2 ?
                                "\uD83D\uDFE7 Admin \uD83D\uDFE7" : user.getPrivilege_level() == 3 ?
                                "\uD83D\uDFE5 Baş Admin \uD83D\uDFE5" : "-") + "\n Son onlayn tarixi: " + userLastSeen +"\n\n"
                        );
                    }
                    sendMessage.setText(stringBuilder.toString());
                    return sendMessage;
                }
                sendMessage.setText("Təəssüf ki, hesabınız barəsində məlumat tapılmadı. Yəqin ki, səhv qeydiyyatdan keçmisiniz. Əks halda, Admin ilə əlaqə saxlayın");
            } catch (RuntimeException e) {
                LoggerService.log("Helper methods: Could nt prepare company info dialog" + e.getMessage(), Level.WARNING);
                throw new RuntimeException(e);
            }
        }else{
            sendMessage.setText("Bir az gözləyin ..");
        }
        return sendMessage;
    }

    @Override
    public SendMessage prepareCompanyDiscussionLinkDialog(Long userID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userID);
        if (bucket.tryConsume(5)){
            try{
                String link = DatabaseController.getCompanyChatGroupLink(userID);
                if (link != null){

                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText("Qrupa keçmək ..");
                    button.setUrl(link);

                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    markup.setKeyboard(List.of(List.of(button)));
                    sendMessage.setReplyMarkup(markup);
                    sendMessage.setText("Buyurun");
                }else {
                    sendMessage.setText("Hal-hazırda, şirkətinizin qrupu yaradılmayıb \uD83D\uDE3F. Admin ilə əlaqə saxlayın");
                }
            } catch (RuntimeException e) {
                LoggerService.log("Helper methods: Could nt prepare company discussion link dialog" + e.getMessage(), Level.WARNING);
                throw new RuntimeException(e);
            }
        }else{
            sendMessage.setText("Bir az gözləyin ..");
        }
        return sendMessage;
    }

    @Override
    public SendMessage notifyUserAboutCommands(Long userID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userID);
        System.out.println("meow");
        try {
            if (bucket.tryConsume(5)) {

                User user = DatabaseController.getUserByTelegramUUID(userID);

                if (user != null) {
                    sendMessage.setText(("Hörmətli %s, zəhmət olmasa menyudan" +
                            " mənim bacardığım hərəkətlər ilə tanış olarsınız \uD83D\uDE04\uD83D\uDE04").formatted(user.getCredentials()));
                } else {
                    sendMessage.setText("Hörmətli istifadəçi, mən Sizi hələ tanımıram, ona görə Siz web-səhifədən söhbətimizi başlamalısınız !\uD83D\uDE38\uD83D\uDE38");
                }

            } else {
                sendMessage.setText("Bir az gözləyin ..");
            }

            return sendMessage;
        } catch (RuntimeException e) {
            LoggerService.log("Helper methods: Could nt notify about commands" + e.getMessage(), Level.WARNING);
            throw new RuntimeException(e);
        }

    }

    @Override
    public SendMessage saveGroupID(String chat, Long id) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        try{
            if (bucket.tryConsume(5)) {

                boolean result = DatabaseController.saveChatID(chat, id);

                if (result) {
                    sendMessage.setText("Yadda saxladım!");
                }else{
                    sendMessage.setText("Uğursuz əməliyyat. Yəqin ki qrupun adı fərqlənir və ya ID artıq saxlanılıb!");
                }

            } else {
                sendMessage.setText("Bir az gözləyin ..");
            }
        } catch (RuntimeException e) {
            LoggerService.log("Helper methods: Couldnt save a group ID" + e.getMessage(), Level.WARNING);
            throw new RuntimeException(e);
        }
        return sendMessage;
    }
}
