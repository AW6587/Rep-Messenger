package com.example.repmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class NewEmailTemplateActivity extends AppCompatActivity {

    private EditText et_templateName, et_subject, et_message, et_signature;
    private String fileName, fileContents, signature;
    public static final String EXTRA_TEMPLATE_NAME = "com.example.android.repmessenger.extra.TEMPLATE_NAME";
    public static final String EXTRA_TEMPLATE_MESSAGE = "com.example.android.repmessenger.extra.TEMPLATE_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_email_template);

        et_templateName = findViewById(R.id.editText_name);
        et_subject = findViewById(R.id.editText_subject);
        et_message = findViewById(R.id.editText_message);
        et_signature = findViewById(R.id.editText_signature);
    }

    public void saveEmail(View view) {
        fileName = et_templateName.getText().toString();
        // make sure fileName was entered
        if (!TextUtils.isEmpty(fileName)) {
            fileName = fileName + ".txt";
            String message = et_message.getText().toString();
            //put together contents with "||" as a delimiter
            signature = et_signature.getText().toString();
            if (TextUtils.isEmpty(signature)) {
                fileContents = et_subject.getText().toString() + "||" + message;
            } else {
                fileContents = et_subject.getText().toString() + "||" + message + "||" + signature;
            }
            Log.i("Email", fileContents);

            //save new file
            String folderPath = getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "emailTemplates";
            File file = new File(folderPath, fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(NewEmailTemplateActivity.this, "File " + fileName + " creation failed", Toast.LENGTH_SHORT).show();
                }

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
                    fileOutputStream.close();

                    //send template name back to MainActivity
                    Intent replyIntent = new Intent();
                    replyIntent.putExtra(EXTRA_TEMPLATE_NAME, fileName);
                    replyIntent.putExtra(EXTRA_TEMPLATE_MESSAGE, message);
                    setResult(RESULT_OK, replyIntent);

                    finish();
                } catch (IOException e) {
                    Toast.makeText(NewEmailTemplateActivity.this, "File " + fileName + " write failed", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(NewEmailTemplateActivity.this, "File " + fileName + " already exists", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(NewEmailTemplateActivity.this, "Please enter a template name", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelEmail(View view) {
        //send result back to main that template was canceled
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }
}