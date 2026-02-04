package com.example.shared.model;

/**
 * PaymentMethod enum representing supported payment methods.
 */
public enum PaymentMethod {
    PAY_ON_DELIVERY("Pay on Delivery - Cash payment upon receipt"),
    CREDIT_CARD("Credit Card - Visa, Mastercard, Amex"),
    DEBIT_CARD("Debit Card - Bank debit card"),
    PAYPAL("PayPal - PayPal account payment"),
    BANK_TRANSFER("Bank Transfer - Direct bank transfer"),
    WALLET("Digital Wallet - Mobile wallet payment");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
