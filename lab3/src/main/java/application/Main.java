package application;

import config.Keys;

public class Main {

    public static void main(String[] args) {
        Keys.loadKeys();
        Requester requester = new Requester();
        View view = new View(requester);
        javax.swing.SwingUtilities.invokeLater(view);
    }
}
