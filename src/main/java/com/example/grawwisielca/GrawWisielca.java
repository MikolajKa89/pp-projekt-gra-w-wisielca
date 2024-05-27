package com.example.grawwisielca;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class GrawWisielca extends Application {
    private final ArrayList<String> easyWords = new ArrayList<>();
    private ArrayList<String> mediumWords = new ArrayList<>();
    private ArrayList<String> hardWords = new ArrayList<>();
    private int totalWins = 0;
    private int totalLosses = 0;

    private Label wordLabel;
    private Label attemptsLabel;
    private Label guessedLabel;
    private TextField guessField;
    private TextField manualWordField;

    private String wordToGuess;
    private StringBuilder hiddenWord;
    private int attemptsLeft;
    private ArrayList<Character> guessedLetters;

    private Canvas canvas;

    private Button guessButton;
    private Button easyButton;
    private Button mediumButton;
    private Button hardButton;
    private Button manualWordButton;
    private Button statsButton;
    private Button backButton;

    @Override
    public void start(Stage primaryStage) {
        initializeWords();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Set background image
        Image backgroundImage = new Image("file:library.jpg");
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, true, false));
        root.setBackground(new Background(background));

        wordLabel = new Label();
        attemptsLabel = new Label();
        guessedLabel = new Label();
        guessField = new TextField();
        guessField.setPromptText("Enter a letter");
        guessButton = createStyledButton("Guess");
        guessButton.setOnAction(e -> makeGuess());

        easyButton = createStyledButton("Easy");
        easyButton.setOnAction(e -> startNewGame("easy"));

        mediumButton = createStyledButton("Medium");
        mediumButton.setOnAction(e -> startNewGame("medium"));

        hardButton = createStyledButton("Hard");
        hardButton.setOnAction(e -> startNewGame("hard"));

        manualWordButton = createStyledButton("Use Manual Word");
        manualWordButton.setOnAction(e -> startNewGameWithManualWord());

        statsButton = createStyledButton("Statistics");
        statsButton.setOnAction(e -> showStatistics());

        backButton = createStyledButton("Back");
        backButton.setOnAction(e -> showGameOptions());

        manualWordField = new TextField();
        manualWordField.setPromptText("Enter your word here");

        VBox buttonBox = new VBox(10, easyButton, mediumButton, hardButton, manualWordButton, statsButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(20));

        HBox inputBox = new HBox(10, guessField, guessButton);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(10, wordLabel, attemptsLabel, guessedLabel, inputBox, manualWordField);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        VBox leftBox = new VBox(20, buttonBox, infoBox);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setPadding(new Insets(20));

        canvas = new Canvas(200, 200);
        VBox canvasBox = new VBox(canvas);
        canvasBox.setAlignment(Pos.CENTER_RIGHT);

        HBox mainBox = new HBox(20, leftBox, canvasBox);
        mainBox.setAlignment(Pos.CENTER_LEFT);

        BorderPane.setAlignment(backButton, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(backButton, new Insets(10));
        root.setBottom(backButton);
        root.setCenter(mainBox);

        Scene scene = new Scene(root, 500, 400);  // Adjusted width to 500 to keep 1 cm padding
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hangman Game");
        primaryStage.show();

        showGameOptions();
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #2E2E2E; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10px 20px; -fx-border-radius: 10; -fx-background-radius: 10;");
        return button;
    }

    private void showGameOptions() {
        easyButton.setVisible(true);
        mediumButton.setVisible(true);
        hardButton.setVisible(true);
        manualWordButton.setVisible(true);
        statsButton.setVisible(true);
        guessField.setVisible(false);
        guessButton.setVisible(false);
        manualWordField.setVisible(true);
        wordLabel.setVisible(false);
        attemptsLabel.setVisible(false);
        guessedLabel.setVisible(false);
        backButton.setVisible(false);
    }

    private void hideGameOptions() {
        easyButton.setVisible(false);
        mediumButton.setVisible(false);
        hardButton.setVisible(false);
        manualWordButton.setVisible(false);
        statsButton.setVisible(false);
        guessField.setVisible(true);
        guessButton.setVisible(true);
        manualWordField.setVisible(false);
        wordLabel.setVisible(true);
        attemptsLabel.setVisible(true);
        guessedLabel.setVisible(true);
        backButton.setVisible(true);
    }

    private void startNewGame(String difficulty) {
        switch (difficulty) {
            case "easy":
                wordToGuess = selectRandomWord(easyWords);
                break;
            case "medium":
                wordToGuess = selectRandomWord(mediumWords);
                break;
            case "hard":
                wordToGuess = selectRandomWord(hardWords);
                break;
        }

        hideGameOptions();
        initializeGame();
    }

    private void startNewGameWithManualWord() {
        wordToGuess = manualWordField.getText().trim().toLowerCase();
        manualWordField.clear();
        hideGameOptions();
        initializeGame();
    }

    private void initializeGame() {
        hiddenWord = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            hiddenWord.append("_");
        }
        attemptsLeft = wordToGuess.length() * 2;  // Liczba prób jest dwukrotnością liczby liter w słowie
        guessedLetters = new ArrayList<>();

        updateLabels();
        clearCanvas();
    }

    private void updateLabels() {
        wordLabel.setText("Word: " + hiddenWord);
        attemptsLabel.setText("Attempts left: " + attemptsLeft);
        guessedLabel.setText("Guessed letters: " + guessedLetters);
    }

    private void makeGuess() {
        if (attemptsLeft > 0 && hiddenWord.toString().contains("_")) {
            String guessInput = guessField.getText();
            if (guessInput.length() == 1) {
                char guess = guessInput.charAt(0);
                guessField.clear();

                if (guessedLetters.contains(guess)) {
                    // Already guessed
                    return;
                }
                guessedLetters.add(guess);

                if (wordToGuess.contains(String.valueOf(guess))) {
                    for (int i = 0; i < wordToGuess.length(); i++) {
                        if (wordToGuess.charAt(i) == guess) {
                            hiddenWord.setCharAt(i, guess);
                        }
                    }
                } else {
                    attemptsLeft--;
                    drawHangman();
                }

                updateLabels();

                if (!hiddenWord.toString().contains("_")) {
                    totalWins++;
                    showWinMessage();
                    showGameOptions();
                } else if (attemptsLeft == 0) {
                    totalLosses++;
                    showLossMessage();
                    showGameOptions();
                }
            }
        }
    }

    private void showWinMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText(null);
        alert.setContentText("You won!");
        alert.showAndWait();
    }

    private void showLossMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("You lost!");
        alert.showAndWait();
    }

    private void showStatistics() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistics");
        alert.setHeaderText(null);
        alert.setContentText("Total Wins: " + totalWins + "\nTotal Losses: " + totalLosses);
        alert.showAndWait();
    }

    private void initializeWords() {
        // Dodaj słowa do odpowiednich poziomów trudności
        easyWords.add("java");
        easyWords.add("code");
        easyWords.add("learn");
        easyWords.add("test");

        mediumWords.add("planet");
        mediumWords.add("banana");
        mediumWords.add("purple");
        mediumWords.add("puzzle");

        hardWords.add("elephant");
        hardWords.add("chocolate");
        hardWords.add("developer");
        hardWords.add("hangman");
    }

    private String selectRandomWord(ArrayList<String> words) {
        Random rand = new Random();
        return words.get(rand.nextInt(words.size()));
    }

    private void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawHangman() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        switch (attemptsLeft) {
            case 5:
                // Draw base
                gc.strokeLine(10, 190, 190, 190);
                break;
            case 4:
                // Draw pole
                gc.strokeLine(50, 190, 50, 10);
                break;
            case 3:
                // Draw top beam
                gc.strokeLine(50, 10, 150, 10);
                break;
            case 2:
                // Draw rope
                gc.strokeLine(150, 10, 150, 30);
                break;
            case 1:
                // Draw head
                gc.strokeOval(140, 30, 20, 20);
                break;
            case 0:
                // Draw body
                gc.strokeLine(150, 50, 150, 100);
                // Draw arms
                gc.strokeLine(150, 60, 130, 80);
                gc.strokeLine(150, 60, 170, 80);
                // Draw legs
                gc.strokeLine(150, 100, 130, 140);
                gc.strokeLine(150, 100, 170, 140);
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
