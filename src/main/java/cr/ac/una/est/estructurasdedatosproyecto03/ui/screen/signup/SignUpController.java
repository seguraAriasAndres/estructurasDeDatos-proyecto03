package cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.signup;

import cr.ac.una.est.estructurasdedatosproyecto03.catalog.CareerCatalog;
import cr.ac.una.est.estructurasdedatosproyecto03.catalog.CareerCatalog.Option;
import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.service.AuthenticationService;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.feedback.AlertPresenter;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.navigation.WindowRouter;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;

public class SignUpController{
    private final AuthenticationService authenticationService;
    private final WindowRouter windowRouter;
    private final AlertPresenter alertPresenter;
    private boolean updatingOptions;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmationField;

    @FXML
    private ComboBox<Option> careerField;

    @FXML
    private ComboBox<Option> campusField;

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

    // Prepare the initial career and campus choices and connect their selection listeners.
    // Output: none; both menus start with all catalog options.
    @FXML
    private void initialize (){
        feedbackLabel.setText("");
        careerField.getItems().setAll(CareerCatalog.getCareers());
        campusField.getItems().setAll(CareerCatalog.getCampuses());

        careerField.valueProperty().addListener((observable, oldCareer, newCareer) ->{
            if (!updatingOptions){
                feedbackLabel.setText("");
                updateCampusOptions(newCareer);
            }
        });

        campusField.valueProperty().addListener((observable, oldCampus, newCampus) ->{
            if (!updatingOptions){
                feedbackLabel.setText("");
                updateCareerOptions(newCampus);
            }
        });
    }

    // Filter campuses when a career is selected and clear any campus that no longer matches.
    // Inputs: selected career, or null when no career is selected; output: campus choices are refreshed.
    private void updateCampusOptions (Option selectedCareer){
        updatingOptions = true;

        try{
            List<Option> availableCampuses = selectedCareer == null
                ? CareerCatalog.getCampuses()
                : CareerCatalog.getCampusesForCareer(selectedCareer.getId());
            Option currentCampus = campusField.getValue();
            boolean keepCurrentCampus = containsOption(availableCampuses, currentCampus);

            campusField.getItems().setAll(availableCampuses);
            campusField.setValue(keepCurrentCampus ? currentCampus : null);
        } finally{
            updatingOptions = false;
        }
    }

    // Filter careers when a campus is selected and clear any career that no longer matches.
    // Inputs: selected campus, or null when no campus is selected; output: career choices are refreshed.
    private void updateCareerOptions (Option selectedCampus){
        updatingOptions = true;

        try{
            List<Option> availableCareers = selectedCampus == null
                ? CareerCatalog.getCareers()
                : CareerCatalog.getCareersForCampus(selectedCampus.getId());
            Option currentCareer = careerField.getValue();
            boolean keepCurrentCareer = containsOption(availableCareers, currentCareer);

            careerField.getItems().setAll(availableCareers);
            careerField.setValue(keepCurrentCareer ? currentCareer : null);
        } finally{
            updatingOptions = false;
        }
    }

    // Check whether the current selection remains in the newly filtered options.
    // Inputs: available options and a selected option; output: true when the selected ID is present.
    private boolean containsOption (List<Option> options, Option selectedOption){
        if (selectedOption == null){
            return false;
        }

        for (Option option : options){
            if (option.getId().equals(selectedOption.getId())){
                return true;
            }
        }

        return false;
    }

    // Validate and save a new student account so the student can sign in afterward.
    // Inputs: form values and selected career and campus; output: none, success returns to sign-in.
    @FXML
    private void handleRegister (){
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmation = confirmationField.getText();
        Option selectedCareer = careerField.getValue();
        Option selectedCampus = campusField.getValue();

        if (selectedCareer == null){
            feedbackLabel.setText("Selecciona una carrera.");
            careerField.requestFocus();
            return;
        }

        if (selectedCampus == null){
            feedbackLabel.setText("Selecciona un campus.");
            campusField.requestFocus();
            return;
        }

        if (password == null || !password.equals(confirmation)){
            feedbackLabel.setText("Las contraseñas no coinciden.");
            confirmationField.requestFocus();
            return;
        }

        try{
            StudentAccount createdAccount = authenticationService.register(
                fullName,
                username,
                password,
                selectedCareer.getId(),
                selectedCampus.getId()
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

    // Switch the interface theme while keeping the sign-up screen open.
    // Output: none; the shared router changes the active screen theme.
    @FXML
    private void handleToggleTheme (){
        windowRouter.toggleTheme();
    }
}
