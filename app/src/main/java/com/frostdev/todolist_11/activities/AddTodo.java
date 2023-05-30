package com.frostdev.todolist_11.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DialogTitle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frostdev.todolist_11.R;
import com.frostdev.todolist_11.database.DatabaseHelper;
import com.frostdev.todolist_11.entities.Todo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTodo extends AppCompatActivity {


    private EditText inputTitleTodo, inputSubTodo, inputDesc;
    private TextView txtDateTime;


    private String selectedColorTodo;
    private View viewSubIndicator;
    private Todo alreadyAvailableTodo;
    private ImageView imgTodo;
    private String selectedImgPath;
    private TextView txtURL;
    private LinearLayout layoutURL;

    //notification
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    public static final String default_notification_channel_id = "default";

    //dialog
    private AlertDialog dialogAddURL;
    private AlertDialog dialogDelete;
    DatePickerDialog.OnDateSetListener dateSetListener;
    int hour, minute;

    //code
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        inputTitleTodo = findViewById(R.id.titleTodo);
        inputSubTodo = findViewById(R.id.subTodo);
        inputDesc = findViewById(R.id.inputTodo);
        viewSubIndicator = findViewById(R.id.descTodoColor);
        imgTodo = findViewById(R.id.imageTodo);
        txtURL = findViewById(R.id.txtWebURl);
        layoutURL = findViewById(R.id.layoutWebURL);
        txtDateTime = findViewById(R.id.dateTodo);


     final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        txtDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AddTodo.this, android.R.style.Theme_DeviceDefault_Dialog,
                        dateSetListener, year, month, day);
               datePickerDialog.show();
            }
        });


        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = "Deadline : "+dayOfMonth+"/"+month+"/"+year;
                txtDateTime.setText(date);
            }
        };

        ImageView btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTodo();
            }
        });

        selectedColorTodo = "#333333";
        selectedImgPath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailableTodo = (Todo) getIntent().getSerializableExtra("todo");
            setViewOrUpdateTodo();
        }

        findViewById(R.id.btnDeleteURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtURL.setText(null);
                layoutURL.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.btnDeleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgTodo.setImageBitmap(null);
                imgTodo.setVisibility(View.GONE);
                findViewById(R.id.btnDeleteImage).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        initMiscellanous();
        setSubIndicatorColor();
    }


    private void dateSchedule(Notification notification, long delay){
        Intent notifIntent = new Intent(this, NotificationTodo.class);
        notifIntent.putExtra(NotificationTodo.NOTIFICATION_ID, 1);
        notifIntent.putExtra(NotificationTodo.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getLayoutInflater().getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
        }

    }


    private void setViewOrUpdateTodo(){
        inputTitleTodo.setText(alreadyAvailableTodo.getTittle());
        inputSubTodo.setText(alreadyAvailableTodo.getSubtitle());
        inputDesc.setText(alreadyAvailableTodo.getDesc());
        txtDateTime.setText(alreadyAvailableTodo.getDateTime());

        if (alreadyAvailableTodo.getImgPath() != null && !alreadyAvailableTodo.getImgPath().trim().isEmpty()){
            imgTodo.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableTodo.getImgPath()));
            imgTodo.setVisibility(View.VISIBLE);
            findViewById(R.id.btnDeleteImage).setVisibility(View.VISIBLE);
            selectedImgPath = alreadyAvailableTodo.getImgPath();
        }

        if (alreadyAvailableTodo.getWebLink() != null && !alreadyAvailableTodo.getWebLink().trim().isEmpty()){
            txtURL.setText(alreadyAvailableTodo.getWebLink());
            layoutURL.setVisibility(View.VISIBLE);
        }
    }


    private void saveTodo(){
        if (inputTitleTodo.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputSubTodo.getText().toString().trim().isEmpty()
        && inputDesc.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Add some subtitle", Toast.LENGTH_SHORT).show();
            return;
        }

        final Todo todo = new Todo();
        todo.setTittle(inputTitleTodo.getText().toString());
        todo.setSubtitle(inputSubTodo.getText().toString());
        todo.setDesc(inputDesc.getText().toString());
        todo.setDateTime(txtDateTime.getText().toString());
        todo.setColor(selectedColorTodo);
        todo.setImgPath(selectedImgPath);

        if (layoutURL.getVisibility() == View.VISIBLE){
            todo.setWebLink(txtURL.getText().toString());
        }

        if (alreadyAvailableTodo != null){
            todo.setId(alreadyAvailableTodo.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class saveTodoList extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... voids){
                DatabaseHelper.getDB(getApplicationContext()).todoDao().insertTodo(todo);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new saveTodoList().execute();
    }


    private void initMiscellanous(){
        final LinearLayout layoutMisc = findViewById(R.id.layoutMisc);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        layoutMisc.findViewById(R.id.txtMisc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imgColor1 = layoutMisc.findViewById(R.id.btnColor1);
        final ImageView imgColor2 = layoutMisc.findViewById(R.id.btnColor2);
        final ImageView imgColor3 = layoutMisc.findViewById(R.id.btnColor3);
        final ImageView imgColor4 = layoutMisc.findViewById(R.id.btnColor4);
        final ImageView imgColor5 = layoutMisc.findViewById(R.id.btnColor5);

        layoutMisc.findViewById(R.id.Color1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColorTodo = "#333333";
                imgColor1.setImageResource(R.drawable.ic_done);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setSubIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.Color2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColorTodo = "#FDBE3B";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(R.drawable.ic_done);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setSubIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.Color3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColorTodo = "#FF4842";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(R.drawable.ic_done);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setSubIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.Color4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColorTodo = "#3592C4";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(R.drawable.ic_done);
                imgColor5.setImageResource(0);
                setSubIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.Color5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColorTodo = "#03DED8";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(R.drawable.ic_done);
                setSubIndicatorColor();
            }
        });

        if (alreadyAvailableTodo != null && alreadyAvailableTodo.getColor() != null && !alreadyAvailableTodo.getColor().trim().isEmpty()){
            switch (alreadyAvailableTodo.getColor()) {
                case "#FDBE3B":
                    layoutMisc.findViewById(R.id.Color2).performClick();
                    break;
                case "#FF4842":
                    layoutMisc.findViewById(R.id.Color3).performClick();
                    break;
                case "#3592C4":
                    layoutMisc.findViewById(R.id.Color4).performClick();
                    break;
                case "#000000":
                    layoutMisc.findViewById(R.id.Color5).performClick();
                    break;
            }
        }



        layoutMisc.findViewById(R.id.layoutAddLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        if (alreadyAvailableTodo != null){
            layoutMisc.findViewById(R.id.layoutDeleteTodo).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layoutDeleteTodo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteDialog();
                }
            });
        }
    }

    private void showDeleteDialog(){
        if (dialogDelete == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddTodo.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_todo,
                    (ViewGroup) findViewById(R.id.layoutDeleteTodoContainer)
            );
            builder.setView(view);
            dialogDelete = builder.create();
            if (dialogDelete.getWindow() != null){
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.txtDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteTodoTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            DatabaseHelper.getDB(getApplicationContext()).todoDao()
                                    .deleteTodo(alreadyAvailableTodo);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isTodoDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteTodoTask().execute();
                }
            });
            view.findViewById(R.id.txtCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDelete.dismiss();
                }
            });
        }
        dialogDelete.show();
    }

    //method sub color
    private void setSubIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColorTodo));
    }

    //method select image
    @SuppressLint("QueryPermissionsNeeded")
    private void selectImg(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImg();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImgUri = data.getData();
                if (selectedImgUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImgUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imgTodo.setImageBitmap(bitmap);
                        imgTodo.setVisibility(View.VISIBLE);
                        findViewById(R.id.btnDeleteImage).setVisibility(View.VISIBLE);

                        selectedImgPath = getPathFromUri(selectedImgUri);

                    } catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    //method get url
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null){
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    //method tambah URL
    private void showAddURLDialog(){
        if (dialogAddURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddTodo.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddURLContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null){
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.edtUrl);
            inputURL.requestFocus();

            view.findViewById(R.id.txtAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText().toString().trim().isEmpty()){
                        Toast.makeText(AddTodo.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(AddTodo.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        txtURL.setText(inputURL.getText().toString());
                        layoutURL.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();
                    }
                }
            });

            view.findViewById(R.id.txtCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }
}