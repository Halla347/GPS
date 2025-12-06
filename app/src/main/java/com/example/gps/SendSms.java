package com.example.gps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SendSms extends AppCompatActivity {
    private static final String TAG = "SMS";
    EditText form_phone;

    String phone_number;
    String message_content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_sms);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        form_phone =findViewById(R.id.form_phone);
        Button formButton = findViewById(R.id.send_sms);
        formButton.setOnClickListener(view -> sendSmsWithIntent());
    }
    private void sendSmsWithIntent(){
        phone_number = form_phone.getText().toString();
        Intent capture  = getIntent();
        double latitude = capture.getDoubleExtra("latitude", 0.0);
        double longitude = capture.getDoubleExtra("longitude", 0.0);
        message_content = "Latitude: " + latitude + "\nLongitude: " + longitude;
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone_number));
        if (!phone_number.isEmpty() && !message_content.isEmpty()){
            intent.putExtra("sms_body",message_content);

        }else {
            Toast.makeText(getApplicationContext(), "empty phone number", Toast.LENGTH_LONG).show();
        }
        try {
            startActivity(intent);
            Log.i(TAG, "sendSmsWithIntent: finished");
        } catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(SendSms.this, "sms failed", Toast.LENGTH_LONG).show();

        }
    }
}