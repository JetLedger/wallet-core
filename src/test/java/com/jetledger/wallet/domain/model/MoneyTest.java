package com.jetledger.wallet.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValidValues() {
        Money money = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        assertEquals(new BigDecimal("100.00"), money.amount());
        assertEquals(Currency.getInstance("USD"), money.currency());
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () ->
            Money.of(null, Currency.getInstance("USD")));
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThrows(NullPointerException.class, () ->
            Money.of(new BigDecimal("100.00"), null));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
            Money.of(new BigDecimal("-50.00"), Currency.getInstance("USD")));
    }

    @Test
    void shouldScaleToTwoDecimalPlaces() {
        Money money = Money.of(new BigDecimal("100.12345"), Currency.getInstance("USD"));
        assertEquals(2, money.amount().scale());
        assertEquals(new BigDecimal("100.12"), money.amount());
    }

    @Test
    void shouldAddMoney() {
        Money a = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        Money b = Money.of(new BigDecimal("25.00"), Currency.getInstance("USD"));
        Money result = a.add(b);
        assertEquals(new BigDecimal("75.00"), result.amount());
    }

    @Test
    void shouldRejectAddWithDifferentCurrency() {
        Money a = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        Money b = Money.of(new BigDecimal("25.00"), Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }

    @Test
    void shouldSubtractMoney() {
        Money a = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        Money b = Money.of(new BigDecimal("20.00"), Currency.getInstance("USD"));
        Money result = a.subtract(b);
        assertEquals(new BigDecimal("30.00"), result.amount());
    }

    @Test
    void shouldRejectSubtractWithDifferentCurrency() {
        Money a = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        Money b = Money.of(new BigDecimal("20.00"), Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
    }

    @Test
    void shouldRejectSubtractResultingInNegative() {
        Money a = Money.of(new BigDecimal("10.00"), Currency.getInstance("USD"));
        Money b = Money.of(new BigDecimal("20.00"), Currency.getInstance("USD"));
        assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Money a = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money b = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldFormatToString() {
        Money money = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        assertTrue(money.toString().contains("100.00"));
        assertTrue(money.toString().contains("USD"));
    }
}
