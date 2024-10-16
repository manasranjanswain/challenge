package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.dto.TransferResponse;
import com.dws.challenge.exception.DuplicateAccountIdException;

import java.math.BigDecimal;


public interface AccountsRepository {

    void createAccount(Account account) throws DuplicateAccountIdException;

    Account getAccount(String accountId);

    void clearAccounts();

    String  transferFunds (Account sender, Account receiver, BigDecimal amount);
}


