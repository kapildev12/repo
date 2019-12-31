package com.example.vishal.ambulance_surveillance.Service;

import com.example.vishal.ambulance_surveillance.Common.Common;
import com.example.vishal.ambulance_surveillance.Models.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by sickbay on 3/28/2018.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        UpdateTokenToServer(refreshedToken);
    }

    private void UpdateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens= db.getReference(Common.token_tbl);

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser() !=null)
        {
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }
    }
}
