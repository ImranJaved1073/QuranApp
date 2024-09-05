package com.example.quran

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchArabic: SwitchCompat
    private lateinit var switchUrduTranslation: SwitchCompat
    private lateinit var switchEnglishTranslation: SwitchCompat
    private lateinit var seekBarArabicFontSize: SeekBar
    private lateinit var seekBarTranslationFontSize: SeekBar
    private lateinit var seekBarEnglishTranslationFontSize: SeekBar
    private lateinit var buttonSetDefaults: Button
    private lateinit var preferences: SharedPreferences
    private lateinit var arabicPreviewTextView: TextView
    private lateinit var translationPreviewTextView: TextView
    private lateinit var englishTranslationPreviewTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Settings"

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        switchArabic = findViewById(R.id.switchArabic)
        switchUrduTranslation = findViewById(R.id.switchUrduTranslation)
        switchEnglishTranslation = findViewById(R.id.switchEnglishTranslation)
        seekBarArabicFontSize = findViewById(R.id.seekBarArabicFontSize)
        seekBarEnglishTranslationFontSize = findViewById(R.id.seekBarEnglishTranslationFontSize)
        seekBarTranslationFontSize = findViewById(R.id.seekBarTranslationFontSize)
        buttonSetDefaults = findViewById(R.id.buttonSetDefault)

        // Preview TextViews
        arabicPreviewTextView = findViewById(R.id.arabicPreviewFontSize) // Assume this exists in XML
        translationPreviewTextView = findViewById(R.id.previewTranslationFontSize)
        englishTranslationPreviewTextView = findViewById(R.id.previewEnglishTranslationFontSize)

        // Set SeekBar limits for font sizes
        seekBarArabicFontSize.max = 20 // Arabic font range: 14sp to 30sp
        seekBarTranslationFontSize.max = 20 // Translation font range: 14sp to 30sp
        seekBarEnglishTranslationFontSize.max = 16 // Translation font range: 14sp to 30sp

        // Load saved preferences
        loadPreferences()

        buttonSetDefaults.setOnClickListener {
            preferences.edit().clear().apply()
            loadPreferences()
        }

        switchArabic.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("arabic_enabled", isChecked).apply()
        }

        switchUrduTranslation.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("translation_enabled", isChecked).apply()
        }

        switchEnglishTranslation.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("english_translation_enabled", isChecked).apply()
        }


        // Listen for SeekBar changes and update font sizes in real-time
        seekBarArabicFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = 16 + progress
                arabicPreviewTextView.textSize = fontSize.toFloat() // Update preview font size
                preferences.edit().putInt("arabic_font_size", fontSize).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarTranslationFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = 16 + progress
                translationPreviewTextView.textSize = fontSize.toFloat() // Update preview font size
                preferences.edit().putInt("translation_font_size", fontSize).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarEnglishTranslationFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontSize = 16 + progress
                englishTranslationPreviewTextView.textSize = fontSize.toFloat() // Update preview font size
                preferences.edit().putInt("english_translation_font_size", fontSize).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadPreferences() {
        switchArabic.isChecked = preferences.getBoolean("arabic_enabled", true)
        switchUrduTranslation.isChecked = preferences.getBoolean("translation_enabled", true)
        switchEnglishTranslation.isChecked = preferences.getBoolean("english_translation_enabled", true)

        // Default Arabic and translation font size to 18sp, corresponding to progress 4 (14sp + 4)
        val arabicFontSize = preferences.getInt("arabic_font_size", 22)
        seekBarArabicFontSize.progress = arabicFontSize - 16
        arabicPreviewTextView.textSize = arabicFontSize.toFloat() // Set initial preview font size

        val translationFontSize = preferences.getInt("translation_font_size", 22)
        seekBarTranslationFontSize.progress = translationFontSize - 16
        translationPreviewTextView.textSize = translationFontSize.toFloat() // Set initial preview font size

        val englishTranslationFontSize = preferences.getInt("english_translation_font_size", 18)
        seekBarEnglishTranslationFontSize.progress = englishTranslationFontSize - 16
        englishTranslationPreviewTextView.textSize = englishTranslationFontSize.toFloat() // Set initial preview font size
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
