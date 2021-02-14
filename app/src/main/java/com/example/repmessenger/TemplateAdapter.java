package com.example.repmessenger;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    private ArrayList<TemplateItems> items;
    private ArrayList<String> templateFilesE, templateFilesT;
    Context context;

    public TemplateAdapter(ArrayList<TemplateItems> items, ArrayList<String> templateFilesE, ArrayList<String> templateFilesT, Context context){
        this.items = items;
        this.templateFilesE = templateFilesE;
        this.templateFilesT = templateFilesT;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.content_manage_templates, parent, false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final TemplateItems item = items.get(position);
        //set the templateTypeImg to either email icon or twitter icon
        if (item.getTemplateType().equals("email")){
            holder.templateTypeImg.setImageResource(R.drawable.red_email_icon);
        } else if (item.getTemplateType().equals("twitter")){
            holder.templateTypeImg.setImageResource(R.drawable.twitter_icon_copy);
        } else{
            holder.templateTypeImg.setVisibility(View.INVISIBLE);
        }
        holder.templateName.setText(item.getTemplateName());
        holder.message.setText(item.getMessage());

        //set onClick action to edit a template
        holder.editTemplateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(item.getTemplateType().equals("email")){
                    intent = new Intent(context, EditEmailTemplateActivity.class);
                } else{ //template type equals "twitter"
                    intent = new Intent(context, EditTwitterTemplateActivity.class);
                }
                intent.putExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_NAME, item.getTemplateName());
                intent.putExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_POSITION, position);
                //position as resultCode so onActivityResult knows which recyclerView item to update
                ((Activity) context).startActivityForResult(intent, ManageTemplatesActivity.EDIT_TEMPLATE);
            }
        });

        //set onClick action to delete a template
        holder.deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(context);
                myAlertBuilder.setMessage("Delete this template?");
                //user selected "delete", delete file
                myAlertBuilder.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i){
                                String fileName = item.getTemplateName() + ".txt";
                                String folderPath;
                                if (item.getTemplateType().equals("email")){
                                    folderPath = context.getFilesDir().getAbsolutePath() + File.separator + "emailTemplates";
                                    //remove file name from email template list
                                    templateFilesE.remove(item.getTemplateName());
                                } else { //template type equals "twitter"
                                    folderPath = context.getFilesDir().getAbsolutePath() + File.separator + "twitterTemplates";
                                    //remove file name from twitter template list
                                    templateFilesT.remove(item.getTemplateName());
                                }
                                File file = new File(folderPath, fileName);
                                if (file.exists()){
                                    file.delete();
                                    items.remove(position);
                                    //notifyItemRemoved(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context, String.format("File %s has been deleted", fileName), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, String.format("File %s doesn't exist", fileName), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
                //user cancelled the dialog, do nothing
                myAlertBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        }
                );

                AlertDialog alertDialog = myAlertBuilder.create();
                alertDialog.show();
            }
        });
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

        public final ConstraintLayout editTemplateBtn;
        public final ImageView templateTypeImg;
        public final TextView templateName;
        public final TextView message;
        public final ImageButton deleteBtn;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            editTemplateBtn = itemView.findViewById(R.id.constraintLayout_edit_template);
            templateTypeImg = itemView.findViewById(R.id.imageView_template_type);
            templateName = itemView.findViewById(R.id.textView_template_name_header);
            message = itemView.findViewById(R.id.textView_message);
            deleteBtn = itemView.findViewById(R.id.imageButton_delete);
        }
    }
}
