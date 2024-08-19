package com.example.task1.Authentication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.task1.Database.DatabaseHelper;
import com.example.task1.ExpenseManagerActivity;
import com.example.task1.Model.User;
import com.example.task1.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView ToRegister;
    private Button btnLogin;
    private ImageButton btnBack; // Back button declaration
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        ToRegister = findViewById(R.id.tvSignUpLink);
        btnBack = findViewById(R.id.btnBack); // Initialize the back button

        databaseHelper = new DatabaseHelper(this);

        // Check if session is active, then navigate to ExpenseManagerActivity
        if (databaseHelper.isSessionActive()) {
            User user = databaseHelper.getSessionUser();
            navigateToExpenseManager(user.getId());
        }

        // Set up the back button listener
        btnBack.setOnClickListener(v -> {
            // Navigate back to the login/signup screen
            Intent intent = new Intent(LoginActivity.this, LoginSignupActivity.class);
            startActivity(intent);
            finish(); // Optional: closes the current activity
        });

        // Set up the login button listener
        btnLogin.setOnClickListener(v -> loginUser());

        // Set up the sign-up link listener
        ToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String emailOrPhone = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (emailOrPhone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USER_NAME, DatabaseHelper.COLUMN_USER_EMAIL, DatabaseHelper.COLUMN_USER_PHONE},
                "(" + DatabaseHelper.COLUMN_USER_EMAIL + "=? OR " + DatabaseHelper.COLUMN_USER_PHONE + "=?) AND " + DatabaseHelper.COLUMN_USER_PASSWORD + "=?",
                new String[]{emailOrPhone, emailOrPhone, password},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
            String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL));
            String phone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PHONE));
            cursor.close();

            // Save user info into the session
            databaseHelper.saveSession(userId, name, email, phone, password);
            // Navigate to ExpenseManagerActivity
            navigateToExpenseManager(userId);
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();  // Ensure cursor is closed if it is not null
        }
    }

    private void navigateToExpenseManager(int userId) {
        Intent intent = new Intent(this, ExpenseManagerActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}
