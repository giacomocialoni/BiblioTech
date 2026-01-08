package controller.app;

import bean.AccountBean;
import bean.PostBean;
import dao.PostDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import model.Account;
import model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private final PostDAO postDAO;

    public PostController() {
        this.postDAO = DAOFactory.getInstance().getPostDAO();
    }

    // ===== READ =====
    public List<PostBean> getAllPostsOrderedByDate() {
        try {
            List<Post> posts = postDAO.getAllPostsOrderedByDate();
            return posts.stream()
                    .map(this::postToBean)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore DAO durante il recupero dei post", e);
            return List.of();
        }
    }

    // ===== CREATE =====
    public boolean createPost(AccountBean user, String title, String content) {
        try {
            Post post = new Post(
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getRole(),
                    title,
                    content,
                    LocalDateTime.now()
            );
            postDAO.addPost(post);
            return true;
        } catch (DAOException e) {
            logger.error("Errore DAO durante la creazione del post", e);
            return false;
        }
    }

    // ===== MAPPING =====
    public static AccountBean accountToBean(Account account) {
        try {
            AccountBean bean = new AccountBean();
            bean.setEmail(account.getEmail());
            bean.setFirstName(account.getFirstName());
            bean.setLastName(account.getLastName());
            bean.setRole(account.getRole());
            return bean;
        } catch (IncorrectDataException e) {
            LoggerFactory.getLogger(PostController.class)
                    .warn("Dati account non validi: {}", account.getEmail(), e);
            return null;
        }
    }

    private PostBean postToBean(Post post) {
        PostBean bean = new PostBean();
        bean.setAuthorName(post.getAuthorName());
        bean.setRole(post.getRole());
        bean.setTitle(post.getTitle());
        bean.setContent(post.getContent());
        bean.setPostDate(post.getPostDate());
        return bean;
    }
}