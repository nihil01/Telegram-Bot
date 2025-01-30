package world.horosho.tgbot.database;

import io.github.cdimascio.dotenv.Dotenv;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class DatabaseManager {
    private static Dotenv dotenv = Dotenv.configure().load();

    public static Connection getConnection() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setDatabaseName(dotenv.get("DATABASE_NAME"));
        dataSource.setUser(dotenv.get("DATABASE_USER"));
        dataSource.setPassword(dotenv.get("DATABASE_PASSWORD"));
        dataSource.setServerNames(new String[]{dotenv.get("DATABASE_HOST")});

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
