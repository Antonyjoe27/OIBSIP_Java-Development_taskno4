# Task 4 — Online Examination System

## Description
A GUI-based examination system built with Java Swing where students log in, update their profile, answer MCQ questions within a countdown timer, and receive detailed results.

## Tech Stack
- Java Swing (GUI)
- No external libraries required

## Prerequisites
- JDK 17 or 21 — download from [https://adoptium.net](https://adoptium.net)

## How to Run

**Step 1: Navigate to the source folder**
```bash
cd task4-exam/src
```
**Step 2: Compile**
```bash
javac ExamSystem.java
```
**Step 3: Run**
```bash
java ExamSystem
```

## Login Credentials
| Username | Password |
|----------|----------|
| alice    | alice123 |
| bob      | bob456   |

## Features
- Login screen with credential validation
- Profile update screen to change display name and password before exam
- 10 MCQ questions with 4 options each
- Previous and Next navigation with answers saved per question
- 10-minute countdown timer visible at all times
- Auto-submits exam when timer reaches zero
- Manual submit button with confirmation dialog
- Closing window during exam triggers "Are you sure?" prompt
- Result screen shows score, time taken, and per-question breakdown
- Logout button returns to login screen

## Screens
| Screen  | Description                        |
|---------|------------------------------------|
| Login   | Enter username and password        |
| Profile | Update display name and password   |
| Exam    | Answer MCQs with timer             |
| Result  | View score and answer breakdown    |

## Project Structure
```
task4-exam/
└── src/
    └── ExamSystem.java
```
