package com.example.nodesocialandroid

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UserProfile : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        val txtName = findViewById<TextView>(R.id.username)
        val currentUserUID = mAuth.currentUser?.uid
        // Initially updating the user's profile name from the default
        txtName.text = mDbRef.child("user").child(currentUserUID!!).child("name").get().toString()

        //mDbRef.child(currentUserUID!!).child("name").setValue(null) // UPDATE THIS WHEN YOU MAKE AN EDITABLE TEXT BOX

        // this will update the user's name on their profile from the Realtime Database info if they choose to make a new username
        mDbRef.child("user").child(currentUserUID!!).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                txtName.text = snapshot.child("name").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", error.toException())
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu) // We've inflated our menu resource
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            // logic for the logout process
            mAuth.signOut()

            val intent = Intent(this@UserProfile, Login::class.java)
            finish() // this will end the current activity
            startActivity(intent) // will bring the user back to the login screen when they opt to log out.
            return true
        }
        return true
    }
}