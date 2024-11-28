package com.example.quiz;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HistoryFragment extends Fragment {

    SQLiteOpenHelper dbHelper;
    SQLiteDatabase database;
    TextView historyTextView;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize UI components
        historyTextView = view.findViewById(R.id.historyTextView);

        // Initialize Database
        dbHelper = new QuizDbHelper(getActivity()); // Use getActivity() for context
        database = dbHelper.getReadableDatabase();

        // Retrieve the score and attempts passed from the MainActivity
        if (getArguments() != null) {
            int score = getArguments().getInt("score", 0);
            int attempts = getArguments().getInt("attempts", 0);

            // Fetch and display history
            fetchHistory(score, attempts);
        }

        return view;
    }

    private void fetchHistory(int score, int attempts) {
        // Query all attempts from the database
        Cursor cursor = database.rawQuery("SELECT * FROM attempts ORDER BY score DESC", null); // Order by score to get highest first
        StringBuilder history = new StringBuilder();

        // Variable to keep track of the highest score
        int highScore = 0;

        // Check if the cursor contains data
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

        // If no attempts are found, set a default message
        if (history.length() == 0) {
            history.append("No attempts found.");
        }

        // Display the high score and history
        String displayText = "High Score: " + highScore + "\n\n" + history.toString();
        historyTextView.setText(displayText);
    }
}
