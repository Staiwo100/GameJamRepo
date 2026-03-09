import javafx.application.Application;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class ComboFighter extends Application {

    private GameState gameState;
    private Canvas gameCanvas;
    private Label comboLabel;
    private Label healthLabel;
    private Label sequenceLabel;
    private Label instructionsLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("⚔️ CONSECUTIVE COMBO FIGHTER ⚔️");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0e27;");

        // Create header
        VBox header = createHeader();
        root.setTop(header);

        // Create game canvas
        gameCanvas = new Canvas(1000, 550);
        root.setCenter(gameCanvas);

        // Create footer with controls
        VBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root);
        
        // Setup keyboard input
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.setOnKeyReleased(this::handleKeyRelease);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Initialize game
        gameState = new GameState();
        
        // Start game loop
        startGameLoop();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1e3f, #0a0e27); -fx-padding: 20;");
        header.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("⚔️ CONSECUTIVE COMBO FIGHTER 🥊");
        titleLabel.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        titleLabel.setFont(Font.font("Arial Black", FontWeight.BOLD, 32));

        instructionsLabel = new Label("Use: Q=Punch | W=Kick | E=Dodge | R=Special | Follow the sequence!");
        instructionsLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #a8dadc; -fx-font-style: italic;");

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-padding: 10;");

        comboLabel = new Label("COMBO: 0/5");
        comboLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ffd700; -fx-font-weight: bold;");

        sequenceLabel = new Label("REQUIRED: PUNCH → KICK → DODGE → SPECIAL");
        sequenceLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #4ecdc4;");

        healthLabel = new Label("ENEMY HP: 100/100");
        healthLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");

        statsBox.getChildren().addAll(comboLabel, sequenceLabel, healthLabel);

        header.getChildren().addAll(titleLabel, instructionsLabel, statsBox);
        return header;
    }

    private VBox createFooter() {
        VBox footer = new VBox(10);
        footer.setStyle("-fx-background-color: #1a1e3f; -fx-padding: 15;");
        footer.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button resetButton = new Button("⟲ RESET GAME");
        resetButton.setStyle("-fx-padding: 10 30; -fx-font-size: 14; -fx-background-color: #ff6b6b; " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        resetButton.setOnAction(e -> {
            gameState = new GameState();
            gameCanvas.getGraphicsContext2D().clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
            updateUI();
        });

        Label helpLabel = new Label("🎮 Follow the attack sequence without breaking the combo!");
        helpLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #a8dadc;");

        footer.getChildren().addAll(buttonBox, helpLabel);
        buttonBox.getChildren().add(resetButton);
        
        return footer;
    }

    private void handleKeyPress(KeyEvent event) {
        if (gameState.isGameOver) return;

        KeyCode code = event.getCode();
        ComboMove move = null;

        switch (code) {
            case Q: move = ComboMove.PUNCH; break;
            case W: move = ComboMove.KICK; break;
            case E: move = ComboMove.DODGE; break;
            case R: move = ComboMove.SPECIAL; break;
            default: return;
        }

        gameState.playerAttemptMove(move);
        updateUI();
    }

    private void handleKeyRelease(KeyEvent event) {
        // Could be used for timing mechanics if needed
    }

    private void updateUI() {
        int comboProgress = gameState.currentComboIndex;
        int requiredLength = gameState.requiredSequence.length;
        comboLabel.setText(String.format("COMBO: %d/%d", comboProgress, requiredLength));

        if (gameState.isComboSuccessful) {
            comboLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ffd700; -fx-font-weight: bold;");
        } else {
            comboLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
        }

        healthLabel.setText(String.format("ENEMY HP: %d/100", Math.max(0, gameState.enemyHealth)));

        StringBuilder seqStr = new StringBuilder("REQUIRED: ");
        for (int i = 0; i < gameState.requiredSequence.length; i++) {
            if (i < gameState.currentComboIndex) {
                seqStr.append("✓ ");
            }
            seqStr.append(gameState.requiredSequence[i].toString()).append(" → ");
        }
        seqStr.setLength(seqStr.length() - 3); // Remove last arrow
        sequenceLabel.setText(seqStr.toString());

        if (gameState.isGameOver) {
            if (gameState.enemyHealth <= 0) {
                instructionsLabel.setText("🎉 VICTORY! You defeated the enemy!");
                instructionsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #ffd700; -fx-font-weight: bold;");
            } else {
                instructionsLabel.setText("💥 DEFEAT! You were overwhelmed!");
                instructionsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
            }
        }
    }

    private void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastFrameTime = 0;
            private static final long FRAME_INTERVAL = 16_666_667; // ~60 FPS

            @Override
            public void handle(long now) {
                if (now - lastFrameTime >= FRAME_INTERVAL) {
                    gameState.update();
                    render();
                    lastFrameTime = now;
                }
            }
        };
        gameLoop.start();
    }

    private void render() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        
        // Clear canvas with gradient background
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, gameCanvas.getHeight(),
            false, null,
            new Stop(0, Color.web("#1a1e3f")),
            new Stop(1, Color.web("#0a0e27"))
        );
        gc.setFill(gradient);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Draw grid background
        gc.setStroke(Color.web("#2a2e4f", 0.3));
        gc.setLineWidth(1);
        for (int i = 0; i < gameCanvas.getWidth(); i += 50) {
            gc.strokeLine(i, 0, i, gameCanvas.getHeight());
        }
        for (int i = 0; i < gameCanvas.getHeight(); i += 50) {
            gc.strokeLine(0, i, gameCanvas.getWidth(), i);
        }

        // Draw player
        gameState.player.render(gc);

        // Draw enemy
        gameState.enemy.render(gc);

        // Draw combo feedback
        if (gameState.lastMoveTime > 0 && System.currentTimeMillis() - gameState.lastMoveTime < 500) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            gc.setFill(gameState.isComboSuccessful ? Color.web("#ffd700") : Color.web("#ff6b6b"));
            String text = gameState.isComboSuccessful ? "✓ COMBO!" : "✗ RESET!";
            gc.fillText(text, gameCanvas.getWidth() / 2 - 80, 100);
        }

        // Draw combo counter animation
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 48));
        if (gameState.currentComboIndex > 0) {
            int pulseVal = (int) (255 * Math.abs(Math.sin(System.currentTimeMillis() / 200.0)));
            gc.setFill(Color.web("#ffd700", 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 200.0)));
            gc.fillText(gameState.currentComboIndex + "/" + gameState.requiredSequence.length, 
                       gameCanvas.getWidth() - 200, 80);
        }

        // Draw difficulty indicator
        gc.setFont(Font.font("Arial", 14));
        gc.setFill(Color.web("#a8dadc"));
        int wave = gameState.waveNumber;
        gc.fillText("WAVE: " + wave + " | SPEED: " + (100 + wave * 20) + "%", 20, 30);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

