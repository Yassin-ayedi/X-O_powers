package com.example.xopowers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnHowTo = findViewById(R.id.btnHowTo);
        TextView tvVersion = findViewById(R.id.tvVersion);

        tvVersion.setText("v1.0 ⚡");

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            startActivity(intent);
        });

        btnHowTo.setOnClickListener(v -> showHowToPlay());
    }

    private void showHowToPlay() {
        String msg =
                "4×4 board, two players: X and O\n\n" +
                        "Each turn:\n" +
                        "1. Pick 1 of 3 random power cards\n" +
                        "2. Use it — that IS your turn!\n\n" +
                        "⚡ POWERS:\n\n" +
                        "✖  None — Place your mark normally\n\n" +
                        "💣 Bomb — Tap an opponent's cell\n" +
                        "      to destroy it\n\n" +
                        "🛡 Shield — Tap your own cell to\n" +
                        "      protect it for 2 turns\n\n" +
                        "🃏 Steal — Tap an opponent's cell\n" +
                        "      to convert it to yours\n\n" +
                        "⚡ Double — Place 2 marks this turn\n\n" +
                        "⛓ Chain — Block an empty cell for\n" +
                        "      3 rounds (opponent can't use it)\n\n" +
                        "🪞 Mirror — Convert opponent's last\n" +
                        "      placed cell to yours\n\n" +
                        "🃏 Wild — Place anywhere on the board\n\n" +
                        "🏆 Get 4 in a row to win the round!\n" +
                        "First to win 2 rounds wins the match.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚡ How To Play")
                .setMessage(msg)
                .setPositiveButton("Let's Play!", null)
                .show();
    }
}