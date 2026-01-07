package dao.csv;

import dao.PostDAO;
import model.Post;
import exception.DAOException;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CSVPostDAO implements PostDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/posts.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_HEADER = "user_fk,title,content,post_date";
    
    @Override
    public List<Post> getAllPostsOrderedByDate() throws DAOException {
        List<Post> posts = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return posts;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
        	String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV post non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                try {
                    List<String> fields = parseCSVLine(line);
                    
                    if (fields.size() >= 4) {
                        String userEmail = fields.get(0);
                        String title = fields.get(1);
                        String content = fields.get(2);
                        
                        String dateStr = fields.get(3).replace("\"", "").trim();
                        LocalDateTime postDate = LocalDateTime.parse(dateStr, DATE_FORMATTER);
                        
                        String authorName = getAuthorNameFromEmail(userEmail);
                        String role = userEmail.contains("admin") ? "admin" : "user";
                        
                        posts.add(new Post(userEmail, authorName, role, title, content, postDate));
                    }
                } catch (DateTimeParseException e) {
                    // Skip invalid date lines
                }
            }
            
            posts.sort((p1, p2) -> p2.getPostDate().compareTo(p1.getPostDate()));
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura dei post da CSV", e);
        }
        
        return posts;
    }
    
    @Override
    public void addPost(Post post) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        boolean fileExists = Files.exists(path);
        
        try (BufferedWriter writer = Files.newBufferedWriter(path, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND)) {
            
            if (!fileExists || Files.size(path) == 0) {
                writer.write("user_fk,title,content,post_date");
                writer.newLine();
            }
            
            String line = String.join(",",
                post.getUserEmail(),
                "\"" + escapeQuotes(post.getTitle()) + "\"",
                "\"" + escapeQuotes(post.getContent()) + "\"",
                "\"" + post.getPostDate().format(DATE_FORMATTER) + "\""
            );
            
            writer.write(line);
            writer.newLine();
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il salvataggio del post in CSV", e);
        }
    }
    
    private String getAuthorNameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Autore sconosciuto";
        }
        
        String username = email.split("@")[0];
        
        if (!username.isEmpty()) {
            return Character.toUpperCase(username.charAt(0)) + 
                   (username.length() > 1 ? username.substring(1) : "");
        }
        
        return "Autore sconosciuto";
    }
    
    private List<String> parseCSVLine(String line) {
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
        return fields;
    }
    
    private String escapeQuotes(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\"", "\"\"");
    }
}