enum ComboMove {
    PUNCH("PUNCH 👊"),
    KICK("KICK 🦵"),
    DODGE("DODGE 🛡️"),
    SPECIAL("SPECIAL ⚡");

    private final String displayName;

    ComboMove(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

class GameState {
    ComboMove[] requiredSequence;
    int currentComboIndex = 0;
    int enemyHealth = 100;
    int playerHealth = 100;
    int waveNumber = 1;
    boolean isComboSuccessful = false;
    boolean isGameOver = false;
    long lastMoveTime = 0;
    
    Player player;
    Enemy enemy;

    GameState() {
        player = new Player();
        enemy = new Enemy();
        generateNewSequence();
    }

    private void generateNewSequence() {
        ComboMove[] moves = ComboMove.values();
        Random rand = new Random();
        
        // Start with 5 moves, increase with waves
        int sequenceLength = Math.min(5 + waveNumber / 2, 8);
        requiredSequence = new ComboMove[sequenceLength];
        
        for (int i = 0; i < sequenceLength; i++) {
            requiredSequence[i] = moves[rand.nextInt(moves.length)];
        }
        
        currentComboIndex = 0;
    }

    void playerAttemptMove(ComboMove move) {
        lastMoveTime = System.currentTimeMillis();
        
        if (isGameOver) return;

        // Check if move matches the required sequence
        if (currentComboIndex < requiredSequence.length && 
            requiredSequence[currentComboIndex] == move) {
            
            // Correct move!
            isComboSuccessful = true;
            currentComboIndex++;
            player.animate(move);

            // Check if combo is complete
            if (currentComboIndex >= requiredSequence.length) {
                // Successful combo!
                int damage = 10 + waveNumber * 2;
                enemyHealth -= damage;
                
                if (enemyHealth <= 0) {
                    isGameOver = true;
                } else {
                    // Prepare next wave
                    waveNumber++;
                    player.reset();
                    enemy.reset();
                    generateNewSequence();
                }
            }
        } else {
            // Wrong move - reset combo
            isComboSuccessful = false;
            currentComboIndex = 0;
            enemy.counterAttack();
            playerHealth -= 5;
            
            if (playerHealth <= 0) {
                isGameOver = true;
            }
        }
    }

    void update() {
        player.update();
        enemy.update();
    }
}

class Player {
    double x = 200;
    double y = 400;
    double width = 60;
    double height = 80;
    ComboMove currentMove = null;
    long moveStartTime = 0;
    double offsetX = 0;
    double offsetY = 0;

    void animate(ComboMove move) {
        currentMove = move;
        moveStartTime = System.currentTimeMillis();
    }

    void update() {
        if (currentMove != null) {
            long elapsed = System.currentTimeMillis() - moveStartTime;
            double progress = Math.min(elapsed / 300.0, 1.0);

            switch (currentMove) {
                case PUNCH:
                    offsetX = 30 * Math.sin(progress * Math.PI);
                    break;
                case KICK:
                    offsetX = 40 * Math.sin(progress * Math.PI);
                    offsetY = -20 * Math.sin(progress * Math.PI);
                    break;
                case DODGE:
                    offsetY = -30 * Math.sin(progress * Math.PI);
                    break;
                case SPECIAL:
                    offsetX = 50 * Math.sin(progress * Math.PI);
                    offsetY = -40 * Math.sin(progress * Math.PI);
                    break;
            }

            if (progress >= 1.0) {
                currentMove = null;
                offsetX = 0;
                offsetY = 0;
            }
        }
    }

    void render(GraphicsContext gc) {
        double renderX = x + offsetX;
        double renderY = y + offsetY;

        // Draw player body
        gc.setFill(Color.web("#4ecdc4"));
        gc.fillRect(renderX, renderY, width, height);

        // Draw player head
        gc.setFill(Color.web("#95e1d3"));
        gc.fillOval(renderX + 15, renderY - 20, 30, 30);

        // Draw current move indicator
        if (currentMove != null) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            gc.setFill(Color.web("#ffd700"));
            gc.fillText(currentMove.toString(), renderX - 30, renderY - 40);
        }

        // Draw HP bar
        gc.setStroke(Color.web("#a8dadc"));
        gc.setLineWidth(2);
        gc.strokeRect(renderX - 20, renderY + 100, 100, 8);
        gc.setFill(Color.web("#ffd700"));
        gc.fillRect(renderX - 20, renderY + 100, 100, 8);
    }

    void reset() {
        offsetX = 0;
        offsetY = 0;
        currentMove = null;
    }
}

class Enemy {
    double x = 700;
    double y = 350;
    double width = 80;
    double height = 100;
    boolean isHit = false;
    long hitStartTime = 0;
    double offsetX = 0;
    int bounceFrame = 0;

