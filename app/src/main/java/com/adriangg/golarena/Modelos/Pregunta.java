package com.adriangg.golarena.Modelos;

import java.util.ArrayList;

public class Pregunta {
    String question;
    String correctAnswer;
    ArrayList<String> options;
    String difficulty;
    String category;

    public Pregunta(String question, String correctAnswer,
                    ArrayList<String> options,
                    String difficulty, String category) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.difficulty = difficulty;
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
