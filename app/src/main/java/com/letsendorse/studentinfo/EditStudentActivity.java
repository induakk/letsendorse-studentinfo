package com.letsendorse.studentinfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class EditStudentActivity extends AppCompatActivity {

    private static final int CAMERA_PIC_REQUEST = 1;
    private static final int SELECT_PICTURE = 2;
    private static final int MY_PERMISSION_READ_STORAGE = 1;
    private Student student;
    private EditText name;
    private ImageView photo;
    private EditText address;
    private EditText phone;
    private EditText email;
    private Bitmap photoBitmap;
    private boolean newStudent;

    private Uri outputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_edit_student);

        this.student = getStudentFromIntent();
        if (student.getName() == null) {
            setTitle(R.string.add_student);
        } else {
            setTitle(R.string.edit_student);
        }
        this.name = (EditText) findViewById(R.id.name);
        this.email = (EditText) findViewById(R.id.email);
        this.phone = (EditText) findViewById(R.id.phone);
        this.address = (EditText) findViewById(R.id.address);
        this.photo = (ImageView) findViewById(R.id.photo);

        this.name.setText(student.getName());
        this.email.setText(student.getEmail());
        this.phone.setText(student.getPhone());
        this.address.setText(student.getAddress());
        byte[] photo = student.getPhoto();
        this.newStudent = student.getId() == 0;
        if (photo != null) {
            this.photo.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.length));
        }
        this.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectPhoto();
            }
        });

    }


    private void onSelectPhoto() {
        AlertDialog.Builder getImageFrom = new AlertDialog.Builder(this);
        getImageFrom.setTitle("Select:");
        final CharSequence[] opsChars = {getResources().getString(R.string.takepic), getResources().getString(R.string.opengallery)};
        getImageFrom.setItems(opsChars, new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra("return-data", true);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                } else if (which == 1) {
                    requestPermission();
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.pickgallery)), SELECT_PICTURE);
                }
                dialog.dismiss();
            }
        });
        getImageFrom.show();
    }

    private void requestPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_READ_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri;
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data == null ? null : data.getData();
                String path = getRealPathFromURI(selectedImageUri);
                this.photoBitmap = BitmapFactory.decodeFile(path, new BitmapFactory.Options());
            } else {
                Bundle extras = data.getExtras();
                this.photoBitmap = (Bitmap) extras.get("data");
            }
            Resources res = getResources();
            RoundedBitmapDrawable dr =
                    RoundedBitmapDrawableFactory.create(res, photoBitmap);
            dr.setCornerRadius(Math.max(photoBitmap.getWidth(), photoBitmap.getHeight()) / 2.0f);
            this.photo.setImageDrawable(dr);
        }
    }

    public String getRealPathFromURI(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private Student getStudentFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return (Student) extras.get("student");
        }
        return new Student();
    }

    private void saveStudent() {

        int fieldErrorCount = 0;

        if (!isValidName(this.name.getText().toString())) {
            fieldErrorCount += 1;
            this.name.setError("Please enter proper name");

        } else {
            this.student.setName(this.name.getText().toString());
        }

        if (!isValidEmail(this.email.getText().toString())) {
            fieldErrorCount += 1;
            this.email.setError("Invalid Email");
        } else {
            this.student.setEmail(this.email.getText().toString());

        }

        if (!isValidPhone(this.phone.getText().toString())) {
            fieldErrorCount += 1;
            this.phone.setError("Please enter valid number");
        } else {
            this.student.setPhone(this.phone.getText().toString());
        }

        if (!isValidAddress(this.address.getText().toString())) {
            fieldErrorCount += 1;
            this.address.setError("Please enter proper address");
        } else {
            this.student.setAddress(this.address.getText().toString());

        }

        if (this.photoBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            this.photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            this.student.setPhoto(byteArray);
        } else {
            fieldErrorCount += 1;
            //this.photoBitmap.setError("Please upload profile pic");

        }

        StudentStore store = StudentStore.getInstance(this.getApplicationContext());

        if (fieldErrorCount > 0) {
            Toast.makeText(getApplicationContext(), "Please enter valid values to save", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (!newStudent) {
                store.updateStudent(student);
            } else {
                store.addStudent(student);
            }
            Intent intent = new Intent();
            intent.putExtra("student", student);

            setResult(RESULT_OK, intent);
            this.finish();

        }


    }


    // validating email id
    private boolean isValidEmail(String email) {
        if (email != null && email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            return true;
        }
        return false;

    }

    // validating Name
    public static boolean isValidName(String name) {
        if (name != null && name.trim().length()>0) {
            return true;
        }
        return false;
    }

    // validate address
    public static boolean isValidAddress(String address)

    {
        if (address != null && address.trim().length()>0) {

            return true;
        }
        return false;

    }

    public static boolean isValidPhone(String phone) {
        if (phone != null && phone.trim().length()>9) {
            return true;
        }
        return false;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveStudent();
                break;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
