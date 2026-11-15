package cr.ac.una.est.estructurasdedatosproyecto03.ui.navigation;

import cr.ac.una.est.estructurasdedatosproyecto03.SocialCampusApplication;
import cr.ac.una.est.estructurasdedatosproyecto03.config.ApplicationContext;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.feedback.AlertPresenter;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.people.PeopleBrowserController;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.signin.SignInController;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.signup.SignUpController;
import cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.timeline.TimelineController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.ReflectiveOperationException;
import java.net.URL;

public class WindowRouter{
    private static final String resourceRoot =
        "/cr/ac/una/est/estructurasdedatosproyecto03/ui/";
    private final Stage primaryStage;
    private final ApplicationContext applicationContext;
    private final AlertPresenter alertPresenter;

    // Keep the primary window and shared services available for navigation and controller creation.
    // Inputs: primary JavaFX stage and application context; output: a ready view router.
    public WindowRouter (Stage primaryStage, ApplicationContext applicationContext){
        if (primaryStage == null){
            throw new IllegalArgumentException("Primary stage cannot be null.");
        }
        if (applicationContext == null){
            throw new IllegalArgumentException("Application context cannot be null.");
        }

        this.primaryStage = primaryStage;
        this.applicationContext = applicationContext;
        this.alertPresenter = new AlertPresenter();
    }

    // Open the sign-in view so the user can authenticate before using the application.
    // Output: the sign-in view is shown when its FXML resource is available.
    public void showSignIn (){
        loadView(resourceRoot + "signin/sign-in-view.fxml", "Social UNA | Iniciar sesión");
    }

    // Open the registration view so a new student can create an account.
    // Output: the registration view is shown when its FXML resource is available.
    public void showSignUp (){
        loadView(resourceRoot + "signup/sign-up-view.fxml", "Social UNA | Crear cuenta");
    }

    // Open the timeline view so the signed-in student can read and publish posts.
    // Output: the timeline view is shown when its FXML resource is available.
    public void showTimeline (){
        loadView(resourceRoot + "timeline/timeline-view.fxml", "Social UNA | Inicio");
    }

    // Open the people view so the signed-in student can search for and connect with others.
    // Output: the people view is shown when its FXML resource is available.
    public void showPeople (){
        loadView(resourceRoot + "people/people-view.fxml", "Social UNA | Personas");
    }

    // Load one FXML screen and apply the shared stylesheet to its scene.
    // Inputs: FXML resource path and Spanish window title; output: the stage changes view or displays an error.
    private void loadView (String fxmlPath, String windowTitle){
        URL viewResource = SocialCampusApplication.class.getResource(fxmlPath);

        if (viewResource == null){
            alertPresenter.showError("No se encontró la vista de la aplicación: " + fxmlPath);
            return;
        }

        try{
            FXMLLoader loader = new FXMLLoader(viewResource);
            loader.setControllerFactory(this::createController);

            Parent root = loader.load();
            Scene scene = new Scene(root);

            URL styleResource = SocialCampusApplication.class.getResource(
                "/cr/ac/una/est/estructurasdedatosproyecto03/social-una.css"
            );

            if (styleResource != null){
                scene.getStylesheets().add(styleResource.toExternalForm());
            }

            primaryStage.setTitle(windowTitle);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException | RuntimeException exception){
            alertPresenter.showError("No se pudo abrir la vista. " + exception.getMessage());
        }
    }

    // Create each screen controller with the services it needs for its user actions.
    // Inputs: requested controller type; output: an initialized controller or an exception for an unsupported type.
    private Object createController (Class<?> controllerType){
        if (controllerType == SignInController.class){
            return instantiateController(
                controllerType,
                new Class<?>[]{
                    cr.ac.una.est.estructurasdedatosproyecto03.service.AuthenticationService.class,
                    WindowRouter.class,
                    AlertPresenter.class
                },
                new Object[]{
                    applicationContext.getAuthenticationService(),
                    this,
                    alertPresenter
                }
            );
        }
        if (controllerType == SignUpController.class){
            return instantiateController(
                controllerType,
                new Class<?>[]{
                    cr.ac.una.est.estructurasdedatosproyecto03.service.AuthenticationService.class,
                    WindowRouter.class,
                    AlertPresenter.class
                },
                new Object[]{
                    applicationContext.getAuthenticationService(),
                    this,
                    alertPresenter
                }
            );
        }
        if (controllerType == PeopleBrowserController.class){
            return instantiateController(
                controllerType,
                new Class<?>[]{
                    cr.ac.una.est.estructurasdedatosproyecto03.service.PeopleService.class,
                    cr.ac.una.est.estructurasdedatosproyecto03.service.ConnectionsService.class,
                    cr.ac.una.est.estructurasdedatosproyecto03.session.AccessSession.class,
                    WindowRouter.class,
                    AlertPresenter.class
                },
                new Object[]{
                    applicationContext.getPeopleService(),
                    applicationContext.getConnectionsService(),
                    applicationContext.getAccessSession(),
                    this,
                    alertPresenter
                }
            );
        }
        if (controllerType == TimelineController.class){
            return instantiateController(
                controllerType,
                new Class<?>[]{
                    cr.ac.una.est.estructurasdedatosproyecto03.service.TimelineService.class,
                    cr.ac.una.est.estructurasdedatosproyecto03.service.PeopleService.class,
                    cr.ac.una.est.estructurasdedatosproyecto03.session.AccessSession.class,
                    WindowRouter.class,
                    AlertPresenter.class
                },
                new Object[]{
                    applicationContext.getTimelineService(),
                    applicationContext.getPeopleService(),
                    applicationContext.getAccessSession(),
                    this,
                    alertPresenter
                }
            );
        }

        throw new IllegalArgumentException(
            "No existe una fábrica de controladores para: " + controllerType.getName()
        );
    }

    // Invoke the matching public constructor so controllers receive shared service instances.
    // Inputs: controller type, constructor parameter types, and constructor values; output: the constructed controller.
    private Object instantiateController (
        Class<?> controllerType,
        Class<?>[] parameterTypes,
        Object[] arguments
    ){
        try{
            Constructor<?> constructor = controllerType.getConstructor(parameterTypes);
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException exception){
            throw new IllegalStateException(
                "No se pudo crear el controlador: " + controllerType.getSimpleName(),
                exception
            );
        }
    }
}
