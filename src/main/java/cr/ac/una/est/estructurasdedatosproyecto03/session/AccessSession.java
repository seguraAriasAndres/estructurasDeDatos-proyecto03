package cr.ac.una.est.estructurasdedatosproyecto03.session;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;

import java.util.Objects;

public class AccessSession{
    private StudentAccount currentAccount;

    // Store the account that has successfully signed in so the app can share session state.
    // Inputs: the signed-in student account; output: none, the account becomes the active session.
    public void startSession (StudentAccount account){
        this.currentAccount = Objects.requireNonNull(account, "Account cannot be null.");
    }

    // Clear the signed-in account so later screens cannot use a stale session.
    // Output: none; the active session is cleared.
    public void endSession (){
        this.currentAccount = null;
    }

    // Return the current account so services and screens can use the signed-in student's identity.
    // Output: the active student account, or null when no account is signed in.
    public StudentAccount getCurrentAccount (){
        return currentAccount;
    }

    // Report whether an account is active so the app can guard signed-in actions.
    // Output: true when an account is signed in, otherwise false.
    public boolean isSignedIn (){
        return currentAccount != null;
    }
}
