# Milestone 1 - Loop-Hole (Unit 7)

## Table of Contents

1. [Overview](#Overview)
2. [Product Spec](#Product-Spec)
3. [Wireframes](#Wireframes)

## Overview

### Description

Loop-Hole is a free Android chess training app that helps beginner and intermediate players understand chess strategy, improve their decision-making, and play with confidence. Instead of focusing on playing matches, Loop-Hole teaches users why certain moves work. Users pick an opening course, learn each line move by move on an interactive board, and lock it into memory with spaced-repetition reviews. Every feature is free, with no subscriptions, paywalls, or daily limits. Leaderboards, community-created courses, computer opponents, and extra strategy games (Blackjack and Tic-Tac-Toe) are planned for later versions.

### App Evaluation

- **Category:** Games / Education
- **Mobile:** A native Android app (Kotlin) with a touch-friendly chessboard, simple navigation, and short practice sessions users can complete anywhere. It uses **push notifications** when reviews are due or a streak is at risk, **haptic and audio feedback** on correct and incorrect moves, and **real-time** leaderboard updates through Firebase.
- **Story:** A student keeps losing chess games to friends because they don't understand opening strategy. They download Loop-Hole, practice a few openings, review their moves for a week, and walk into their next game already knowing what to play.
- **Market:** Beginner and intermediate chess players, college students, chess clubs, and casual players who want to improve without paying for expensive training tools. Online chess has over 100 million players.
- **Habit:** Spaced repetition creates a review queue every day, so users return daily. Practice streaks, progress tracking, XP, and leaderboards reinforce the habit.
- **Scope:** Version 1 focuses on opening courses, interactive lessons, review sessions, and progress tracking, which is a complete product on its own. Later versions add community courses, computer opponents, engine analysis, and additional strategy games.
- **APIs:** Firebase (Authentication, Firestore, Cloud Messaging), the free Lichess API (opening explorer and real-game move data), and the open-source Stockfish engine.

## Product Spec

### 1. User Features (Required and Optional)

**Required Features**

1. User can create an account and log in using Firebase Authentication
2. User can browse a catalog of available chess opening courses
3. User can select a chess opening and view its details
4. User can learn chess openings through interactive lessons
5. User can move chess pieces on a touch-friendly chessboard
6. User can receive feedback after making correct or incorrect moves
7. User can review previously learned moves without hints
8. User can track completed lessons and overall progress
9. User can access chess training without subscriptions or daily limits

**Optional Features**

1. User can preview the first lesson without creating an account
2. User can maintain a daily practice streak
3. User can earn XP by completing lessons and reviews
4. User can view weekly and all-time leaderboards
5. User can create custom chess opening courses
6. User can publish courses for other users
7. User can practice against computer-generated opponents
8. User can analyze chess positions using Stockfish
9. User can receive daily practice reminders (push notifications)
10. User can switch between light and dark mode
11. Stretch: User can practice Blackjack basic strategy
12. Stretch: User can play Tic-Tac-Toe against an unbeatable AI and get hints on the optimal move
13. Stretch: User can switch between Chess, Blackjack, and Tic-Tac-Toe

### 2. Screen Archetypes

- [ ] **Login / Sign Up Screen**
  - User can create an account and log in using Firebase Authentication
  - User can preview the first lesson without creating an account
- [ ] **Home / Openings Catalog Screen**
  - User can browse a catalog of available chess opening courses
- [ ] **Course Detail Screen**
  - User can select a chess opening and view its details
- [ ] **Learn Screen**
  - User can learn chess openings through interactive lessons
  - User can move chess pieces on a touch-friendly chessboard
  - User can receive feedback after making correct or incorrect moves
- [ ] **Review Screen**
  - User can review previously learned moves without hints
- [ ] **Progress Dashboard Screen**
  - User can track completed lessons and overall progress
  - User can maintain a daily practice streak
- [ ] **Leaderboard Screen** (optional)
  - User can view weekly and all-time leaderboards
- [ ] **Community Screen** (optional)
  - User can browse and publish courses for other users
- [ ] **Course Creator Screen** (optional)
  - User can create custom chess opening courses
- [ ] **Practice Opponent Screen** (optional)
  - User can practice against computer-generated opponents
- [ ] **Analysis Board Screen** (optional)
  - User can analyze chess positions using Stockfish
- [ ] **Additional Games Screen** (stretch)
  - User can practice Blackjack basic strategy
  - User can play Tic-Tac-Toe against an unbeatable AI

### 3. Navigation

**Tab Navigation** (Tab to Screen)

- [ ] Openings (Home / Openings Catalog)
- [ ] Review
- [ ] Dashboard
- [ ] Leaderboards (optional)
- [ ] Community (optional)
- [ ] Games (stretch)

**Flow Navigation** (Screen to Screen)

- [ ] **Login / Sign Up Screen**
  - => Home (after login or sign up)
  - => Home (continue as guest)
- [ ] **Home / Openings Catalog Screen**
  - => Course Detail Screen (select a course)
- [ ] **Course Detail Screen**
  - => Learn Screen (start lesson)
  - => Practice Opponent Screen (practice the opening)
  - => Login / Sign Up Screen (guest finishes the preview lesson)
  - => Home Screen (go back)
- [ ] **Learn Screen**
  - => Course Detail Screen (complete lesson)
  - => Review Screen (practice learned moves)
  - => Analysis Board Screen (tap "Analyze")
- [ ] **Review Screen**
  - => Dashboard Screen (complete review)
  - => Analysis Board Screen (analyze a missed move)
- [ ] **Progress Dashboard Screen**
  - => Review Screen (review due moves)
  - => Course Detail Screen (select a course in progress)
- [ ] **Community Screen**
  - => Course Detail Screen (select a community course)
  - => Course Creator Screen (create a course)
- [ ] **Course Creator Screen**
  - => Community Screen (after publishing)
- [ ] **Practice Opponent Screen**
  - => Analysis Board Screen (analyze the game)
  - => Course Detail Screen (go back)
- [ ] **Analysis Board Screen**
  - => Previous screen (go back)
- [ ] **Additional Games Screen**
  - => Blackjack Trainer
  - => Tic-Tac-Toe

## Wireframes

<img width="1510" height="1041" alt="Hand-Drawn Chess App Wireframes" src="https://github.com/user-attachments/assets/b4199c97-8f68-4861-84a0-c338c3484996" />

### [BONUS] Digital Wireframes & Mockups

<img width="601" height="834" alt="Digital Wireframes" src="https://github.com/user-attachments/assets/c576b926-789b-43c7-92f6-413c5a0c6d72" />

### [BONUS] Interactive Prototype

<br>

# Milestone 2 - Build Sprint 1 (Unit 8)

## GitHub Project board

[Add screenshot of your Project Board with three milestones visible in
this section]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## Issue cards

- [Add screenshot of your Project Board with the issues that you've been working on for this unit's milestone] <img src="YOUR_WIREFRAME_IMAGE_URL" width=600>
- [Add screenshot of your Project Board with the issues that you're working on in the **NEXT sprint**. It should include issues for next unit with assigned owners.] <img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## Issues worked on this sprint

- List the issues you completed this sprint
- [Add giphy that shows current build progress for Milestone 2. Note: We will be looking for progression of work between Milestone 2 and 3. Make sure your giphys are not duplicated and clearly show the change from Sprint 1 to 2.]

<br>

# Milestone 3 - Build Sprint 2 (Unit 9)

## GitHub Project board

[Add screenshot of your Project Board with the updated status of issues for Milestone 3. Note that these should include the updated issues you worked on for this sprint and not be a duplicate of Milestone 2 Project board.] <img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## Completed user stories

- List the completed user stories from this unit
- List any pending user stories / any user stories you decided to cut
from the original requirements

[Add video/gif of your current application that shows build progress]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

## App Demo Video

- Embed the YouTube/Vimeo link of your Completed Demo Day prep video
