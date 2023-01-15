package controllers;

import com.google.protobuf.InvalidProtocolBufferException;
import application.Config;
import connection.Connection;
import connection.SocketConnection;
import connection.Message;
import gameModel.Field;
import gameModel.User;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import snakes.SnakesProto;
import view.View;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Controller extends KeyAdapter {
    private final View view;
    private User user;
    private final Connection connection;
    private InetAddress serverAddress;
    private int serverPort;
    private InetAddress deputyAddress;
    private int deputyPort;
    private final Map<String, Message> games;
    private final Map<Integer, Color> playersColors;
    private MessageController msgController;
    private FieldController fieldController;
    private SnakesProto.GameState currentState;

    public Controller(View view) throws IOException {
        this.user = null;
        this.connection = new SocketConnection();
        this.view = view;
        games = new HashMap<>();
        playersColors = new HashMap<>();
        currentState = null;
    }

    public SnakesProto.GameState getCurrentState() {
        SnakesProto.GameState state = currentState;
        currentState = null;
        return state;
    }

    public void setCurrentState(SnakesProto.GameState state) {
        if (currentState == null || currentState.getStateOrder() < state.getStateOrder()){
            currentState = state;
        }
    }

    public Color getPlayerColor(int id){
        return playersColors.get(id);
    }

    public void setServer(InetAddress address, int port){
        serverAddress = address;
        serverPort = port;
    }

    public void setDeputy(InetAddress address, int port){
        deputyAddress = address;
        deputyPort = port;
    }

    public void setUser(String name){
        if (user == null) {
            user = new User(name);
            msgController = new MessageController(connection, user, this);
        }
    }

    public void changeDirection(SnakesProto.Direction direction){
        if (user.getRole() == SnakesProto.NodeRole.VIEWER){
            return;
        }
        SnakesProto.GameMessage.SteerMsg info = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();
        SnakesProto.GameMessage message = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(user.getSeq())
                .setSteer(info)
                .build();
        msgController.send(message, serverAddress, serverPort);
    }

    @Override
    public void keyPressed(KeyEvent e){
        System.out.println("Press " + e.getKeyCode());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> changeDirection(SnakesProto.Direction.UP);
            case KeyEvent.VK_DOWN -> changeDirection(SnakesProto.Direction.DOWN);
            case KeyEvent.VK_LEFT -> changeDirection(SnakesProto.Direction.LEFT);
            case KeyEvent.VK_RIGHT -> changeDirection(SnakesProto.Direction.RIGHT);
        }
    }

    public void handleError(SnakesProto.GameMessage.@NotNull ErrorMsg msg) {
        view.showError(msg.getErrorMessage());
    }

    public void showGameState(SnakesProto.GameMessage.@NotNull StateMsg msg) {
        setCurrentState(msg.getState());
        List<SnakesProto.GamePlayer> players = msg.getState().getPlayers().getPlayersList();
        for (SnakesProto.GamePlayer player : players){
            playersColors.put(player.getId(), new Color(player.getId()));
            if (player.getRole() == SnakesProto.NodeRole.DEPUTY){
                try{
                    setDeputy(InetAddress.getByName(player.getIpAddress()), player.getPort());
                } catch (UnknownHostException ignored) {}
            }
        }
    }

    public Field getField(SnakesProto.GameState state){
        return fieldController.getField(state);
    }

    public void showGoingGames(Message msg){
        List<SnakesProto.GameAnnouncement> announcements;
        try{
            announcements = SnakesProto.GameMessage.parseFrom(msg.getMessage())
                            .getAnnouncement().getGamesList();
        } catch (InvalidProtocolBufferException e) {
            System.out.println(e.getMessage());
            return;
        }
        for (SnakesProto.GameAnnouncement game : announcements){
            String name = game.getGameName();
            games.put(name, msg);
        }
        view.showGoingGames(announcements);
    }

    public void setConfig(int height, int width, int food, int delay){
        Config.HEIGHT = height;
        Config.WIDTH = width;
        Config.foodStatic = food;
        Config.state_delay_ms = delay;
    }

    public void joinGame(String name){
        fieldController = new FieldController(this);
        Message message = games.get(name);
        SnakesProto.GameMessage.JoinMsg joinMsg = SnakesProto.GameMessage.JoinMsg.newBuilder()
                .setPlayerName(user.getName())
                .setGameName(name)
                .setRequestedRole(SnakesProto.NodeRole.NORMAL)
                .build();
        SnakesProto.GameMessage msg = SnakesProto.GameMessage.newBuilder()
                .setJoin(joinMsg)
                .setMsgSeq(user.getSeq())
                .build();
        connection.send(msg.toByteArray(), message.getAddress(), message.getPort());
        setServer(message.getAddress(), message.getPort());
    }

    public void viewGame(){
        user.setRole(SnakesProto.NodeRole.VIEWER);
        SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                .setSenderRole(SnakesProto.NodeRole.VIEWER)
                .build();
        SnakesProto.GameMessage msg = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(user.getSeq())
                .setSenderId(user.getId())
                .setRoleChange(roleChangeMsg)
                .build();
        connection.send(msg.toByteArray(), serverAddress, serverPort);
    }
}
