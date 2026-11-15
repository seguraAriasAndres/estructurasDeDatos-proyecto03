package cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.timeline;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.model.WallEntry;
import cr.ac.una.est.estructurasdedatosproyecto03.service.PeopleService;
import cr.ac.una.est.estructurasdedatosproyecto03.service.TimelineService;
import cr.ac.una.est.estructurasdedatosproyecto03.session.AccessSession;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.feedback.AlertPresenter;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.navigation.WindowRouter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TimelineController{
    private final TimelineService timelineService;
    private final PeopleService peopleService;
    private final AccessSession accessSession;
    private final WindowRouter windowRouter;
    private final AlertPresenter alertPresenter;
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter
        .ofPattern("d MMM yyyy · HH:mm", new Locale("es", "CR"))
        .withZone(ZoneId.systemDefault());

    @FXML
    private Label activeNameLabel;

    @FXML
    private TextArea messageArea;

    @FXML
    private VBox timelineContainer;

    // Connect timeline actions to post, account, session, navigation, and alert services.
    // Inputs: timeline service, people service, access session, router, and alert presenter; output: a ready timeline controller.
    public TimelineController (
        TimelineService timelineService,
        PeopleService peopleService,
        AccessSession accessSession,
        WindowRouter windowRouter,
        AlertPresenter alertPresenter
    ){
        if (timelineService == null || peopleService == null || accessSession == null
            || windowRouter == null || alertPresenter == null){
            throw new IllegalArgumentException("Timeline screen dependencies cannot be null.");
        }

        this.timelineService = timelineService;
        this.peopleService = peopleService;
        this.accessSession = accessSession;
        this.windowRouter = windowRouter;
        this.alertPresenter = alertPresenter;
    }

    // Set the greeting and load the signed-in student's feed when the view opens.
    // Output: none; the timeline screen is initialized from the current session.
    @FXML
    private void initialize (){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        activeNameLabel.setText("Hola, " + activeAccount.getFullName());
        refreshTimeline();
    }

    // Validate and publish the written message so it appears in the student's feed.
    // Inputs: message text from the composer; output: none, a successful post is saved and shown.
    @FXML
    private void handlePublish (){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        try{
            timelineService.publish(activeAccount.getId(), messageArea.getText());
            messageArea.clear();
            refreshTimeline();
        } catch (IllegalArgumentException exception){
            alertPresenter.showInformation(exception.getMessage());
        } catch (IOException exception){
            alertPresenter.showError(
                "No se pudo guardar la publicación. " + exception.getMessage()
            );
        }
    }

    // Open people search.
    @FXML
    private void openPeople (){
        windowRouter.showPeople();
    }

    // Clear the session.
    @FXML
    private void handleSignOut (){
        accessSession.endSession();
        windowRouter.showSignIn();
    }

    // Reload the feed so it reflects saved posts from the student and their connections.
    // Output: none; the timeline container is refreshed with newest-first entries.
    private void refreshTimeline (){
        StudentAccount activeAccount = accessSession.getCurrentAccount();

        if (activeAccount == null){
            windowRouter.showSignIn();
            return;
        }

        try{
            List<WallEntry> entries = timelineService.getTimeline(activeAccount.getId());
            timelineContainer.getChildren().clear();

            if (entries.isEmpty()){
                Label emptyMessage = new Label(
                    "Todavía no hay publicaciones. ¡Comparte la primera!"
                );
                emptyMessage.setWrapText(true);
                emptyMessage.getStyleClass().add("muted-text");
                timelineContainer.getChildren().add(emptyMessage);
                return;
            }

            for (WallEntry entry : entries){
                timelineContainer.getChildren().add(createEntryCard(entry));
            }
        } catch (IOException | IllegalArgumentException exception){
            alertPresenter.showError(
                "No se pudo cargar el inicio. " + exception.getMessage()
            );
        }
    }

    // Build a compact post card with its author's name, timestamp, and message.
    // Inputs: a wall entry; output: a styled card for the timeline.
    private VBox createEntryCard (WallEntry entry) throws IOException{
        StudentAccount author = peopleService.getStudentById(entry.getAuthorId());
        String authorName = author == null ? "Estudiante" : author.getFullName();
        String username = author == null ? "" : "@" + author.getUsername();

        Label authorLabel = new Label(authorName);
        authorLabel.getStyleClass().add("section-title");

        Label metadataLabel = new Label(
            username + "  ·  " + timestampFormatter.format(entry.getCreatedAt())
        );
        metadataLabel.getStyleClass().add("post-meta");

        Label messageLabel = new Label(entry.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("post-message");

        VBox authorDetails = new VBox(3.0, authorLabel, metadataLabel);
        HBox authorRow = new HBox(authorDetails);
        authorRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12.0, authorRow, messageLabel);
        card.setPadding(new Insets(16.0));
        card.getStyleClass().add("post-card");
        VBox.setVgrow(card, Priority.NEVER);

        return card;
    }
}
