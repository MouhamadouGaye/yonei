package com.mgaye.yonei.dto;

public class CardDTO {

    private Long id;
    private String cardNumberLast4;
    private String brand;
    private int expMonth;
    private int expYear;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumberLast4() {
        return cardNumberLast4;
    }

    public void setCardNumberLast4(String cardNumberLast4) {
        this.cardNumberLast4 = cardNumberLast4;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getExpiryMonth() {
        return expMonth;
    }

    public void setExpiryMonth(int expMonth) {
        this.expMonth = expMonth;
    }

    public int getExpiryYear() {
        return expYear;
    }

    public void setExpiryYear(int expiryYear) {
        this.expYear = expiryYear;
    }

    // Static factory method to convert from entity to DTO
    public static CardDTO fromEntity(com.mgaye.yonei.entity.Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setCardNumberLast4(card.getLast4());
        dto.setBrand(card.getBrand());
        dto.setExpiryMonth(card.getExpMonth());
        dto.setExpiryYear(card.getExpYear());
        return dto;
    }

}
