package com.jim.pocketaccounter.utils.billing;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

public class PurchaseImplementation {

    private IabHelper mHelper;
    private String base64rsa = PocketAccounterGeneral.BASE64RSA;
    private Context context;
    private SharedPreferences preferences;
    //accesses
    private int smsCount = 0;
    private int maxPageCount = 0;
    private boolean isCategoryChangeAvailable = false;
    private boolean isDebtBorrowChangeAvailable = false;
    private boolean isCreditChangeAvailable = false;
    private boolean isFunctionChangeAvailable = false;
    private boolean isPageChangeAvailable = false;
    private boolean isWhiteDesignAvailbale = false;
    private boolean isVioletteDesignAvailbale = false;

    public static final int RC_REQUEST = 10001;

    public PurchaseImplementation(Context context, SharedPreferences preferences) {
        this.context = context;
        this.preferences = preferences;
        mHelper = new IabHelper(context, base64rsa);
        initialize();
    }

    public void initialize() {
        smsCount = preferences.getInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SMS_PARSING_COUNT_KEY, 1);
        maxPageCount = preferences.getInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.PAGING_COUNT_KEY, 2);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d("sss", "Problem setting up in-app billing: " + result);
                    return;
                }
                if (mHelper == null) return;
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d("sss", "Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    public boolean isCategoryChangeAvailable() {
        return isCategoryChangeAvailable;
    }

    public int getMaxPageCount() {
        return maxPageCount;
    }

    public int getSmsCount() {
        return smsCount;
    }

    public IabHelper getHelper() { return mHelper; }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                Log.d("sss", "Failed to query inventory: " + result);
                return;
            }
            Purchase categoryChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU);
            isCategoryChangeAvailable = (categoryChange != null && verifyDeveloperPayload(categoryChange));
//            Purchase creditChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CREDIT_ON_MAIN_BOARD_SKU);
//            isCreditChangeAvailable = (creditChange != null && verifyDeveloperPayload(creditChange));
//            Purchase debtBorrowChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_DEBT_BORROW_ON_MAIN_BOARD_SKU);
//            isDebtBorrowChangeAvailable = (debtBorrowChange != null && verifyDeveloperPayload(debtBorrowChange));
//            Purchase functionChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_FUNCTION_ON_MAIN_BOARD_SKU);
//            isDebtBorrowChangeAvailable = (functionChange != null && verifyDeveloperPayload(functionChange));
//            Purchase pageChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_PAGE_ON_MAIN_BOARD_SKU);
//            isPageChangeAvailable = (pageChange != null && verifyDeveloperPayload(pageChange));

            return;
        }
    };

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                Log.d("sss", "Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                Log.d("sss", "Error purchasing. Authenticity verification failed.");
                return;
            }
            if (purchase.getSku().equals(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU)) {
                Log.d("sss", "add replace item");
                isCategoryChangeAvailable = true;
                return;
            }
        }
    };

    public void buyCategoryChange(Context context) {
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            Log.d("sss", "Error launching purchase flow. Another async operation in progress.");
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
