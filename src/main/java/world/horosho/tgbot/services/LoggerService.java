package world.horosho.tgbot.services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
public class LoggerService {
    private final static Logger logger = Logger.getLogger("LoggerService");
    private static FileHandler fh;

    public static void log(String message, Level level) {

        try {
            // Create the full directory path first to avoid NoSuchFileException

            Path logPath = Path.of(System.getProperty("user.dir"), "logs/log.log");
            Files.createDirectories(logPath.getParent());
            if (!Files.exists(logPath)) Files.createFile(logPath);

            fh = new FileHandler("logs/log.log");
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.log(level, message);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