    void counterAttack() {
        isHit = true;
        hitStartTime = System.currentTimeMillis();
    }

    void update() {
        if (isHit) {
            long elapsed = System.currentTimeMillis() - hitStartTime;
            double progress = Math.min(elapsed / 300.0, 1.0);

            // Knockback effect
            offsetX = -50 * Math.sin(progress * Math.PI);
            bounceFrame = (int) ((progress * 10) % 2);

            if (progress >= 1.0) {
                isHit = false;
                offsetX = 0;
                bounceFrame = 0;
            }
        }
    }

    void render(GraphicsContext gc) {
        double renderX = x + offsetX;
        double renderY = y + (bounceFrame * 5);

        // Draw enemy body
        gc.setFill(Color.web("#ff6b6b"));
        gc.fillRect(renderX, renderY, width, height);

        // Draw enemy head
        gc.setFill(Color.web("#ff8787"));
        gc.fillOval(renderX + 20, renderY - 30, 40, 40);

        // Draw enemy eyes
        gc.setFill(Color.web("#0a0e27"));
        gc.fillOval(renderX + 30, renderY - 20, 8, 8);
        gc.fillOval(renderX + 50, renderY - 20, 8, 8);

        // Draw enemy mouth
        gc.setStroke(Color.web("#0a0e27"));
        gc.setLineWidth(2);
        gc.strokeArc(renderX + 32, renderY - 10, 16, 10, 0, 180, ArcType.OPEN);

        // Draw hit indicator
        if (isHit) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            gc.setFill(Color.web("#ffd700"));
            gc.fillText("💥", renderX + 30, renderY - 50);
        }
    }

    void reset() {
        offsetX = 0;
        bounceFrame = 0;
        isHit = false;
    }
}
