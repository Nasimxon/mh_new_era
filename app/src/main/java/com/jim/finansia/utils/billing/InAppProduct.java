package com.jim.finansia.utils.billing;

class InAppProduct {
    public String productId;
    public String storeName;
    public String storeDescription;
    public String price;
    public boolean isSubscription;
    public int priceAmountMicros;
    public String currencyIsoCode;
    public String getSku() {
        return productId;
    }
    String getType() {
        return isSubscription ? "subs" : "inapp";
    }
}
