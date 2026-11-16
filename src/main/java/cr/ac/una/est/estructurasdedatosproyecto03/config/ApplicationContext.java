package cr.ac.una.est.estructurasdedatosproyecto03.config;

import cr.ac.una.est.estructurasdedatosproyecto03.persistence.AccountJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.ConnectionJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.JsonDocumentStore;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.WallEntryJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.security.CredentialHasher;
import cr.ac.una.est.estructurasdedatosproyecto03.service.AuthenticationService;
import cr.ac.una.est.estructurasdedatosproyecto03.service.ConnectionsService;
import cr.ac.una.est.estructurasdedatosproyecto03.service.PeopleService;
import cr.ac.una.est.estructurasdedatosproyecto03.service.TimelineService;
import cr.ac.una.est.estructurasdedatosproyecto03.session.AccessSession;
import cr.ac.una.est.estructurasdedatosproyecto03.structure.StudentConnectionGraph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class ApplicationContext{
    private final AccountJsonRepository accountRepository;
    private final ConnectionJsonRepository connectionRepository;
    private final WallEntryJsonRepository wallEntryRepository;
    private final StudentConnectionGraph connectionGraph;
    private final AccessSession accessSession;
    private final AuthenticationService authenticationService;
    private final PeopleService peopleService;
    private final ConnectionsService connectionsService;
    private final TimelineService timelineService;

    // Build shared repositories and services so each screen uses the same application state.
    // Inputs: the writable data folder; output: a fully initialized application context.
    public ApplicationContext (Path dataDirectory) throws IOException{
        Objects.requireNonNull(dataDirectory, "Data directory cannot be null.");

        Path normalizedDataDirectory = dataDirectory.toAbsolutePath().normalize();
        JsonDocumentStore documentStore = new JsonDocumentStore();

        this.accountRepository = new AccountJsonRepository(
            normalizedDataDirectory.resolve("accounts.json"),
            documentStore
        );
        this.connectionRepository = new ConnectionJsonRepository(
            normalizedDataDirectory.resolve("connections.json"),
            documentStore
        );
        this.wallEntryRepository = new WallEntryJsonRepository(
            normalizedDataDirectory.resolve("entries.json"),
            documentStore
        );

        this.connectionGraph = new StudentConnectionGraph();
        this.connectionGraph.loadConnections(connectionRepository.loadAll());

        this.accessSession = new AccessSession();
        CredentialHasher credentialHasher = new CredentialHasher();

        this.authenticationService = new AuthenticationService(
            accountRepository,
            credentialHasher,
            accessSession
        );
        this.peopleService = new PeopleService(accountRepository);
        this.connectionsService = new ConnectionsService(
            accountRepository,
            connectionRepository,
            connectionGraph
        );
        this.timelineService = new TimelineService(
            accountRepository,
            wallEntryRepository,
            connectionGraph
        );
    }

    // Return the shared authentication service so screens use the configured sign-in logic.
    // Output: the shared authentication service.
    public AuthenticationService getAuthenticationService (){
        return authenticationService;
    }

    // Return the shared people service so screens use the configured account catalog.
    // Output: the shared people service.
    public PeopleService getPeopleService (){
        return peopleService;
    }

    // Return the shared connections service so screens use the configured friendship graph.
    // Output: the shared connections service.
    public ConnectionsService getConnectionsService (){
        return connectionsService;
    }

    // Return the shared timeline service so screens use the configured post history.
    // Output: the shared timeline service.
    public TimelineService getTimelineService (){
        return timelineService;
    }

    // Return the shared session so screens can check the active account.
    // Output: the shared access session.
    public AccessSession getAccessSession (){
        return accessSession;
    }
}
