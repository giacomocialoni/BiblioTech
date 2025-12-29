package app.state;

import app.StageManagerGUI;
import controller.gui.ReservationControllerGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationState extends PrimaryState {

    private static final Logger logger = LoggerFactory.getLogger(ReservationState.class);

    public ReservationState(StateManager stateManager) {
        super(stateManager);
    }

    @Override
    protected void loadContent() {
        ReservationControllerGUI controllerReservation =
                stateManager.getStageManager().<ReservationControllerGUI>loadContent(StageManagerGUI.RESERVATION_VIEW);

        if (controllerReservation != null) {
            try {
                controllerReservation.setStateManager(stateManager);
            } catch (Exception e) {
                logger.error("Errore durante l'inizializzazione di ReservationControllerGUI", e);
            }
        } else {
            logger.warn("ReservationControllerGUI è null - impossibile caricare la vista");
        }
    }
}