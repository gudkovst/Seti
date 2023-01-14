package gameModel;

import application.Config;

import javax.swing.*;
import java.awt.*;

public class Cell extends JPanel {

    public Cell(int x, int y){
        setBounds(x * Config.CellSize, y * Config.CellSize, Config.CellSize, Config.CellSize);
    }

    public void setColor(Color color){
        setBackground(color);
    }
}
