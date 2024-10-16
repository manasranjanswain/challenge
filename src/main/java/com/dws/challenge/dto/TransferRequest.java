package com.dws.challenge.dto;


import com.dws.challenge.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TransferRequest {
    private String transactionId;
    private LocalDateTime transferTime;
    private Account fromAccount;
    private Account toAccount;
    private String currency;
    private BigDecimal amount;

}
