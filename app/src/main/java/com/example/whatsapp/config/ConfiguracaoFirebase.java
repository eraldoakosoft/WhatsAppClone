package com.example.whatsapp.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {
    private static DatabaseReference databaseReference;
    private static FirebaseAuth auth;


    //RETORNA A INSTANCIA DO FITEBASEDATABASE
    public static DatabaseReference getDatabaseReference(){
        if ( databaseReference == null ){
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    //RETORNA A INSTANCIA DO FIREBASEAUTH
    public static FirebaseAuth getFireBaseAutenticacao(){
        if( auth == null ){
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

}
