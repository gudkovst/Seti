package application;

public class Main {

    public static void main(String[] args){
        Requester requester = new Requester();
        View view = new View(requester);
        javax.swing.SwingUtilities.invokeLater(view);
    }
}
