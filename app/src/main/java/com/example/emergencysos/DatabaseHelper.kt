package com.example.emergencysos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    companion object {

        private const val DATABASE_NAME = "ContactDB"

        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "contacts"

        private const val COLUMN_ID = "id"

        private const val COLUMN_NAME = "name"

        private const val COLUMN_PHONE = "phone"

        private const val COLUMN_EMAIL = "email"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val createTable = """
            
            CREATE TABLE $TABLE_NAME (
            
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            
            $COLUMN_NAME TEXT,
            
            $COLUMN_PHONE TEXT,
            
            $COLUMN_EMAIL TEXT
            
            )
            
        """.trimIndent()

        db?.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {

        db?.execSQL(
            "DROP TABLE IF EXISTS $TABLE_NAME"
        )

        onCreate(db)
    }

    fun insertContact(
        name: String,
        phoneNumber: String,
        email: String
    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {

            put(COLUMN_NAME, name)

            put(COLUMN_PHONE, phoneNumber)

            put(COLUMN_EMAIL, email)
        }

        val result =
            db.insert(
                TABLE_NAME,
                null,
                values
            )

        db.close()

        return result != -1L
    }

    fun getAllContacts(): ArrayList<Contact> {

        val contacts = ArrayList<Contact>()

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME",
            null
        )

        if (cursor.moveToFirst()) {

            do {

                val id =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_ID
                        )
                    )

                val name =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_NAME
                        )
                    )

                val phone =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_PHONE
                        )
                    )

                val email =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_EMAIL
                        )
                    )

                contacts.add(
                    Contact(
                        id = id,
                        name = name,
                        phone = phone,
                        email = email
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor.close()

        db.close()

        return contacts
    }

    fun deleteContact(
        id: String
    ): Boolean {

        val db = writableDatabase

        val rowsDeleted =
            db.delete(
                TABLE_NAME,
                "$COLUMN_ID = ?",
                arrayOf(id)
            )

        db.close()

        return rowsDeleted > 0
    }
}
