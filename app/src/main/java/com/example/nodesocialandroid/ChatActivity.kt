package com.example.nodesocialandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

// This will be used when establishing a chatroom between two "friended" users.
class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }
}