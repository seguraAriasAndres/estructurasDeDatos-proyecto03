package cr.ac.una.est.estructurasdedatosproyecto03.service;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.model.WallEntry;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.AccountJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.WallEntryJsonRepository;
import cr.ac.una.est.estructurasdedatosproyecto03.structure.StudentConnectionGraph;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TimelineService{
    private final AccountJsonRepository accountRepository;
    private final WallEntryJsonRepository wallEntryRepository;
    private final StudentConnectionGraph connectionGraph;

    // Connect account lookup, saved posts, and the friendship graph for timeline operations.
    // Inputs: account repository, post repository, and connection graph; output: a ready timeline service.
    public TimelineService (
        AccountJsonRepository accountRepository,
        WallEntryJsonRepository wallEntryRepository,
        StudentConnectionGraph connectionGraph
    ){
        if (accountRepository == null){
            throw new IllegalArgumentException("Account repository cannot be null.");
        }
        if (wallEntryRepository == null){
            throw new IllegalArgumentException("Post repository cannot be null.");
        }
        if (connectionGraph == null){
            throw new IllegalArgumentException("Connection graph cannot be null.");
        }

        this.accountRepository = accountRepository;
        this.wallEntryRepository = wallEntryRepository;
        this.connectionGraph = connectionGraph;
    }

    // Create and save a timestamped post.
    // Inputs: author account ID and message text; output: the saved wall entry.
    public WallEntry publish (String authorAccountId, String message) throws IOException{
        String normalizedAuthorId = normalizeAccountId(authorAccountId);

        if (accountRepository.findById(normalizedAuthorId) == null){
            throw new IllegalArgumentException("The author account does not exist.");
        }
        if (message == null || message.trim().isEmpty()){
            throw new IllegalArgumentException("Post message cannot be empty.");
        }

        WallEntry wallEntry = new WallEntry(
            UUID.randomUUID().toString(),
            normalizedAuthorId,
            message,
            Instant.now()
        );

        wallEntryRepository.add(wallEntry);
        return wallEntry;
    }

    // Collect the student's own posts and their friends' posts to build the main feed.
    // Inputs: the active account ID; output: matching wall entries sorted newest first.
    public List<WallEntry> getTimeline (String accountId) throws IOException{
        String normalizedAccountId = normalizeAccountId(accountId);

        if (accountRepository.findById(normalizedAccountId) == null){
            throw new IllegalArgumentException("The account does not exist.");
        }

        Set<String> visibleAuthorIds = new HashSet<>(
            connectionGraph.getConnectedAccountIds(normalizedAccountId)
        );
        visibleAuthorIds.add(normalizedAccountId);

        List<WallEntry> timelineEntries = new ArrayList<>();

        for (WallEntry wallEntry : wallEntryRepository.loadAll()){
            if (wallEntry != null && visibleAuthorIds.contains(wallEntry.getAuthorId())){
                timelineEntries.add(wallEntry);
            }
        }

        return sortNewestFirst(timelineEntries);
    }

    // Retrieve a student's own posts so a profile or other view can show their history.
    // Inputs: the author account ID; output: that student's wall entries sorted newest first.
    public List<WallEntry> getEntriesByAuthor (String authorAccountId) throws IOException{
        String normalizedAuthorId = normalizeAccountId(authorAccountId);

        if (accountRepository.findById(normalizedAuthorId) == null){
            throw new IllegalArgumentException("The author account does not exist.");
        }

        List<WallEntry> authorEntries = new ArrayList<>();

        for (WallEntry wallEntry : wallEntryRepository.loadAll()){
            if (wallEntry != null && normalizedAuthorId.equals(wallEntry.getAuthorId())){
                authorEntries.add(wallEntry);
            }
        }

        return sortNewestFirst(authorEntries);
    }

    // Order posts consistently so the newest content appears first in every feed.
    // Inputs: a list of wall entries; output: the same list sorted newest first.
    private List<WallEntry> sortNewestFirst (List<WallEntry> wallEntries){
        wallEntries.sort(
            Comparator.comparing(WallEntry::getCreatedAt)
                .reversed()
                .thenComparing(WallEntry::getId)
        );

        return wallEntries;
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
