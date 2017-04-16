package com.letsendorse.studentinfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StudentsList extends AppCompatActivity {

    private static final int EDIT_STUDENT = 1;
    private static final int ADD_STUDENT = 2;
    private StudentListAdapter studentListAdapter;
    private List<Student> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_list);
        setTitle(R.string.students);

        final ListView listView = (ListView) findViewById(R.id.listView);
        studentListAdapter = new StudentListAdapter(this, loadStudents());
        listView.setAdapter(studentListAdapter);
    }

    private List<Student> loadStudents() {
        data.addAll(StudentStore.getInstance(getApplicationContext()).getAll());
        data.add(0, null); // This will cause Add New Student to be displayed
        return data;
    }


    public class StudentListAdapter extends ArrayAdapter<Student> {
        public StudentListAdapter(Context context, List<Student> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Student student = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_content, parent, false);
            }


            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView address = (TextView) convertView.findViewById(R.id.address);
            TextView email = (TextView) convertView.findViewById(R.id.email);
            TextView phone = (TextView) convertView.findViewById(R.id.phone);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);

            if (student == null) {
                name.setText(R.string.add_student);
                address.setVisibility(View.INVISIBLE);
                email.setVisibility(View.INVISIBLE);
                phone.setVisibility(View.INVISIBLE);
                icon.setVisibility(View.INVISIBLE);

                name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddNewClicked();
                    }
                });
            } else {
                address.setVisibility(View.VISIBLE);
                email.setVisibility(View.VISIBLE);
                phone.setVisibility(View.VISIBLE);
                icon.setVisibility(View.VISIBLE);
                name.setText(student.getName());
                address.setText(student.getAddress());
                email.setText(student.getEmail());
                phone.setText(student.getPhone());
                byte[] bytes = student.getPhoto();
                if (bytes != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    icon.setImageBitmap(bitmap);
                }

                address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddressClicked(student);
                    }
                });
                phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPhoneClicked(student);
                    }
                });
                email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onEmailClicked(student);
                    }
                });
                name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNameClicked(student);
                    }
                });
            }

            return convertView;
        }

    }

    private void onNameClicked(Student student) {
        editStudent(student);
    }

    private void onEmailClicked(Student student) {
// Create the Intent
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

// Fill it with Data
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{student.getEmail()});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

/* Send it off to the Activity-Chooser */
        this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private void onPhoneClicked(Student student) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + student.getPhone()));
        startActivity(intent);
    }

    private void onAddressClicked(Student student) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(student.getAddress()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void onAddNewClicked() {
        editStudent(new Student());
    }

    private void editStudent(Student student) {
        Intent editIntent = new Intent(this, EditStudentActivity.class);
        editIntent.putExtra("student", student);
        startActivityForResult(editIntent, student.getName() == null ? ADD_STUDENT : EDIT_STUDENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Student student = (Student) data.getExtras().get("student");

            if (requestCode == EDIT_STUDENT) {
                int index = getIndex(student);
                this.data.set(index, student);
            } else {
                this.data.add(student);
            }
            this.studentListAdapter.notifyDataSetChanged();
        }
    }

    private int getIndex(Student student) {
        for (int i = 1; i < this.data.size(); i++) {
            if (this.data.get(i).getId() == student.getId()) {
                return i;
            }
        }
        return 0;
    }
}
