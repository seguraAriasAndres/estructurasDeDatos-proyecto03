package cr.ac.una.est.estructurasdedatosproyecto03.service;

import cr.ac.una.est.estructurasdedatosproyecto03.model.StudentAccount;
import cr.ac.una.est.estructurasdedatosproyecto03.persistence.AccountJsonRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PeopleService{
    private final AccountJsonRepository accountRepository;

    // Connect student search operations to the persistent account repository.
    // Inputs: the account repository; output: a ready student search service.
    public PeopleService (AccountJsonRepository accountRepository){
        if (accountRepository == null){
            throw new IllegalArgumentException("Account repository cannot be null.");
        }

        this.accountRepository = accountRepository;
    }

    // Simple search method for finding other students.
    // Inputs: search query and active account ID; output: matching accounts sorted by name.
    public List<StudentAccount> searchStudents (String searchQuery, String activeAccountId) throws IOException{
        if (searchQuery == null || searchQuery.trim().isEmpty()){
            return new ArrayList<>();
        }

        String normalizedQuery = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<StudentAccount> matchingAccounts = new ArrayList<>();

        for (StudentAccount account : accountRepository.loadAll()){
            if (account == null || account.getId() == null){
                continue;
            }
            if (activeAccountId != null && account.getId().equals(activeAccountId.trim())){
                continue;
            }

            String normalizedName = account.getFullName() == null
                ? ""
                : account.getFullName().toLowerCase(Locale.ROOT);
            String normalizedUsername = account.getUsername() == null
                ? ""
                : account.getUsername().toLowerCase(Locale.ROOT);

            if (normalizedName.contains(normalizedQuery) || normalizedUsername.contains(normalizedQuery)){
                matchingAccounts.add(account);
            }
        }

        matchingAccounts.sort(
            Comparator.comparing(StudentAccount::getFullName, String.CASE_INSENSITIVE_ORDER)
        );

        return matchingAccounts;
    }

    // Retrieve one account so other services can display its student details.
    // Inputs: the account ID; output: the matching account or null when it does not exist.
    public StudentAccount getStudentById (String accountId) throws IOException{
        if (accountId == null || accountId.trim().isEmpty()){
            return null;
        }

        return accountRepository.findById(accountId.trim());
    }
}
