package com.example.quiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SQLiteOpenHelper dbHelper;
    SQLiteDatabase database;
    TextView questionText, questionCount, scoreDisplay, totalAttempts;
    Button option1, option2, option3, option4, historyButton;
    List<Question> questionsList = new ArrayList<>();
    int currentQuestionIndex = 0, score = 0, attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        questionText = findViewById(R.id.questionText);
        questionCount = findViewById(R.id.questionCount);
        scoreDisplay = findViewById(R.id.scoreDisplay);
        totalAttempts = findViewById(R.id.totalAttempts);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        historyButton = findViewById(R.id.historyButton);

        // Initialize Database
        dbHelper = new QuizDbHelper(this);
        database = dbHelper.getWritableDatabase();

        // Initialize static questions
        initStaticQuestions();

        // Fetch 15 random questions
        Collections.shuffle(questionsList);
        if (questionsList.size() > 15) {
            questionsList = questionsList.subList(0, 15);
        }

        // Display the first question
        displayQuestion();

        // Set listeners for options
        View.OnClickListener optionClickListener = view -> checkAnswer((Button) view);
        option1.setOnClickListener(optionClickListener);
        option2.setOnClickListener(optionClickListener);
        option3.setOnClickListener(optionClickListener);
        option4.setOnClickListener(optionClickListener);

        // Manage quiz history with SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("QuizHistory", MODE_PRIVATE);
        attempts = sharedPreferences.getInt("attempts", 0); // Get saved attempts

        // Set listener for history button
        historyButton.setOnClickListener(v -> {
            // Switch to HistoryFragment
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("attempts", attempts);
            startActivity(intent);
        });
    }

    private void initStaticQuestions() {
        questionsList.add(new Question(
                "What is the capital of France?",
                "Berlin", "Madrid", "Paris", "Rome", "Paris"
        ));
        questionsList.add(new Question(
                "What is 2 + 2?",
                "3", "4", "5", "6", "4"
        ));
        questionsList.add(new Question(
                "Which planet is known as the Red Planet?",
                "Earth", "Mars", "Jupiter", "Venus", "Mars"
        ));
        // Add more questions here
    }

    private void displayQuestion() {
        resetButtonColors();
        if (currentQuestionIndex < questionsList.size()) {
            Question currentQuestion = questionsList.get(currentQuestionIndex);
            questionText.setText(currentQuestion.getQuestion());
            questionCount.setText("Question " + (currentQuestionIndex + 1) + " of " + questionsList.size());
            option1.setText(currentQuestion.getOption1());
            option2.setText(currentQuestion.getOption2());
            option3.setText(currentQuestion.getOption3());
            option4.setText(currentQuestion.getOption4());
        } else {
            // Quiz finished
            saveScore();
            displayFinalScore();
        }
    }

    private void checkAnswer(Button selectedOption) {
        Question currentQuestion = questionsList.get(currentQuestionIndex);

        // Check if the selected option is correct
        if (selectedOption.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            score++;
            selectedOption.setTextColor(Color.GREEN);
        } else {
            selectedOption.setTextColor(Color.RED);
        }

        // Proceed to next question after a short delay
        selectedOption.postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 1000); // 1 second delay before displaying the next question
    }

    private void resetButtonColors() {
        // Reset all button colors to default
        option1.setTextColor(ContextCompat.getColor(this, R.color.option_button_default));
        option2.setTextColor(ContextCompat.getColor(this, R.color.option_button_default));
        option3.setTextColor(ContextCompat.getColor(this, R.color.option_button_default));
        option4.setTextColor(ContextCompat.getColor(this, R.color.option_button_default));
    }

    private void saveScore() {
        database.execSQL("INSERT INTO attempts (score) VALUES (" + score + ")");
        updateTotalAttempts();
    }

    private void updateTotalAttempts() {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM attempts", null);
        if (cursor.moveToFirst()) {
            int attempts = cursor.getInt(0);
            totalAttempts.setVisibility(View.VISIBLE);
            totalAttempts.setText("Total Attempts: " + attempts);
        }
        cursor.close();
    }

    private void displayFinalScore() {
        questionText.setText("Quiz Over!");
        questionCount.setText("");
        option1.setVisibility(View.GONE);
        option2.setVisibility(View.GONE);
        option3.setVisibility(View.GONE);
        option4.setVisibility(View.GONE);

        scoreDisplay.setVisibility(View.VISIBLE);
        scoreDisplay.setText("Your Score: " + score + "/" + questionsList.size());

        historyButton.setVisibility(View.VISIBLE);
    }

    static class Question {
        private final String question, option1, option2, option3, option4, correctAnswer;

        public Question(String question, String option1, String option2, String option3, String option4, String correctAnswer) {
            this.question = question;
            this.option1 = option1;
            this.option2 = option2;
            this.option3 = option3;
            this.option4 = option4;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public String getOption1() {
            return option1;
        }

        public String getOption2() {
            return option2;
        }

        public String getOption3() {
            return option3;
        }

        public String getOption4() {
            return option4;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }
}
