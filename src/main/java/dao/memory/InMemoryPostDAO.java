package dao.memory;

import dao.PostDAO;
import exception.DAOException;
import model.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryPostDAO implements PostDAO {

    private static InMemoryPostDAO instance = null;
    private final List<Post> posts = new ArrayList<>();

    public InMemoryPostDAO() {
        posts.add(new Post(
                "admin@bibliotech.it",
                "Rebecca Ferrari",
                "Admin",
                "Benvenuti in BiblioTech",
                "Benvenuti nella bacheca ufficiale di BiblioTech. Qui troverete avvisi e comunicazioni importanti.",
                LocalDateTime.now().minusDays(1)
        ));

        posts.add(new Post(
                "librarian@bibliotech.it",
                "Mario Rossi",
                "Admin",
                "Nuovi libri disponibili",
                "Sono arrivati nuovi titoli nella sezione Programmazione. Date un'occhiata al catalogo!",
                LocalDateTime.now().minusHours(3)
        ));
    }

    public static InMemoryPostDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryPostDAO();
        }
        return instance;
    }

    @Override
    public List<Post> getAllPostsOrderedByDate() throws DAOException {
        return posts.stream()
                .sorted(Comparator.comparing(Post::getPostDate).reversed())
                .toList();
    }

    @Override
    public void addPost(Post post) throws DAOException {
        posts.add(post);
    }
}