package com.example.xopowers;

public class GameState {

    public static final int BOARD_SIZE = 4;
    public static final int EMPTY = 0;
    public static final int PLAYER_X = 1;
    public static final int PLAYER_O = 2;

    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][] shieldTurns = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][] chainTurns = new int[BOARD_SIZE][BOARD_SIZE]; // 0=free, >0=chained
    private int frozenByPlayer = 0;

    private int lastRow = -1;
    private int lastCol = -1;
    private int lastPlayer = 0;

    private boolean doubleFirstDone = false;

    private int currentPlayer = PLAYER_X;
    private int scoreX = 0;
    private int scoreO = 0;

    public GameState() { reset(); }

    public void reset() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        shieldTurns = new int[BOARD_SIZE][BOARD_SIZE];
        chainTurns = new int[BOARD_SIZE][BOARD_SIZE];
        lastRow = -1; lastCol = -1; lastPlayer = 0;
        currentPlayer = PLAYER_X;
        doubleFirstDone = false;
        frozenByPlayer = 0;
    }

    // Getters
    public int getCell(int row, int col) { return board[row][col]; }
    public int getCurrentPlayer() { return currentPlayer; }
    public int getScoreX() { return scoreX; }
    public int getScoreO() { return scoreO; }
    public int getLastRow() { return lastRow; }
    public int getLastCol() { return lastCol; }
    public int getLastPlayer() { return lastPlayer; }
    public boolean isShielded(int row, int col) { return shieldTurns[row][col] > 0; }
    public boolean isFrozen(int row, int col) { return chainTurns[row][col] > 0; }
    public int getChainTurns(int row, int col) { return chainTurns[row][col]; }
    public int getFrozenByPlayer() { return frozenByPlayer; }
    public int getShieldTurns(int row, int col) { return shieldTurns[row][col]; }
    public boolean isDoubleFirstDone() { return doubleFirstDone; }
    public void setDoubleFirstDone(boolean v) { doubleFirstDone = v; }

    // NONE: just place mark, turn ends
    public boolean placeNormal(int row, int col) {
        if (board[row][col] != EMPTY) return false;
        if (isCellBlocked(row, col)) return false;
        board[row][col] = currentPlayer;
        lastRow = row; lastCol = col; lastPlayer = currentPlayer;
        endTurn();
        return true;
    }

    // DOUBLE: place 2 marks total, turn ends after second
    // Returns true when turn is fully over
    public boolean placeDouble(int row, int col) {
        if (board[row][col] != EMPTY) return false;
        if (isCellBlocked(row, col)) return false;
        board[row][col] = currentPlayer;
        lastRow = row; lastCol = col; lastPlayer = currentPlayer;
        if (!doubleFirstDone) {
            doubleFirstDone = true;
            return false; // need second placement
        } else {
            doubleFirstDone = false;
            endTurn();
            return true;
        }
    }

    // WILDCARD: place anywhere ignoring freeze, turn ends
    public boolean placeWildcard(int row, int col) {
        if (board[row][col] != EMPTY) return false;
        board[row][col] = currentPlayer;
        lastRow = row; lastCol = col; lastPlayer = currentPlayer;
        endTurn();
        return true;
    }

    // BOMB: destroy opponent cell, turn ends
    public boolean applyBomb(int row, int col) {
        if (board[row][col] != opponent()) return false;
        if (shieldTurns[row][col] > 0) return false;
        board[row][col] = EMPTY;
        endTurn();
        return true;
    }

    // SHIELD: protect own cell, turn ends
    public boolean applyShield(int row, int col) {
        if (board[row][col] != currentPlayer) return false;
        shieldTurns[row][col] = 2;
        endTurn();
        return true;
    }

    // STEAL: convert one chosen opponent mark into yours, turn ends
    public boolean applySteal(int row, int col) {
        if (board[row][col] != opponent()) return false;
        if (shieldTurns[row][col] > 0) return false;
        board[row][col] = currentPlayer;
        endTurn();
        return true;
    }

    // CHAIN: block an empty cell for 3 turns, opponent cannot place there
    public boolean applyChain(int row, int col) {
        if (board[row][col] != EMPTY) return false;
        chainTurns[row][col] = 6; // 3 rounds = 6 turns (both players)
        frozenByPlayer = currentPlayer;
        endTurn();
        return true;
    }





    // MIRROR: steal opponent's last placed cell, turn ends
    public boolean applyMirror() {
        if (lastPlayer != opponent() || lastRow < 0) return false;
        if (board[lastRow][lastCol] != opponent()) return false;
        if (shieldTurns[lastRow][lastCol] > 0) return false;
        board[lastRow][lastCol] = currentPlayer;
        endTurn();
        return true;
    }

    public void forceSwitchPlayer() {
        currentPlayer = (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
    }

    private boolean isCellBlocked(int row, int col) {
        return chainTurns[row][col] > 0 && frozenByPlayer != currentPlayer;
    }

    private int opponent() {
        return (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
    }

    private void endTurn() {
        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (shieldTurns[r][c] > 0) shieldTurns[r][c]--;
                if (chainTurns[r][c] > 0) chainTurns[r][c]--;
            }
        currentPlayer = (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
    }

    public int checkWinner() {
        for (int r = 0; r < BOARD_SIZE; r++)
            if (board[r][0]!=EMPTY && board[r][0]==board[r][1] && board[r][1]==board[r][2] && board[r][2]==board[r][3])
                return board[r][0];
        for (int c = 0; c < BOARD_SIZE; c++)
            if (board[0][c]!=EMPTY && board[0][c]==board[1][c] && board[1][c]==board[2][c] && board[2][c]==board[3][c])
                return board[0][c];
        if (board[0][0]!=EMPTY && board[0][0]==board[1][1] && board[1][1]==board[2][2] && board[2][2]==board[3][3])
            return board[0][0];
        if (board[0][3]!=EMPTY && board[0][3]==board[1][2] && board[1][2]==board[2][1] && board[2][1]==board[3][0])
            return board[0][3];
        return EMPTY;
    }

    public boolean isBoardFull() {
        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                if (board[r][c] == EMPTY) return false;
        return true;
    }

    public void addScore(int player) {
        if (player == PLAYER_X) scoreX++;
        else scoreO++;
    }
}