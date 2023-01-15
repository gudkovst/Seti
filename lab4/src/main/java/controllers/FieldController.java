package controllers;

import gameModel.Field;
import snakes.SnakesProto;

import java.awt.*;
import java.util.List;

public class FieldController {
    private final Controller controller;
    private final Field field;

    private final Color empty = Color.LIGHT_GRAY;
    private final Color food = Color.ORANGE;
    private final Color head = Color.BLACK;

    public FieldController(Controller controller){
        this.controller = controller;
        field = new Field();
    }

    public Field getField(SnakesProto.GameState state){
        field.init(empty);
        List<SnakesProto.GameState.Coord> foods = state.getFoodsList();
        for (SnakesProto.GameState.Coord coord : foods){
            field.setCell(coord.getX(), coord.getY(), food);
        }
        List<SnakesProto.GameState.Snake> snakes = state.getSnakesList();
        for (SnakesProto.GameState.Snake snake : snakes){
            Color snakeColor = controller.getPlayerColor(snake.getPlayerId());
            List<SnakesProto.GameState.Coord> keyPoints = snake.getPointsList();
            SnakesProto.GameState.Coord prevPoint = keyPoints.get(0);
            keyPoints = keyPoints.subList(1, keyPoints.size());
            field.setCell(prevPoint.getX(), prevPoint.getY(), head);
            for (SnakesProto.GameState.Coord point : keyPoints){
                int shiftX = point.getX();
                int shiftY = point.getY();
                int signShiftX = shiftX >= 0? 1 : -1;
                int signShiftY = shiftY >= 0? 1 : -1;
                for (int i = 1; i <= Math.abs(shiftX); i++){
                    int x = (prevPoint.getX() + signShiftX * i + field.getWidth()) % field.getWidth();
                    field.setCell(x, prevPoint.getY(), snakeColor);
                }
                for (int i = 1; i <= Math.abs(shiftY); i++){
                    int y = (prevPoint.getY() + signShiftY * i + field.getHeight()) % field.getHeight();
                    field.setCell(prevPoint.getX(), y, snakeColor);
                }
                prevPoint = SnakesProto.GameState.Coord.newBuilder()
                        .setX(prevPoint.getX() + shiftX)
                        .setY(prevPoint.getY() + shiftY)
                        .build();
            }
        }
        return field;
    }
}
