package world.horosho.tgbot.http.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import world.horosho.tgbot.database.controller.DatabaseController;
import world.horosho.tgbot.http.WebAppModel;
import world.horosho.tgbot.manager.BotManager;
import world.horosho.tgbot.manager.BotMngrProperties;
import world.horosho.tgbot.services.LoggerService;

import java.util.List;
import java.util.logging.Level;

public class DataBroadcaster extends BotManager {

    private static final String HTTP_LINK = "https://uygunsuzluqlar.horosho.world/discussion/";
    public DataBroadcaster(BotMngrProperties props) {
        super(props);
    }

    public static String generateLink(String room){
        return "<a href=\"%s%s\">Sayta keçmək..</a>".formatted(HTTP_LINK, room);
    }

    public void directBroadcast(WebAppModel model) {
        Integer userID = model.getUserCreator().getFirst().getId();
        System.out.println("admin list " + model.getAdminList().size());

        for (WebAppModel.Admin admin: model.getAdminList()){
            Integer adminID = admin.getId();

            if (!model.getAdminList().isEmpty() && !model.getIssuer().equalsIgnoreCase(admin.getAdminmessages())){
                directBroadcastMessagePrep(model, adminID);
            }

        }


        if (!model.getUserCreator().getFirst().getCredentials().equalsIgnoreCase(model.getIssuer())){
            directBroadcastMessagePrep(model, userID);
        }

    }

    public void groupBroadcast(WebAppModel model, String text) {
        String company = model.getCompany();
        Long id = DatabaseController.getGroupID(company);

        if (id.describeConstable().isPresent()){
            SendMessage msg = new SendMessage();
            msg.setChatId(id);
            msg.setParseMode("HTML");

            msg.setText(text);

            try {
                super.execute(msg);
            } catch (TelegramApiException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public void groupBroadcast(long id, String text, String mentions) {
            SendMessage msg = new SendMessage();
            msg.setChatId(id);
            msg.setParseMode("HTML");

            msg.setText(text + "\n\n" + mentions);

            try {
                super.execute(msg);
            } catch (TelegramApiException e) {
                LoggerService.log("Could not make a group broadcast" + e.getMessage(), Level.SEVERE);
                throw new RuntimeException(e);
            }
        }

    private void directBroadcastMessagePrep(WebAppModel model, int id){
        String corporation = model.getRoomData().getFirst().getCorporation();
        String userCredentials = model.getUserCreator().getFirst().getCredentials();
        String category = model.getRoomData().getFirst().getCategory();
        String lastMessageRole = model.getLastMessageRole();
        String issuer = model.getIssuer();
        String discussionLink = "<a href=\"%s%s\">Sayta keçmək..</a>".formatted(HTTP_LINK, model.getRoom());

        SendMessage msg = new SendMessage();
        msg.setChatId((long) id);
        msg.setParseMode("HTML");
        msg.setText(
            "📩 <b>Yeni ismarış</b> 📩\n" +
                    "<b>Müəssisənin adı:</b> " + corporation + "\n" +
                    "<b>Əməkdaşın adı:</b> " + userCredentials + "\n" +
                    "<b>Kategoriyanın adı:</b> " + category + "\n" +
                    "<b>Son messaj:</b> (" + lastMessageRole + ") " + issuer + "\n" +
                    "Müzakirənin linki: " + discussionLink +"\n"+
                    "<a href=\""+ "tg://user?id=%d".formatted((long)id) +"\"></a>"
        );

        try {
            super.execute(msg);
        } catch (TelegramApiException e) {
            LoggerService.log("Could not make a direct broadcast" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }

    public static String joinedAdminList(List<Integer> adminList){
        StringBuilder adminListBuilder = new StringBuilder();

        for (Integer integer : adminList) {
            adminListBuilder
                    .append("<a href=\"")
                    .append("tg://user?id=")
                    .append(integer)
                    .append("\">")
                    .append("@")
                    .append(integer)
                    .append("</a> ");
        }

        return "\n" + adminListBuilder.toString().trim();
    }


}
