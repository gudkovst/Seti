package gameModel;

import application.Config;
import lombok.Getter;

import java.awt.*;

@Getter
public class Field {
    private final Cell[][] field;

    public Field(){
        field = new Cell[Config.FieldWidth][Config.FieldHeight];
        for (int x = 0; x < Config.FieldWidth; x++)
            for (int y = 0; y < Config.FieldHeight; y++)
                field[x][y] = new Cell(x, y);
    }

    public void setCell(int x, int y, Color color){
        field[x][y].setColor(color);
    }

    public void init(Color color){
        for (Cell[] cells : field)
            for (Cell cell : cells)
                cell.setColor(color);
    }

    public int getWidth(){
        return Config.FieldWidth;
    }

    public int getHeight(){
        return Config.FieldHeight;
    }
}
