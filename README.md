# Quran App

This Quran app is built using **Kotlin** for Android, providing users with a rich experience to read, listen, and learn from the Quran. 
It integrates current **Islamic date**, daily Ayat, customizable settings, and a smooth UI for reading the Quran **Surah-wise**, **Parah-wise**, and also includes **Tajweed** rules for better recitation.

## Features

### Home Page:
- **Hijri & Gregorian Dates**: Displays both Hijri and Gregorian dates, fetched through a custom-built API published on **Azure**.
- **Ayat of the Day**: Showcases a random Quranic verse with translations in **English** and **Urdu**, giving reciters a new verse to learn every day.

### Settings:
- **Language Options**: Users can choose to display the Quran in **Arabic**, **English**, or **Urdu**.
- **Font Customization**: Font sizes for Arabic, English, and Urdu can be adjusted using seek bars.
- **Reciter Selection**: Users can choose their favorite reciter to hear Quranic verses.
- **Set as Default**: Allows users to save their preferences.

### Quran Sections:
- **Surah-Wise Reading**: Displays all 114 Surahs with search options by **Surah name** or **Surah number**. 
  - Makki and Madni Surahs are visually distinguished.
  - **Audio Recitation**: Each Ayat has a play button for recitation with audio.
  - **Ayat Actions**: Users can **copy** an Ayat, **bookmark** it, or send it to bookmarks for later access.
  - **Search by Ayat and Ruku**: Enhanced search functionality.
  - **Last Read Option**: Direct navigation to the last-read Ayat.

- **Parah-Wise Reading**: Quran is displayed based on its 30 Parahs with similar layouts and functionalities as the Surah-wise section.

### Tajweed Section:
- **Tajweed Rules**: Provides a guide to Quranic recitation with **Tajweed rules** by displaying PDF converted images of the Quran.
  - Search by **Surah number**, **page number**, or **Parah number**.

### Bookmark Section:
- **Bookmarks**: Displays all saved bookmarks. Users can navigate back to the saved verses.

### Offline Access:
- **Offline Reading**: Users can access the complete Quran and their bookmarks even when offline, ensuring a seamless reading experience without an internet connection.

## API Integration
This app interacts with custom **web APIs** developed using **.NET C#** and published on **Azure** for:
- Fetching Hijri dates.
- Retrieving Quranic verses and their audio.
- Managing user settings and reciter options.

## Technologies Used
- **Kotlin**: The core programming language for Android app development.
- **.NET C#**: Used for developing the backend APIs.
- **Azure**: APIs are deployed and hosted on Microsoft Azure.
- **RecyclerView**: Efficiently displays large lists of Surahs, Ayats, and Tajweed pages.
- **Retrofit & Gson**: For API calls and JSON parsing in Kotlin.

## How to Run the Project
1. Clone the repository.
2. Open the project in **Android Studio**.
3. Ensure you have an active **internet connection** for API calls.
4. Build and run the app on an Android device or emulator.

## Future Improvements
- Add more reciter options.
- Implement advanced search filters.
- Add more language translations.
- Improve UI/UX for better user experience.

## Contributing
Feel free to open issues or contribute to the project by submitting pull requests.

## License
This project is licensed under the MIT License.
