package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.dto.TransferResponse;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    private final Lock senderLock = new ReentrantLock();
    private final Lock receiverLock = new ReentrantLock();

    @Autowired
    public AccountsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public TransferResponse transfer(TransferRequest transferRequest) {

        Account sender = transferRequest.getFromAccount();
        Account receiver = transferRequest.getToAccount();
        BigDecimal amount = transferRequest.getAmount();

        transferRequest.setTransactionId(UUID.randomUUID().toString());
        transferRequest.setTransferTime(LocalDateTime.now());

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setTransactionId(transferRequest.getTransactionId());
        transferResponse.setTransferTime(transferRequest.getTransferTime());
        transferResponse.setFromAccountId(transferRequest.getFromAccount().getAccountId());
        transferResponse.setToAccountId(transferRequest.getToAccount().getAccountId());
        transferResponse.setCurrency(transferRequest.getCurrency());
        transferResponse.setAmount(transferRequest.getAmount());

        Lock firstLock =  getLockObject(sender, receiver);
        Lock secondLock = getLockObject(receiver, sender);

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                accountsRepository.transferFunds(sender, receiver, amount);
                transferResponse.setStatus("SUCCESS");
                transferResponse.setMessage("Transfer Successful");

                emailNotificationService.notifyAboutTransfer(sender,
                        "You have transferred" + transferRequest.getCurrency() + " " +
                                amount + "to account" + receiver);


                emailNotificationService.notifyAboutTransfer(receiver,
                        "You have received " + transferRequest.getCurrency() + " " +
                                amount + " from account " + sender);

            } catch (AccountNotFoundException | InsufficientFundException e) {
                transferResponse.setStatus("FAILURE");
                transferResponse.setMessage("Transfer Declined");
                emailNotificationService.notifyAboutTransfer(sender,
                        "Transaction Declined :" + transferRequest.getCurrency() + " " +
                                amount + "to account" + receiver);

                emailNotificationService.notifyAboutTransfer(receiver,
                        "Transaction Declined : " + transferRequest.getCurrency() + " " +
                                amount + " from account " + sender);
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }

        return transferResponse;
    }

    private Lock getLockObject(Account account1, Account account2) {
        return (account1.getAccountId().compareTo(account2.getAccountId()) < 0) ? senderLock : receiverLock;
    }

}
