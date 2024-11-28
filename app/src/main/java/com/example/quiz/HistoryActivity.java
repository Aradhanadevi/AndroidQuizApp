package com.example.quiz;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {

    SQLiteOpenHelper dbHelper;
    SQLiteDatabase database;
    TextView historyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize UI components
        historyTextView = findViewById(R.id.historyTextView);

        // Initialize Database
        dbHelper = new QuizDbHelper(this); // No need to reference MainActivity anymore
        database = dbHelper.getReadableDatabase();

        // Retrieve the score and attempts passed from MainActivity
        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);
        int attempts = intent.getIntExtra("attempts", 0);

        // Fetch and display history
        fetchHistory(score, attempts);
    }

    private void fetchHistory(int score, int attempts) {
        // Query all attempts from the database
        Cursor cursor = database.rawQuery("SELECT * FROM attempts ORDER BY score DESC", null); // Order by score to get highest first
        StringBuilder history = new StringBuilder();

        // Variable to keep track of the highest score
        int highScore = 0;

        // Fetch all attempts and find the highest score
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int storedScore = cursor.getInt(cursor.getColumnIndex("score"));

                // Keep track of the highest score
                if (storedScore > highScore) {
                    highScore = storedScore;
                }

                // Append each attempt's details to the history
                history.append("Attempt ").append(id).append(": Score = ").append(storedScore).append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Display the high score and history
        String displayText = "High Score: " + highScore + "\n\n" + history.toString();
        historyTextView.setText(displayText);
    }
}
