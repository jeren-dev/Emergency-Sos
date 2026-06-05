# 🚨 Emergency SOS

A modern Android safety application designed to provide quick emergency assistance by sending alerts and sharing location information with trusted contacts during critical situations.

## 📱 Features

### 🔴 Emergency SOS

Instantly trigger an SOS alert with a single tap during emergencies.

### 📍 Real-Time Location Sharing

Automatically fetches and shares the user's current GPS location with saved emergency contacts.

### 👥 Emergency Contact Management

Add, view, and manage trusted contacts for emergency communication.

### 🚨 Emergency Siren

Built-in emergency siren to attract attention and alert nearby people.

### ⚡ Fast & Reliable

Designed with a simple and responsive interface for quick access during emergencies.

---

## 🛠️ Tech Stack

* **Kotlin**
* **Jetpack Compose**
* **SQLite Database**
* **Android SDK**
* **Material Design 3**

---

## 🚀 How It Works

1. User opens the Emergency SOS app.
2. Emergency contacts are saved locally using SQLite.
3. During an emergency, the user activates the SOS feature.
4. The app retrieves the current GPS location.
5. Emergency information can be shared with trusted contacts.
6. The emergency siren can be activated for additional attention.

---

## 📂 Project Structure

```text
app/
├── src/main/java/
│   ├── MainActivity.kt
│   ├── AddContact.kt
│   ├── ShowContact.kt
│   ├── Contact.kt
│   ├── ContactsAdapter.kt
│   └── DatabaseHelper.kt
│
├── src/main/res/
│   ├── drawable/
│   ├── mipmap/
│   ├── values/
│   ├── xml/
│   └── raw/
│       └── siren.mp3
```

---

## 🔐 Permissions Required

* Location Permission
* Internet Permission
* SMS Permission (if enabled)
* Phone State Permission (if required)

---

## 📸 Screenshots

Add application screenshots here.

```text
screenshots/
├── home_screen.png
├── contacts_screen.png
└── emergency_screen.png
```

---

## ⚠️ Disclaimer

This application is intended to assist users during emergency situations. It should not be considered a replacement for official emergency services. Always contact local authorities when immediate assistance is required.

---

## ⭐ Future Enhancements

* Voice-Activated SOS
* Live Location Tracking
* Cloud Backup & Sync
* Emergency Call Integration
* AI-Based Threat Detection
* Wearable Device Support

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a new feature branch
3. Commit your changes
4. Push to your branch
5. Create a Pull Request

---

## 👨‍💻 Author

**Jeren J**

Android & Full Stack Developer

GitHub: https://github.com/jeren-dev

---

## 📄 License

This project is licensed for educational and personal use.
