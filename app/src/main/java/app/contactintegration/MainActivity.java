package app.contactintegration;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static int MY_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    private boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkPermission() {
        String[] permissions = new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        if (hasPermissions(permissions)) {
            findViewById(R.id.add_account).setOnClickListener(this);
        } else {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS) {
            for(int result : grantResults) {
                if(result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getApplicationContext(), "Permission not granted", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            findViewById(R.id.add_account).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.add_account) {
            DeviceAccountManager.getInstance(this).addAccountToDevice();
        }
    }
}
