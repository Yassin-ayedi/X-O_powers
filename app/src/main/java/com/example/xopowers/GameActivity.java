package com.example.xopowers;

import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private static final int BOARD_SIZE = GameState.BOARD_SIZE;

    private GameState gameState;
    private PowerDeal powerDeal;

    private Button[][] boardButtons;
    private GridLayout boardGrid;

    private Button[] powerButtons = new Button[3];
    private Power[] currentPowers = new Power[3];
    private Power selectedPower = null;

    private TextView tvCurrentPlayer;
    private TextView tvScore;
    private TextView tvPowerInfo;
    private TextView tvRound;

    // Which phase are we in?
    // PICK_POWER: player must pick one of the 3 powers
    // PICK_CELL:  player must tap a board cell to execute the power
    private enum Phase { PICK_POWER, PICK_CELL }
    private Phase phase = Phase.PICK_POWER;

    // For DOUBLE power: waiting for 2nd cell
    private boolean doubleSecondNeeded = false;

    private int currentRound = 1;
    private static final int MAX_ROUNDS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameState = new GameState();
        powerDeal = new PowerDeal();

        initUI();
        startNewRound();
    }

    private void initUI() {
        boardGrid = findViewById(R.id.boardGrid);
        tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);
        tvScore = findViewById(R.id.tvScore);
        tvPowerInfo = findViewById(R.id.tvPowerInfo);
        tvRound = findViewById(R.id.tvRound);

        powerButtons[0] = findViewById(R.id.btnPower0);
        powerButtons[1] = findViewById(R.id.btnPower1);
        powerButtons[2] = findViewById(R.id.btnPower2);

        boardButtons = new Button[BOARD_SIZE][BOARD_SIZE];
        boardGrid.removeAllViews();
        boardGrid.setColumnCount(BOARD_SIZE);
        boardGrid.setRowCount(BOARD_SIZE);

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                final int row = r, col = c;
                Button btn = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(c, 1f);
                params.rowSpec = GridLayout.spec(r, 1f);
                params.setMargins(6, 6, 6, 6);
                btn.setLayoutParams(params);
                btn.setTextSize(28f);
                btn.setTextColor(Color.WHITE);
                btn.setBackgroundResource(R.drawable.cell_bg);
                btn.setOnClickListener(v -> onCellClicked(row, col));
                boardButtons[r][c] = btn;
                boardGrid.addView(btn);
            }
        }

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            powerButtons[i].setOnClickListener(v -> onPowerSelected(idx));
        }

        Button btnQuit = findViewById(R.id.btnQuit);
        btnQuit.setOnClickListener(v -> finish());
    }

    // -------------------------------------------------------
    // ROUND / TURN MANAGEMENT
    // -------------------------------------------------------

    private void startNewRound() {
        gameState.reset();
        doubleSecondNeeded = false;
        selectedPower = null;
        phase = Phase.PICK_POWER;

        tvRound.setText("Round " + currentRound + " / " + MAX_ROUNDS);
        refreshBoard();
        beginTurn();
    }

    /** Called at the start of every new turn: deal 3 powers, wait for pick */
    private void beginTurn() {
        phase = Phase.PICK_POWER;
        selectedPower = null;
        doubleSecondNeeded = false;

        currentPowers = powerDeal.deal();
        for (int i = 0; i < 3; i++) {
            Power p = currentPowers[i];
            powerButtons[i].setText(p.getIcon() + "\n" + shortName(p));
            try {
                powerButtons[i].setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor(p.getColor())));
            } catch (Exception ignored) {}
            powerButtons[i].setAlpha(1f);
            powerButtons[i].setEnabled(true);
            powerButtons[i].setScaleX(1f);
            powerButtons[i].setScaleY(1f);
        }

        setBoardEnabled(false); // board disabled until power is picked
        updateStatusUI();
        tvPowerInfo.setText("🎴 " + playerName(gameState.getCurrentPlayer()) + ": Pick a power!");
    }

    // -------------------------------------------------------
    // POWER SELECTION
    // -------------------------------------------------------

    private void onPowerSelected(int idx) {
        // If DOUBLE is in progress (first cell placed), can't change power
        if (doubleSecondNeeded) {
            toast("⚡ You already placed the first mark! Place the second.");
            return;
        }

        // Allow re-selecting a different power freely before acting on the board
        // If we were in PICK_CELL phase, go back to pick power (cancel previous selection)
        if (phase == Phase.PICK_CELL) {
            // Reset board state — no action was taken yet, safe to switch
            phase = Phase.PICK_POWER;
            setBoardEnabled(false);
        }

        selectedPower = currentPowers[idx];

        // Highlight selected button — keep all enabled so player can still switch
        for (int i = 0; i < 3; i++) {
            powerButtons[i].setAlpha(i == idx ? 1f : 0.5f);
            powerButtons[i].setScaleX(i == idx ? 1.08f : 1f);
            powerButtons[i].setScaleY(i == idx ? 1.08f : 1f);
        }

        // Handle powers that act INSTANTLY (no board tap needed)
        switch (selectedPower) {
            case MIRROR:
                confirmAndExecute("🪞 Mirror", "Convert your opponent's last placed cell to yours?", this::executeMirror);
                return;
            default:
                break;
        }

        // Powers that need a board tap — go to PICK_CELL phase
        phase = Phase.PICK_CELL;
        setBoardEnabled(true);

        switch (selectedPower) {
            case NONE:
                tvPowerInfo.setText("✖ No power — tap an empty cell. (Tap another power to switch)");
                break;
            case BOMB:
                tvPowerInfo.setText("💣 Bomb — tap an OPPONENT's cell to destroy it. (Tap another power to switch)");
                break;
            case SHIELD:
                tvPowerInfo.setText("🛡 Shield — tap YOUR OWN cell to protect it for 2 turns. (Tap another power to switch)");
                break;
            case DOUBLE:
                tvPowerInfo.setText("⚡ Double — tap TWO empty cells. (Tap another power to switch)");
                break;
            case STEAL:
                tvPowerInfo.setText("🃏 Steal — tap an OPPONENT's cell to convert it to yours. (Tap another power to switch)");
                break;
            case CHAIN:
                tvPowerInfo.setText("⛓ Chain — tap an EMPTY cell to block it for a full round. (Tap another power to switch)");
                break;
            default:
                break;
        }
    }

    /** Shows a confirm dialog before firing an instant power */
    private void confirmAndExecute(String title, String message, Runnable action) {
        new AlertDialog.Builder(this, R.style.PowerDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Use it!", (d, w) -> action.run())
                .setNegativeButton("Go back", (d, w) -> {
                    // Reset selection so player can pick again
                    selectedPower = null;
                    phase = Phase.PICK_POWER;
                    for (int i = 0; i < 3; i++) {
                        powerButtons[i].setAlpha(1f);
                        powerButtons[i].setScaleX(1f);
                        powerButtons[i].setScaleY(1f);
                    }
                    tvPowerInfo.setText("🎴 Pick a power!");
                })
                .setCancelable(false)
                .show();
    }

    // -------------------------------------------------------
    // CELL TAPPED
    // -------------------------------------------------------

    private void onCellClicked(int row, int col) {
        if (phase != Phase.PICK_CELL) {
            toast("Pick a power first!");
            return;
        }

        boolean turnOver = false;

        switch (selectedPower) {
            case NONE:
                if (!gameState.placeNormal(row, col)) {
                    toast(frozenMessage(row, col));
                    return;
                }
                animatePlacement(row, col);
                turnOver = true;
                break;

            case BOMB:
                if (gameState.getCell(row, col) != opponent()) {
                    toast("💣 Tap an OPPONENT's cell!");
                    return;
                }
                if (gameState.isShielded(row, col)) {
                    toast("🛡 That cell is shielded! Can't bomb it.");
                    return;
                }
                gameState.applyBomb(row, col);
                animateEffect(row, col, "💥");
                turnOver = true;
                break;

            case SHIELD:
                if (gameState.getCell(row, col) != gameState.getCurrentPlayer()) {
                    toast("🛡 Tap YOUR OWN marked cell!");
                    return;
                }
                gameState.applyShield(row, col);
                animateEffect(row, col, "🛡");
                turnOver = true;
                break;

            case DOUBLE:
                boolean done = gameState.placeDouble(row, col);
                animatePlacement(row, col);
                if (!done) {
                    // First placement done, wait for second
                    doubleSecondNeeded = true;
                    tvPowerInfo.setText("⚡ Good! Now tap your SECOND cell.");
                    refreshBoard();
                    return;
                }
                turnOver = true;
                break;

            case STEAL:
                if (gameState.getCell(row, col) != opponent()) {
                    toast("🃏 Tap an OPPONENT's cell!");
                    return;
                }
                if (gameState.isShielded(row, col)) {
                    toast("🛡 That cell is shielded! Can't steal it.");
                    return;
                }
                gameState.applySteal(row, col);
                animateEffect(row, col, "🃏");
                turnOver = true;
                break;

            case CHAIN:
                if (gameState.getCell(row, col) != GameState.EMPTY) {
                    toast("⛓ Tap an EMPTY cell to chain it!");
                    return;
                }
                gameState.applyChain(row, col);
                animateEffect(row, col, "⛓");
                turnOver = true;
                break;

            case WILDCARD:
                if (!gameState.placeWildcard(row, col)) {
                    toast("Can't place there!");
                    return;
                }
                animatePlacement(row, col);
                turnOver = true;
                break;

            default:
                break;
        }

        if (turnOver) {
            refreshBoard();
            if (checkRoundOver()) return;
            beginTurn();
        }
    }

    // -------------------------------------------------------
    // INSTANT POWERS (no board tap)
    // -------------------------------------------------------

    private void executeMirror() {
        boolean ok = gameState.applyMirror();
        refreshBoard();
        toast(ok ? "🪞 Mirrored opponent's last move!" : "🪞 Mirror failed — no last move to copy. Turn skipped.");
        if (!ok) gameState.forceSwitchPlayer();
        if (checkRoundOver()) return;
        beginTurn();
    }

    // -------------------------------------------------------
    // UI HELPERS
    // -------------------------------------------------------

    private void refreshBoard() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int cell = gameState.getCell(r, c);
                boolean shielded = gameState.isShielded(r, c);
                boolean frozen = gameState.isFrozen(r, c);

                String text = "";
                int textColor = Color.WHITE;

                if (cell == GameState.PLAYER_X) {
                    text = shielded ? "X🛡" : "X";
                    textColor = Color.parseColor("#FF6B6B");
                    boardButtons[r][c].setBackgroundResource(R.drawable.cell_filled);
                } else if (cell == GameState.PLAYER_O) {
                    text = shielded ? "O🛡" : "O";
                    textColor = Color.parseColor("#4ECDC4");
                    boardButtons[r][c].setBackgroundResource(R.drawable.cell_filled);
                } else if (frozen) {
                    text = "⛓";
                    textColor = Color.parseColor("#FF9800");
                    boardButtons[r][c].setBackgroundResource(R.drawable.cell_frozen);
                } else {
                    text = "";
                    boardButtons[r][c].setBackgroundResource(R.drawable.cell_bg);
                }
                boardButtons[r][c].setTextSize(28f);

                boardButtons[r][c].setText(text);
                boardButtons[r][c].setTextColor(textColor);
            }
        }
    }

    private void updateStatusUI() {
        int player = gameState.getCurrentPlayer();
        String name = playerName(player);
        int color = (player == GameState.PLAYER_X) ?
                Color.parseColor("#FF6B6B") : Color.parseColor("#4ECDC4");
        tvCurrentPlayer.setText("⚔ " + name + "'s Turn");
        tvCurrentPlayer.setTextColor(color);
        tvScore.setText("X: " + gameState.getScoreX() + "  |  O: " + gameState.getScoreO());
    }

    private void setBoardEnabled(boolean enabled) {
        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                boardButtons[r][c].setEnabled(enabled);
    }

    private void animatePlacement(int row, int col) {
        Button btn = boardButtons[row][col];
        btn.setScaleX(0f);
        btn.setScaleY(0f);
        btn.animate().scaleX(1f).scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateEffect(int row, int col, String emoji) {
        Button btn = boardButtons[row][col];
        String orig = btn.getText().toString();
        btn.setText(emoji);
        btn.animate().scaleX(1.4f).scaleY(1.4f).setDuration(120).withEndAction(() ->
                btn.animate().scaleX(1f).scaleY(1f).setDuration(120).withEndAction(() ->
                        btn.setText(orig)).start()).start();
    }

    private boolean checkRoundOver() {
        int winner = gameState.checkWinner();
        if (winner != GameState.EMPTY) {
            gameState.addScore(winner);
            showRoundResult(playerName(winner) + " wins the round! 🎉");
            return true;
        }
        if (gameState.isBoardFull()) {
            showRoundResult("It's a Draw! 🤝");
            return true;
        }
        return false;
    }

    private void showRoundResult(String message) {
        setBoardEnabled(false);
        for (int i = 0; i < 3; i++) powerButtons[i].setEnabled(false);

        String scoreMsg = "X: " + gameState.getScoreX() + "  |  O: " + gameState.getScoreO();
        boolean matchOver = gameState.getScoreX() >= 2 || gameState.getScoreO() >= 2 || currentRound >= MAX_ROUNDS;

        AlertDialog.Builder b = new AlertDialog.Builder(this, R.style.PowerDialog);
        b.setCancelable(false);

        if (matchOver) {
            String champ = gameState.getScoreX() > gameState.getScoreO() ? "🏆 Player X" :
                    gameState.getScoreO() > gameState.getScoreX() ? "🏆 Player O" : "It's a tie!";
            b.setTitle("MATCH OVER!");
            b.setMessage(message + "\n\nFinal Score: " + scoreMsg + "\n\nChampion: " + champ);
            b.setPositiveButton("Play Again", (d, w) -> restartMatch());
            b.setNegativeButton("Main Menu", (d, w) -> finish());
        } else {
            currentRound++;
            b.setTitle("⚡ " + message);
            b.setMessage("Score: " + scoreMsg + "\n\nReady for Round " + currentRound + "?");
            b.setPositiveButton("Next Round ▶", (d, w) -> {
                setBoardEnabled(true);
                for (int i = 0; i < 3; i++) powerButtons[i].setEnabled(true);
                startNewRound();
            });
        }
        b.show();
    }

    private void restartMatch() {
        currentRound = 1;
        gameState = new GameState();
        startNewRound();
    }

    // -------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------

    private String playerName(int player) {
        return player == GameState.PLAYER_X ? "Player X" : "Player O";
    }

    private int opponent() {
        return gameState.getCurrentPlayer() == GameState.PLAYER_X ?
                GameState.PLAYER_O : GameState.PLAYER_X;
    }

    private String frozenMessage(int row, int col) {
        if (gameState.isFrozen(row, col) && gameState.getFrozenByPlayer() != gameState.getCurrentPlayer())
            return "❄ That cell is frozen!";
        return "Can't place there!";
    }

    private String shortName(Power p) {
        return p.getDisplayName().replaceAll("[💣🛡🔄⚡❄🪞🃏✖] ", "");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}