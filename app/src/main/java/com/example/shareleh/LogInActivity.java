package com.example.shareleh;

import androidx.appcompat.app.AppCompatActivity;

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

public class LogInActivity extends AppCompatActivity {

    private EditText editTxtLoginEmail, editTxtLoginPw;
    private TextView txtWarning;
    private Button btnLogin, btnRegister;
    DatabaseHelper dbHelper = new DatabaseHelper(LogInActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        initViews();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTxtLoginEmail.getText().toString();
                String password = editTxtLoginPw.getText().toString();
                
                if (email.equals("") || password.equals("")) {
                    txtWarning.setText("Fields cannot be left blank");
                    if (txtWarning.getVisibility() == View.GONE) {
                        txtWarning.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (loginSuccess(email, password)) {
                        String username = getUsername(email);
                        if (!username.equals("")) {
                            Toast.makeText(LogInActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                            intent.putExtra("admin_username", username);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LogInActivity.this, "An error has occured, please try again!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        txtWarning.setText("Incorrect email address/password, please try again!");
                        if (txtWarning.getVisibility() == View.GONE) {
                            txtWarning.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean loginSuccess(String email, String password) {
        boolean isValid = false;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("users", null, "email=? AND password=?", new String[] {email, password}, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    isValid = true;
                }

                cursor.close();
                db.close();
            } else {
                db.close();
            }

        }catch(SQLException e) {
            e.printStackTrace();
        }

        return isValid;
    }

    private String getUsername(String email) {
        String username ="";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "email=?", new String[] {email}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int usernameIndex = cursor.getColumnIndex("username");
                username = cursor.getString(usernameIndex);
            }

            cursor.close();
        }
        db.close();
        
        return username;
    }

    private void initViews () {
        editTxtLoginEmail = findViewById(R.id.editTxtLoginEmail);
        editTxtLoginPw = findViewById(R.id.editTxtLoginPw);
        txtWarning = findViewById(R.id.txtWarning);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }
}