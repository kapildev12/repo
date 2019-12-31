package com.example.vishal.ambulance_surveillance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.widget.Toast;

import com.example.vishal.ambulance_surveillance.Common.Common;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;

public class ChangePassword extends AppCompatActivity {

    private AppCompatEditText etCurrentPass;
    private AppCompatEditText etNewPass;
    private AppCompatEditText etConfirmPass;
    private AppCompatButton btChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etCurrentPass = (AppCompatEditText) findViewById(R.id.etCurrentPass);
        etNewPass = (AppCompatEditText) findViewById(R.id.etNewPass);
        etConfirmPass = (AppCompatEditText) findViewById(R.id.etConfirmPass);
        btChange = (AppCompatButton) findViewById(R.id.btChange);

        btChange.setOnClickListener(view -> {
            if (!etCurrentPass.getText().toString().equals(Paper.book().read(Common.PasField))) {
                showToast("Incorrect current password");
            } else if (!etNewPass.getText().toString().equals(etConfirmPass.getText().toString())) {
                showToast("New pass and confirm pass does not match");
            } else
                changePass(etNewPass.getText().toString());
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void changePass(String newPass) {
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Changing Password...");
        mDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider.getCredential(Common.currentUser.getEmail(), Paper.book().read(Common.PasField));

// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPass).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        showToast("Password updated");
                        Paper.book().write(Common.PasField, newPass);
                        Common.currentUser.setPassword(newPass);
                        FirebaseDatabase.getInstance().getReference(Common.user_drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("password").setValue(newPass);
                        startActivity(new Intent(this, Navigation.class));
                    } else {
                        showToast("Error password not updated");
                    }
                    mDialog.dismiss();
                });
            } else {
                showToast("Error auth failed");
                mDialog.dismiss();
            }
        });
    }
}
