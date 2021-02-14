package com.example.repmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class NewTwitterTemplateActivity extends AppCompatActivity {

    private EditText et_templateName, et_message, et_hashtags;
    private String hashtags, fileName, fileContents;
    public static final String EXTRA_TEMPLATE_NAME = "com.example.android.repmessenger.extra.TEMPLATE_NAME";
    public static final String EXTRA_TEMPLATE_MESSAGE = "com.example.android.repmessenger.extra.TEMPLATE_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_twitter_template);

        et_templateName = findViewById(R.id.editText_name);
        et_message = findViewById(R.id.editText_message);
        et_hashtags = findViewById(R.id.editText_hashtags);

    }

    //save new Twitter template and return to main
    public void saveTweet(View view) {
        //format hashtags, remove # signs and replace space separators with commas
        hashtags = et_hashtags.getText().toString();
        hashtags = hashtags.replaceAll("#", "");
        hashtags = hashtags.replaceAll("\\s", ",");

        fileName = et_templateName.getText().toString();
        if (!TextUtils.isEmpty(fileName)) {
            fileName = fileName + ".txt";
            String message = et_message.getText().toString();
            fileContents = message + "||" + hashtags; //put together contents with "||" as a delimiter

            //save new file
            String folderPath = getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "twitterTemplates";
            File file = new File(folderPath, fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(NewTwitterTemplateActivity.this, "File " + fileName + " creation failed", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(NewTwitterTemplateActivity.this, "File " + fileName + " write failed", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(NewTwitterTemplateActivity.this, "File " + fileName + " already exists", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(NewTwitterTemplateActivity.this, "Please enter a template name", Toast.LENGTH_SHORT).show();
        }
    }

    //cancel and return to main
    public void cancelTweet(View view) {
        //send result back to main that template was canceled
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }

}