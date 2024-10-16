package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundException;
import com.dws.challenge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dws.challenge.domain.AppConstants.ERROR_ACCOUNT_NOT_FOUND;
import static com.dws.challenge.domain.AppConstants.ERROR_INSUFFICIENT_BALANCE;
import static com.dws.challenge.domain.AppConstants.SUCCESS;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {


    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public String transferFunds(Account sender, Account receiver, BigDecimal amount) throws
            AccountNotFoundException, InsufficientFundException {


        // Check if sender and receiver exist
        if (sender == null || receiver == null) {
            throw new AccountNotFoundException(ERROR_ACCOUNT_NOT_FOUND);
        }


        // Check if sender has enough balance
        if ((sender.getBalance()).compareTo(amount) < 0) {
            throw new InsufficientFundException(ERROR_INSUFFICIENT_BALANCE);
        }

        // Deduct from sender and add to receiver
        sender.setBalance((sender.getBalance()).subtract(amount));
        receiver.setBalance((receiver.getBalance()).add(amount));

        // Update accounts in the HashMap
        accounts.put(sender.getAccountId(), sender);
        accounts.put(receiver.getAccountId(), receiver);

        return SUCCESS;
    }

}
