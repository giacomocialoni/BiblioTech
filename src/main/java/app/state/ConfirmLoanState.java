package app.state;

import app.StageManagerGUI;
import controller.gui.LoanControllerGUI;
import bean.BookBean;

public class ConfirmLoanState extends SecondaryState {

    private final BookBean book;

    public ConfirmLoanState(StateManager stateManager, BookBean book) {
        super(stateManager);
        this.book = book;
    }

    @Override
    public void onEnter() {
        LoanControllerGUI controller = stateManager.getStageManager()
            .<LoanControllerGUI>loadContent(StageManagerGUI.CONFIRM_LOAN_VIEW);
        if (controller != null) {
            controller.setStateManager(stateManager);
            controller.setBorrowData(book);
        }
    }
}