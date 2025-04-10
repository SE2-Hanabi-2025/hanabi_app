# Hanabi Frontend 🎴 (Android | Kotlin | Jetpack Compose)

Welcome to the **Hanabi** Android frontend! This app is part of a collaborative Hanabi card game implementation, designed with modern Android development practices using **Kotlin**.

This frontend connects to a backend (Java + Spring Boot) and brings the Hanabi game experience to mobile devices — including dynamic card rendering, real-time gameplay, and multiplayer support.
  
---  
## 📱 Features

✅ Card UI using **real card images**  
✅ Draw, discard, and play logic with animations    
✅ Dynamic card rendering
✅ Real-time gameplay
✅ Multiplayer support
  
---  

## 🧱 Tech Stack

| Layer            | Technology          |     |
| ---------------- | ------------------- | --- |
| Language         | Kotlin              |     |
| UI Framework     | Jetpack Compose     |     |
| Networking       | Ktor Client         |     |
| Dependency Mgmt. | Gradle (Kotlin DSL) |     |
| Testing          | JUnit 5             |     |
| Code Quality     | SonarCloud + Jacoco |     |
  
---
## 🎮 Hanabi Game Rules (User Manual)

### Objective
The goal of **Hanabi** is to work together with your team to create a beautiful fireworks display. Players need to play cards in the correct order, with the same color cards in increasing numerical order from **1 to 5**.

### Setup
- **Players**: Hanabi can be played with **2 to 5 players**.
- **Deck**: The game is played with a deck of **50 cards**, each of **5 colors** (Red, Blue, Green, Yellow, White) and numbered **1 through 5**.
- **Hands**: At the start, each player receives a hand of **5 cards**. Players cannot see their own cards but can see other players' cards.

### Turns
Each player takes turns performing one of the following actions:

1. **Give Information**  
   You can give a hint to another player about the color or number of cards in their hand. This costs one information token.

2. **Play a Card**  
   You can play a card from your hand onto the table. If the card follows the correct order for that color (e.g., Red 1, Red 2, Red 3...), it is placed on the table; if not, the card is discarded and a penalty is incurred.

3. **Discard a Card**  
   You can discard a card from your hand to gain one information token.

### Hints
- A hint can only indicate **one color or one number** at a time.
- Players must give as much useful information as possible when providing a hint.

### Scoring
- **Successful Plays**: When you play a card in the correct order, the fireworks display advances, and you score points.
- **Penalties**: Incorrect plays will cause you to lose points or make it impossible to play the right card at a later stage.
- The game ends when either:
    1. The deck is empty, and you have successfully completed all fireworks.
    2. You run out of mistakes.

### Winning the Game
- You win the game by successfully completing the fireworks display with all 5 colors in the correct order.
- If you make too many mistakes, the game ends and you lose.