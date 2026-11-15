package cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.signup;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.service.AuthenticationService;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.feedback.AlertPresenter;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.navigation.WindowRouter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class SignUpController{
    private final AuthenticationService authenticationService;
    private final WindowRouter windowRouter;
    private final AlertPresenter alertPresenter;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmationField;

    @FXML
    private Label feedbackLabel;

    // Connect registration input to account creation and screen navigation.
    // Inputs: authentication service, view router, and alert presenter; output: a ready registration controller.
    public SignUpController (
        AuthenticationService authenticationService,
        WindowRouter windowRouter,
        AlertPresenter alertPresenter
    ){
        if (authenticationService == null || windowRouter == null || alertPresenter == null){
            throw new IllegalArgumentException("Registration dependencies cannot be null.");
        }

        this.authenticationService = authenticationService;
        this.windowRouter = windowRouter;
        this.alertPresenter = alertPresenter;
    }

    // Clear old feedback when the registration view is loaded.
    // Output: none; the feedback label starts empty.
    @FXML
    private void initialize (){
        feedbackLabel.setText("");
    }

    // Validate and save a new student account so the student can sign in afterward.
    // Inputs: full name, username, password, and confirmation from the form; output: none, success returns to sign-in.
    @FXML
    private void handleRegister (){
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmation = confirmationField.getText();

        if (password == null || !password.equals(confirmation)){
            feedbackLabel.setText("Las contraseñas no coinciden.");
            return;
        }

        try{
            StudentAccount createdAccount = authenticationService.register(
                fullName,
                username,
                password
            );

            alertPresenter.showInformation(
                "La cuenta de @" + createdAccount.getUsername()
                    + " se creó correctamente. Ya puedes iniciar sesión."
            );
            windowRouter.showSignIn();
        } catch (IllegalArgumentException exception){
            feedbackLabel.setText(exception.getMessage());
        } catch (IOException exception){
            alertPresenter.showError(
                "No se pudo guardar la cuenta. " + exception.getMessage()
            );
        }
    }

    // Return to sign-in when a student already has an account.
    // Output: none; the sign-in view is opened.
    @FXML
    private void returnToSignIn (){
        windowRouter.showSignIn();
    }
}
