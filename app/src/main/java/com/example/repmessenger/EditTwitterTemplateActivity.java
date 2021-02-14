package com.example.repmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class EditTwitterTemplateActivity extends AppCompatActivity {
    private TextView tv_templateName;
    private EditText et_message, et_hashtags;
    private File file;
    private String folderPath;
    private String fileName, fileContents;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_twitter_template);

        tv_templateName = findViewById(R.id.textView_template_name);
        et_message = findViewById(R.id.editText_message);
        et_hashtags = findViewById(R.id.editText_hashtags);

        fileName = getIntent().getStringExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_NAME);
        position = getIntent().getIntExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_POSITION, 0);
        tv_templateName.setText(fileName);
        fileName = fileName + ".txt";

        //load saved file information into editText fields
        byte[] bytes = new byte[1024];
        folderPath = EditTwitterTemplateActivity.this.getFilesDir().getAbsolutePath() + File.separator + "twitterTemplates";
        file = new File(folderPath, fileName);

        try {
            //read file contents into byte[]
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(bytes);
            inputStream.close();

            //remove empty elements from byte array
            int i;
            for (i = 0; i < bytes.length && bytes[i] != 0; i++) { }
            String string = new String(bytes, 0, i, Charset.defaultCharset());

            //split contents into message and hashtags by deliminator "||"
            String[] fileContents = string.split("\\|\\|");

            // check if there's a message at [0]
            if (fileContents.length != 0) {
                et_message.setText(fileContents[0]);

                // check if there's hashtags at [1]
                if (fileContents.length > 1) {
                    String hashtags = fileContents[1];
                    //hashtags are comma separated, replace with space and #
                    hashtags = hashtags.replaceAll(",", " #");
                    //add # to first hashtag
                    hashtags = "#" + hashtags;
                    et_hashtags.setText(hashtags);
                } else {
                    et_hashtags.setText("");
                }
            } else {
                et_message.setText("");
                et_hashtags.setText("");
            }

        } catch (FileNotFoundException e){
            Log.e("TWITTER_ERROR", e.toString());
        } catch (IOException e){
            Log.e("TWITTER_ERROR", e.toString());
        } catch (NullPointerException e){
            Log.e("TWITTER_ERROR", e.toString());
        }
    }

    public void saveTweet(View view) {
        fileName = tv_templateName.getText().toString() + ".txt";
        String message = et_message.getText().toString();
        String hashtags = et_hashtags.getText().toString();
        if (TextUtils.isEmpty(hashtags)){
            fileContents = message;
        } else {
            //format hashtags, remove # signs and replace space separators with commas
            hashtags = hashtags.replaceAll("#", "");
            hashtags = hashtags.replaceAll("\\s", ",");
            fileContents = et_message.getText().toString() + "||" + hashtags; //put together contents with "||" as a delimiter
        }

        //save new file
        try {
            file.createNewFile();
        } catch (IOException e){
            Toast.makeText(EditTwitterTemplateActivity.this, "File " + fileName + " creation failed", Toast.LENGTH_SHORT).show();
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
            fileOutputStream.close();

            //send edited template message back to ManageTemplatesActivity
            Intent replyIntent = new Intent();
            replyIntent.putExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_MESSAGE, message);
            replyIntent.putExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_POSITION, position);
            setResult(RESULT_OK, replyIntent);
            finish();

        } catch(IOException e){
            Toast.makeText(EditTwitterTemplateActivity.this, "File " + fileName + " write failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelTweet(View view) {
        //send result back to main that template was canceled
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }
}