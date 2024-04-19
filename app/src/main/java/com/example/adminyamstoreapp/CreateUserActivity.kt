package com.example.adminyamstoreapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.adminyamstoreapp.databinding.ActivityCreateUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateUserActivity : AppCompatActivity() {
    private val binding: ActivityCreateUserBinding by lazy {
        ActivityCreateUserBinding.inflate(layoutInflater)
    }

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        // Add click listener for createUserButton
        binding.createUserButton.setOnClickListener {
            val name = binding.Name.text.toString().trim()
            val email = binding.password.text.toString().trim() // Using email field for email, can be changed
            val password = binding.passsword.text.toString().trim()

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        // Store additional user information in the Realtime Database
                        user?.let {
                            val uid = user.uid
                            val userRef = database.getReference("users").child(uid)
                            // Create a HashMap to store user data
                            val userData = hashMapOf(
                                "name" to name,
                                "email" to email
                                // Add additional fields as needed
                            )
                            // Set the user data in the database
                            userRef.setValue(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "New user has been created successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this@CreateUserActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()

                                }
                                .addOnFailureListener { e ->

                                    Toast.makeText(
                                        this,
                                        "Failed to store user data: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
