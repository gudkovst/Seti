package view;

import application.Config;
import controllers.Controller;
import snakes.SnakesProto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewSwing extends JFrame implements View, Runnable {
    private final Controller controller;
    private final List<SnakesProto.GameAnnouncement> games;
    private final List<String> gamesNames;
    private final JPanel entryField;
    private boolean isGame;
    private final ViewGameSwing viewGameSwing;

    public ViewSwing() throws IOException {
        controller = new Controller(this);
        games = new ArrayList<>();
        gamesNames = new ArrayList<>();
        entryField = new JPanel();
        isGame = false;
        viewGameSwing = new ViewGameSwing(controller);
    }

    @Override
    public void run() {
        setSize(Config.WIDTH, Config.HEIGHT);
        setBackground(Config.color); // Set background color
        setDefaultCloseOperation(EXIT_ON_CLOSE); // When "(X)" clicked, process is being killed
        setTitle("Snakes"); // Set title
        setResizable(true);
        setVisible(true); // Show everything
        registration();
    }

    @Override
    public void start() {
        javax.swing.SwingUtilities.invokeLater(this);
    }

    @Override
    public void registration() {
        JLabel label = new JLabel("Enter your name");
        label.setHorizontalAlignment(JLabel.CENTER);
        JTextField field = new JTextField();
        field.setToolTipText("Enter your name");
        field.setHorizontalAlignment(JTextField.CENTER);
        JButton button = new JButton("OK");
        button.setMnemonic(KeyEvent.VK_ENTER);
        button.addActionListener(e -> {
            controller.setUser(field.getText());
            JLabel header = new JLabel("Find current games...");
            header.setHorizontalAlignment(JLabel.CENTER);
            javax.swing.SwingUtilities.invokeLater(() -> {
                entryField.removeAll();
                entryField.add(header);
                setContentPane(entryField);
            });
        });
        entryField.setLayout(new GridLayout(3, 1));
        entryField.add(label);
        entryField.add(field);
        entryField.add(button);
        setContentPane(entryField);
    }

    @Override
    public void showGoingGames(List<SnakesProto.GameAnnouncement> gamesMsg) {
        if (isGame)
            return;
        if (gamesMsg.isEmpty()){
            JLabel header = new JLabel("No current games found yet");
            header.setHorizontalAlignment(JLabel.CENTER);
            javax.swing.SwingUtilities.invokeLater(() -> {
                entryField.removeAll();
                entryField.add(header);
                setContentPane(entryField);
            });
        }
        for (SnakesProto.GameAnnouncement game : gamesMsg){
            if (!gamesNames.contains(game.getGameName())){
                gamesNames.add(game.getGameName());
                games.add(game);
            }
            else {
                games.removeIf(announcement -> announcement.getGameName().equals(game.getGameName()));
                games.add(game);
            }
        }
        JLabel header = new JLabel("Going games");
        header.setHorizontalAlignment(JLabel.CENTER);

        JButton[] gamesBut = new JButton[games.size()];
        for (int i = 0; i < games.size(); i++){
            String name = games.get(i).getGameName();
            SnakesProto.GameConfig config = games.get(i).getConfig();
            gamesBut[i] = new JButton(name);
            gamesBut[i].addActionListener(e -> {
                isGame = true;
                controller.setConfig(config.getHeight(), config.getWidth(), config.getFoodStatic(), config.getStateDelayMs());
                controller.joinGame(name);
                viewGameSwing.setTitle(name);
                viewGameSwing.run();
                dispose();
            });
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.setLayout(new GridLayout(gamesBut.length + 1, 1));
            entryField.add(header);
            for (JButton button : gamesBut){
                entryField.add(button);
            }
            setContentPane(entryField);
        });
    }

    @Override
    public void showGameState(SnakesProto.GameState state) {
        viewGameSwing.showGameState(state);
    }

    @Override
    public void showError(String error) {
        JLabel errorLabel = new JLabel(error);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        JButton button = new JButton("return to list of games");
        button.setMnemonic(KeyEvent.VK_ENTER);
        button.addActionListener(e -> {
            isGame = false;
            entryField.removeAll();
            showGoingGames(games);
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.removeAll();
            entryField.add(errorLabel);
            entryField.add(button);
            setContentPane(entryField);
        });
    }
}
