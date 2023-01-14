package gameModel;

import lombok.Getter;
import snakes.SnakesProto;

@Getter
public class User {
    private SnakesProto.NodeRole role;
    private int id;
    private final String name;
    private long seq;

    public User(String name){
        this.name = name;
        seq = 0;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSeq() {
        seq++;
        return seq;
    }

    public void setRole(SnakesProto.NodeRole role) {
        this.role = role;
    }
}
