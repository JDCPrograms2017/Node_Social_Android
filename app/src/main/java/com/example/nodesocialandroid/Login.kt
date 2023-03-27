package com.example.nodesocialandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonSignup: Button
    private lateinit var errorMessageDNE: TextView
    private lateinit var errorMessageInvalidPar: TextView

    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance() // initializing our connection to FireBase Auth

        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        buttonLogin = findViewById(R.id.button_login)
        buttonSignup = findViewById(R.id.button_signup)
        errorMessageDNE = findViewById(R.id.error_message_DNE)
        errorMessageInvalidPar = findViewById(R.id.error_message_invalid_params)

        errorMessageDNE.visibility = View.INVISIBLE // start the error message out by disappearing
        errorMessageInvalidPar.visibility = View.INVISIBLE // start the error message out by disappearing

        // NOTE: View.INVISIBLE makes the element disappear but still take up space. View.GONE makes it disappear and not take up space


        buttonSignup.setOnClickListener {
            val intent = Intent(this, SignUp::class.java) // this is how we switch to the sign up screen / activity by pressing the button
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            var email = edtEmail.text.toString()
            var password = edtPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) { // testing for empty or NULL strings
                if (errorMessageDNE.visibility == View.VISIBLE) {
                    errorMessageDNE.visibility = View.INVISIBLE
                }
                errorMessageInvalidPar.visibility = View.VISIBLE
            } else {
                login(email, password)
            }
        }


    }

    private fun login(email: String, password: String) {
        // the process of logging in an existing user
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, jump to the main page
                    val intent = Intent(this@Login, MainActivity::class.java)
                    finish() // finishes and destroys the Login activity (so that if we hit the "back" button on our phone, it won't mess stuff up)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    //Toast.makeText(this@Login, "User does not exist!", Toast.LENGTH_SHORT).show()
                    if (errorMessageInvalidPar.visibility == View.VISIBLE) {
                        errorMessageInvalidPar.visibility = View.INVISIBLE
                    }
                    errorMessageDNE.visibility = View.VISIBLE
                }
            }
    }
}