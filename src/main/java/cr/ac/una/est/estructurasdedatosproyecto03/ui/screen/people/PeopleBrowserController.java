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

            renderAccounts(matchingAccounts, searchResultsContainer, true);

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

    // Add the selected account as a mutual connection and refresh the displayed list.
    // Inputs: selected student account; output: none, the connection is saved and shown in the list.
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
            alertPresenter.showInformation(
                "Ahora tienes una conexión con " + targetAccount.getFullName() + "."
            );
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
            renderAccounts(connectedAccounts, connectionsContainer, false);

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

    // Create compact account rows and add a connection button only for search results.
    // Inputs: accounts to show, destination container, and whether rows allow adding; output: none, the container is replaced with account rows.
    private void renderAccounts (
        List<StudentAccount> accounts,
        VBox destination,
        boolean allowConnection
    ){
        destination.getChildren().clear();

        for (StudentAccount account : accounts){
            if (account != null){
                destination.getChildren().add(createAccountRow(account, allowConnection));
            }
        }
    }

    // Build one styled account card so names, usernames, and connection actions scan clearly.
    // Inputs: account to display and whether to show its add button; output: the completed account row.
    private HBox createAccountRow (StudentAccount account, boolean allowConnection){
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

        if (allowConnection){
            Button addButton = new Button("Agregar");
            addButton.getStyleClass().add("button-primary");
            addButton.setOnAction(event -> handleAddConnection(account));
            accountRow.getChildren().add(addButton);
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
