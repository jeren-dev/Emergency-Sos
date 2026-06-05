package com.example.emergencysos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.emergencysos.DatabaseHelper

class ShowContect : ComponentActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        databaseHelper = DatabaseHelper(this)

        val contacts =
            databaseHelper.getAllContacts()

        setContent {

            ContactsScreen(
                contactsList = contacts,
                databaseHelper = databaseHelper
            )
        }
    }
}