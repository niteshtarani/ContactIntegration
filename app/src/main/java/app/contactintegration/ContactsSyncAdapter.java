package app.contactintegration;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class ContactsSyncAdapter extends AbstractThreadedSyncAdapter {

    ContactsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras,
                              String authority, ContentProviderClient provider,
                              SyncResult syncResult) {

        try {
            fetchAndSaveContacts(provider);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void fetchAndSaveContacts(ContentProviderClient provider) throws RemoteException {

        final Cursor idCursor = provider.query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);

        if(idCursor != null && idCursor.moveToFirst()) {
            // add contact ids and names to the list
            do {
                final String id = idCursor.getString(idCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                final String name = idCursor.getString(idCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                if(!contactExists(id)) {
                    // query all contact numbers corresponding to current id
                    final Cursor phoneCursor = provider
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                                    new String[]{id},null);

                    if(phoneCursor != null && phoneCursor.moveToFirst()) {

                        // add contact number to the mNumbers list
                        do {
                            final String number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            addNewRawContact(name, number);
                        } while (phoneCursor.moveToNext());
                        phoneCursor.close();
                    }
                }
            } while (idCursor.moveToNext());
            idCursor.close();
        }
    }

    private boolean contactExists(@NonNull String contactId) {

        final String[] projection = new String[]{ContactsContract.RawContacts._ID};
        final String selection = ContactsContract.RawContacts.CONTACT_ID + " = ? AND " +
                ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
        final String[] selectionArgs = new String[]{contactId, getContext().getString(R.string.account_type)};
        boolean contactExists = false;

        try (Cursor cursor = getContext().getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null)) {

            if (cursor != null) {
                contactExists = cursor.getCount() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactExists;
    }

    private void addNewRawContact(String name, String number) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        final Context context = getContext();

        // insert account name and account type
        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.RawContacts.CONTENT_URI))
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, context.getString(R.string.account_name))
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, context.getString(R.string.account_type))
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                        ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());

        // insert contact number
        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .build());

        // insert contact name
        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // insert mime-type data
        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, context.getString(R.string.mimetype_action1))
                .withValue(ContactsContract.Data.DATA1, number)
                .withValue(ContactsContract.Data.DATA2, context.getString(R.string.app_name))
                .withValue(ContactsContract.Data.DATA3, context.getString(R.string.perform_action1))
                .build());

        ops.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, context.getString(R.string.mimetype_action2))
                .withValue(ContactsContract.Data.DATA1, number)
                .withValue(ContactsContract.Data.DATA2, context.getString(R.string.app_name))
                .withValue(ContactsContract.Data.DATA3, context.getString(R.string.perform_action2))
                .build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException re) {
            re.printStackTrace();
        } catch (OperationApplicationException oae) {
            oae.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private Uri addCallerIsSyncAdapterParameter(@NonNull Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();
    }
}
