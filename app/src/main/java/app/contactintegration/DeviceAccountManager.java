package app.contactintegration;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.widget.Toast;

class DeviceAccountManager {

    private static DeviceAccountManager instance;

    private final String ACCOUNT_NAME;
    private final String ACCOUNT_TYPE;
    private final AccountManager accountManager;

    private DeviceAccountManager(@NonNull final Context context) {
        ACCOUNT_NAME = context.getString(R.string.account_name);
        ACCOUNT_TYPE = context.getString(R.string.account_type);
        accountManager = AccountManager.get(context);
    }

    static DeviceAccountManager getInstance(@NonNull final Context context) {
        if(instance == null)
            instance = new DeviceAccountManager(context);
        return instance;
    }

    void addAccountToDevice() {

        if(!accountExists()) {
            addNewAccount();
        } else {
            showToast(R.string.account_already_exists);
        }
    }

    private void showToast(final int resId) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getInstance().getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewAccount() {
        Account account = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        try {
            accountManager.addAccountExplicitly(account, null, null);
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
            showToast(R.string.account_added);
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean accountExists() {

        for(Account account : accountManager.getAccounts()) {
            if(ACCOUNT_TYPE.equals(account.type)) {
                return true;
            }
        }
        return false;
    }
}
