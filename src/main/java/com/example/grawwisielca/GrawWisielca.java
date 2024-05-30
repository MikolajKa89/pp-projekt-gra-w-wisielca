package com.example.grawwisielca;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class GrawWisielca extends Application {
    private final ArrayList<String> easyWords = new ArrayList<>();
    private final ArrayList<String> mediumWords = new ArrayList<>();
    private final ArrayList<String> hardWords = new ArrayList<>();
    private int totalWins = 0;
    private int totalLosses = 0;
    private Label wordLabel;
    private Label attemptsLabel;
    private Label guessedLabel;
    private TextField guessField;
    private String wordToGuess;
    private StringBuilder hiddenWord;
    private int attemptsLeft;
    private ArrayList<Character> guessedLetters;
    private Canvas canvas;
    private Button guessButton;
    private Button easyButton;
    private Button mediumButton;
    private Button hardButton;
    private Button wordButton; // Poprawione na camelCase
    private Button statsButton;
    private Button backButton;
    private Scene gameOptionsScene;
    private Scene gameScene;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeWords();
        createGameOptionsScene();
        createGameScene();
        primaryStage.setScene(gameOptionsScene);
        primaryStage.setTitle("Gra w Wisielca");
        primaryStage.show();
    }

    private void createGameOptionsScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        easyButton = createStyledButton("Easy", false);
        easyButton.setOnAction(e -> startNewGame("easy"));
        mediumButton = createStyledButton("Medium", false);
        mediumButton.setOnAction(e -> startNewGame("medium"));
        hardButton = createStyledButton("Hard", false);
        hardButton.setOnAction(e -> startNewGame("hard"));
        wordButton = createStyledButton("Use Word", false); // Poprawione na camelCase
        wordButton.setOnAction(e -> showManualWordDialog(primaryStage));
        statsButton = createStyledButton("Statistics", false);
        statsButton.setOnAction(e -> showStatistics());
        VBox buttonBox = new VBox(15, easyButton, mediumButton, hardButton, wordButton, statsButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 10, 0, 10));
        root.setCenter(buttonBox);
        gameOptionsScene = new Scene(root, 500, 500);
    }

    private void createGameScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        backButton = createStyledButton("Back", true);
        backButton.setOnAction(e -> primaryStage.setScene(gameOptionsScene));
        BorderPane.setAlignment(backButton, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(backButton, new Insets(0, 0, 10, 0));
        root.setTop(backButton);
        wordLabel = new Label();
        wordLabel.setStyle("-fx-font-size: 24px;");
        attemptsLabel = new Label();
        attemptsLabel.setStyle("-fx-font-size: 24px;");
        guessedLabel = new Label();
        guessedLabel.setStyle("-fx-font-size: 24px;");
        guessField = new TextField();
        guessField.setPromptText("Enter a letter");
        guessField.setOnKeyPressed(this::handleKeyPressed);
        guessField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z]*")) {
                guessField.setText(newValue.replaceAll("[^a-zA-Z]", ""));
            }
        });
        guessButton = createStyledButton("Guess", true);
        guessButton.setOnAction(e -> makeGuess());
        canvas = new Canvas(200, 200);
        VBox infoBox = new VBox(5, wordLabel, attemptsLabel, guessedLabel, canvas, guessField, guessButton);
        infoBox.setAlignment(Pos.TOP_LEFT);
        infoBox.setPadding(new Insets(10, 0, 0, 0));
        root.setCenter(infoBox);
        gameScene = new Scene(root, 500, 500);
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            makeGuess();
        }
    }

    private Button createStyledButton(String text, boolean isSmall) {
        Button button = new Button(text);
        String style = "-fx-background-color: #2E2E2E; -fx-text-fill: white; -fx-font-size: ";
        style += isSmall ? "14px" : "18px";
        style += "; -fx-padding: ";
        style += isSmall ? "5px 10px" : "10px 20px";
        style += "; -fx-border-radius: 10; -fx-background-radius: 10;";
        button.setStyle(style);
        button.setMinWidth(isSmall ? 75 : 150);
        return button;
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
        initializeGame();
        primaryStage.setScene(gameScene);
    }

    private void showManualWordDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Enter Manual Word");
        VBox dialogVbox = new VBox(20);
        dialogVbox.setPadding(new Insets(10, 20, 10, 20));
        TextField manualWordInput = new TextField();
        manualWordInput.setPromptText("Enter your word here");
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            wordToGuess = manualWordInput.getText().trim().toLowerCase();
            dialog.close();
            initializeGame();
            primaryStage.setScene(gameScene);
        });
        dialogVbox.getChildren().addAll(new Label("Enter a word to guess:"), manualWordInput, submitButton);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void initializeGame() {
        hiddenWord = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            hiddenWord.append("_");
        }
        attemptsLeft = wordToGuess.length() * 2; // Zwiększono liczbę prób.
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
                    primaryStage.setScene(gameOptionsScene);
                } else if (attemptsLeft == 0) {
                    totalLosses++;
                    showLossMessage();
                    primaryStage.setScene(gameOptionsScene);
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
        alert.setContentText("You lost! The word was: " + wordToGuess);
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
        int mistakes = wordToGuess.length() * 2 - attemptsLeft; // Poprawiona logika błędów.
        switch (mistakes) {
            case 1:
                gc.strokeLine(10, 190, 190, 190);
                break;
            case 2:
                gc.strokeLine(50, 190, 50, 10);
                break;
            case 3:
                gc.strokeLine(50, 10, 150, 10);
                break;
            case 4:
                gc.strokeLine(150, 10, 150, 30);
                break;
            case 5:
                gc.strokeOval(140, 30, 20, 20);
                break;
            case 6:
                gc.strokeLine(150, 50, 150, 100);
                gc.strokeLine(150, 60, 130, 80);
                gc.strokeLine(150, 60, 170, 80);
                gc.strokeLine(150, 100, 130, 140);
                gc.strokeLine(150, 100, 170, 140);
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
