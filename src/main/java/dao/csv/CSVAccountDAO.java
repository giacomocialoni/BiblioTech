package dao.csv;

import dao.AccountDAO;
import exception.DAOException;
import exception.EmailAlreadyRegisteredException;
import model.Account;
import model.Admin;
import model.User;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVAccountDAO implements AccountDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/users.csv";
    private static final String CSV_HEADER = "email,password,first_name,last_name,role";
    
    @Override
    public Account login(String email, String password) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            throw new DAOException("Credenziali non valide");
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                
                if (fields.length >= 5 && fields[0].equals(email) && fields[1].equals(password)) {
                    String role = fields[4].toLowerCase();
                    return "admin".equals(role) ? 
                           new Admin(fields[0], fields[1], fields[2], fields[3]) :
                           new User(fields[0], fields[1], fields[2], fields[3]);
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il login", e);
        }
        
        throw new DAOException("Credenziali non valide");
    }
    
    @Override
    public boolean register(String email, String password, 
                           String firstName, String lastName) 
            throws DAOException, EmailAlreadyRegisteredException {
        
        if (emailExists(email)) {
            throw new EmailAlreadyRegisteredException("Email già registrata: " + email);
        }
        
        List<String[]> users = readAllUsers();
        users.add(new String[]{email, password, firstName, lastName, "logged_user"});
        saveAllUsers(users);
        
        return true;
    }
    
    private boolean emailExists(String email) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            return false;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 1 && fields[0].equalsIgnoreCase(email)) {
                    return true;
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la verifica dell'email", e);
        }
        
        return false;
    }
    
    private List<String[]> readAllUsers() throws DAOException {
        List<String[]> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return users;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                users.add(parseCSVLine(line));
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura degli utenti", e);
        }
        
        return users;
    }
    
    private void saveAllUsers(List<String[]> users) throws DAOException {
        try {
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(FILE_PATH), 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                
                writer.write(CSV_HEADER);
                writer.newLine();
                
                for (String[] user : users) {
                    writer.write(formatCSVLine(user));
                    writer.newLine();
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il salvataggio degli utenti", e);
        }
    }
    
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
    
    private String formatCSVLine(String[] fields) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            String field = fields[i] != null ? fields[i] : "";
            
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                sb.append("\"").append(field.replace("\"", "\"\"")).append("\"");
            } else {
                sb.append(field);
            }
        }
        
        return sb.toString();
    }
}