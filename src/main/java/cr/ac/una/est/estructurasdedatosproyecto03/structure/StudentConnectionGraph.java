package cr.ac.una.est.estructurasdedatosproyecto03.structure;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentConnection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class StudentConnectionGraph{
    private final Map<String, Set<String>> adjacentAccountIds;

    // Initialize an empty adjacency list so the application can build its network in memory.
    // Ooutput: an empty connection graph.
    public StudentConnectionGraph (){
        this.adjacentAccountIds = new LinkedHashMap<>();
    }

    // Rebuild the in-memory network from saved links so services can query connections quickly.
    // Inputs: persisted connections; output: none, the graph is replaced with those valid links.
    public void loadConnections (Collection<StudentConnection> connections){
        adjacentAccountIds.clear();

        if (connections == null){
            return;
        }

        for (StudentConnection connection : connections){
            addConnection(connection);
        }
    }

    // Add a mutual link only once so both accounts see the friendship in their adjacency lists.
    // Inputs: a connection record; output: true if added, otherwise false for null, invalid, or duplicate links.
    public boolean addConnection (StudentConnection connection){
        if (connection == null){
            return false;
        }

        String firstAccountId = connection.getFirstAccountId();
        String secondAccountId = connection.getSecondAccountId();

        if (firstAccountId == null || firstAccountId.trim().isEmpty()){
            return false;
        }
        if (secondAccountId == null || secondAccountId.trim().isEmpty()){
            return false;
        }
        if (firstAccountId.equals(secondAccountId)){
            return false;
        }
        if (areConnected(firstAccountId, secondAccountId)){
            return false;
        }

        adjacentAccountIds
            .computeIfAbsent(firstAccountId, accountId -> new LinkedHashSet<>())
            .add(secondAccountId);
        adjacentAccountIds
            .computeIfAbsent(secondAccountId, accountId -> new LinkedHashSet<>())
            .add(firstAccountId);

        return true;
    }

    // Remove both directions of a friendship so the graph remains mutual after an unlink.
    // Inputs: two account IDs; output: true if either side of the link existed and was removed.
    public boolean removeConnection (String firstAccountId, String secondAccountId){
        if (firstAccountId == null || secondAccountId == null){
            return false;
        }

        Set<String> firstConnections = adjacentAccountIds.get(firstAccountId);
        Set<String> secondConnections = adjacentAccountIds.get(secondAccountId);

        boolean removedFromFirst = firstConnections != null && firstConnections.remove(secondAccountId);
        boolean removedFromSecond = secondConnections != null && secondConnections.remove(firstAccountId);

        return removedFromFirst || removedFromSecond;
    }

    // Check one adjacency list so services can quickly determine if two accounts are friends.
    // Inputs: two account IDs; output: true only if the first account lists the second as connected.
    public boolean areConnected (String firstAccountId, String secondAccountId){
        if (firstAccountId == null || secondAccountId == null){
            return false;
        }
        if (firstAccountId.equals(secondAccountId)){
            return false;
        }

        Set<String> connections = adjacentAccountIds.get(firstAccountId);
        return connections != null && connections.contains(secondAccountId);
    }

    // Return a read-only copy so callers cannot mutate the graph outside its connection operations.
    // Inputs: an account ID; output: an unmodifiable set of connected account IDs, or an empty set.
    public Set<String> getConnectedAccountIds (String accountId){
        if (accountId == null){
            return Collections.emptySet();
        }

        Set<String> connections = adjacentAccountIds.get(accountId);

        if (connections == null){
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(new LinkedHashSet<>(connections));
    }
}
