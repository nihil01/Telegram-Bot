package world.horosho.tgbot.http.telegram;

import world.horosho.tgbot.database.DatabaseManager;
import world.horosho.tgbot.database.models.CalendarData;
import world.horosho.tgbot.database.models.DiscussionData;
import world.horosho.tgbot.database.models.UserData;
import world.horosho.tgbot.services.LoggerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * This service handles notifications and data broadcasting for a Telegram bot.
 *
 * Key responsibilities:
 * - Checks if notifications are allowed based on calendar data and current time
 * - Broadcasts notifications to users grouped by company
 * - Filters and formats notification messages based on deadlines
 * - Retrieves user, discussion and calendar data from database
 *
 * Main methods:
 * - isAllowedForNotifyMessage(): Checks if notifications can be sent based on calendar
 * - broadcastDataInGroupPerUser(): Iterates through users and broadcasts notifications
 * - notifyBot(): Formats notification messages for users
 * - notifyEveryDay(): Retrieves notification data for users
 *
 * Database interactions:
 * - Queries calendar data, user data, and discussion details
 * - Uses prepared statements for secure database access
 * - Handles SQL exceptions appropriately
 */

public class NotificationService {

    public CompletableFuture<Boolean> isAllowedForNotifyMessage() {
        try {
            return CompletableFuture.supplyAsync(() -> {
                List<CalendarData> data = getCalendarData(LocalDate.now().getMonthValue());
                System.out.println("Calendar data!!");
                System.out.println(data);

                long currentTime = System.currentTimeMillis();
                int currentDay = LocalDate.now().getDayOfWeek().getValue(); // 1 - Понедельник, 7 - Воскресенье

                if (data.isEmpty()){
                   return !(currentDay == 6 || currentDay == 7);
                }

                for (CalendarData item : data) {
                    if (currentDay == 6 || currentDay == 7) {
                        return !(item.getWeekendWork() != null &&
                                (currentTime - item.getWeekendWork().atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) < 86400000);
                    } else {
                        return !(item.getHolidays() != null ||
                                (currentTime - item.getHolidays().atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) < 86400000);
                    }
                }

                return false;
            });
        } catch (Exception e) {
            LoggerService.log("NOTIFICATION SERVICE ERROR: Is not allowed for sending message!" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }


    public Iterator<String> broadcastDataInGroupPerUser(String company) {
        try {
            return new Iterator<>() {
                private final List<UserData> result = notifyEveryDay(company).join();
                private final Map<Integer, String> states = Map.of(
                        -3, "3️⃣ Cavablandırılmasına 3 gün qalmış uyğunsuzluqlar:\n",
                        -2, "2️⃣ Cavablandırılmasına 2 gün qalmış uyğunsuzluqlar:\n",
                        -1, "1️⃣ Cavablandırılmasına 1 gün qalmış uyğunsuzluqlar:\n",
                        0, "⏳ <b>Sonuncu gün olan uyğunsuzluqlar: </b>\n",
                        1, "⌛️ <b>Vaxtı bitmiş cavablandırılmamış uyğunsuzluqlar</b>:\n"
                );


                private final Map<String, List<UserData>> grouped = result.stream()
                        .collect(Collectors.groupingBy(UserData::getCredentials, ConcurrentHashMap::new, Collectors.toList()));

                private final Iterator<List<UserData>> userIterator = grouped.values().iterator();
                private Iterator<Map<Integer, List<UserData>>> filterIterator;

                @Override
                public boolean hasNext() {
                    return (filterIterator != null && filterIterator.hasNext()) || userIterator.hasNext();
                }

                @Override
                public String next() {
                    if (filterIterator == null || !filterIterator.hasNext()) {
                        if (!userIterator.hasNext()) throw new NoSuchElementException();
                        filterIterator = filterPrecedingDataInArrays(userIterator.next()).iterator();
                    }

                    Map<Integer, List<UserData>> nextData = filterIterator.next();
                    for (Map.Entry<Integer, List<UserData>> entry : nextData.entrySet()) {
                        return notifyBot(states.getOrDefault(entry.getKey(), states.get(1)), entry.getValue()).join();
                    }

                    throw new NoSuchElementException();
                }
            };
        } catch (Exception e) {
            LoggerService.log("NOTIFICATION SERVICE ERROR: Broadcast per user!" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }

    private List<Map<Integer, List<UserData>>> filterPrecedingDataInArrays(List<UserData> data) {
        try {
            if (data == null || data.isEmpty()) return Collections.emptyList();

            List<Map<Integer, List<UserData>>> result = new ArrayList<>();
            int dayCounter = -3;

            while (dayCounter <= 0) {
                int finalDayCounter = dayCounter;
                List<UserData> filteredData = data.stream()
                        .filter(user -> user.getDay() == finalDayCounter)
                        .toList();

                if (!filteredData.isEmpty()) {
                    result.add(Map.of(dayCounter, filteredData));
                }

                dayCounter++;

                if (dayCounter == 0) {
                    List<UserData> expiredData = data.stream()
                            .filter(user -> user.getDay() > 0)
                            .toList();
                    if (!expiredData.isEmpty()) result.add(Map.of(1, expiredData));
                }
            }

            return result;
        } catch (Exception e) {
            LoggerService.log("NOTIFICATION SERVICE ERROR: Filter data in preceding arrays" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<String> notifyBot(String message, List<UserData> main) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                if (main == null || main.isEmpty()) return "";

                StringBuilder block = new StringBuilder();
                boolean flag = false;

                for (UserData el : main) {
                    if (!flag) {
                        block.append("ƏMƏKDAŞIN ADI : <i>").append(el.getCredentials()).append("</i>\n");
                        block.append(message);
                        flag = true;
                    }

                    DiscussionData data = getRoomData(el.getDiscussion_uid());

                    if (Objects.nonNull(data)) {
                        block.append("\n<a href=\"")
                                .append("https://uygunsuzluqlar.horosho.world/discussion/%s".formatted(el.getDiscussion_uid()))
                                .append("\">Sayta keçid ..</a>")
                                .append("\n Muəssisə: <i>").append(data.getCorporation()).append("</i>")
                                .append("\n Kategoriya: <i>").append(data.getCategory()).append("</i>\n");
                    }

                }
                return block.toString();
            });
        } catch (Exception e) {
            LoggerService.log("NOTIFICATION SERVICE ERROR: notifybot!" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }


    /**
        SQL QUERIES BEGIN HERE
    */

    private CompletableFuture<List<UserData>> notifyEveryDay(String company) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                List<UserData> data = new ArrayList<>();
                try(Connection connection = DatabaseManager.getConnection()) {
                    PreparedStatement ps = connection.prepareStatement(
                        "SELECT EXTRACT(DAY FROM (NOW() - inprogress.enddate::Date)) as day, " +
                        "inprogress.discussion_uid, users.credentials, users.number " +
                        "FROM inprogress " +
                        "JOIN users ON users.id = inprogress.user_id " +
                        "WHERE status = 'İcrada olan' AND enddate != '0' AND users.company = ? " +
                        "ORDER by day");
                    ps.setString(1, company);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        data.add(new UserData(
                            rs.getInt("day"),
                            rs.getString("discussion_uid"),
                            rs.getString("credentials"),
                            rs.getString("number")
                        ));
                    }
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                    throw new RuntimeException(e);
                }
                return data;
            });
        } catch (Exception e) {
            LoggerService.log("NOTIFICATION SERVICE ERROR: SQL QUERY" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }



    private List<CalendarData> getCalendarData(int month) {
        List<CalendarData> data = new ArrayList<>();
        try(Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM deadlinesdate WHERE startmonth = ?::text");
            ps.setInt(1, month);
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                data.add(
                    new CalendarData(
                    rs.getString("weekend_work") != null ? LocalDate.parse(rs.getString("weekend_work")) : null,
                    rs.getString("holidays") != null ? LocalDate.parse(rs.getString("holidays")) : null
                ));
            }
        } catch (SQLException e) {
            LoggerService.log("NOTIFICATION SERVICE ERROR: SQL QUERY" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }

        return data;
    }

    private DiscussionData getRoomData(String discussionUid) {
        try(Connection connection = DatabaseManager.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT corporation, category FROM inprogress WHERE discussion_uid::text = ?");
            ps.setString(1, discussionUid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new DiscussionData(rs.getString("corporation"), rs.getString("category"));
            }
        } catch (SQLException e) {
            LoggerService.log("NOTIFICATION SERVICE ERROR: SQL QUERY" + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }

        return null;
    }
}
