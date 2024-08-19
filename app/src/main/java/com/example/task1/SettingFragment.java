package com.example.task1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.task1.Authentication.LoginActivity;
import com.example.task1.Database.DatabaseHelper;
import com.example.task1.Model.User;
import com.example.task1.R;

public class SettingFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private Button btnEdit, btnLogout;
    private DatabaseHelper databaseHelper;
    private User sessionUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initViews(view);
        setupListeners();
        loadUserInfo();

        return view;
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnLogout = view.findViewById(R.id.btn_logout);
        databaseHelper = new DatabaseHelper(getActivity());
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> showEditUserDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void loadUserInfo() {
        sessionUser = databaseHelper.getSessionUser();
        if (sessionUser != null) {
            tvName.setText("Name: " + sessionUser.getName());
            tvEmail.setText("Email: " + sessionUser.getEmail());
            tvPhone.setText("Phone: " + sessionUser.getPhone());
        } else {
            showToast("Failed to load user info");
        }
    }

    private void showEditUserDialog() {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_user, null);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .create();

        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_password);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);

        setInitialValues(etName, etEmail, etPhone);

        btnSubmit.setOnClickListener(v -> {
            String newName = etName.getText().toString();
            String newEmail = etEmail.getText().toString();
            String newPhone = etPhone.getText().toString();
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmNewPassword = etConfirmNewPassword.getText().toString();

            // Validate fields (password fields are optional)
            if (validateFields(newName, newEmail, newPhone)) {
                if (validatePasswordChange(currentPassword, newPassword, confirmNewPassword)) {
                    updateUser(newName, newEmail, newPhone, newPassword);
                    updateUI(newName, newEmail, newPhone);
                    showToast("User updated successfully.");
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }


    private void setInitialValues(EditText etName, EditText etEmail, EditText etPhone) {
        etName.setText(sessionUser.getName());
        etEmail.setText(sessionUser.getEmail());
        etPhone.setText(sessionUser.getPhone());
    }

    private boolean validateFields(String name, String email, String phone) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            showToast("Name must be filled");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            showToast("Email must be filled");
            isValid = false;
        }
        if (TextUtils.isEmpty(phone)) {
            showToast("Phone number must be filled");
            isValid = false;
        }

        return isValid;
    }


    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmNewPassword) {
        if (!TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmNewPassword)) {
            // Validate current password
            if (TextUtils.isEmpty(currentPassword) || !currentPassword.equals(sessionUser.getPassword())) {
                showToast("Current password is incorrect or not provided");
                return false;
            }
            // Validate new password
            if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
                showToast("New password must be at least 6 characters long");
                return false;
            }
            // Check new password match
            if (!newPassword.equals(confirmNewPassword)) {
                showToast("New passwords do not match");
                return false;
            }
        }
        return true;
    }


    private void updateUser(String newName, String newEmail, String newPhone, String newPassword) {
        User updatedUser = new User(
                sessionUser.getId(),
                TextUtils.isEmpty(newName) ? sessionUser.getName() : newName,
                TextUtils.isEmpty(newEmail) ? sessionUser.getEmail() : newEmail,
                TextUtils.isEmpty(newPhone) ? sessionUser.getPhone() : newPhone,
                TextUtils.isEmpty(newPassword) ? sessionUser.getPassword() : newPassword
        );
        databaseHelper.updateUser(updatedUser);
    }


    private void updateUI(String newName, String newEmail, String newPhone) {
        tvName.setText("Name: " + newName);
        tvEmail.setText("Email: " + newEmail);
        tvPhone.setText("Phone: " + newPhone);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    databaseHelper.deleteSession(); // Remove session
                    showToast("Logged out successfully");
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}