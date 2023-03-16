package com.example.basicchatapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var buttonSignup: Button
    private lateinit var showPasswordSwitch: Switch
    private lateinit var mDatabaseRef: DatabaseReference

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        edtName = findViewById(R.id.edt_name)
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        buttonSignup = findViewById(R.id.button_signup)
        showPasswordSwitch = findViewById(R.id.show_password_switch)

        mAuth = FirebaseAuth.getInstance()

        buttonSignup.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            signUp(name, email, password)
        }

        showPasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                edtPassword.transformationMethod = null // making the password visible
            }
            else {
                edtPassword.transformationMethod = PasswordTransformationMethod() // making the password invisible
            }
        }
    }

    private fun signUp(name: String, email: String, password: String) {
        // the process for creating a user
        mAuth.createUserWithEmailAndPassword(email, password) // referenced from https://firebase.google.com/docs/auth/android/password-auth
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, jump to the home activity AND add the new user to our database
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!) // the !! makes the datatype NULL safe
                    val intent = Intent(this@SignUp, MainActivity::class.java)
                    finish() // Finishes and destroys the SignUp activity
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@SignUp, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        // Initializing the real-time database with Firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference()
        mDatabaseRef.child("user").child(uid).setValue(User(name, email, uid)) // Adds a node to the database that will have a child of the unique user identification
    }
}