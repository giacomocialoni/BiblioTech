package controller.gui;

import java.util.List;
import controller.app.BachecaController;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import bean.PostBean;
import view.components.PostCardFactory;

public class BachecaControllerGUI {

    @FXML private VBox postsContainer;
    private final BachecaController controllerApp = new BachecaController();

    public void setStateManager() {
        loadPosts();
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();

        List<PostBean> posts = controllerApp.getAllPostsOrderedByDate();
        PostCardFactory cardFactory = new PostCardFactory();

        for (PostBean post : posts) {
            VBox postBox = cardFactory.createPostCard(post);
            postsContainer.getChildren().add(postBox);
        }
    }
}