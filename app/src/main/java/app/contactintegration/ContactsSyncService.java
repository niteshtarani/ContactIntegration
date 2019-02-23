package app.contactintegration;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ContactsSyncService extends Service {

    private ContactsSyncAdapter contactsSyncAdapter;

    @Override
    public void onCreate() {
        contactsSyncAdapter = new ContactsSyncAdapter(this, true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return contactsSyncAdapter.getSyncAdapterBinder();
    }
}
