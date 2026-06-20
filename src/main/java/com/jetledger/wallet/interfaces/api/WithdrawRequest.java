package com.jetledger.wallet.interfaces.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record WithdrawRequest(@JsonProperty(required = true) BigDecimal amount) {}
