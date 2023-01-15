package controllers;

import application.Config;
import com.google.protobuf.InvalidProtocolBufferException;
import connection.Connection;
import connection.Message;
import gameModel.User;
import snakes.SnakesProto;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.*;

public class MessageController {
    private final Connection connection;
    private final User user;
    private final Controller controller;
    private final Map<Message, Long> messages;
    private long num;

    public MessageController(Connection connection, User user, Controller controller){
        this.connection = connection;
        this.user = user;
        this.controller = controller;
        num = 0;
        messages = new ConcurrentHashMap<>();
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        int checkTime = Config.confirmTime / 5;
        scheduledThreadPool.scheduleAtFixedRate(this::resend, checkTime, checkTime, TimeUnit.MILLISECONDS);
        threadPool.execute(this::receive);
        threadPool.execute(this::listenMulticast);
    }

    public void send(SnakesProto.GameMessage message, InetAddress to, int port){
        byte[] data = message.toByteArray();
        connection.send(data, to, port);
        Message msg = new Message(data, to, port);
        messages.put(msg, System.currentTimeMillis());
    }

    public void resend(){
        for (Message msg : messages.keySet()){
            if (System.currentTimeMillis() - messages.get(msg) > Config.confirmTime){
                connection.send(msg.getMessage(), msg.getAddress(), msg.getPort());
                messages.put(msg, System.currentTimeMillis());
            }
        }
    }

    public void listenMulticast(){
        while (!Thread.currentThread().isInterrupted()){
            Message message = connection.listenMulticast();
            if (message == null){
                continue;
            }
            controller.showGoingGames(message);
        }
    }

    private boolean equalSender(Message f, Message s){
        return f.getAddress() == s.getAddress() && f.getPort() == s.getPort();
    }

    public void receive() {
        while (!Thread.currentThread().isInterrupted()){
            SnakesProto.GameMessage msg;
            Message message = connection.recv();
            if (message == null){
                continue;
            }
            try {
                msg = SnakesProto.GameMessage.parseFrom(message.getMessage());
            } catch (InvalidProtocolBufferException e) {
                continue;
            }
            if (num >= msg.getMsgSeq()){
                continue;
            }
            System.out.println("Get message " + msg.getTypeCase().name() + msg.getMsgSeq());
            num = msg.getMsgSeq();
            switch (msg.getTypeCase()){
                case PING -> sendAck(message);
                case ACK -> {
                    long num = msg.getMsgSeq();
                    for (Message m : messages.keySet()){
                        try{
                            SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(m.getMessage());
                            if (gameMessage.getMsgSeq() == num && equalSender(message, m)){
                                if (gameMessage.hasJoin()){
                                    user.setId(msg.getReceiverId());
                                }
                                messages.remove(m);
                                break;
                            }
                        } catch (InvalidProtocolBufferException ignored) {}
                    }
                }
                case JOIN, STEER, DISCOVER -> sendError(message);
                case ERROR -> {
                    sendAck(message);
                    controller.handleError(msg.getError());
                }
                case STATE -> {
                    sendAck(message);
                    controller.showGameState(msg.getState());
                }
                case ROLE_CHANGE -> {
                    sendAck(message);
                    SnakesProto.NodeRole role = msg.getRoleChange().getReceiverRole();
                    user.setRole(role);
                    role = msg.getRoleChange().getSenderRole();
                    if (role == SnakesProto.NodeRole.MASTER){
                        controller.setServer(message.getAddress(), message.getPort());
                    }
                }
                case ANNOUNCEMENT -> controller.showGoingGames(message);
            }
        }
    }

    private void sendAck(Message message) {
        SnakesProto.GameMessage msg;
        try {
            msg = SnakesProto.GameMessage.parseFrom(message.getMessage());
        } catch (InvalidProtocolBufferException ignored) {
            return;
        }
        SnakesProto.GameMessage answer = SnakesProto.GameMessage.newBuilder()
                .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(msg.getMsgSeq())
                .setSenderId(user.getId())
                .setReceiverId(msg.getSenderId())
                .build();
        connection.send(answer.toByteArray(), message.getAddress(), message.getPort());
    }

    private void sendError(Message message){
        SnakesProto.GameMessage msg;
        try {
            msg = SnakesProto.GameMessage.parseFrom(message.getMessage());
        } catch (InvalidProtocolBufferException ignored) {
            return;
        }
        SnakesProto.GameMessage answer = SnakesProto.GameMessage.newBuilder()
                .setError(SnakesProto.GameMessage.ErrorMsg.newBuilder().
                        setErrorMessage("Я не умею быть сервером, не надо мне такое слать.").build())
                .setMsgSeq(msg.getMsgSeq())
                .setSenderId(user.getId())
                .setReceiverId(msg.getSenderId())
                .build();
        send(answer, message.getAddress(), message.getPort());
    }
}
