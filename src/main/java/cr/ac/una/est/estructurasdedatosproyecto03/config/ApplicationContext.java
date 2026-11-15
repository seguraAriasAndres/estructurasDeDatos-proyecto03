package cr.ac.una.est.estructurasdedatosproyecto03.config;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentConnection;
import cr.ac.una.est.estructurasdedatosproyecto03.model.WallEntry;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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

        seedSampleDataIfNeeded();

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

    // Return the shared authentication service.
    public AuthenticationService getAuthenticationService (){
        return authenticationService;
    }

    // Return the shared people service.
    public PeopleService getPeopleService (){
        return peopleService;
    }

    // Return the shared connections service.
    public ConnectionsService getConnectionsService (){
        return connectionsService;
    }

    // Return the shared timeline service.
    public TimelineService getTimelineService (){
        return timelineService;
    }

    // Return the shared session.
    public AccessSession getAccessSession (){
        return accessSession;
    }

    // Seed demo records only on a clean first launch so required sample data is available without overwriting user data.
    // Output: none; the three JSON collections are populated only when all are empty.
    private void seedSampleDataIfNeeded () throws IOException{
        List<StudentAccount> savedAccounts = accountRepository.loadAll();
        List<StudentConnection> savedConnections = connectionRepository.loadAll();
        List<WallEntry> savedEntries = wallEntryRepository.loadAll();

        if (!savedAccounts.isEmpty() || !savedConnections.isEmpty() || !savedEntries.isEmpty()){
            return;
        }

        CredentialHasher credentialHasher = new CredentialHasher();
        String samplePasswordHash = credentialHasher.hashPassword("socialuna2026");

        //DELETE LATER AFTER MANUAL CREATION IS DONE!!!
        StudentAccount anaAccount = new StudentAccount(
            "student-demo-001",
            "Ana Ruiz",
            "ana.ruiz",
            samplePasswordHash
        );
        StudentAccount luisAccount = new StudentAccount(
            "student-demo-002",
            "Luis Vargas",
            "luis.vargas",
            samplePasswordHash
        );
        StudentAccount sofiaAccount = new StudentAccount(
            "student-demo-003",
            "Sofía Mora",
            "sofia.mora",
            samplePasswordHash
        );

        Instant currentTime = Instant.now();

        List<StudentConnection> sampleConnections = Arrays.asList(
            new StudentConnection(
                anaAccount.getId(),
                luisAccount.getId(),
                currentTime.minusSeconds(172800)
            ),
            new StudentConnection(
                luisAccount.getId(),
                sofiaAccount.getId(),
                currentTime.minusSeconds(86400)
            )
        );

        List<WallEntry> sampleEntries = Arrays.asList(
            new WallEntry(
                "entry-demo-001",
                anaAccount.getId(),
                "¡Hola! Ya estoy usando Social UNA.",
                currentTime.minusSeconds(10800)
            ),
            new WallEntry(
                "entry-demo-002",
                luisAccount.getId(),
                "¿Quién más está preparando los proyectos de esta semana?",
                currentTime.minusSeconds(7200)
            ),
            new WallEntry(
                "entry-demo-003",
                sofiaAccount.getId(),
                "Un saludo a toda la comunidad estudiantil.",
                currentTime.minusSeconds(3600)
            )
        );

        accountRepository.saveAll(Arrays.asList(anaAccount, luisAccount, sofiaAccount));
        connectionRepository.saveAll(sampleConnections);
        wallEntryRepository.saveAll(sampleEntries);
    }
}
