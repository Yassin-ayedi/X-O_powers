# ⚡ X/O Powers
### A Modern Tic-Tac-Toe Game
![Game Demo](demo.gif)
---

# 🎮 The Classic Game
- 3×3 board
- Player X vs Player O
- Get 3 in a row

➡️ Problem: Too predictable, often ends in a draw

---

# 💡 Our Idea
- Expand to **4×4 board**
- Add **powers**
- Make the game strategic and dynamic

---

# ⚡ Game Features
- 4×4 grid
- 2 players
- Power-based gameplay
- Best of 3 rounds

---

# 🎴 Power System
Each turn:
1. Player gets 3 random powers
2. Chooses 1
3. Uses it as their move

---

# 💣 Example Powers
- 💣 Bomb → destroy opponent cell
- 🛡 Shield → protect your cell
- ⚡ Double → play twice
- 🃏 Steal → convert opponent cell

---

# 🏗 Architecture
- GameActivity → UI & game flow
- GameState → logic & rules
- Power → power definitions
- PowerDeal → randomness

---

# 🧠 Board Representation
```java
private int[][] board = new int[4][4];
