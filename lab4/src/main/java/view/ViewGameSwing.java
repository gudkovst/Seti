package view;

import application.Config;
import controllers.Controller;
import gameModel.Cell;
import gameModel.Field;
import snakes.SnakesProto;

import javax.swing.*;
import java.awt.*;

public class ViewGameSwing extends JFrame implements Runnable {
    private final Controller controller;
    private int numState;

    public ViewGameSwing(Controller controller){
        this.controller = controller;
    }

    @Override
    public void run() {
        addKeyListener(controller);
        setSize(1000, 600);
        setBackground(Config.color); // Set background color
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setVisible(true); // Show everything
    }

    public void showGameState(SnakesProto.GameState state){
        if (numState > state.getStateOrder())
            return;
        numState = state.getStateOrder();
        Field field = controller.getField(state);
        javax.swing.SwingUtilities.invokeLater(() -> {
            String[] columnNames = {"Player", "Score"};
            SnakesProto.GamePlayers players = state.getPlayers();
            String[][] data = new String[players.getPlayersCount()][];
            for (int i = 0; i < players.getPlayersCount(); i++){
                SnakesProto.GamePlayer player = players.getPlayers(i);
                data[i] = new String[]{player.getName(), String.valueOf(player.getScore())};
            }
            JTable table = new JTable(data, columnNames);
            JButton viewButton = new JButton("View game");
            viewButton.addActionListener(e -> controller.viewGame());
            JPanel tablePanel = new JPanel(new GridLayout(2, 1));
            tablePanel.add(table);
            tablePanel.add(viewButton);
            tablePanel.setBounds(Config.CellSize * Config.WIDTH, 0, 450, 600);
            add(tablePanel);
            for (Cell[] row : field.getField()){
                for (Cell cell : row){
                    add(cell);
                }
            }
            revalidate();
            repaint();
        });
    }
}
