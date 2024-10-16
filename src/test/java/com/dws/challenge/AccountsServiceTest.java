package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.dto.TransferResponse;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsRepository accountsRepository;

  @Autowired
  private EmailNotificationService emailNotificationService;

  @Autowired
  private AccountsService accountsService;

  private static final String SUCCESS = "Success";
  private static final String FAILURE = "Failure";



  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void testTransferSuccess() {

   Account sender = new Account("Id-124", new BigDecimal(2000));
   Account receiver = new Account("Id-125", new BigDecimal(1000));

    TransferRequest transferRequest = new TransferRequest();
    transferRequest.setFromAccount(sender);
    transferRequest.setToAccount(receiver);
    transferRequest.setAmount(new BigDecimal("100.00"));
    transferRequest.setCurrency("INR");


    TransferResponse response = accountsService.transfer(transferRequest);

    // Assert
    assertNotNull(response.getTransactionId());
    assertNotNull(response.getTransferTime());
    assertEquals("SUCCESS", response.getStatus());
    assertEquals("Transfer Successful", response.getMessage());
    assertEquals(transferRequest.getFromAccount().getAccountId(), response.getFromAccountId());
    assertEquals(transferRequest.getToAccount().getAccountId(), response.getToAccountId());
    assertEquals(transferRequest.getCurrency(), response.getCurrency());
    assertEquals(transferRequest.getAmount(), response.getAmount());


  }

  @Test
  void testTransferFailure() {
    Account sender = new Account("Id-124", new BigDecimal(2000));
    Account receiver = new Account("Id-125", new BigDecimal(1000));

    TransferRequest transferRequest = new TransferRequest();
    transferRequest.setFromAccount(sender);
    transferRequest.setToAccount(receiver);
    transferRequest.setAmount(new BigDecimal("3000.00"));
    transferRequest.setCurrency("INR");


    TransferResponse response = accountsService.transfer(transferRequest);

    // Assert
    assertNotNull(response.getTransactionId());
    assertNotNull(response.getTransferTime());
    assertEquals("FAILURE", response.getStatus());
    assertEquals("Transfer Declined", response.getMessage());
    assertEquals(transferRequest.getFromAccount().getAccountId(), response.getFromAccountId());
    assertEquals(transferRequest.getToAccount().getAccountId(), response.getToAccountId());
    assertEquals(transferRequest.getCurrency(), response.getCurrency());
    assertEquals(transferRequest.getAmount(), response.getAmount());

  }


}
