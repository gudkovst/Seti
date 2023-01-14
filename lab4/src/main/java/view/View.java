package view;

import snakes.SnakesProto;

import java.util.List;

public interface View {
    public void start();
    public void registration();
    public void showGoingGames(List<SnakesProto.GameAnnouncement> games);
    public void showGameState(SnakesProto.GameState state);
    public void showError(String error);
}
