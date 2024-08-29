package com.example.quran

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Load previously saved settings
        val switchNotifications: Switch = findViewById(R.id.switch_notifications)
        val radioGroupTheme: RadioGroup = findViewById(R.id.radio_group_theme)
        val editTextUsername: EditText = findViewById(R.id.edit_text_username)
        val buttonSave: Button = findViewById(R.id.button_save)

        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        val themeSelection = sharedPreferences.getString("theme", "Light")
        val username = sharedPreferences.getString("username", "")

        switchNotifications.isChecked = notificationsEnabled
        when (themeSelection) {
            "Dark" -> radioGroupTheme.check(R.id.radio_dark)
            else -> radioGroupTheme.check(R.id.radio_light)
        }
        editTextUsername.setText(username)

        buttonSave.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("notifications_enabled", switchNotifications.isChecked)
            val selectedThemeId = radioGroupTheme.checkedRadioButtonId
            val selectedTheme = if (selectedThemeId == R.id.radio_dark) "Dark" else "Light"
            editor.putString("theme", selectedTheme)
            editor.putString("username", editTextUsername.text.toString())
            editor.apply()

            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
