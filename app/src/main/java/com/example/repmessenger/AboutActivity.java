package com.example.repmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        TextView aboutTitle = findViewById(R.id.textView_about_title);
        
        try {
            String versionNum = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            String formatted = getString(R.string.about_title, versionNum);
            aboutTitle.setText(formatted);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView aboutBody = findViewById(R.id.textView_about_body);
        aboutBody.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
