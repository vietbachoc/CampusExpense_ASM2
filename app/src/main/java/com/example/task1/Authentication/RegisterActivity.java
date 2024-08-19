package com.example.task1.Authentication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.task1.Database.DatabaseHelper;
import com.example.task1.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhoneNumber, etPassword;
    private Button btnSubmitRegister;
    private TextView tvLoginLink;
    private DatabaseHelper databaseHelper;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        etPhoneNumber = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        btnBack = findViewById(R.id.ivBack); // Find the back button

        databaseHelper = new DatabaseHelper(this);

        // Set the click listener for the back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Handle back button press
            }
        });

        // Add text change listeners
        etName.addTextChangedListener(new CustomTextWatcher(etName, 0)); // Non-empty name
        etEmail.addTextChangedListener(new CustomTextWatcher(etEmail, Patterns.EMAIL_ADDRESS, databaseHelper));
        etPhoneNumber.addTextChangedListener(new CustomTextWatcher(etPhoneNumber, Patterns.PHONE)); // Phone number pattern
        etPassword.addTextChangedListener(new CustomTextWatcher(etPassword, 5)); // Password length > 5

        // Set the click listener for the submit button
        btnSubmitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Set an OnClickListener for the login link
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmailUsed(email)) {
            Toast.makeText(this, "Email is already in use", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.PHONE.matcher(phone).matches()) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() <= 5) {
            Toast.makeText(this, "Password must be longer than 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert user into database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password); // Consider hashing the password

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        if (result != -1) {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailUsed(String email) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_EMAIL},
                DatabaseHelper.COLUMN_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        boolean emailExists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return emailExists;
    }

    private static class CustomTextWatcher implements TextWatcher {

        private final EditText editText;
        private final Object validationCriteria;
        private final DatabaseHelper databaseHelper;

        CustomTextWatcher(EditText editText, Object validationCriteria, DatabaseHelper databaseHelper) {
            this.editText = editText;
            this.validationCriteria = validationCriteria;
            this.databaseHelper = databaseHelper;
        }

        CustomTextWatcher(EditText editText, Object validationCriteria) {
            this(editText, validationCriteria, null);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            boolean isValid = false;

            if (validationCriteria instanceof java.util.regex.Pattern) {
                isValid = ((java.util.regex.Pattern) validationCriteria).matcher(s).matches();
                if (editText.getId() == R.id.etEmail && isValid) {
                    isValid = !isEmailUsed(s.toString());
                }
            } else if (validationCriteria instanceof Integer) {
                int minLength = (Integer) validationCriteria;
                isValid = s.length() > minLength;
            } else if (validationCriteria == null) {
                isValid = s.length() > 0; // Default non-empty check
            }

            updateIconBasedOnCondition(isValid);
        }

        private boolean isEmailUsed(String email) {
            if (databaseHelper == null) return false;
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USER_EMAIL},
                    DatabaseHelper.COLUMN_USER_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);

            boolean emailExists = cursor != null && cursor.moveToFirst();
            if (cursor != null) {
                cursor.close();
            }
            return emailExists;
        }

        private void updateIconBasedOnCondition(boolean isValid) {
            if (isValid) {
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.done_icon, 0);
            } else {
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.hilden_icon, 0);
            }
        }
    }
}
