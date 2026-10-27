
<div align="center">

# ♟️ LOOP-HOLE

### Learn the Opening. Master the Strategy. Play with Confidence.

**A free chess training app designed to help players improve their skills, learn openings, and become more confident in every game.**

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Course](https://img.shields.io/badge/CodePath-AND102-blue)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)
![Price](https://img.shields.io/badge/Price-100%25%20Free-brightgreen)

</div>

---

## 📌 Table of Contents

- [Overview](#-overview)
- [App Evaluation](#-app-evaluation)
- [Product Spec](#-product-spec)
- [Screen Archetypes](#-screen-archetypes)
- [Navigation](#-navigation)
- [Wireframes](#-wireframes)
- [Milestone 2 - Build Sprint 1](#-milestone-2---build-sprint-1-unit-8)
- [Milestone 3 - Build Sprint 2](#-milestone-3---build-sprint-2-unit-9)
- [App Demo](#-app-demo)

---

# 🚀 Milestone 1 - App Planning (Unit 7)

## 📱 Overview

### Description

**Loop-Hole** is a free mobile chess training application designed to help beginner and intermediate players understand chess strategies, improve their decision-making, and become more confident when playing against others.

Unlike traditional chess applications that focus mainly on playing matches, Loop-Hole focuses on teaching users **why certain moves work and how to use them effectively**.

Users can select chess opening courses, learn moves step by step, and strengthen their knowledge through spaced-repetition reviews.

The application also aims to make chess education accessible by providing unlimited practice without subscriptions, paywalls, or daily restrictions.

Future updates will introduce leaderboards, community-created courses, computer opponents, and additional strategy games.

### ✨ Key Highlights

| Feature | Description |
|---|---|
| ♟️ Chess Openings | Learn popular chess openings step by step |
| 📚 Interactive Lessons | Practice moves on an interactive chessboard |
| 🔁 Review Mode | Remember openings through spaced repetition |
| 📊 Progress Tracking | Monitor completed lessons and mastered openings |
| 🔥 Daily Streaks | Stay motivated through consistent practice |
| 🏆 Leaderboards | Compete with other players and earn XP |
| 💰 Free Learning | No subscriptions or daily learning limits |

---

## 📊 App Evaluation

| Category | Evaluation |
|---|---|
| **Category** | Games / Education |
| **Mobile** | Loop-Hole is designed for mobile devices with a touch-friendly chessboard, simple navigation, and short practice sessions that users can complete anywhere. |
| **Story** | A student constantly loses chess games against friends because they do not understand opening strategies. They download Loop-Hole, practice different openings, review their moves, and gradually become a stronger player. |
| **Market** | Beginner and intermediate chess players, college students, chess clubs, and casual players looking to improve their skills without paying for expensive training tools. |
| **Habit** | Daily reviews, practice streaks, progress tracking, XP, and leaderboards encourage users to return and continue learning. |
| **Scope** | Version 1 focuses on chess opening courses, interactive lessons, review sessions, and progress tracking. Future versions will introduce community features, AI-supported practice, and additional strategy games. |

---

# 🛠️ Product Spec

## 1. User Features

### ✅ Required Features (MVP)

- [ ] User can create an account and log in using Firebase Authentication.
- [ ] User can browse a catalog of available chess opening courses.
- [ ] User can select a chess opening and view its details.
- [ ] User can learn chess openings through interactive lessons.
- [ ] User can move chess pieces on a touch-friendly chessboard.
- [ ] User can receive feedback after making correct or incorrect moves.
- [ ] User can review previously learned moves without hints.
- [ ] User can track completed lessons and overall progress.
- [ ] User can access chess training without subscriptions or daily limits.

### ⭐ Optional Features

- [ ] User can preview the first lesson without creating an account.
- [ ] User can maintain a daily practice streak.
- [ ] User can earn XP by completing lessons and reviews.
- [ ] User can view weekly and all-time leaderboards.
- [ ] User can create custom chess opening courses.
- [ ] User can publish courses for other users.
- [ ] User can practice against computer-generated opponents.
- [ ] User can analyze chess positions using Stockfish.
- [ ] User can receive daily practice reminders.
- [ ] User can switch between light and dark mode.

### 🎮 Stretch Features

- [ ] User can practice Blackjack basic strategy.
- [ ] User can play Tic-Tac-Toe against an unbeatable AI.
- [ ] User can receive hints about optimal Tic-Tac-Toe moves.
- [ ] User can switch between Chess, Blackjack, and Tic-Tac-Toe.

---

# 🖥️ Screen Archetypes

The following screens will allow users to navigate through Loop-Hole and access its main features.

### 1. Login / Sign Up Screen
- Create an account.
- Log in using Firebase Authentication.
- Access saved learning progress.

### 2. Home / Openings Catalog Screen
- Browse available chess opening courses.
- View course names and difficulty levels.
- Select a course to begin learning.

### 3. Course Detail Screen
- View information about an opening.
- See available lessons.
- Start learning a selected course.

### 4. Learn Screen
- Interact with a chessboard.
- Follow step-by-step instructions.
- Practice opening moves.
- Receive feedback on moves.

### 5. Review Screen
- Practice previously learned openings.
- Complete review sessions without hints.
- Strengthen memory through repetition.

### 6. Progress Dashboard Screen
- View completed lessons.
- Track mastered openings.
- Monitor daily practice activity.
- View learning progress.

### 7. Leaderboard Screen (Optional)
- View weekly rankings.
- Compare XP with other players.
- Track competitive progress.

### 8. Community Screen (Optional)
- Browse community-created courses.
- Discover new opening strategies.
- Share custom courses.

### 9. Analysis Board Screen (Optional)
- Analyze chess positions.
- Review possible moves using Stockfish.

### 10. Additional Games Screen (Stretch)
- Access Blackjack strategy training.
- Play Tic-Tac-Toe against AI.

---

# 🧭 Navigation

## Tab Navigation

The application will use bottom navigation to allow users to switch between the main sections.

| Tab | Destination |
|---|---|
| 🏠 Openings | Home / Opening Catalog |
| 🔁 Review | Review Screen |
| 📊 Dashboard | Progress Dashboard |
| 🏆 Leaderboards | Leaderboard Screen (Optional) |
| 🌐 Community | Community Screen (Optional) |
| 🎮 Games | Additional Games (Stretch) |

## Flow Navigation

### Login / Sign Up Screen

- Login / Sign Up → Home Screen

### Home / Openings Catalog Screen

- Home → Course Detail Screen (select a course)

### Course Detail Screen

- Course Detail → Learn Screen (start lesson)
- Course Detail → Home Screen (go back)

### Learn Screen

- Learn → Course Detail Screen (complete lesson)
- Learn → Review Screen (practice learned moves)

### Review Screen

- Review → Dashboard Screen (complete review)
- Review → Home Screen (return to openings)

### Dashboard Screen

- Dashboard → Review Screen (review due moves)
- Dashboard → Course Detail Screen (select course in progress)

### Community Screen (Optional)

- Community → Course Detail Screen (select community course)
- Community → Course Creator Screen (create a course)

### Games Screen (Stretch)

- Games → Blackjack Trainer
- Games → Tic-Tac-Toe

---

# 🎨 Wireframes

Wireframes will illustrate the layout and navigation of Loop-Hole before development.

### Planned Wireframes

1. Login / Sign Up Screen
2. Home / Openings Catalog Screen
3. Course Detail Screen
4. Learn Screen
5. Review Screen
6. Progress Dashboard Screen

**Hand-Drawn Wireframes**

> 📷 Add screenshots of hand-drawn wireframes here.

### [BONUS] Digital Wireframes & Mockups

> 🎨 Add Figma designs here.

### [BONUS] Interactive Prototype

> 🔗 Add Figma prototype link here.

---

# 💻 Milestone 2 - Build Sprint 1 (Unit 8)

## GitHub Project Board

> 📷 Add screenshot of the GitHub Project Board showing all three milestones.

## Issue Cards

> 📷 Add screenshots of Sprint 1 issues and upcoming Sprint 2 issues with assigned owners.

## Issues Worked on This Sprint

### Planned Sprint 1 Tasks

- [ ] Set up Android Studio project.
- [ ] Create GitHub repository and Project Board.
- [ ] Design initial application layouts.
- [ ] Implement navigation between screens.
- [ ] Create Home / Openings Catalog Screen.
- [ ] Create chess opening course data model.
- [ ] Display opening courses using RecyclerView.
- [ ] Create Course Detail Screen.
- [ ] Build initial interactive chessboard.
- [ ] Connect Course Detail Screen to Learn Screen.

## Sprint 1 Build Progress

> 🎥 Add GIF demonstrating the Home Screen, opening courses, and initial chessboard.

---

# 🚧 Milestone 3 - Build Sprint 2 (Unit 9)

## GitHub Project Board

> 📷 Add updated screenshot of the Project Board showing Sprint 2 progress.

## Completed User Stories

### Planned Sprint 2 Tasks

- [ ] User can select a chess opening course.
- [ ] User can interact with a chessboard.
- [ ] User can follow step-by-step opening instructions.
- [ ] User can make moves and receive feedback.
- [ ] User can complete a chess lesson.
- [ ] User can review previously learned moves.
- [ ] User can view completed lessons and progress.
- [ ] User can navigate between the main application screens.

## Pending User Stories / Future Improvements

- [ ] Advanced spaced-repetition scheduling.
- [ ] XP and leaderboards.
- [ ] Community-created courses.
- [ ] Human-like opponent simulation.
- [ ] Stockfish chess analysis.
- [ ] Blackjack strategy trainer.
- [ ] Tic-Tac-Toe strategy trainer.

## Sprint 2 Build Progress

> 🎥 Add GIF or video showing the updated chessboard, lesson progression, review mode, and dashboard.

---

# 🎬 App Demo

## Demo Day Video

> 🎥 Insert YouTube or Vimeo link to the completed application demo.

---

## 🧰 Planned Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Android Studio | Development environment |
| Firebase Authentication | User accounts and login |
| Firebase Firestore | Store user progress and course data |
| RecyclerView | Display chess opening courses |
| Stockfish | Future chess analysis functionality |
| GitHub | Version control and team collaboration |
| Figma | UI/UX design and prototyping |

---

<div align="center">

### ♟️ LOOP-HOLE

**Practice Smarter. Play Better.**

Built as part of the CodePath AND102 Android Development Capstone Project.

</div>
