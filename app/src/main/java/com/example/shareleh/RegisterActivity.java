package com.example.shareleh;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTxtUsername, editTxtFullName, editTxtEmail, editTxtPw, editTxtReenterPw;
    private TextView txtWarning;
    private Button btnRegister;
    DatabaseHelper dbHelper = new DatabaseHelper(RegisterActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        initViews();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editTxtUsername.getText().toString();
                String fullName = editTxtFullName.getText().toString();
                String email = editTxtEmail.getText().toString();
                String password = editTxtPw.getText().toString();
                String reenterPassword = editTxtReenterPw.getText().toString();

                if (username.equals("")||fullName.equals("")||email.equals("")||password.equals("")||reenterPassword.equals("")) {
                    txtWarning.setText("Fields cannot be left blank");
                    if (txtWarning.getVisibility() == View.GONE) {
                        txtWarning.setVisibility(View.VISIBLE);
                    }
                } else if (!password.equals(reenterPassword)) {
                    txtWarning.setText("Password do not match");
                    if (txtWarning.getVisibility() == View.GONE) {
                        txtWarning.setVisibility(View.VISIBLE);
                    }
                } else {
                    String result = uniqueUser(username, email);
                    if (result.equals("username")) {
                        txtWarning.setText("Username has already been taken");
                        if (txtWarning.getVisibility() == View.GONE) {
                            txtWarning.setVisibility(View.VISIBLE);
                        }
                    } else if (result.equals("email")) {
                        txtWarning.setText("Email already exists");
                        if (txtWarning.getVisibility() == View.GONE) {
                            txtWarning.setVisibility(View.VISIBLE);
                        }
                    } else if (result.equals("unique")) {
                        if (addUser(username, fullName, email, password)) {
                            Toast.makeText(RegisterActivity.this, "Registration success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.putExtra("admin_username", username);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterActivity.this, "An error has occurred, please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private String uniqueUser(String username, String email) {
        String result = "error";
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursorUsername = db.query("users", null, "username=?", new String[] {username}, null, null, null);
            Cursor cursorEmail = db.query("users", null, "email=?", new String[] {email}, null, null, null);
            if (cursorUsername != null && cursorEmail != null) {
                if (cursorUsername.getCount() > 0) {
                    result = "username";
                } else if (cursorEmail.getCount() > 0) {
                    result = "email";
                } else {
                    result = "unique";
                }

                cursorUsername.close();
                cursorEmail.close();
            }
            db.close();

        }catch(SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean addUser(String username, String fullname, String email, String password) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues newUser = new ContentValues();
            newUser.put("username", username);
            newUser.put("full_name", fullname);
            newUser.put("email", email);
            newUser.put("password", password);

            long result = db.insert("users", null, newUser);

            db.close();
            if (result == -1) {
                return false;
            } else return true;

    }

    private void initViews() {
        editTxtUsername = findViewById(R.id.editTxtUsername);
        editTxtFullName = findViewById(R.id.editTxtFullName);
        editTxtEmail = findViewById(R.id.editTxtEmail);
        editTxtPw = findViewById(R.id.editTxtPw);
        editTxtReenterPw = findViewById(R.id.editTxtReenterPw);

        txtWarning = findViewById(R.id.txtWarning);

        btnRegister = findViewById(R.id.btnRegister);
    }
}