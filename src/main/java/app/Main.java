package app;

import java.io.FileInputStream;

import exception.ApplicationStartupException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import controller.observer.WishlistEmailObserver;
import controller.observer.WishlistObservable;
import dao.database.DBConnection;
import dao.database.DatabaseBookDAO;
import dao.factory.DAOFactory;

public class Main {

    public static void main(String[] args) {

    	Session.initGuest();
    	
        String viewType;
        String dataSourceType;

        WishlistObservable wishlistObservable = new WishlistObservable();

        // --- Lettura start.properties ---
        Properties startProps = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/start.properties")) {
            startProps.load(input);
            viewType = startProps.getProperty("view.type");
            dataSourceType = startProps.getProperty("data.source");
        } catch (IOException e) {
        	throw new ApplicationStartupException("Errore durante la lettura di start.properties", e);
        }

        // --- Configurazione DAOFactory Singleton ---
        if ("FULL".equalsIgnoreCase(viewType)) {

            if ("DB".equalsIgnoreCase(dataSourceType)) {

                Properties dbProps = new Properties();
                try (InputStream dbInput = new FileInputStream("src/main/resources/db.properties")) {
                    dbProps.load(dbInput);
                } catch (IOException e) {
                	throw new ApplicationStartupException("Errore durante la lettura di db.properties", e);
                }

                String url = resolveEnv(dbProps.getProperty("db.url"));
                String user = resolveEnv(dbProps.getProperty("db.user"));
                String password = resolveEnv(dbProps.getProperty("db.password"));

                if (url == null || user == null) {
                    throw new IllegalStateException(
                            "Variabili d'ambiente DB non configurate correttamente");
                }

                DBConnection dbConnection = new DBConnection(url, user, password);

                // Inizializza il Singleton con DB
                DAOFactory.init("DB", dbConnection);

                // Sovrascrivo il BookDAO con quello custom (che usa observer)
                DatabaseBookDAO customBookDAO = new DatabaseBookDAO(dbConnection);
                DAOFactory.getInstance().setCustomBookDAO(customBookDAO);

            } else if ("CSV".equalsIgnoreCase(dataSourceType)) {
                DAOFactory.init("CSV", null);
            } else {
                throw new IllegalArgumentException("Tipo di data source non valido: " + dataSourceType);
            }

            // --- Registrazione observer ---
            wishlistObservable.addObserver(new WishlistEmailObserver());

            // --- Avvio GUI ---
            ApplicationGUI.launchApp(args);

        } else if ("DEMO".equalsIgnoreCase(viewType)) {

            DAOFactory.init("INMEMORY", null);
            ApplicationCLI.start();

        } else {
            throw new IllegalArgumentException("Tipo di view non valido: " + viewType);
        }
    }

    /**
     * Risolve un valore del tipo ${ENV_VAR} usando le variabili d'ambiente.
     */
    private static String resolveEnv(String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith("${") && value.endsWith("}")) {
            String envKey = value.substring(2, value.length() - 1);
            return System.getenv(envKey);
        }
        return value;
    }
}