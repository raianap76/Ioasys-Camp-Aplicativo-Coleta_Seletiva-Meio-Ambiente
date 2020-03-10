package com.ricardo.uber.config

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


 class ConfiguracaoFirebase { companion object {


     var database: DatabaseReference? = null
     var auth: FirebaseAuth? = null


     fun getFirebaseDatabase(): DatabaseReference {

         if (database == null) {
             database = FirebaseDatabase.getInstance().getReference()
         }
         return database as DatabaseReference
     }

     fun getFirebaseAutenticacao(): FirebaseAuth {

         if (auth == null) {

             auth = FirebaseAuth.getInstance()
         }
         return auth as FirebaseAuth
     }
 }
}