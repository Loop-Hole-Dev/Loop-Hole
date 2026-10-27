Mobile App — Loop-Hole
CodePath AND102 Capstone Project
Table of Contents
- Milestone 1 — Loop-Hole (Unit 7)
  - Overview
  - Product Spec
  - Navigation
  - Wireframes
- Milestone 2 — Build Sprint 1 (Unit 8)
- Milestone 3 — Build Sprint 2 (Unit 9)
- App Demo Video
Milestone 1 — Loop-Hole (Unit 7)
Overview
Description
Loop-Hole is a free chess training app that teaches players the strategy behind the game so they can improve their skills and compete more confidently with friends. Instead of only playing matches, users choose an opening course, learn each line move by move, and reinforce what they learn through spaced-repetition reviews.

Loop-Hole is designed to make chess training accessible: no subscriptions, paywalls, daily caps, or limits on lessons and reviews. Players can try a sample lesson as guests, create accounts using Firebase Authentication, track their progress, and build consistent practice habits. Planned advanced features include leaderboards, community courses, human-like opponents based on real-game move frequencies, and chess position analysis using Stockfish. Blackjack and Tic-Tac-Toe strategy trainers are stretch goals.
App Evaluation
- Category: Games / Education
- Mobile: Loop-Hole is designed for touch-friendly mobile use. Players can complete short lessons and reviews anywhere, while practice reminders and streak tracking encourage regular use. A progressive web app (PWA) version is a possible future enhancement.
- Story: A player keeps losing to a friend in the first ten moves. They select an opening course, practice a few lines, review them over the week, and return to their next game with a stronger understanding of what to play and why.
- Market: Beginner and intermediate chess players, students, school and college chess clubs, and casual players who want affordable, structured training. The app's free model removes a common barrier to learning.
- Habit: Spaced repetition brings users back when learned lines are due for review. Streaks, progress tracking, XP, activity history, and future leaderboards provide additional motivation.
- Scope: V1 focuses on Firebase login, opening courses, Learn mode, Review mode, and a progress dashboard. V2 adds XP, leaderboards, course creation and sharing, Human Moves practice, and a Stockfish analysis board. V3 explores Blackjack and Tic-Tac-Toe trainers. V1 is intended to be useful on its own.
Product Spec
1. User Features (Required and Optional)
Required Features (MVP)
- [ ] User can create an account and log in with Firebase Authentication (email/password or Google).
- [ ] User can browse a catalog of chess opening courses.
- [ ] User can view a course's details and enroll in it.
- [ ] User can try the first line of a course as a guest without creating an account.
- [ ] User can learn opening lines move by move on an interactive chessboard, with arrows and explanations before playing each move.
- [ ] User can practice unlimited lessons and reviews without paywalls or daily caps.
- [ ] User can review previously learned lines without hints and receive feedback on their moves.
- [ ] User's review schedule can adapt using spaced repetition.
- [ ] User can view a progress dashboard showing due reviews, completed lines, mastery progress, and practice activity.
Optional Features
- [ ] User can earn XP and see weekly and all-time leaderboards.
- [ ] User can track daily streaks and view an activity heatmap.
- [ ] User can create a private course by importing a PGN or editing a move tree.
- [ ] User can publish courses, browse community courses, and upvote useful lessons.
- [ ] User can practice against human-like opponent moves informed by real-game data.
- [ ] User can analyze positions on a free board using Stockfish.
- [ ] User can enable dark mode and receive review reminders.
- [ ] User can install a future PWA version on a home screen.
Stretch Features
- [ ] User can practice Blackjack basic strategy (hit, stand, double, split) and receive feedback.
- [ ] User can play Tic-Tac-Toe against an unbeatable computer opponent and request optimal-move hints.
- [ ] User can switch among Chess, Blackjack, and Tic-Tac-Toe from a game picker.
2. Screen Archetypes
Screen	What the user can do
Login / Sign Up	Create an account or sign in with Firebase Authentication.
Home / Openings Catalog	Browse available opening courses and choose one.
Course Detail	Read a course description, view its lessons, enroll, or try a guest lesson.
Learn	Follow instructions and play moves on an interactive chessboard.
Review	Recall learned lines without hints and receive performance feedback.
Dashboard	Check reviews due, mastery progress, completed lessons, and practice history.
Leaderboards (optional)	View weekly and all-time XP rankings.
Community (optional)	Browse, filter, and upvote shared courses.
Course Creator (optional)	Import PGN files, edit move trees, and publish courses.
Human Moves (optional)	Practice against moves modeled on real-game frequencies.
Analysis Board (optional)	Explore positions with a Stockfish engine.
Blackjack Trainer (stretch)	Drill basic-strategy decisions and see accuracy.
Tic-Tac-Toe (stretch)	Play against an unbeatable AI and receive hints.


