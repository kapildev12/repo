package com.example.vishal.ambulance_surveillance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.vishal.ambulance_surveillance.Common.Common;
import com.example.vishal.ambulance_surveillance.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button nbtn_signIn;
    private EditText nPhone;
    private EditText nPassword;
    private TextView forget;


    //
    RelativeLayout rt;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference usersRef;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_registration);
        findViewById(R.id.buttonUp).setOnClickListener(this);
        findViewById(R.id.btn_signIn).setOnClickListener(this);
        forget = (TextView) findViewById(R.id.buttonfor);
        forget.setVisibility(View.INVISIBLE);
        forget.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Intent intent = new Intent(LoginRegistrationActivity.this, Forgot.class);
                startActivity(intent);
            }
        });

        //init paperDb
        Paper.init(this);

        //init firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        usersRef = db.getReference(Common.user_drivers);
        // init buttons
        nPhone = (EditText) findViewById(R.id.phone_text);

        nPassword = (EditText) findViewById(R.id.password_text);
        nbtn_signIn = (Button) findViewById(R.id.btn_signIn);


        //Auto signin
        String user = Paper.book().read(Common.UserField);
        String pasw = Paper.book().read(Common.PasField);


        if (user != null && pasw != null) {
            if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pasw)) {
                autoLogin(user, pasw);
            }

        }

        nbtn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(nPhone.getText().toString())) {
                    Toast.makeText(LoginRegistrationActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Common.isEmailValid(nPhone.getText().toString())) {
                    Toast.makeText(LoginRegistrationActivity.this, "Invalid E-mail Format", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(nPassword.toString())) {
                    Toast.makeText(LoginRegistrationActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                final ProgressDialog mDialog = new ProgressDialog(LoginRegistrationActivity.this);
                mDialog.setMessage("signing in....");
                mDialog.show();

                auth.signInWithEmailAndPassword(nPhone.getText().toString(), nPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        FirebaseDatabase.getInstance().getReference(Common.user_drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // saving remember value
                                Paper.book().write(Common.UserField, nPhone.getText().toString());
                                Paper.book().write(Common.PasField, nPassword.getText().toString());

                                Common.currentUser = dataSnapshot.getValue(User.class);
                                mDialog.dismiss();
                                startActivity(new Intent(LoginRegistrationActivity.this, Navigation.class));
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                mDialog.dismiss();
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginRegistrationActivity.this, "Failed to Sign In" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }
                });
            }
        });


    }

    private void autoLogin(String user, String pasw) {

        final ProgressDialog mDialog = new ProgressDialog(LoginRegistrationActivity.this);
        mDialog.setMessage("signing in....");
        mDialog.show();
        auth.signInWithEmailAndPassword(user, pasw).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseDatabase.getInstance().getReference(Common.user_drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDialog.dismiss();
                        Common.currentUser = dataSnapshot.getValue(User.class);
                        startActivity(new Intent(LoginRegistrationActivity.this, Navigation.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mDialog.dismiss();
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginRegistrationActivity.this, "Failed to Sign In" + e.getMessage(), Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonUp:
                startActivity(new Intent(this, Registration.class));
                break;

        }
    }
}





