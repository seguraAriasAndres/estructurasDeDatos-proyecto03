package cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.signin;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.service.AuthenticationService;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.feedback.AlertPresenter;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.navigation.WindowRouter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class SignInController{
    private final AuthenticationService authenticationService;
    private final WindowRouter windowRouter;
    private final AlertPresenter alertPresenter;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label feedbackLabel;

    // Connect sign-in input to authentication and navigation services.
    // Inputs: authentication service, view router, and alert presenter; output: a ready sign-in controller.
    public SignInController (
        AuthenticationService authenticationService,
        WindowRouter windowRouter,
        AlertPresenter alertPresenter
    ){
        if (authenticationService == null || windowRouter == null || alertPresenter == null){
            throw new IllegalArgumentException("Sign-in dependencies cannot be null.");
        }

        this.authenticationService = authenticationService;
        this.windowRouter = windowRouter;
        this.alertPresenter = alertPresenter;
    }

    // Clear old feedback when the sign-in view is loaded.
    // Output: none; the feedback label starts empty.
    @FXML
    private void initialize (){
        feedbackLabel.setText("");
    }

    // Validate required fields and authenticate the entered credentials.
    // Inputs: username and password from the sign-in fields; output: none, successful sign-in opens the timeline.
    @FXML
    private void handleSignIn (){
        String username = usernameField.getText();
        String password = passwordField.getText();

        feedbackLabel.setText("");

        if (username == null || username.trim().isEmpty()){
            feedbackLabel.setText("Escribe tu nombre de usuario.");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.isEmpty()){
            feedbackLabel.setText("Escribe tu contraseña.");
            passwordField.requestFocus();
            return;
        }

        try{
            StudentAccount account = authenticationService.authenticate(username, password);

            if (account == null){
                feedbackLabel.setText("El usuario o la contraseña no son correctos.");
                return;
            }

            windowRouter.showTimeline();
        } catch (IOException exception){
            alertPresenter.showError(
                "No se pudieron leer las cuentas guardadas. " + exception.getMessage()
            );
        }
    }

    // Open registration so a student without an account can create one.
    // Output: none; the registration view is opened.
    @FXML
    private void openSignUp (){
        windowRouter.showSignUp();
    }

    // Switch the interface theme while keeping the sign-in screen open.
    // Output: none; the shared router changes the active screen theme.
    @FXML
    private void handleToggleTheme (){
        windowRouter.toggleTheme();
    }
}
