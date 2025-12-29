package app.state;

import app.StageManagerGUI;
import bean.BookBean;
import controller.gui.PurchaseControllerGUI;

public class ConfirmPurchaseState extends SecondaryState {

    private final BookBean book;
    private final int quantity;

    public ConfirmPurchaseState(StateManager stateManager, BookBean book, int quantity) {
        super(stateManager);
        this.book = book;
        this.quantity = quantity;
    }

    @Override
    public void onEnter() {
        PurchaseControllerGUI controller = stateManager.getStageManager()
            .<PurchaseControllerGUI>loadContent(StageManagerGUI.CONFIRM_PURCHASE_VIEW);
        if (controller != null) {
            controller.setStateManager(stateManager);
            controller.setPurchaseData(book, quantity);
        }
    }
}