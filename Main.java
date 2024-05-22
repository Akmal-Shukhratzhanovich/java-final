package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Main extends Application {
    // qazaqsha zhaz
    static int speed = 7; // 蛇的移动速度
    static int foodcolor = 0; // 食物颜色索引
    static int width = 32; // 游戏网格宽度
    static int height = 30; // 游戏网格高度
    static int foodX = 0; // 食物X坐标
    static int foodY = 0; // 食物Y坐标
    static int cornersize = 25; // 单个网格单元的大小
    static List<Corner> snake = new ArrayList<>(); // 第一条蛇的坐标
    static List<Corner> snake2 = new ArrayList<>(); // 第二条蛇的坐标（双人模式）
    static Dir direction = Dir.left; // 第一条蛇的初始方向
    static Dir direction2 = Dir.right; // 第二条蛇的初始方向（双人模式）
    static boolean gameOver = false; // 游戏结束标志
    static boolean isTwoPlayer = false; // 双人模式标志
    static boolean playerOneGameOver = false; // 玩家一游戏结束标志
    static boolean playerTwoGameOver = false; // 玩家二游戏结束标志（双人模式）
    static Random rand = new Random(); // 随机数生成器
    static Image backgroundImage; // 背景图片
    static int playerOneScore = 0; // 玩家一的分数
    static int playerTwoScore = 0; // 玩家二的分数（双人模式）

    // 方向枚举
    public enum Dir {
        left, right, up, down
    }

    // 代表蛇身体部分的类
    public static class Corner {
        int x;
        int y;

        public Corner(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            newFood(); // 生成新食物

            VBox root = new VBox(); // 创建垂直布局容器
            Canvas c = new Canvas(width * cornersize, height * cornersize); // 创建画布
            GraphicsContext gc = c.getGraphicsContext2D(); // 获取画布的图形上下文
            root.getChildren().add(c); // 将画布添加到布局容器中

            new AnimationTimer() {
                long lastTick = 0;

                // 动画定时器，每个tick调用一次handle方法
                public void handle(long now) {
                    if (lastTick == 0) {
                        lastTick = now;
                        tick(gc);
                        return;
                    }

                    if (now - lastTick > 1000000000 / speed) {
                        lastTick = now;
                        tick(gc);
                    }
                }
            }.start();

            Scene gameScene = new Scene(root, width * cornersize, height * cornersize);

            // 键盘事件处理
            gameScene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
                if (key.getCode() == KeyCode.W && direction != Dir.down) {
                    direction = Dir.up;
                }
                if (key.getCode() == KeyCode.A && direction != Dir.right) {
                    direction = Dir.left;
                }
                if (key.getCode() == KeyCode.S && direction != Dir.up) {
                    direction = Dir.down;
                }
                if (key.getCode() == KeyCode.D && direction != Dir.left) {
                    direction = Dir.right;
                }
                if (key.getCode() == KeyCode.UP && direction2 != Dir.down) {
                    direction2 = Dir.up;
                }
                if (key.getCode() == KeyCode.LEFT && direction2 != Dir.right) {
                    direction2 = Dir.left;
                }
                if (key.getCode() == KeyCode.DOWN && direction2 != Dir.up) {
                    direction2 = Dir.down;
                }
                if (key.getCode() == KeyCode.RIGHT && direction2 != Dir.left) {
                    direction2 = Dir.right;
                }
                if (key.getCode() == KeyCode.ENTER && gameOver) {
                    resetGame();
                }
            });

            resetGame(); // 初始化游戏

            backgroundImage = new Image(Main.class.getResourceAsStream("nurbek1.jpg"), width * cornersize, height * cornersize, false, true);

            // 创建开始菜单
            StackPane startRoot = new StackPane();
            Scene startScene = new Scene(startRoot, width * cornersize, height * cornersize);
            
            Label titleLabel = new Label(" The Best SNAKE GAME");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            titleLabel.setTextFill(Color.GREEN);
            
            Button startButton = new Button("One Player Mode");
            Button twoPlayerButton = new Button("Two Player Mode");
            startButton.setPrefSize(200, 50);
            twoPlayerButton.setPrefSize(200, 50);

            startButton.setOnAction(e -> primaryStage.setScene(gameScene));
            twoPlayerButton.setOnAction(e -> {
                isTwoPlayer = true;
                resetGame();
                primaryStage.setScene(gameScene);
            });

            Label creatorLabel = new Label("Created by: Nurbek and Akmal");
            creatorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            creatorLabel.setTextFill(Color.GRAY);
            creatorLabel.setAlignment(Pos.BOTTOM_RIGHT);

            VBox menu = new VBox(40, titleLabel, startButton, twoPlayerButton);
            menu.setAlignment(Pos.CENTER);
            startRoot.getChildren().addAll(menu, creatorLabel);
            StackPane.setAlignment(creatorLabel, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(creatorLabel, new javafx.geometry.Insets(0, 10, 10, 0));

            primaryStage.setScene(startScene);
            primaryStage.setTitle("Snake Game");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void tick(GraphicsContext gc) {
        if (gameOver) {
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("", FontWeight.BOLD, 50));
            gc.fillText("Press Enter to restart", 170, 200);
            gc.fillText("Game Over", 260, 100);
            return;
        }
    
        // 判断两个玩家是否都结束游戏
        if (playerOneGameOver && playerTwoGameOver) {
            gameOver = true;
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("", FontWeight.BOLD, 50));
            String winner = playerOneScore > playerTwoScore ? "Player 1 Wins!" : (playerTwoScore > playerOneScore ? "Player 2 Wins!" : "It's a tie!");
            gc.fillText(winner, 300, 300);
            return;
        }
    
        // 移动玩家一的蛇
        if (!playerOneGameOver && !snake.isEmpty()) {
            for (int i = snake.size() - 1; i >= 1; i--) {
                snake.get(i).x = snake.get(i - 1).x;
                snake.get(i).y = snake.get(i - 1).y;
            }
    
            switch (direction) {
                case up:
                    snake.get(0).y--;
                    if (snake.get(0).y < 0) {
                        playerOneGameOver = true;
                        gameOver = !isTwoPlayer;
                    }
                    break;
                case down:
                    snake.get(0).y++;
                    if (snake.get(0).y >= height) {
                        playerOneGameOver = true;
                        gameOver = !isTwoPlayer;
                    }
                    break;
                case left:
                    snake.get(0).x--;
                    if (snake.get(0).x < 0) {
                        playerOneGameOver = true;
                        gameOver = !isTwoPlayer;
                    }
                    break;
                case right:
                    snake.get(0).x++;
                    if (snake.get(0).x >= width) {
                        playerOneGameOver = true;
                        gameOver = !isTwoPlayer;
                    }
                    break;
            }
        }
    
        // 移动玩家二的蛇
        if (isTwoPlayer && !playerTwoGameOver && !snake2.isEmpty()) {
            for (int i = snake2.size() - 1; i >= 1; i--) {
                snake2.get(i).x = snake2.get(i - 1).x;
                snake2.get(i).y = snake2.get(i - 1).y;
            }
    
            switch (direction2) {
                case up:
                    snake2.get(0).y--;
                    if (snake2.get(0).y < 0) {
                        playerTwoGameOver = true;
                    }
                    break;
                case down:
                    snake2.get(0).y++;
                    if (snake2.get(0).y >= height) {
                        playerTwoGameOver = true;
                    }
                    break;
                case left:
                    snake2.get(0).x--;
                    if (snake2.get(0).x < 0) {
                        playerTwoGameOver = true;
                    }
                    break;
                case right:
                    snake2.get(0).x++;
                    if (snake2.get(0).x >= width) {
                        playerTwoGameOver = true;
                    }
                    break;
            }
        }
    
        // 玩家一的蛇吃食物
        if (foodX == snake.get(0).x && foodY == snake.get(0).y) {
            snake.add(new Corner(-1, -1));
            playerOneScore++;
            newFood();
        }
    
        // 玩家二的蛇吃食物
        if (isTwoPlayer && foodX == snake2.get(0).x && foodY == snake2.get(0).y) {
            snake2.add(new Corner(-1, -1));
            playerTwoScore++;
            newFood();
        }
    
        // 玩家一的蛇与自身相撞
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(0).x == snake.get(i).x && snake.get(0).y == snake.get(i).y) {
                playerOneGameOver = true;
                gameOver = !isTwoPlayer;
            }
        }
    
        // 玩家二的蛇与自身相撞
        if (isTwoPlayer) {
            for (int i = 1; i < snake2.size(); i++) {
                if (snake2.get(0).x == snake2.get(i).x && snake2.get(0).y == snake2.get(i).y) {
                    playerTwoGameOver = true;
                }
            }
            // 玩家二的蛇与玩家一的蛇相撞
            for (Corner c : snake) {
                if (snake2.get(0).x == c.x && snake2.get(0).y == c.y) {
                    playerOneGameOver = true;
                    playerTwoGameOver = true;
                }
            }
        }
    
        // 绘制背景和分数
        gc.drawImage(backgroundImage, 0, 0, width * cornersize, height * cornersize);
    
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("", FontWeight.BOLD, 30));
        gc.fillText("Player 1 Score: " + playerOneScore, 10, 30);
        if (isTwoPlayer) {
            gc.fillText("Player 2 Score: " + playerTwoScore, 10, 60);
        }
    
        // 绘制食物
        Color cc = Color.WHITE;
    
        switch (foodcolor) {
            case 0:
                cc = Color.PURPLE;
                break;
            case 1:
                cc = Color.LIGHTBLUE;
                break;
            case 2:
                cc = Color.YELLOW;
                break;
            case 3:
                cc = Color.PINK;
                break;
        }
        gc.setFill(cc);
        gc.fillOval(foodX * cornersize, foodY * cornersize, cornersize, cornersize);
    
        // 绘制玩家一的蛇
        for (Corner c : snake) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 1, cornersize - 1);
            gc.setFill(Color.GREEN);
            gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 2, cornersize - 2);
        }
    
        // 绘制玩家二的蛇
        if (isTwoPlayer) {
            for (Corner c : snake2) {
                gc.setFill(Color.LIGHTBLUE);
                gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 1, cornersize - 1);
                gc.setFill(Color.BLUE);
                gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 2, cornersize - 2);
            }
        }
    }

    public static void newFood() {
        start: while (true) {
            // 随机生成食物位置
            foodX = rand.nextInt(width);
            foodY = rand.nextInt(height);

            // 确保食物不与玩家一的蛇重叠
            for (Corner c : snake) {
                if (c.x == foodX && c.y == foodY) {
                    continue start;
                }
            }

            // 确保食物不与玩家二的蛇重叠（双人模式）
            if (isTwoPlayer) {
                for (Corner c : snake2) {
                    if (c.x == foodX && c.y == foodY) {
                        continue start;
                    }
                }
            }

            // 随机选择食物颜色并增加速度
            foodcolor = rand.nextInt(4);
            speed++;
            break;
        }
    }

    public static void resetGame() {
        // 重置游戏状态
        snake.clear();
        snake2.clear();
        direction = Dir.up;
        direction2 = Dir.up;
        gameOver = false; 
        playerOneGameOver = false;
        playerTwoGameOver = false;
        playerOneScore = 0;
        playerTwoScore = 0;
        speed = 7;
        newFood();

        // 初始化玩家一的蛇位置
        snake.add(new Corner(width / 2, height / 2));
        snake.add(new Corner(width / 2, height / 2));
        snake.add(new Corner(width / 2, height / 2));

        // 初始化玩家二的蛇位置（双人模式）
        if (isTwoPlayer) {
            snake2.add(new Corner(width / 2 - 5, height / 2));
            snake2.add(new Corner(width / 2 - 5, height / 2));
            snake2.add(new Corner(width / 2 - 5, height / 2));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
