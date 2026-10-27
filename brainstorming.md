# Brainstorming – Group Project

**Team:** Shaniah Smith, David Mack, Preston Frazier

## Table of Contents

1. [New App Ideas – List](#new-app-ideas--list)
2. [Top 3 New App Ideas](#top-3-new-app-ideas)
3. [New App Ideas – Evaluate and Categorize](#new-app-ideas--evaluate-and-categorize)
4. [Final Decision](#final-decision)

---

## New App Ideas – List

### Shaniah Smith

1. **Loop-Hole (Chess Opening Trainer)** – Learn chess openings move by move, then lock them in with daily spaced-repetition reviews so you can beat your friends in the first 10 moves.
2. **StudyBuddy Match** – Matches college students in the same class for study sessions based on schedule, location on campus, and topics they're stuck on.
3. **ThriftMap** – A map of nearby thrift and consignment stores where users post photos of their best finds and rate each store.

### David Mack

4. **GymCrowd** – Shows how busy your gym is in real time, based on check-ins from other members, so you can pick the best time to work out.
5. **Recipe Roulette** – Snap a photo of what's in your fridge and get a random recipe you can make with those ingredients.
6. **Card Shark Trainer** – Drills Blackjack basic strategy and grades each decision (hit, stand, double, split) to show how often you'd beat the house.

### Preston Frazier

7. **Pickup Finder** – Find or post pickup basketball and soccer games at local courts and fields, with player counts and a live "need 2 more" alert.
8. **Commute Quest** – Turns your daily commute into a game: earn points for walking, biking, or taking transit, and compete with friends on a weekly leaderboard.
9. **SoundSnap Journal** – A daily journal where each entry is a 10-second audio clip plus a mood tag, with a calendar view of your year.

---

## Top 3 New App Ideas

1. **Loop-Hole (Chess Opening Trainer)**
2. **Pickup Finder**
3. **Recipe Roulette**

---

## New App Ideas – Evaluate and Categorize

### 1. Loop-Hole (Chess Opening Trainer)

- **Description:** A free chess training app. Users pick an opening course, learn each line move by move on an interactive board, then review it on a spaced-repetition schedule until it's memorized. Progress, streaks, XP, and leaderboards keep users coming back.
- **Category:** Games / Education
- **Mobile:** Touch-friendly chessboard built for a phone. Short review sessions fit into spare minutes. Uses **push notifications** for due reviews and streak reminders, **haptic and audio feedback** on each move, and **real-time** leaderboards.
- **Story:** A player keeps losing in the opening to the same friend. After a week of learning and reviewing a few lines, they walk into the next game already knowing what to play. Friends who play chess would immediately understand the value.
- **Market:** Beginner to intermediate chess players, school and college chess clubs, and casual players. Online chess has over 100 million players, and the app is completely free.
- **Habit:** Very high. Spaced repetition creates a review queue every day, and streaks, XP, and leaderboards reinforce it. Users mostly consume courses, but can also create and share their own.
- **Scope:** Moderate. V1 (login, course catalog, Learn mode, Review mode, progress dashboard) is a complete product on its own. Community courses, engine analysis, and extra games can be added later.
- **API:** Firebase (Authentication, Firestore, Cloud Messaging), the free Lichess API (opening explorer and real-game move data), and the open-source Stockfish engine.

### 2. Pickup Finder

- **Description:** Users find or post pickup basketball and soccer games at local courts and fields, see how many players have joined, and get alerts when a nearby game needs more players.
- **Category:** Sports / Social
- **Mobile:** Strongly mobile. Uses **maps**, **location**, and **push notifications** for "need 2 more" alerts.
- **Story:** Compelling for anyone who has shown up to an empty court. Friends and classmates who play sports would use it.
- **Market:** Large: casual athletes, college students, and recreational leagues. However, the app is only useful once enough local players join.
- **Habit:** Medium. Users open it a few times a week before playing. Users both create games and join them.
- **Scope:** Medium-high. Needs maps, location, real-time player counts, and enough users in one area to feel alive, which is hard to show in a demo.
- **API:** Google Maps SDK and Places API, Firebase for real-time game data.

### 3. Recipe Roulette

- **Description:** Users take a photo of the ingredients they have, and the app suggests a random recipe they can make with them. Users can save favorites and re-roll for new ideas.
- **Category:** Lifestyle / Food
- **Mobile:** Uses the **camera** to scan ingredients and **push notifications** for a daily recipe idea.
- **Story:** Clear value for students who don't know what to cook with what's in the fridge.
- **Market:** Large: students, busy professionals, and anyone trying to waste less food.
- **Habit:** Medium. Users open it around mealtimes. Mostly consumption, with saved favorites.
- **Scope:** Medium-high. Ingredient recognition from photos is technically difficult. A stripped-down version where users type their ingredients is still useful.
- **API:** Spoonacular API (limited free tier) or TheMealDB (free), plus ML Kit for image labeling.

---

## Final Decision

**We chose Loop-Hole (Chess Opening Trainer).**

All three of us were most excited to build it, and it scored best on **Habit** and **Scope**. Spaced repetition gives users a reason to open the app every day, and V1 alone is a complete, demo-ready product. Unlike Pickup Finder, it doesn't need a large local user base to be useful, and unlike Recipe Roulette, its core feature doesn't depend on difficult image recognition. Free APIs (Firebase, Lichess, Stockfish) cover everything we need. David's Card Shark Trainer idea was folded into Loop-Hole as a stretch feature.