Navigation
Tab Navigation (Tab to Screen)
Core tabs
- Openings (Home): Browse and select chess opening courses.
- Review: Complete due review sessions.
- Dashboard: View progress, completed lessons, and practice history.
Optional / future tabs
- Leaderboards: Compare weekly and all-time XP.
- Community: Discover and share courses.
- Games: Choose Blackjack or Tic-Tac-Toe trainers (stretch).
Flow Navigation (Screen to Screen)
Login / Sign Up Screen
- → Home (after successful login or sign-up)
Home / Openings Catalog Screen
- → Course Detail (tap an opening course)
Course Detail Screen
- → Learn (tap Start Learning)
- → Guest Lesson (tap Try First Line without an account)
- → Human Moves (optional practice mode)
Guest Lesson
- → Sign Up (after previewing the first line, if the user wants to save progress)
Learn Screen
- → Course Detail (finish a line or return to the course)
- → Review (practice learned material)
Review Screen
- → Dashboard (finish the review queue)
Dashboard Screen
- → Review (tap Review Due Lines)
- → Course Detail (tap a course in progress)
Community Screen (optional)
- → Course Detail (tap a community course)
- → Course Creator (tap Create Course)
Course Creator Screen (optional)
- → Community (after publishing a course)
Analysis Board Screen (optional)
- → Previous Screen (tap Back)
Games Screen (stretch)
- → Blackjack Trainer
- → Tic-Tac-Toe
Wireframes
To add: Insert pictures of hand-drawn wireframes for the Login, Home, Course Detail, Learn, Review, and Dashboard screens.

<!-- Example: ![Home Screen Wireframe](images/home-wireframe.jpg) -->

[BONUS] Digital Wireframes & Mockups
To add: Figma mockups or screenshots.

[BONUS] Interactive Prototype
To add: Figma prototype link.

Milestone 2 — Build Sprint 1 (Unit 8)
GitHub Project Board
To add in Unit 8: Screenshot of the GitHub Project Board showing all three milestones.

Issue Cards
To add in Unit 8: Screenshot of issues worked on during Sprint 1.

To add in Unit 8: Screenshot of issues planned for Sprint 2, including assigned owners.

Issues Worked on This Sprint
To complete in Unit 8: List only issues actually completed during Sprint 1.

Suggested Sprint 1 issues (planning only):
- [ ] Set up the Android Studio project and repository structure.
- [ ] Create the Home / Openings Catalog screen.
- [ ] Define the opening course data model.
- [ ] Display course listings in a RecyclerView.
- [ ] Build the Course Detail screen.
- [ ] Create the first interactive chessboard layout.
- [ ] Connect the Home, Course Detail, and Learn screens.
Sprint 1 Build Progress
To add in Unit 8: GIF showing actual progress from Sprint 1. Use a different recording for Sprint 2.

Milestone 3 — Build Sprint 2 (Unit 9)
GitHub Project Board
To add in Unit 9: Updated Project Board screenshot reflecting Sprint 2 work, not a duplicate of Sprint 1.

Completed User Stories
To complete in Unit 9: List only user stories implemented and tested during Sprint 2.

Suggested Sprint 2 issues (planning only):
- [ ] Let users select an opening and begin a lesson.
- [ ] Support interactive chess moves during lessons.
- [ ] Display hints and feedback for correct and incorrect moves.
- [ ] Record completed lessons.
- [ ] Create a basic Review mode.
- [ ] Display progress in the Dashboard.
Pending or Cut User Stories
To complete in Unit 9: Identify remaining, postponed, or removed features and explain any scope changes.

Sprint 2 Build Progress
To add in Unit 9: Video or GIF demonstrating newly implemented functionality since Sprint 1.

App Demo Video
To add: YouTube or Vimeo link to the completed Demo Day preparation video.
