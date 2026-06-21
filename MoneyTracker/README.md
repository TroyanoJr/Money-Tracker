# Spending Tracker - Chalk Style 📝

**Spending Tracker** is a unique, visually distinctive Android application designed to help you manage your personal finances with ease and style. Unlike standard financial apps, it features a custom **Blackboard/Chalk-inspired UI**, bringing a creative and tactile feel to money management.

## 🎨 Unique Visual Identity
The app is built around a "Blackboard" concept:
- **Chalk Aesthetics:** Custom buttons, dotted dividers, and cursive typography that mimic handwriting on a chalkboard.
- **Optimized Themes:** Deep, "Blackboard Black" backgrounds that are easy on the eyes and professional.
- **Immersive Experience:** Every screen is designed to maintain the blackboard metaphor, from transaction entries to advanced reports.

## 🚀 Key Features
- **Intuitive Financial Logging:** Record income and expenses with ease, adding notes and repeating patterns.
- **Smart Navigation:** A robust UI that prevents element overlap and ensures focused interaction through "Modal" screen blocking (to prevent accidental clicks through layers).
- **Advanced Data Visualization:**
    - **GitHub-style Heatmap:** An improved yearly calendar heatmap that visualizes spending density and balance trends with clear month separators and date ranges.
    - **Interactive Charts:** Dynamic Pie and Bar charts to analyze spending by category with toggleable cash/percentage views.
- **Automatic Reporting:** Rotate your device to **Landscape mode** to instantly access a dedicated, full-screen reports dashboard.
- **Customizable Categories:** Manage categories with a built-in two-level color picker for high-level organization.
- **Data Export:** Securely generate and share **CSV reports** of your financial history with customizable date ranges and separators.

## 🛠 Tech Stack
- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/compose) (Modern, declarative UI)
- **Design System:** [Material 3](https://m3.material.io/)
- **Database:** [Room](https://developer.android.com/training/data-storage/room) (Local persistence)
- **Architecture:** MVVM (Model-View-ViewModel)

## 📸 Screenshots
### 🏠 Main Views
<p align="center">
  <img src="Screenshots/Main dashBoard.png" width="30%" />
  <img src="Screenshots/Show all Transactions.png" width="30%" />
  <img src="Screenshots/Calendar Heatmap.png" width="30%" />
</p>

### 📊 Data Insights & Graphics
<p align="center">
  <img src="Screenshots/Pie chart Graphic.png" width="45%" />
  <img src="Screenshots/Bar chart graph.png" width="45%" />
</p>

### 🛠 Tools & Data Entry
<p align="center">
  <img src="Screenshots/Add Transactions.png" width="23%" />
  <img src="Screenshots/Add Category.png" width="23%" />
  <img src="Screenshots/Select color.png" width="23%" />
  <img src="Screenshots/Export doc.png" width="23%" />
</p>

## ⚠️ Current Limitations & Known Issues
- **Payment Parser (Prototype):** The automated payment parsing logic (`parserPayment`) is currently in **Beta**. It is an early prototype and may not be fully efficient across all banking or payment applications yet.
- **Rotation State Loss:** Due to the automatic switch to "Reports View" in landscape mode, any unsaved data in the "Add Transaction" or "Add Category" screens may be lost if the device is rotated during entry.
- **Hardcoded Date Formats:** Some parts of the app use specific hardcoded locales (e.g., `Locale.CHINA`) for date formatting, which may not align with every user's system settings.
- **Fixed Category Icons:** The ability to choose custom icons is currently disabled; a default placeholder icon is used while the full picker is under development.
- **Keyboard Overlap:** On small-screen devices, the software keyboard might overlap input fields in modal screens (IME padding refinements are ongoing).
- **Data Persistence:** Currently, data is only stored locally. Uninstalling the app without a manual CSV export will result in data loss.

## ⚙️ Installation
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/spending-tracker.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Sync project with Gradle files.
4. Run the app on an emulator or physical device.

---
Developed with ❤️ focusing on unique UI/UX and financial clarity.
