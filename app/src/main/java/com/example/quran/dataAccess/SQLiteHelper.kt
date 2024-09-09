package com.example.quran.dataAccess

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.quran.models.Bookmark
import com.example.quran.models.Surah
import com.example.quran.models.Verse

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Quran.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_SURAH = "Surah"
        private const val COLUMN_NUMBER = "number"
        private const val COLUMN_ARABIC_NAME = "arabic_name"
        private const val COLUMN_ENGLISH_NAME = "english_name"
        private const val COLUMN_ENGLISH_NAME_TRANSLATION = "english_name_translation"
        private const val COLUMN_VERSES = "number_of_ayahs"
        private const val COLUMN_REVELATION_TYPE = "revelation_type"

        private const val TABLE_VERSE = "Verse"
        private const val COLUMN_AYA_ID = "aya_id"
        private const val COLUMN_SURA_ID = "sura_id"
        private const val COLUMN_AYA_NO = "aya_no"
        private const val COLUMN_ARABIC_TEXT = "arabic_text"
        private const val COLUMN_FATEH_MUHAMMAD_JALANDHRIELD = "fateh_muhammad_jalandhrield"
        private const val COLUMN_MEHMOODUL_HASSAN = "mehmoodul_hassan"
        private const val COLUMN_DR_MOHSIN_KHAN = "dr_mohsin_khan"
        private const val COLUMN_MUFTI_TAQI_USMANI = "mufti_taqi_usmani"
        private const val COLUMN_RUKU_ID = "ruku_id"
        private const val COLUMN_PRUKU_ID = "pruku_id"
        private const val COLUMN_PARA_ID = "para_id"
        private const val COLUMN_P_AYA_ID = "p_aya_id"
        private const val COLUMN_TOTAL_RUKU = "total_ruku"

        private const val TABLE_BOOKMARKS = "Bookmarks"
        private const val COLUMN_AYAT_ID = "aya_id"
        private const val COLUMN_BOOKMARK_SURA_ID = "sura_id"
        private const val COLUMN_BOOKMARK_AYA_NO = "aya_no"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createSurahTable = ("CREATE TABLE $TABLE_SURAH (" +
                "$COLUMN_NUMBER INTEGER PRIMARY KEY," +
                "$COLUMN_ARABIC_NAME TEXT," +
                "$COLUMN_ENGLISH_NAME TEXT," +
                "$COLUMN_ENGLISH_NAME_TRANSLATION TEXT," +
                "$COLUMN_VERSES INTEGER," +
                "$COLUMN_REVELATION_TYPE TEXT)")

        val createVerseTable = ("CREATE TABLE $TABLE_VERSE (" +
                "$COLUMN_AYA_ID INTEGER PRIMARY KEY," +
                "$COLUMN_SURA_ID INTEGER," +
                "$COLUMN_AYA_NO INTEGER," +
                "$COLUMN_ARABIC_TEXT TEXT," +
                "$COLUMN_FATEH_MUHAMMAD_JALANDHRIELD TEXT," +
                "$COLUMN_MEHMOODUL_HASSAN TEXT," +
                "$COLUMN_DR_MOHSIN_KHAN TEXT," +
                "$COLUMN_MUFTI_TAQI_USMANI TEXT," +
                "$COLUMN_RUKU_ID INTEGER," +
                "$COLUMN_PRUKU_ID INTEGER," +
                "$COLUMN_PARA_ID INTEGER," +
                "$COLUMN_P_AYA_ID INTEGER," +
                "$COLUMN_TOTAL_RUKU INTEGER)")

        val createBookmarksTable = ("CREATE TABLE $TABLE_BOOKMARKS (" +
                "$COLUMN_AYAT_ID INTEGER PRIMARY KEY," +
                "$COLUMN_BOOKMARK_SURA_ID INTEGER," +
                "$COLUMN_BOOKMARK_AYA_NO INTEGER)")

        db?.execSQL(createSurahTable)
        db?.execSQL(createVerseTable)
        db?.execSQL(createBookmarksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SURAH")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_VERSE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKMARKS")
        onCreate(db)
    }

    fun insertSurahs(surahs: List<Surah>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (surah in surahs) {
                val contentValues = ContentValues().apply {
                    put(COLUMN_NUMBER, surah.number)
                    put(COLUMN_ARABIC_NAME, surah.name)
                    put(COLUMN_ENGLISH_NAME, surah.englishName)
                    put(COLUMN_VERSES, surah.numberOfAyahs)
                    put(COLUMN_ENGLISH_NAME_TRANSLATION, surah.englishNameTranslation)
                    put(COLUMN_REVELATION_TYPE, surah.revelationType)
                }
                db.insert(TABLE_SURAH, null, contentValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getSurahs(): List<Surah> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SURAH", null)
        val surahs = mutableListOf<Surah>()

        while (cursor.moveToNext()) {
            val number = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NUMBER))
            val arabicName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARABIC_NAME))
            val englishName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_NAME))
            val verses = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VERSES))
            val englishNameTranslation =
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_NAME_TRANSLATION))
            val revelationType =
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REVELATION_TYPE))
            surahs.add(
                Surah(
                    number,
                    arabicName,
                    englishName,
                    englishNameTranslation,
                    verses,
                    revelationType
                )
            )
            //surahs.sortBy { it.number }
        }
        cursor.close()
        db.close()
        return surahs
    }

    fun getSurah(surahNumber: Int) : Surah? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SURAH WHERE $COLUMN_NUMBER = ?", arrayOf(surahNumber.toString()))

        val surah = if (cursor.moveToFirst()) {
            val arabicName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARABIC_NAME))
            val englishName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_NAME))
            val verses = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VERSES))
            val englishNameTranslation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_NAME_TRANSLATION))
            val revelationType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REVELATION_TYPE))
            Surah(surahNumber, arabicName, englishName, englishNameTranslation, verses, revelationType)
        } else {
            null
        }
        cursor.close()
        db.close()
        return surah
    }

    fun deleteSurahs() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_SURAH")
        db.close()
    }

    fun insertVerses(verses: List<Verse>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (verse in verses) {
                val contentValues = ContentValues().apply {
                    put(COLUMN_AYA_ID, verse.ayaID)
                    put(COLUMN_SURA_ID, verse.suraID)
                    put(COLUMN_AYA_NO, verse.ayaNo)
                    put(COLUMN_ARABIC_TEXT, verse.arabicText)
                    put(COLUMN_FATEH_MUHAMMAD_JALANDHRIELD, verse.fatehMuhammadJalandhrield)
                    put(COLUMN_MEHMOODUL_HASSAN, verse.mehmoodulHassan)
                    put(COLUMN_DR_MOHSIN_KHAN, verse.drMohsinKhan)
                    put(COLUMN_MUFTI_TAQI_USMANI, verse.muftiTaqiUsmani)
                    put(COLUMN_RUKU_ID, verse.rakuID)
                    put(COLUMN_PRUKU_ID, verse.pRakuID)
                    put(COLUMN_PARA_ID, verse.paraID)
                    put(COLUMN_P_AYA_ID, verse.pAyatID)
                    put(COLUMN_TOTAL_RUKU, verse.totalRukuCount)
                }
                db.insert(TABLE_VERSE, null, contentValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()

        }
    }

    fun getVerses(surahNumber: Int): List<Verse> {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_VERSE WHERE $COLUMN_SURA_ID = ?",
            arrayOf(surahNumber.toString())
        )
        val verses = mutableListOf<Verse>()
        while (cursor.moveToNext()) {
            val verse = Verse(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AYA_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SURA_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AYA_NO)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARABIC_TEXT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FATEH_MUHAMMAD_JALANDHRIELD)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEHMOODUL_HASSAN)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DR_MOHSIN_KHAN)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MUFTI_TAQI_USMANI)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RUKU_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRUKU_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PARA_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_P_AYA_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_RUKU))
            )
            verses.add(verse)
        }
        cursor.close()
        db.close()
        return verses
    }

    fun deleteVerses() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_VERSE")
        db.close()
    }

    // Create: Insert a new bookmark using transactions
    fun insertBookmark(bookmark: Bookmark) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val contentValues = ContentValues().apply {
                put(SQLiteHelper.COLUMN_AYA_ID, bookmark.ayaID)
                put(SQLiteHelper.COLUMN_BOOKMARK_SURA_ID, bookmark.suraID)
                put(SQLiteHelper.COLUMN_BOOKMARK_AYA_NO, bookmark.ayaNo)
            }

            db.insert(SQLiteHelper.TABLE_BOOKMARKS, null, contentValues)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error inserting bookmark", e)
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // Read: Get all bookmarks
    fun getAllBookmarks(): List<Bookmark> {
        val bookmarkList = mutableListOf<Bookmark>()
        val selectQuery = "SELECT * FROM $TABLE_BOOKMARKS"
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val ayaID = it.getInt(it.getColumnIndexOrThrow(COLUMN_AYA_ID))
                    val suraID = it.getInt(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_SURA_ID))
                    val ayaNo = it.getInt(it.getColumnIndexOrThrow(COLUMN_BOOKMARK_AYA_NO))

                    val bookmark = Bookmark(ayaID, suraID, ayaNo)
                    bookmarkList.add(bookmark)
                } while (it.moveToNext())
            }
        }

        db.close()
        return bookmarkList
    }

    // Update: Update an existing bookmark using transactions
    fun updateBookmark(bookmark: Bookmark): Boolean {
        val db = this.writableDatabase
        var success = false
        db.beginTransaction()
        try {
            val contentValues = ContentValues().apply {
                put(COLUMN_BOOKMARK_AYA_NO, bookmark.ayaNo)
            }

            val result = db.update(TABLE_BOOKMARKS, contentValues, "${COLUMN_AYAT_ID} = ?", arrayOf(bookmark.ayaID.toString()))
            if (result > 0) {
                success = true
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error updating bookmark", e)
        } finally {
            db.endTransaction()
            db.close()
        }
        return success
    }

    // Delete: Delete a bookmark by ID
    fun deleteBookmark(ayaID: Int): Boolean {
        val db = this.writableDatabase
        var success = false

        db.beginTransaction()

        try {
            val result = db.delete(TABLE_BOOKMARKS, "${COLUMN_AYAT_ID} = ?", arrayOf(ayaID.toString()))
            if (result > 0) {
                success = true
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error deleting bookmark", e)
        } finally {
            db.endTransaction()
            db.close()
        }
        return success
    }
}
