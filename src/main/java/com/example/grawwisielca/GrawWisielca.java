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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    private String wordToGuess;
    private StringBuilder hiddenWord;
    private int attemptsLeft;
    private ArrayList<Character> guessedLetters;

    private Canvas canvas;

    @Override
    public void start(Stage primaryStage) {
        initializeWords();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        wordLabel = new Label();
        attemptsLabel = new Label();
        guessedLabel = new Label();
        guessField = new TextField();
        Button guessButton = new Button("Guess");
        guessButton.setOnAction(e -> makeGuess());

        Button easyButton = new Button("Easy");
        easyButton.setOnAction(e -> startNewGame("easy"));

        Button mediumButton = new Button("Medium");
        mediumButton.setOnAction(e -> startNewGame("medium"));

        Button hardButton = new Button("Hard");
        hardButton.setOnAction(e -> startNewGame("hard"));

        Button statsButton = new Button("Statistics");
        statsButton.setOnAction(e -> showStatistics());

        HBox buttonBox = new HBox(10, easyButton, mediumButton, hardButton, statsButton);
        buttonBox.setAlignment(Pos.CENTER);

        HBox inputBox = new HBox(10, guessField, guessButton);
        inputBox.setAlignment(Pos.CENTER);

        VBox infoBox = new VBox(10, wordLabel, attemptsLabel, guessedLabel, inputBox, buttonBox);
        infoBox.setAlignment(Pos.CENTER);

        canvas = new Canvas(200, 200);
        root.setRight(canvas);

        root.setCenter(infoBox);

        // Ustawienie koloru tła na niebieski
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hangman Game");
        primaryStage.show();

        startNewGame("easy");
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

        hiddenWord = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            hiddenWord.append("_");
        }
        attemptsLeft = 6;
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
            char guess = guessField.getText().charAt(0);
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
                startNewGame("easy"); // Powrót do poziomu łatwego
            } else if (attemptsLeft == 0) {
                totalLosses++;
                showLossMessage();
                startNewGame("easy"); // Powrót do poziomu łatwego
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