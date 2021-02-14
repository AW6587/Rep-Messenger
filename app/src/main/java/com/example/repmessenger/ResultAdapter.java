package com.example.repmessenger;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import de.cketti.mailto.EmailIntentBuilder;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    Context context;
    private final ArrayList<ResultItems> items;
    private ArrayAdapter<String> spinnerAdapterE, spinnerAdapterT;

    public ResultAdapter(Context context, ArrayList<ResultItems> items, ArrayList<String> templateFilesE, ArrayList<String> templateFilesT) {
        this.context = context;
        this.items = items;

        //adapter for email spinner files and twitter spinner files
        spinnerAdapterE = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, templateFilesE);
        spinnerAdapterE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapterT = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, templateFilesT);
        spinnerAdapterT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemLayout = inflater.inflate(R.layout.content_results, parent, false);
        return new ViewHolder(itemLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ResultItems item = items.get(position);

        final String city = item.getCity();
        final String name = item.getName();
        final String office = item.getOffice();
        final String party = item.getParty();
        final String phone = item.getPhone();
        final String email = item.getEmail();
        final String twitter = item.getTwitter();

        // set values retrieved from Json object to content_results.xml
        if (name != null){
            holder.editName.setText(name);
        } else { holder.editName.setText("Name not found");}
        if (office != null){
            holder.editOffice.setText(office);
        } else { holder.editOffice.setText("Office not found");}
        if (party != null){
            holder.editParty.setText(party);
        } else { holder.editParty.setText("Party not found");}
        if (phone != null){
            holder.editPhone.setText(phone);
            // make phone launch dialer
            holder.editPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("tel:" + phone);
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    try{
                        //launch phone app's dialer with phone number to call
                        v.getContext().startActivity(intent);
                    }
                    catch (SecurityException s){
                        // do nothing
                    }
                }
            });
        } else {
            holder.editPhone.setText("Phone number not found");
            holder.editPhone.setOnClickListener(null);
        }
        if (email != null){
            holder.editEmail.setText(email);
            // launch email
            holder.editEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //using email-intent-builder-2.0.0 library
                    EmailIntentBuilder
                            .from(v.getContext())
                            .to(email)
                            .start();

                    //easy way if just an empty email to send to
                    /*Uri uri = Uri.parse("mailto:" + email);
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    try{
                        v.getContext().startActivity(intent);
                    }
                    catch (SecurityException s){
                        // do nothing
                    }*/
                }
            });

            //set values for email template spinner
            holder.emailSpinnerContainer.setVisibility(View.VISIBLE);
            holder.editEmailSpinner.setAdapter(new NoSelectionSpinnerAdapter(spinnerAdapterE, R.layout.email_spinner_no_selection_row, this.context));
            holder.editEmailSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    // reset spinner to hint
                    holder.editEmailSpinner.setSelection(0);
                    try{
                        String fileName = adapterView.getItemAtPosition(pos).toString() + ".txt";
                        byte[] bytes = new byte[1024];
                        String folderPath = view.getContext().getFilesDir().getAbsolutePath() + File.separator + "emailTemplates";
                        File file = new File(folderPath, fileName);

                        //read file contents into byte[]
                        FileInputStream inputStream = new FileInputStream(file);
                        inputStream.read(bytes);
                        inputStream.close();

                        //remove empty elements from byte array
                        int i;
                        for (i = 0; i < bytes.length && bytes[i] != 0; i++){}
                        String string = new String(bytes, 0, i, Charset.defaultCharset());

                        //split contents into email subject and message by deliminator "||"
                        String[] fileContents = string.split("\\|\\|");
                        String message;
                        if (fileContents.length < 3){
                            message = "Dear " + office + " " + name + ", <br><br>" + fileContents[1] + "<br><br>Sincerely, <br> Your constituent in " + city;
                        }
                        else if (fileContents[2].contains("Your constituent in")){
                            message = "Dear " + office + " " + name + ", <br><br>" + fileContents[1] + "<br><br>Sincerely, <br>" + fileContents[2] + city;
                        } else {
                            message = "Dear " + office + " " + name + ", <br><br>" + fileContents[1] + "<br><br>Sincerely, <br>" + fileContents[2];
                        }
                        String subject = fileContents[0];
                        Log.i("Email", string);
                        //using email-intent-builder-2.0.0.aar library
                        EmailIntentBuilder
                                .from(view.getContext())
                                .to(email)
                                .subject(subject)
                                .body(message)
                                .start();


                    } catch (FileNotFoundException e){
                        Log.e("EMAIL_ERROR", e.toString());
                    } catch (IOException e){
                        Log.e("EMAIL_ERROR", e.toString());
                    } catch (NullPointerException e){
                        Log.e("EMAIL_ERROR", e.toString());
                    }
                }
                // required, do nothing
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

        } else {
            holder.editEmail.setText("Email not found");
            holder.editEmail.setOnClickListener(null);
            holder.emailSpinnerContainer.setVisibility(View.GONE);
        }
        if (twitter != null){
            holder.editTwitter.setText(twitter);
            // launch twitter with user's handle
            holder.editTwitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=" + twitter));
                    v.getContext().startActivity(intent);
                }
            });

            //set values for twitter template spinner
            holder.twitterSpinnerContainer.setVisibility(View.VISIBLE);
            holder.editTwitterSpinner.setAdapter(new NoSelectionSpinnerAdapter(spinnerAdapterT, R.layout.twitter_spinner_no_selection_row, this.context));
            holder.editTwitterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    // reset spinner to hint
                    holder.editTwitterSpinner.setSelection(0);
                    try{
                        String fileName = adapterView.getItemAtPosition(pos).toString() + ".txt";
                        byte[] bytes = new byte[600];
                        String folderPath = view.getContext().getFilesDir().getAbsolutePath() + File.separator + "twitterTemplates";
                        File file = new File(folderPath, fileName);

                        //read file contents into byte[]
                        FileInputStream inputStream = new FileInputStream(file);
                        inputStream.read(bytes);
                        inputStream.close();

                        //remove empty elements from byte array
                        int i;
                        for (i = 0; i < bytes.length && bytes[i] != 0; i++){}
                        String string = new String(bytes, 0, i, Charset.defaultCharset());

                        //split contents into email subject and message by deliminator "||"
                        String[] fileContents = string.split("\\|\\|");

                        //check if there's a message and/or hashtags
                        Intent intent;
                        if (fileContents.length > 1){
                            String message = fileContents[0];
                            String hashtags = fileContents[1];
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=" + twitter + " " + message + "&hashtags=" + hashtags));
                        } else if (fileContents.length > 0){
                            String message = fileContents[0];
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=" + twitter + " " + message));
                        } else {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=" + twitter + " "));
                        }
                        view.getContext().startActivity(intent);


                    } catch (FileNotFoundException e){
                        Log.e("TWITTER_ERROR", e.toString());
                    } catch (IOException e){
                        Log.e("TWITTER_ERROR", e.toString());
                    } catch (NullPointerException e){
                        Log.e("TWITTER_ERROR", e.toString());
                    }

                }
                // required, do nothing
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

        } else {
            holder.editTwitter.setText("Twitter handle not found");
            holder.editTwitter.setOnClickListener(null);
            holder.twitterSpinnerContainer.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        if (items != null){
            return items.size();
        } else {
            return 0;
        }
    }





    class ViewHolder extends RecyclerView.ViewHolder {

        TextView editName, editOffice, editParty, editPhone, editEmail, editTwitter;
        ConstraintLayout emailSpinnerContainer, twitterSpinnerContainer;
        Spinner editEmailSpinner, editTwitterSpinner;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            editName = itemView.findViewById(R.id.editText_name);
            editOffice = itemView.findViewById(R.id.textView_office);
            editParty = itemView.findViewById(R.id.textView_party);
            editPhone = itemView.findViewById(R.id.textView_phone);
            editEmail = itemView.findViewById(R.id.textView_email);
            editTwitter = itemView.findViewById(R.id.textView_twitter);
            emailSpinnerContainer = itemView.findViewById(R.id.constraintLayout_email_template);
            twitterSpinnerContainer = itemView.findViewById(R.id.constraintLayout_twitter_template);
            editEmailSpinner = itemView.findViewById(R.id.spinner_email_template);
            editTwitterSpinner = itemView.findViewById(R.id.spinner_twitter_template);
        }
    }



    // utility class for custom spinner adapter behavior
    // instead of displaying first file in template list, displays a prompt
    class NoSelectionSpinnerAdapter implements SpinnerAdapter, ListAdapter{
        protected static final int EXTRA = 1;
        protected SpinnerAdapter adapter;
        protected Context context;
        protected int noSelectionLayout;
        protected int noSelectionDropdownLayout;
        protected LayoutInflater layoutInflater;

        /**
         * Use this constructor to have NO 'Select One...' item, instead use
         * the standard prompt or nothing at all.
         * @param spinnerAdapter wrapped Adapter.
         * @param noSelectionLayout layout for nothing selected, perhaps
         * you want text grayed out like a prompt...
         * @param context
         */
        public NoSelectionSpinnerAdapter(
                SpinnerAdapter spinnerAdapter,
                int noSelectionLayout, Context context) {

            this(spinnerAdapter, noSelectionLayout, -1, context);
        }

        /**
         * Use this constructor to Define your 'Select One...' layout as the first
         * row in the returned choices.
         * If you do this, you probably don't want a prompt on your spinner or it'll
         * have two 'Select' rows.
         * @param spinnerAdapter wrapped Adapter. Should probably return false for isEnabled(0)
         * @param noSelectionLayout layout for nothing selected, perhaps you want
         * text grayed out like a prompt...
         * @param noSelectionDropdownLayout layout for your 'Select an Item...' in
         * the dropdown.
         * @param context
         */
        public NoSelectionSpinnerAdapter(SpinnerAdapter spinnerAdapter,
                                             int noSelectionLayout, int noSelectionDropdownLayout, Context context) {
            this.adapter = spinnerAdapter;
            this.context = context;
            this.noSelectionLayout = noSelectionLayout;
            this.noSelectionDropdownLayout = noSelectionDropdownLayout;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public final View getView(int position, View convertView, ViewGroup parent) {
            // This provides the View for the Selected Item in the Spinner, not
            // the dropdown (unless dropdownView is not set).
            if (position == 0) {
                return getNoSelectionView(parent);
            }
            return adapter.getView(position - EXTRA, null, parent); // Could re-use
            // the convertView if possible.
        }

        /**
         * View to show in Spinner with Nothing Selected
         * Override this to do something dynamic... e.g. "37 Options Found"
         * @param parent
         * @return
         */
        protected View getNoSelectionView(ViewGroup parent) {
            return layoutInflater.inflate(noSelectionLayout, parent, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // Android BUG! http://code.google.com/p/android/issues/detail?id=17128 -
            // Spinner does not support multiple view types
            if (position == 0) {
                return noSelectionDropdownLayout == -1 ?
                        new View(context) :
                        getNoSelectionDropdownView(parent);
            }

            // Could re-use the convertView if possible, use setTag...
            return adapter.getDropDownView(position - EXTRA, null, parent);
        }

        /**
         * Override this to do something dynamic... For example, "Pick your favorite
         * of these 37".
         * @param parent
         * @return
         */
        protected View getNoSelectionDropdownView(ViewGroup parent) {
            return layoutInflater.inflate(noSelectionDropdownLayout, parent, false);
        }

        @Override
        public int getCount() {
            int count = adapter.getCount();
            return count == 0 ? 0 : count + EXTRA;
        }

        @Override
        public Object getItem(int position) {
            return position == 0 ? null : adapter.getItem(position - EXTRA);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position >= EXTRA ? adapter.getItemId(position - EXTRA) : position - EXTRA;
        }

        @Override
        public boolean hasStableIds() {
            return adapter.hasStableIds();
        }

        @Override
        public boolean isEmpty() {
            return adapter.isEmpty();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            adapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            adapter.unregisterDataSetObserver(observer);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return position != 0; // Don't allow the 'no selection' item to be picked.
        }
    }
}
