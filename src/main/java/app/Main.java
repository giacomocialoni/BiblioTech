package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import controller.observer.WishlistEmailObserver;
import controller.observer.WishlistObservable;
import dao.BookDAO;
import dao.database.DBConnection;
import dao.database.DatabaseBookDAO;
import dao.factory.DAOFactory;
import dao.factory.DatabaseDAOFactory;
import dao.factory.InMemoryDAOFactory;

public class Main {

    public static void main(String[] args) {

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
            throw new RuntimeException("Errore durante la lettura di start.properties", e);
        }

        // --- Configurazione DAOFactory ---
        DAOFactory factory;
        
        // --- Avvio interfaccia ---
        if ("FULL".equalsIgnoreCase(viewType)) {

	        if ("DB".equalsIgnoreCase(dataSourceType)) {
	
	            Properties dbProps = new Properties();
	            try (InputStream dbInput = new FileInputStream("src/main/resources/db.properties")) {
	                dbProps.load(dbInput);
	            } catch (IOException e) {
	                throw new RuntimeException("Errore durante la lettura di db.properties", e);
	            }
	
	            String url = resolveEnv(dbProps.getProperty("db.url"));
	            String user = resolveEnv(dbProps.getProperty("db.user"));
	            String password = resolveEnv(dbProps.getProperty("db.password"));
	
	            if (url == null || user == null) {
	                throw new IllegalStateException(
	                        "Variabili d'ambiente DB non configurate correttamente");
	            }
	
	            DBConnection dbConnection = new DBConnection(url, user, password);
	
	            DatabaseBookDAO bookDAO = new DatabaseBookDAO(dbConnection);
	            
	            factory = new CustomDatabaseDAOFactory(dbConnection, bookDAO);
	
	        } else if ("CSV".equalsIgnoreCase(dataSourceType)) {
	            factory = DAOFactory.getFactory("CSV", null);
	        } else {
	            throw new IllegalArgumentException("Tipo di data source non valido: " + dataSourceType);
	        }
	
	        // --- Impostazione factory globale ---
	        DAOFactory.setActiveFactory(factory);
	
	        // --- Registrazione observer ---
	        wishlistObservable.addObserver(new WishlistEmailObserver());
            ApplicationGUI.setDaoFactory(factory);
            ApplicationGUI.launchApp(args);
        } else if ("DEMO".equalsIgnoreCase(viewType)) {
            factory = new InMemoryDAOFactory();
            DAOFactory.setActiveFactory(factory);

            ApplicationCLI.start(factory);
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

    // --- Factory custom per iniettare BookDAO con observable ---
    static class CustomDatabaseDAOFactory extends DatabaseDAOFactory {

        private final BookDAO customBookDAO;

        public CustomDatabaseDAOFactory(DBConnection dbConnection, BookDAO customBookDAO) {
            super(dbConnection);
            this.customBookDAO = customBookDAO;
        }

        @Override
        public BookDAO getBookDAO() {
            return customBookDAO;
        }
    }
}