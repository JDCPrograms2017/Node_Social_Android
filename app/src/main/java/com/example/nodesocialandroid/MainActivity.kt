package com.example.nodesocialandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userList = ArrayList()
        adapter = UserAdapter(this, userList)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        userRecyclerView = findViewById(R.id.userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter // the adapter that we created earlier for recycling User views

        // Reading the values from the Firebase Database
        //NOTE: This method from the tutorial displays all existing users in the system.
        //TODO: Change this to only include users that the logged-in user has ADDED to their profile
        mDbRef.child("user").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear the previously existing userList
                userList.clear()
                // snapshot is a particular snapshot of the data
                for (postSnapshot in snapshot.children) {
                    // create a current user object and add it to our userList
                    val currentUser = postSnapshot.getValue(User::class.java)

                    if(mAuth.currentUser?.uid != currentUser?.uid) { // if the UID of the logged-in User matches that of the one we want to add, don't add them.
                        userList.add(currentUser!!) // make sure it's NULL safe
                    }
                    //println("Added a user to the userList!");
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }) // the path string needs to be the same as the node we are accessing

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu) // We've inflated our menu resource
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            // logic for the logout process
            mAuth.signOut()

            val intent = Intent(this@MainActivity, Login::class.java)
            finish()
            startActivity(intent) // will bring the user back to the login screen when they opt to log out.
            return true
        }
        return true
    }
}