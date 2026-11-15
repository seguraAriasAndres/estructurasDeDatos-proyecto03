package cr.ac.una.est.estructurasdedatosproyecto03;

import javafx.application.Application;

public class SocialCampusLauncher{
    // Start the JavaFX runtime through the application class so Maven has a stable main entry point.
    // Inputs: command-line arguments; output: none, JavaFX launches the application.
    public static void main (String[] args){
        Application.launch(SocialCampusApplication.class, args);
    }
}
