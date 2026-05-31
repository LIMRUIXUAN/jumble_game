# Jumble Game (Scramble Word Reconstruction Game)

Welcome to the **Jumble Game**, a modern word reconstruction game built on top of a robust Spring Boot Java backend engine and a beautiful, interactive Vue 3 Single Page Application (SPA) frontend.

---

## 🌐 Live Deployments

* **Frontend Application (Vercel):** [https://jumble-game-3kuf11hp6-limruixuans-projects.vercel.app/](https://jumble-game-3kuf11hp6-limruixuans-projects.vercel.app/)
* **Backend API & Testing Play Page (Render):** [https://jumble-game.onrender.com/game/play](https://jumble-game.onrender.com/game/play)

---

## 🛠️ Project Tasks Breakdown

The application is structured into four core tasks: **Task A**, **Task B**, **Task C**, and **Task D**.

### **Task A: Jumble Core Engine (done)**
* **Description:** Implements the core logical functions of the word engine in `JumbleEngine.java`.
* **Key Components:**
  * Word Scrambling (`scramble`)
  * Palindrome words retrieval (`retrievePalindromeWords`)
  * Fast random word picking (`pickOneRandomWord`)
  * Case-insensitive word existence check (`exists`)
  * Prefix and suffix matching (`wordsMatchingPrefix` / `wordsMatchingSuffix`)
  * Advanced search criteria (start char, end char, length) (`searchWords`)
  * Subword combinations generation (`generateSubWords`)

### **Task B: Web UI & Console Controllers (done)**
* **Description:** Exposes Task A's core engine functions to the Terminal (CLI Console App) and Thymeleaf Web Views.
* **Key Components:**
  * **Console App:** Interactive terminal application menu (`ConsoleApp.java`).
  * **Thymeleaf MVC:** Validation and rendering of pages for Scramble, Palindrome, Exist Check, Prefix, Search, and SubWords.

### **Task C: REST Game APIs & Database Persistence (done)**
* **Description:** Exposes game mechanics as REST API endpoints with persistent game states.
* **Key Components:**
  * **Game Endpoints:** `/api/game/new` (start new game) and `/api/game/guess` (submit guesses).
  * **Database State:** Real-time state syncing and storage using PostgreSQL and Spring Data JPA.

### **Task D: Modern Vue 3 Web Client (done)**
* **Description:** Single Page Application (SPA) designed to deliver a premium gameplay experience.
* **Key Components:**
  * **Game Settings:** Easy, Medium (limited guesses), and Hard (timer-controlled) difficulty levels.
  * **Aesthetics & UX:** Responsive layout, game audio effects, gravity-falling score drop overlays, tab-hiding logic during active play to prevent leaving, and a custom dark brown (`#3f2c1c`) Home button.
  * **Caching & DOM Management:** Cleans and resets DOM inputs/records when switching between WordTools tabs.

---

## 🚀 How to Run Locally

### Prerequisites
* **Java 8** or higher
* **Node.js v18+** & npm
* **Maven** (packaged wrapper included)

### 1. Running the Backend (Spring Boot)
To run the Spring Boot application, you must provide the database connection details. You can do this by setting environment variables in your terminal before running the application, or by passing them as comma-separated Maven run arguments:

#### Option A: Set environment variables in your terminal (Recommended)
**PowerShell (Windows):**
```powershell
$env:DB_URL="jdbc:postgresql://ep-restless-sun-aourh246.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require"
$env:DB_USERNAME="neondb_owner"
$env:DB_PASSWORD="npg_1ATQxG8pkOwf"
.\mvnw.cmd spring-boot:run
```

**CMD (Windows Command Prompt):**
```cmd
set DB_URL=jdbc:postgresql://ep-restless-sun-aourh246.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
set DB_USERNAME=neondb_owner
set DB_PASSWORD=npg_1ATQxG8pkOwf
mvnw.cmd spring-boot:run
```

**Bash (Linux / macOS):**
```bash
export DB_URL="jdbc:postgresql://ep-restless-sun-aourh246.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require"
export DB_USERNAME="neondb_owner"
export DB_PASSWORD="npg_1ATQxG8pkOwf"
./mvnw spring-boot:run
```

#### Option B: Pass arguments in the Maven command (Single Line)
If you want to run it in a single command, you can pass comma-separated spring properties:
**Windows (PowerShell):**
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:postgresql://ep-restless-sun-aourh246.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require,--spring.datasource.username=neondb_owner,--spring.datasource.password=npg_1ATQxG8pkOwf"
```
*(Note: Arguments must be separated by **commas**, not spaces, for the Maven plugin to parse them correctly)*

By default, the backend runs on [http://localhost:8080](http://localhost:8080).

### 2. Running the Console App (CLI Menu)
To run the interactive CLI console application directly in your terminal:
```bash
# Windows
.\mvnw.cmd compile exec:java

# Linux / macOS
./mvnw compile exec:java
```

### 3. Running the Frontend (Vue 3 client)
Open another terminal session and run the Vite server:
```bash
cd web-client
npm install
npm run dev
```
By default, the frontend runs on [http://localhost:5173](http://localhost:5173).

---

## 🧪 How to Run Tests

### **Testing Tasks A, B, and C (Backend Java)**
The backend contains a suite of JUnit 5 tests covering the engine, Thymeleaf controllers, and REST APIs. These tests run out-of-the-box using an **in-memory H2 database**, meaning you **do not** need to set up any environment variables to run them.
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

* **Task A Tests:** Handled in `JumbleEngineTest.java` (testing engine functions).
* **Task B Tests:** Handled in `RootControllerTest.java` (testing HTML views).
* **Task C Tests:** Handled in `GameApiControllerTest.java` (testing REST API guess & validation outputs).

### **Testing Task D (Frontend Vitest)**
To run the Vitest unit tests for Vue components and user interactions:
```bash
cd web-client
npm run test
```

---

## 🎮 How to Use the Game

1. Open the [Vercel Web App](https://jumble-game-3kuf11hp6-limruixuans-projects.vercel.app/).
2. Select your **Difficulty Mode**:
   * **Easy:** Infinite wrong guesses, no timer.
   * **Medium:** Up to 8 mistakes allowed.
   * **Hard:** 5 mistakes max, with a 30-second timer resetting on every correct guess (earning time-based speed bonuses!).
3. Hit **Start Game** and type valid subwords constructed from the scrambled letters shown.
4. Click the **Home** button at any time to return to the configuration menu.
5. Explore the **WordTools** on the top navigation bar to analyze words by prefix, suffix, subwords, search parameters, or to verify if a word exists in the dictionary.
