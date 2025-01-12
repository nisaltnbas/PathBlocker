# PathBlocker Game

A path-finding puzzle game where you need to guide a player to a target while blocking the path behind you.

## Project Structure

```
PathBlocker/
├── src/
│   └── game/
│       ├── graphics/
│       │   └── ChartMap.java
│       ├── model/
│       │   ├── CellType.java
│       │   ├── Direction.java
│       │   ├── Elevation.java
│       │   ├── GameState.java
│       │   ├── Player.java
│       │   ├── Scene.java
│       │   └── State.java
│       ├── util/
│       │   └── FileUtil.java
│       └── PathBlocker.java
├── level*.txt (Level files)
└── level*/ (Generated level solution folders)
```

## Features

- Path-finding with A* algorithm
- Elevation-based movement cost
- Graphical output of game states
- Multiple levels support

## How to Run

1. Make sure you have Java 19 or later installed
2. Place level files (level1.txt through level10.txt) in the project root
3. Compile and run the game:
   ```bash
   javac src/game/PathBlocker.java
   java -cp src game.PathBlocker
   ```

## Level File Format

Each level file should contain a grid of numbers:
- 0: Empty cell
- 1: Wall
- 2: Target
- 3: Player starting position

## Output

The game will create a folder for each level (level01, level02, etc.) containing PNG images showing the solution path.
