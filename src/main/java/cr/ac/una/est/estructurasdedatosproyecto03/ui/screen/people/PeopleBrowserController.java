package cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.people;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.service.ConnectionsService;
import cr.ac.una.est.estructurasdedatosproyecto03.service.PeopleService;
import cr.ac.una.est.estructurasdedatosproyecto03.session.AccessSession;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.feedback.AlertPresenter;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.navigation.WindowRouter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class PeopleBrowserController{
    private final PeopleService peopleService;
    private final ConnectionsService connectionsService;
    private final AccessSession accessSession;
    private final WindowRouter windowRouter;
    private final AlertPresenter alertPresenter;

    @FXML
    private Label activeNameLabel;

    @FXML
    private TextField searchField;

    @FXML
    private VBox searchResultsContainer;

    @FXML
    private VBox connectionsContainer;

    // Connect people search and connection actions to the shared app services and session.
    // Inputs: people service, connections service, access session, router, and alert presenter; output: a ready people controller.
    public PeopleBrowserController (
        PeopleService peopleService,
        ConnectionsService connectionsService,
        AccessSession accessSession,
        WindowRouter windowRouter,
        AlertPresenter alertPresenter
    ){
        if (peopleService == null || connectionsService == null || accessSession == null
            || windowRouter == null || alertPresenter == null){
            throw new IllegalArgumentException("People screen dependencies cannot be null.");
        }

        this.peopleService = peopleService;
        this.connectionsService = connectionsService;
        this.accessSession = accessSession;
        this.windowRouter = windowRouter;
        this.alertPresenter = alertPresenter;
    }

    // Show the current student's name and load existing connections when the screen opens.
    // Output: none; the people screen is initialized from the active session.
    @FXML
    private void initialize (){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        activeNameLabel.setText("Hola, " + activeAccount.getFullName());
        searchResultsContainer.getChildren().add(
            createMessageLabel("Busca por nombre o usuario para encontrar estudiantes.")
        );
        refreshConnections();
    }

    // Search by name or username, or show all other accounts when the query is blank.
    // Inputs: text entered in the search field; output: matching accounts appear in the results panel.
    @FXML
    private void handleSearch (){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        String searchQuery = searchField.getText();

        try{
            List<StudentAccount> matchingAccounts = peopleService.searchStudents(
                searchQuery,
                activeAccount.getId()
            );

            renderAccounts(
                matchingAccounts,
                searchResultsContainer,
                activeAccount.getId(),
                true
            );

            if (matchingAccounts.isEmpty()){
                String emptyMessage = searchQuery == null || searchQuery.trim().isEmpty()
                    ? "Todavía no hay otras personas registradas."
                    : "No encontramos estudiantes con esa búsqueda.";
                searchResultsContainer.getChildren().add(createMessageLabel(emptyMessage));
            }
        } catch (IOException exception){
            alertPresenter.showError(
                "No se pudieron buscar estudiantes. " + exception.getMessage()
            );
        }
    }

    // Add the selected account as a connection and refresh both visible account lists.
    // Inputs: selected student account; output: none, the result button and connection list are refreshed.
    private void handleAddConnection (StudentAccount targetAccount){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        if (targetAccount == null){
            alertPresenter.showError("Selecciona un estudiante válido.");
            return;
        }

        try{
            connectionsService.addConnection(activeAccount.getId(), targetAccount.getId());
            refreshConnections();
            handleSearch();
        } catch (IllegalArgumentException exception){
            alertPresenter.showInformation(exception.getMessage());
        } catch (IOException exception){
            alertPresenter.showError(
                "No se pudo guardar la conexión. " + exception.getMessage()
            );
        }
    }

    // Remove the selected account as a connection and refresh both visible account lists.
    // Inputs: selected student account; output: none, the result button and connection list are refreshed.
    private void handleRemoveConnection (StudentAccount targetAccount){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        if (targetAccount == null){
            alertPresenter.showError("Selecciona un estudiante válido.");
            return;
        }

        try{
            boolean removed = connectionsService.removeConnection(
                activeAccount.getId(),
                targetAccount.getId()
            );

            if (!removed){
                alertPresenter.showInformation("Esta persona ya no está en tus conexiones.");
            }

            refreshConnections();
            handleSearch();
        } catch (IllegalArgumentException exception){
            alertPresenter.showInformation(exception.getMessage());
        } catch (IOException | IllegalStateException exception){
            alertPresenter.showError(
                "No se pudo eliminar la conexión. " + exception.getMessage()
            );
        }
    }

    // Open the timeline so the signed-in student can return to the feed.
    // Output: none; the timeline view is opened.
    @FXML
    private void openTimeline (){
        windowRouter.showTimeline();
    }

    // Clear the active session so the next screen cannot use the signed-in account.
    // Output: none; the sign-in view is opened.
    @FXML
    private void handleSignOut (){
        accessSession.endSession();
        windowRouter.showSignIn();
    }

    // Switch the interface theme while keeping the people screen open.
    // Output: none; the shared router changes the active screen theme.
    @FXML
    private void handleToggleTheme (){
        windowRouter.toggleTheme();
    }

    // Reload and render the signed-in student's connections from the graph and account repository.
    // Output: none; the connections panel reflects the current saved links.
    private void refreshConnections (){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        try{
            List<StudentAccount> connectedAccounts = connectionsService.listConnections(
                activeAccount.getId()
            );
            renderAccounts(connectedAccounts, connectionsContainer, activeAccount.getId(), false);

            if (connectedAccounts.isEmpty()){
                connectionsContainer.getChildren().add(
                    createMessageLabel("Todavía no tienes conexiones.")
                );
            }
        } catch (IOException | IllegalArgumentException exception){
            alertPresenter.showError(
                "No se pudo cargar la lista de conexiones. " + exception.getMessage()
            );
        }
    }

    // Create account rows and optionally add connection actions for each result.
    // Inputs: accounts, destination container, active account ID, and action visibility; output: the container is refreshed.
    private void renderAccounts (
        List<StudentAccount> accounts,
        VBox destination,
        String activeAccountId,
        boolean showConnectionActions
    ){
        destination.getChildren().clear();

        for (StudentAccount account : accounts){
            if (account != null){
                destination.getChildren().add(
                    createAccountRow(account, activeAccountId, showConnectionActions)
                );
            }
        }
    }

    // Build one account card with an action that reflects its current connection status.
    // Inputs: account, active account ID, and whether to show a connection action; output: the completed account row.
    private HBox createAccountRow (
        StudentAccount account,
        String activeAccountId,
        boolean showConnectionAction
    ){
        VBox accountDetails = new VBox(4.0);
        Label fullNameLabel = new Label(account.getFullName());
        Label usernameLabel = new Label("@" + account.getUsername());

        fullNameLabel.getStyleClass().add("section-title");
        usernameLabel.getStyleClass().add("muted-text");
        accountDetails.getChildren().addAll(fullNameLabel, usernameLabel);
        HBox.setHgrow(accountDetails, Priority.ALWAYS);

        HBox accountRow = new HBox(12.0);
        accountRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        accountRow.setPadding(new Insets(12.0));
        accountRow.getStyleClass().add("post-card");
        accountRow.getChildren().add(accountDetails);

        if (showConnectionAction){
            boolean isConnected = connectionsService.areConnected(
                activeAccountId,
                account.getId()
            );
            Button connectionButton = new Button(
                isConnected ? "Eliminar conexión" : "Agregar"
            );
            connectionButton.getStyleClass().add(
                isConnected ? "button-secondary" : "button-primary"
            );
            connectionButton.setOnAction(event ->{
                if (isConnected){
                    handleRemoveConnection(account);
                } else{
                    handleAddConnection(account);
                }
            });
            accountRow.getChildren().add(connectionButton);
        }

        return accountRow;
    }

    // Create a muted message for empty or instructional list states.
    // Inputs: Spanish message text; output: a label styled for secondary information.
    private Label createMessageLabel (String message){
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("muted-text");
        return messageLabel;
    }
}
