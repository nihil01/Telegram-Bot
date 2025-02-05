package world.horosho.tgbot.database.controller;
import world.horosho.tgbot.database.DatabaseManager;
import world.horosho.tgbot.database.models.Company;
import world.horosho.tgbot.database.models.User;
import world.horosho.tgbot.services.LoggerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DatabaseController {

    public static User getUserByTelegramUUID(long telegramUUID) {
        try(Connection con = DatabaseManager.getConnection()){
            PreparedStatement ps = con.prepareStatement(
            "SELECT credentials, number, company FROM users JOIN tgbot ON tgbot.user_phone = users.number WHERE tgbot.id = ?"
            );
            ps.setLong(1, telegramUUID);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return new User(rs.getString("credentials"),
                        rs.getString("number"), rs.getString("company"));
            }

        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
        return null;
    }

    public static boolean linkUserToTelegramUUID(long telegramUUID, String phoneNumber) {
        try(Connection con = DatabaseManager.getConnection()){
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO tgbot (id, user_phone) VALUES(?, ?)"
            );
            ps.setLong(1, telegramUUID);
            ps.setString(2, phoneNumber);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            return false;
        }
    }

    private static String getMyCompany(long telegramUUID){
        try(Connection con = DatabaseManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT company FROM users WHERE number = (SELECT user_phone FROM tgbot WHERE tgbot.id = ?);"
            );
            ps.setLong(1, telegramUUID);

            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("company");
            }
            return null;
        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }

    public static Map<String, List<User>> getAllUsersInMyCompany(long telegramUUID) {
        Map<String, List<User>> data = new HashMap<>();
        List<User> users = new ArrayList<>();

        try(Connection con = DatabaseManager.getConnection()){
            String company = getMyCompany(telegramUUID);
            if (company != null) {
                PreparedStatement ps2 = con.prepareStatement(
                        "SELECT credentials, number, company, privilege_level, last_seen FROM users WHERE company = ? ORDER BY privilege_level ASC"
                );
                ps2.setString(1, company);
                ResultSet rs2 = ps2.executeQuery();
                while(rs2.next()){
                    users.add(new User(rs2.getString("credentials"),
                            rs2.getString("number"), rs2.getString("company"),
                            rs2.getInt("privilege_level"), rs2.getString("last_seen")));
                }
                data.put(company, users);
            }else{
                data.put("error", null);
            }

        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
        return data;
    }

    public static String getCompanyChatGroupLink(long telegramUUID) {
        String company = getMyCompany(telegramUUID);
        try(Connection con = DatabaseManager.getConnection()){
            PreparedStatement ps = con.prepareStatement(
                "SELECT telegram_group_link FROM companies WHERE name = ?"
            );
            ps.setString(1, company);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return rs.getString("telegram_group_link");
            }

            return null;
        } catch (SQLException exc) {
            LoggerService.log("Error in SQL DB: " + exc.getMessage(), Level.SEVERE);
            throw new RuntimeException(exc);
        }
    }

    public static boolean saveChatID(String chatName, Long groupID) {
        try(Connection con = DatabaseManager.getConnection()){
            PreparedStatement ps = con.prepareStatement("SELECT telegram_group_id FROM companies WHERE LOWER(name) = ?");
            ps.setString(1, chatName.toLowerCase());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getLong("telegram_group_id") != groupID) {
                    PreparedStatement ps2 = con.prepareStatement("UPDATE companies SET telegram_group_id = ? WHERE LOWER(name) = ?");
                    ps2.setLong(1,groupID);
                    ps2.setString(2, chatName.toLowerCase());
                    ps2.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
        return false;
    }

    public static long getGroupID(String chatName) {
        try(Connection con = DatabaseManager.getConnection()){
            PreparedStatement ps = con.prepareStatement("SELECT telegram_group_id FROM companies WHERE LOWER(name) = ?");
            ps.setString(1, chatName.toLowerCase());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("telegram_group_id");
            }
        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
        return -1;
    }

    public static List<Integer> getAdminsForCompanyMention(String company) {
        List<Integer> admins = new ArrayList<>();
        try(Connection con = DatabaseManager.getConnection()){
            PreparedStatement ps = con.prepareStatement("SELECT \n" +
                    "    f.id\n" +
                    "FROM \n" +
                    "    tgbot_admin_notifications t\n" +
                    "JOIN \n" +
                    "    users u \n" +
                    "    ON TRIM(LOWER(u.credentials)) = TRIM(LOWER(t.groupmessages))\n" +
                    "JOIN \n" +
                    "\ttgbot f\n" +
                    "\tON f.user_phone = u.number\n" +
                    "WHERE \n" +
                    "    t.groupmessages IS NOT NULL \n" +
                    "    AND TRIM(LOWER(t.company)) = ?;\n");

            ps.setString(1, company.toLowerCase());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                admins.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }

        return admins;
    }

    public static List<Company> getCompanies(){
        List<Company> companiesList = new ArrayList<>();
        try(Connection con = DatabaseManager.getConnection()){
            PreparedStatement ps = con.prepareStatement("SELECT name, telegram_group_id FROM companies;");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                    companiesList.add(new Company(
                            rs.getString("name"),
                            rs.getLong("telegram_group_id"
                    ))
                );
            }
            return companiesList;
        } catch (SQLException e) {
            LoggerService.log("Error in SQL DB: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException(e);
        }
    }

}
