package cr.ac.una.est.estructurasdedatosproyecto03.service;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentConnection;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.AccountJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.ConnectionJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.structure.StudentConnectionGraph;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ConnectionsService{
    private final AccountJsonRepository accountRepository;
    private final ConnectionJsonRepository connectionRepository;
    private final StudentConnectionGraph connectionGraph;

    // Connect account lookup, saved links, and the in-memory graph for mutual connection operations.
    // Inputs: account repository, connection repository, and connection graph; output: a ready connections service.
    public ConnectionsService (AccountJsonRepository accountRepository,
        ConnectionJsonRepository connectionRepository, StudentConnectionGraph connectionGraph){
        if (accountRepository == null){
            throw new IllegalArgumentException("Account repository cannot be null.");
        }
        if (connectionRepository == null){
            throw new IllegalArgumentException("Connection repository cannot be null.");
        }
        if (connectionGraph == null){
            throw new IllegalArgumentException("Connection graph cannot be null.");
        }

        this.accountRepository = accountRepository;
        this.connectionRepository = connectionRepository;
        this.connectionGraph = connectionGraph;
    }

    // Save a mutual link after validating both students so the connection is usable immediately.
    // Inputs: the two account IDs to connect; output: the persisted connection record.
    public StudentConnection addConnection (String firstAccountId, String secondAccountId) throws IOException{
        String normalizedFirstId = normalizeAccountId(firstAccountId);
        String normalizedSecondId = normalizeAccountId(secondAccountId);

        if (normalizedFirstId.equals(normalizedSecondId)){
            throw new IllegalArgumentException("An account cannot connect to itself.");
        }
        if (accountRepository.findById(normalizedFirstId) == null){
            throw new IllegalArgumentException("The first account does not exist.");
        }
        if (accountRepository.findById(normalizedSecondId) == null){
            throw new IllegalArgumentException("The second account does not exist.");
        }
        if (connectionGraph.areConnected(normalizedFirstId, normalizedSecondId)){
            throw new IllegalArgumentException("These accounts are already connected.");
        }

        StudentConnection connection = new StudentConnection(
            normalizedFirstId,
            normalizedSecondId,
            Instant.now()
        );

        if (!connectionGraph.addConnection(connection)){
            throw new IllegalStateException("The connection could not be added to the in-memory graph.");
        }

        try{
            connectionRepository.add(connection);
        } catch (IOException exception){
            connectionGraph.removeConnection(normalizedFirstId, normalizedSecondId);
            throw exception;
        }

        return connection;
    }

    // Remove a mutual link from memory and saved data so both students are disconnected.
    // Inputs: the two account IDs; output: true when a saved connection was removed.
    public boolean removeConnection (String firstAccountId, String secondAccountId) throws IOException{
        String normalizedFirstId = normalizeAccountId(firstAccountId);
        String normalizedSecondId = normalizeAccountId(secondAccountId);

        if (normalizedFirstId.equals(normalizedSecondId)){
            throw new IllegalArgumentException("No puedes eliminar una conexión contigo mismo.");
        }
        if (accountRepository.findById(normalizedFirstId) == null
            || accountRepository.findById(normalizedSecondId) == null){
            throw new IllegalArgumentException("No se encontraron ambas cuentas.");
        }
        if (!connectionGraph.areConnected(normalizedFirstId, normalizedSecondId)){
            return false;
        }

        List<StudentConnection> savedConnections = connectionRepository.loadAll();
        List<StudentConnection> remainingConnections = new ArrayList<>();
        StudentConnection removedConnection = null;

        for (StudentConnection connection : savedConnections){
            if (connection == null){
                continue;
            }

            boolean matchesConnection = isSameConnection(
                connection,
                normalizedFirstId,
                normalizedSecondId
            );

            if (matchesConnection && removedConnection == null){
                removedConnection = connection;
            } else{
                remainingConnections.add(connection);
            }
        }

        if (removedConnection == null){
            throw new IllegalStateException(
                "La conexión existe en memoria, pero no aparece en los datos guardados."
            );
        }

        if (!connectionGraph.removeConnection(normalizedFirstId, normalizedSecondId)){
            return false;
        }

        try{
            connectionRepository.saveAll(remainingConnections);
        } catch (IOException exception){
            connectionGraph.addConnection(removedConnection);
            throw exception;
        }

        return true;
    }

    // Resolve graph neighbors to account details so the interface can display a proper connection list.
    // Inputs: one account ID; output: existing connected accounts sorted by name.
    public List<StudentAccount> listConnections (String accountId) throws IOException{
        String normalizedAccountId = normalizeAccountId(accountId);

        if (accountRepository.findById(normalizedAccountId) == null){
            throw new IllegalArgumentException("The account does not exist.");
        }

        Set<String> connectedAccountIds = connectionGraph.getConnectedAccountIds(normalizedAccountId);
        List<StudentAccount> connectedAccounts = new ArrayList<>();

        for (String connectedAccountId : connectedAccountIds){
            StudentAccount connectedAccount = accountRepository.findById(connectedAccountId);

            if (connectedAccount != null){
                connectedAccounts.add(connectedAccount);
            }
        }

        connectedAccounts.sort(
            Comparator.comparing(StudentAccount::getFullName, String.CASE_INSENSITIVE_ORDER)
        );

        return connectedAccounts;
    }

    // Check the graph so screens and services can determine if two students are connected.
    // Inputs: two account IDs; output: true when a mutual connection exists, otherwise false.
    public boolean areConnected (String firstAccountId, String secondAccountId){
        if (firstAccountId == null || secondAccountId == null){
            return false;
        }

        return connectionGraph.areConnected(firstAccountId.trim(), secondAccountId.trim());
    }

    // Compare a saved connection without depending on which account ID was stored first.
    // Inputs: connection record and two account IDs; output: true when both IDs match the link.
    private boolean isSameConnection (
        StudentConnection connection,
        String firstAccountId,
        String secondAccountId
    ){
        String savedFirstId = connection.getFirstAccountId();
        String savedSecondId = connection.getSecondAccountId();

        if (savedFirstId == null || savedSecondId == null){
            return false;
        }

        String normalizedSavedFirstId = savedFirstId.trim();
        String normalizedSavedSecondId = savedSecondId.trim();

        return normalizedSavedFirstId.equals(firstAccountId)
            && normalizedSavedSecondId.equals(secondAccountId)
            || normalizedSavedFirstId.equals(secondAccountId)
            && normalizedSavedSecondId.equals(firstAccountId);
    }

    // Reject missing account IDs and trim valid values before repository and graph operations.
    // Inputs: an account ID; output: the trimmed ID or an exception when it is blank.
    private String normalizeAccountId (String accountId){
        if (accountId == null || accountId.trim().isEmpty()){
            throw new IllegalArgumentException("Account ID cannot be empty.");
        }

        return accountId.trim();
    }
}
