package world.horosho.tgbot.services;
import io.github.bucket4j.Bucket;
import world.horosho.tgbot.database.controller.DatabaseController;
import world.horosho.tgbot.database.models.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;

public class UserService {
    private static Bucket bucket = RateLimitInitializer.getLimitBucket(1,1,Duration.ofHours(1));

    public static HashMap<ResultStatus, User> getUser(Long userID) {
        User user = DatabaseController.getUserByTelegramUUID(userID);
        HashMap<ResultStatus, User> users = new HashMap<>();

        if (user != null) {
            users.put(ResultStatus.SUCCESS, user);
        } else {
            System.out.println("User not found in database !");
            users.put(ResultStatus.FAILURE, null);
        }

        return users;
    }

    public static String recoverPassword(User user, String desiredPassword) {
        if (bucket.tryConsume(1)){
            try (HttpClient client = HttpClient.newHttpClient()) {
                String json = "{\"issuer\":\"WHATSAPP_BOT\",\"recipient\":\"%s\",\"password\":\"%s\"}".formatted(user.getNumber().substring(1), desiredPassword);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://uygunsuzluqlar.horosho.world/api/passwordRecovery"))
                        .header("Content-Type", "application/json")
                        .header("X-From", "WHATSAPP_BOT")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body().replace("{", "").replace("}", "").split(":")[1];
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return "❌ Zəhmət olmasa, bir saatdan sonra yenədən cəhd edərsiniz ❌";
    }
}
