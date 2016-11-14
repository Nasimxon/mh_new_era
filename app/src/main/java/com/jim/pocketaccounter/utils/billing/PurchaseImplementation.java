package com.jim.pocketaccounter.utils.billing;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.utils.PocketAccounterGeneral;

import javax.inject.Inject;

public class PurchaseImplementation {

    private IabHelper mHelper;
    private String base64rsa = PocketAccounterGeneral.BASE64RSA;
    private Context context;
    //accesses
    public static final int RC_REQUEST = 10001;
    private PAFragmentManager paFragmentManager;
    @Inject SharedPreferences preferences;
    public PurchaseImplementation(Context context, PAFragmentManager paFragmentManager) {
        this.context = context;
        ((PocketAccounter) context).component((PocketAccounterApplication) context.getApplicationContext()).inject(this);
        this.paFragmentManager = paFragmentManager;
        mHelper = new IabHelper(context, base64rsa);
        initialize();
    }

    public void initialize() {
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



    public IabHelper getHelper() { return mHelper; }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                Log.d("sss", "Failed to query inventory: " + result);
                return;
            }
//            Purchase categoryChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU);
//            isCategoryChangeAvailable = (categoryChange != null && verifyDeveloperPayload(categoryChange));
//            Purchase creditChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CREDIT_ON_MAIN_BOARD_SKU);
//            isCreditChangeAvailable = (creditChange != null && verifyDeveloperPayload(creditChange));
//            Purchase debtBorrowChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_DEBT_BORROW_ON_MAIN_BOARD_SKU);
//            isDebtBorrowChangeAvailable = (debtBorrowChange != null && verifyDeveloperPayload(debtBorrowChange));
//            Purchase functionChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_FUNCTION_ON_MAIN_BOARD_SKU);
//            isDebtBorrowChangeAvailable = (functionChange != null && verifyDeveloperPayload(functionChange));
//            Purchase pageChange = inventory.getPurchase(PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_PAGE_ON_MAIN_BOARD_SKU);
//            isPageChangeAvailable = (pageChange != null && verifyDeveloperPayload(pageChange));

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
            if (purchase == null) {
                Log.d("sss", "Null purchase.");
                return;
            }
            boolean pageBought = false;
            switch (purchase.getSku()) {
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.ZERO_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.ZERO_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIRST_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIRST_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SECOND_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SECOND_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.THIRD_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.THIRD_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FOURTH_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FOURTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIFTH_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.FIFTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SIXTH_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SIXTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SEVENTH_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SEVENTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.EIGHTH_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.EIGHTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
                case PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.NINTH_PAGE_COUNT_KEY:
                    pageBought = true;
                    preferences
                            .edit()
                            .putBoolean(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.NINTH_PAGE_COUNT_KEY, true)
                            .commit();
                    break;
            }
            if (pageBought)
                paFragmentManager.updateAllFragmentsPageChanges();


            if (purchase.getSku().equals(PocketAccounterGeneral.MoneyHolderSkus.SMS_PARSING_SKU)) {
                try {

                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d("sss", "Error consuming gas. Another async operation in progress.");
                    return;
                }

            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d("sss", "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                int count = preferences.getInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SMS_PARSING_COUNT_KEY, 1);
                count++;
                preferences
                        .edit()
                        .putInt(PocketAccounterGeneral.MoneyHolderSkus.SkuPreferenceKeys.SMS_PARSING_COUNT_KEY, count)
                        .commit();
            }
            else {
                Log.d("sss", "Error while consuming: " + result);
            }
            Log.d("sss", "End consumption flow.");
        }
    };

    public void buyPage(int position) {
        String payload = "";
        String sku = "";
        switch (position) {
            case 0:
                sku = PocketAccounterGeneral.MoneyHolderSkus.ZERO_PAGE_SKU;
                break;
            case 1:
                sku = PocketAccounterGeneral.MoneyHolderSkus.FIRST_PAGE_SKU;
                break;
            case 2:
                sku = PocketAccounterGeneral.MoneyHolderSkus.SECOND_PAGE_SKU;
                break;
            case 3:
                sku = PocketAccounterGeneral.MoneyHolderSkus.THIRD_PAGE_SKU;
                break;
            case 4:
                sku = PocketAccounterGeneral.MoneyHolderSkus.FOURTH_PAGE_SKU;
                break;
            case 5:
                sku = PocketAccounterGeneral.MoneyHolderSkus.FIFTH_PAGE_SKU;
                break;
            case 6:
                sku = PocketAccounterGeneral.MoneyHolderSkus.SIXTH_PAGE_SKU;
                break;
            case 7:
                sku = PocketAccounterGeneral.MoneyHolderSkus.SEVENTH_PAGE_SKU;
                break;
            case 8:
                sku = PocketAccounterGeneral.MoneyHolderSkus.EIGHTH_PAGE_SKU;
                break;
            case 9:
                sku = PocketAccounterGeneral.MoneyHolderSkus.NINTH_PAGE_SKU;
                break;
        }
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    sku, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
            Log.d("sss", "Error launching purchase flow. Another async operation in progress.");
        }
    }

    public void buySms() {
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    PocketAccounterGeneral.MoneyHolderSkus.SMS_PARSING_SKU, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void buyChangingCategory() {
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CATEGORY_ON_MAIN_BOARD_SKU, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void buyChanchingCredit() {
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_CREDIT_ON_MAIN_BOARD_SKU, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void buyChangingDebtBorrow() {
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_DEBT_BORROW_ON_MAIN_BOARD_SKU, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void buyChangingPage() {
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_PAGE_ON_MAIN_BOARD_SKU, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }

    }

    public void buyChangingFunction() {
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(((PocketAccounter) context),
                    PocketAccounterGeneral.MoneyHolderSkus.ADD_REPLACE_FUNCTION_ON_MAIN_BOARD_SKU, RC_REQUEST,
                    mPurchaseFinishedListener,
                    payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
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
