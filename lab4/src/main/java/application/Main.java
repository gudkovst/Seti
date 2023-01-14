package application;

import view.View;
import view.ViewSwing;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        View view = new ViewSwing();
        view.start();
    }
}
