module cr.ac.una.est.estructurasdedatosproyecto03 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens cr.ac.una.est.estructurasdedatosproyecto03 to javafx.fxml;
    opens cr.ac.una.est.estructurasdedatosproyecto03.model to com.google.gson;
    opens cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.signin to javafx.fxml;
    opens cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.signup to javafx.fxml;
    opens cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.timeline to javafx.fxml;
    opens cr.ac.una.est.estructurasdedatosproyecto03.ui.screen.people to javafx.fxml;

    exports cr.ac.una.est.estructurasdedatosproyecto03;
}
