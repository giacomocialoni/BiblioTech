package controller.app;

import java.util.List;
import dao.PostDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import model.Post;
import bean.PostBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BachecaController {

    private static final Logger logger = LoggerFactory.getLogger(BachecaController.class);
    private final PostDAO postDAO;

    public BachecaController() {
        this.postDAO = DAOFactory.getInstance().getPostDAO();
    }

    public List<PostBean> getAllPostsOrderedByDate() {
        try {
            List<Post> posts = postDAO.getAllPostsOrderedByDate();
            return posts.stream()
                    .map(this::toPostBean)
                    .toList();  
        } catch (DAOException e) {
            logger.error("Errore DAO durante il recupero dei post", e);
            return List.of();
        }
    }

    // Mapping
    private PostBean toPostBean(Post post) {
        PostBean bean = new PostBean();
        bean.setAuthorName(post.getAuthorName());
        bean.setRole(post.getRole());
        bean.setTitle(post.getTitle());
        bean.setContent(post.getContent());
        bean.setPostDate(post.getPostDate());
        return bean;
    }
}