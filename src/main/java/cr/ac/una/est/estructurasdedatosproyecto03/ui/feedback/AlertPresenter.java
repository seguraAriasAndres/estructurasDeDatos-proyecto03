package cr.ac.una.est.estructurasdedatosproyecto03.ui.feedback;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertPresenter{
    // Show an error dialog so the user receives a clear explanation of a failed action.
    // Inputs: the message to display; output: none, the modal error dialog is shown.
    public void showError (String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("No se pudo completar la acción");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show an information dialog so the user receives feedback after a successful action.
    // Inputs: the message to display; output: none, the modal information dialog is shown.
    public void showInformation (String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Ask for confirmation before a controller performs an action that should be reversible.
    // Inputs: dialog title and question; output: true when the user chooses the confirmation button.
    public boolean showConfirmation (String title, String question){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(question);

        Optional<ButtonType> selectedButton = alert.showAndWait();
        return selectedButton.isPresent() && selectedButton.get() == ButtonType.OK;
    }
}
