import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class Snake extends Application {
    private static int gridSize = 20;
    private static int windowSize = 800;
    private static int gap = 1;

    private static Rectangle[][] grid;
    private static int startBodySize = 3;
    double gameSpeed = 10.0;

    private static GridPane gridView;
    private static LinkedList<Body> snake;
    private final Color snakeColor = Color.RED;
    private final Color gridColor = Color.ALICEBLUE;
    private final Color foodColor = Color.BLUE;

    private Stage worldStage;
    private AnimationTimer timer;

    private boolean foodAlive = false;
    private Body food;

    enum Direction{
        right,
        left,
        up,
        down
    }

    private Direction currentDirection = null;
    private Direction desiredDirection = Direction.right;

    public static void main(String [] args){
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Snake");
        gridView = new GridPane();

        gridView.setPadding(new Insets(1,1,1,1));
        gridView.setVgap(gap);
        gridView.setHgap(gap);
        gridView.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        grid = new Rectangle[gridSize][gridSize];

        double blockUnit = ((windowSize)/(double)gridSize) - (gap);

        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j < gridSize; j++){
                Rectangle block = new Rectangle(blockUnit, blockUnit);
                block.setFill(gridColor);
                grid[i][j] = block;
                gridView.add(block,i,j);
            }
        }


        Scene scene = new Scene(gridView, windowSize, windowSize);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.RIGHT) {
                desiredDirection = Direction.right;
            } else if (e.getCode() == KeyCode.LEFT) {
                desiredDirection = Direction.left;
            } else if (e.getCode() == KeyCode.UP) {
                desiredDirection = Direction.up;
            } else if (e.getCode() == KeyCode.DOWN) {
                desiredDirection = Direction.down;
            }
        });
        worldStage = primaryStage;
        primaryStage.setScene(scene);
        primaryStage.show();
        runGame();
    }

    private void runGame(){
        initializeSnake();
        timer = new AnimationTimer(){
            long count;
            @Override
            public void handle(long now){
                if(now - count >= 1000_000_000/gameSpeed){
                    try{
                        updateDirection();
                        timeStep();
                    }catch(Exception e){
                        displayMessage(e.getMessage());
                    }
                    count = now;
                }
            }
        };
        timer.start();
    }

    private boolean oppositeOf(Direction first, Direction second){
        if(first == Direction.left && second == Direction.right){
            return true;
        }
        if(first == Direction.right && second == Direction.left){
            return true;
        }
        if(first == Direction.up && second == Direction.down){
            return true;
        }
        if(first == Direction.down && second == Direction.up){
            return true;
        }
        return false;
    }

    private void updateDirection(){
        if(!oppositeOf(desiredDirection,currentDirection)){
            currentDirection = desiredDirection;
        }
    }
    private void initializeSnake(){
        snake = new LinkedList<Body>();
        int startHeight = gridSize/2;
        int startX = gridSize/4;
        for(int i = 0; i < startBodySize; i++){
            Body body = new Body(startX - i,startHeight);
            snake.add(body);
            grid[body.x][body.y].setFill(Color.RED);
        }

        food = new Body(0,0);
        generateFood();
    }

    private void timeStep() throws Exception {
        Body newHead = getNextLoc();
        snake.addFirst(newHead);
        if((newHead.x < 0 || newHead.x >= gridSize) || (newHead.y < 0 || newHead.y >= gridSize)){   //checking for edge collision
            throw new Exception("Snake hit the edge");
        }else if(grid[newHead.x][newHead.y].getFill() == snakeColor){
            throw new Exception("The dummy snake tried to eat itself");
        }

        if(newHead.x == food.x && newHead.y == food.y){
            foodAlive = false;
        }else{
            Body removedTail = snake.removeLast();
            grid[removedTail.x][removedTail.y].setFill(gridColor);
        }

        grid[newHead.x][newHead.y].setFill(snakeColor);

        if(!foodAlive){
            generateFood();
        }
    }

    private void generateFood(){
        Random rand = new Random();
        boolean colliding = true;
        while(colliding) {
            int x = rand.nextInt(gridSize);
            int y = rand.nextInt(gridSize);

            if (grid[x][y].getFill() != Color.RED) {
                colliding = false;
                food.x = x;
                food.y = y;
                grid[x][y].setFill(foodColor);
                foodAlive = true;
            }
        }
    }

    private void resetGame(){
        for(Body body: snake){
            try{
                grid[body.x][body.y].setFill(gridColor);
            }catch(ArrayIndexOutOfBoundsException e){
                //it's fine
            }
        }
        snake = null;
        foodAlive = false;
        grid[food.x][food.y].setFill(gridColor);
        food = null;
        runGame();
    }

    private void displayMessage(String message){
        timer.stop();

        Stage window = new Stage();
        StackPane layout = new StackPane();
        Scene scene = new Scene(layout, 350,100);
        Label feedback = new Label();
        feedback.setText(message);
        feedback.setFont(new Font(20.0));
        layout.getChildren().add(feedback);
        window.setScene(scene);
        window.show();
        window.setOnCloseRequest(e -> {
            resetGame();
        });
    }

    private Body getNextLoc(){
        Body newHead = new Body(snake.getFirst());

        switch(currentDirection){
            case left:
                newHead.x -= 1;
                break;
            case right:
                newHead.x += 1;
                break;
            case up:
                newHead.y -= 1;
                break;
            default:
                newHead.y += 1;
                break;
        }
        return newHead;
    }

    private class Body{
        public int x;
        public int y;
        public Body(int x, int y){
            this.x = x;
            this.y = y;
        }

        public Body(Body other){
            this(other.x,other.y);
        }

        public int getIndex() {
            return this.x + (this.y*gridSize);
        }
    }
}
