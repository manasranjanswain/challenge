package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.dto.TransferResponse;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.dws.challenge.domain.AppConstants.SUCCESS;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {


    private final AccountsService accountsService;

    private TransferResponse transferResponse;


    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest transferRequest) {

        log.info("Transferring amount {} from  account {} to account {}", transferRequest.getAmount(),
                transferRequest.getFromAccount().getAccountId(), transferRequest.getToAccount().getAccountId());

        transferResponse = accountsService.transfer(transferRequest);

        if (transferResponse.getStatus().equals(SUCCESS)) {
            return new ResponseEntity<>(transferResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(transferResponse, HttpStatus.BAD_REQUEST);
        }


    }
}
