package cr.ac.una.est.estructurasdedatosproyecto03;

import cr.ac.una.est.estructurasdedatosproyecto03.config.ApplicationContext;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.navigation.WindowRouter;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SocialCampusApplication extends Application{
    private ApplicationContext applicationContext;
    private WindowRouter windowRouter;

    // Initialize shared services and open the sign-in screen when JavaFX creates the main window.
    // Inputs: the primary JavaFX stage; output: none, the first application view is shown.
    @Override
    public void start (Stage primaryStage) throws IOException{
        Path dataDirectory = Paths.get(System.getProperty("user.dir"), "data");

        applicationContext = new ApplicationContext(dataDirectory);
        windowRouter = new WindowRouter(primaryStage, applicationContext);
        windowRouter.showSignIn();
    }

    // Release application references when JavaFX closes so the session is no longer retained.
    // Output: none; application references are cleared.
    @Override
    public void stop (){
        windowRouter = null;
        applicationContext = null;
    }
}
