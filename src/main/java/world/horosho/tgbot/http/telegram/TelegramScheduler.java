package world.horosho.tgbot.http.telegram;

import world.horosho.tgbot.database.controller.DatabaseController;
import world.horosho.tgbot.database.models.Company;
import world.horosho.tgbot.manager.BotMngrProperties;
import world.horosho.tgbot.services.LoggerService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import static world.horosho.tgbot.http.telegram.DataBroadcaster.joinedAdminList;

public class TelegramScheduler {
    private static final Timer timer = new Timer();

    public static void scheduleTask(int hour, int minute, int seconds) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LoggerService.log("Task began executing ...", Level.INFO);
                broadcastDataInGroup();
            }
        };

        long delay = getDelayUntilNextRun(hour, minute, seconds);

        timer.scheduleAtFixedRate(task, delay, 1000 * 3600 * 24);
    }

    private static long getDelayUntilNextRun(int hour, int minute, int seconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(seconds);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1); // Если время уже прошло, назначаем на завтра
        }

        long delay = nextRun.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();

        return Math.max(delay, 1000);
    }

    private static void broadcastDataInGroup(){
        try {
            //Firstly, check if broadcast is allowed
            NotificationService notificationService = new NotificationService();

            if (notificationService.isAllowedForNotifyMessage().join()){

                //Secondly, we need to get available groups & companies
                Iterator<Company> companies = DatabaseController.getCompanies().iterator();
                while (companies.hasNext()){
                    Company company = companies.next();
                    //admins, to be mentioned in message according to company
                    String mentions = joinedAdminList(DatabaseController.getAdminsForCompanyMention(company.getName()));

                    //broadcast data for each company's group
                    notificationService.broadcastDataInGroupPerUser(company.getName()).forEachRemaining(inputData -> {
                        new DataBroadcaster(new BotMngrProperties()).groupBroadcast(company.getTelegram_group_id(), inputData, mentions);
                    });
                }

            }else{
                System.out.println("Broadcast is not allowed at this time");
            }


        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
