package app.contactintegration;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class TargetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

        handleContextualOptions(getIntent(), (TextView) findViewById(R.id.text));
    }

    private void handleContextualOptions(Intent intent, TextView textView) {
        if(intent == null || intent.getType() == null || !intent.getType().contains(getString(R.string.account_type)))
            return;

        String dataString = getIntent().getDataString();

        if(dataString == null || !dataString.contains(ContactsContract.AUTHORITY)) {
            return;
        }

        final Uri uri = Uri.parse(dataString);
        final Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {

            final String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            final String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

            if(getString(R.string.mimetype_action1).equals(mimeType)) {
                textView.setText(getString(R.string.perform_action1) + " for " + number);
            } else if(getString(R.string.mimetype_action2).equals(mimeType)) {
                textView.setText(getString(R.string.perform_action2) + " for " + number);
            }

            cursor.close();
        }

    }
}
