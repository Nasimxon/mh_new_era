package com.jim.finansia.utils.billing;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.fragments.ChangeColorOfStyleFragment;
import com.jim.finansia.fragments.SmsParseMainFragment;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

public class PurchaseImplementation {

    private IabHelper mHelper;
    private String base64rsa = PocketAccounterGeneral.BASE64RSA;
    //accesses

    public static final int RC_REQUEST = 10001;
    private PAFragmentManager paFragmentManager;
    private IInAppBillingService inAppBillingService;
    private Context context;

    public static final int REQUEST_CODE_BUY = 1234;

    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;
    public static final int PURCHASE_STATUS_PURCHASED = 0;
    public static final int PURCHASE_STATUS_CANCELLED = 1;
    public static final int PURCHASE_STATUS_REFUNDED = 2;
    @Inject SharedPreferences preferences;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            inAppBillingService = IInAppBillingService.Stub.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            inAppBillingService = null;
        }
    };

    public PurchaseImplementation(Context context, PAFragmentManager paFragmentManager) {
        this.context = context;
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        this.paFragmentManager = paFragmentManager;
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        if (serviceConnection != null) {
            context.unbindService(serviceConnection);
        }
    }

    public void purchaseProduct(InAppProduct product) throws Exception {
        String sku = product.getSku();
        String type = product.getType();
        // сюда вы можете добавить произвольные данные
        // потом вы сможете получить их вместе с покупкой
        String developerPayload = "12345";
        Bundle buyIntentBundle = inAppBillingService.getBuyIntent(
                3, context.getPackageName(),
                sku, type, developerPayload);
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        ((PocketAccounter)context).startIntentSenderForResult(pendingIntent.getIntentSender(),
                REQUEST_CODE_BUY, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                Integer.valueOf(0), null);
    }

    public List<InAppProduct> getInAppPurchases(String type, String... productIds) throws Exception {
        ArrayList<String> skuList = new ArrayList<>(Arrays.asList(productIds));
        Bundle query = new Bundle();
        query.putStringArrayList("ITEM_ID_LIST", skuList);
        Bundle skuDetails = inAppBillingService.getSkuDetails(
                3, context.getPackageName(), type, query);
        ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
        List<InAppProduct> result = new ArrayList<>();
        for (String responseItem : responseList) {
            JSONObject jsonObject = new JSONObject(responseItem);
            InAppProduct product = new InAppProduct();
            // "com.example.myapp_testing_inapp1"
            product.productId = jsonObject.getString("productId");
            // Покупка
            product.storeName = jsonObject.getString("title");
            // Детали покупки
            product.storeDescription = jsonObject.getString("description");
            // "0.99USD"
            product.price = jsonObject.getString("price");
            // "true/false"
            product.isSubscription = jsonObject.getString("type").equals("subs");
            // "990000" = цена x 1000000
            product.priceAmountMicros =
                    Integer.parseInt(jsonObject.getString("price_amount_micros"));
            // USD
            product.currencyIsoCode = jsonObject.getString("price_currency_code");
            result.add(product);
        }
        return result;
    }

    public void readPurchase(String purchaseData) {
        try {
            JSONObject jsonObject = new JSONObject(purchaseData);
            // ид покупки, для тестовой покупки будет null
            String orderId = jsonObject.optString("orderId");
            // "com.example.myapp"
            String packageName = jsonObject.getString("packageName");
            // "com.example.myapp_testing_inapp1"
            String productId = jsonObject.getString("productId");
            // unix-timestamp времени покупки
            long purchaseTime = jsonObject.getLong("purchaseTime");
            // PURCHASE_STATUS_PURCHASED
            // PURCHASE_STATUS_CANCELLED
            // PURCHASE_STATUS_REFUNDED
            int purchaseState = jsonObject.getInt("purchaseState");
            // "12345"
            String developerPayload = jsonObject.optString("developerPayload");
            // токен покупки, с его помощью можно получить
            // данные о покупке на сервере
            String purchaseToken = jsonObject.getString("purchaseToken");
            // далее вы обрабатываете покупку
            boolean pageBought = false;
            switch (productId) {
                case PocketAccounterGeneral.MoneyHolderSkus.FIRST_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIRST_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SECOND_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SECOND_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.THIRD_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.THIRD_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.FOURTH_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FOURTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.FIFTH_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIFTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SIXTH_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SIXTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SEVENTH_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SEVENTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.EIGHTH_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.EIGHTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.NINTH_PAGE_SKU:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.NINTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_CATEGORY_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.VOICE_RECOGNITION_SKU:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.VOICE_RECOGNITION_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CREDIT_ON_MAIN_BOARD_SKU:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_CREDIT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_DEBT_BORROW_ON_MAIN_BOARD_SKU:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_DEBT_BORROW_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_PAGE_ON_MAIN_BOARD_SKU:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_PAGE, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_FUNCTION_ON_MAIN_BOARD_SKU:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.IS_AVAILABLE_CHANGING_OF_FUNCTION, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SMS_PARSING_SKU:
                    try {
                        consumePurchase(purchaseToken);
                    } catch (Exception e) {
                        Log.d("sss", "Error consuming gas. Another async operation in progress.");
                        return;
                    }
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.YELLOW_THEME:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.YELLOW_THEME, true)
                            .commit();
                    paFragmentManager.displayFragment(new ChangeColorOfStyleFragment());
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIOLA_THEME:
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIOLA_THEME, true)
                            .commit();
                    paFragmentManager.displayFragment(new ChangeColorOfStyleFragment());
                    break;
            }
            if (pageBought)
                paFragmentManager.updateAllFragmentsPageChanges();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void consumePurchase(String purchaseToken) throws Exception {
        int result = inAppBillingService.consumePurchase(3,
                context.getPackageName(), purchaseToken);
        if (result == BILLING_RESPONSE_RESULT_OK) {
            // начисляем бонусы
            paFragmentManager.displayFragment(new SmsParseMainFragment());
        } else {
            // обработка ошибки
            paFragmentManager.displayMainWindow();
        }

    }

    public void buySms() {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = PocketAccounterGeneral.MoneyHolderSkus.SMS_PARSING_SKU;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyChangingCategory() {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyChanchingCredit() {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CREDIT_ON_MAIN_BOARD_SKU;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyChangingDebtBorrow() {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_DEBT_BORROW_ON_MAIN_BOARD_SKU;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyTheme(String themeName) {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = themeName;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyChangingPage() {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_PAGE_ON_MAIN_BOARD_SKU;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyChangingFunction() {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_FUNCTION_ON_MAIN_BOARD_SKU;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buyVoiceRecognition() {
        try {
            InAppProduct product = new InAppProduct();
            product.productId = PocketAccounterGeneral.MoneyHolderSkus.VOICE_RECOGNITION_SKU;
            product.isSubscription = false;
            purchaseProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
}
