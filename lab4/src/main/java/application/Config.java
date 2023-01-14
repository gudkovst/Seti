package application;

import java.awt.*;

public class Config {
    public static String multicastAddress = "239.192.0.4";
    public static int multicastPort = 9192;

    public static int CellSize = 20;
    public static int FieldHeight = 60;
    public static int FieldWidth = 60;

    public static int maxSizeMessage = 4096;
    public static int HEIGHT = 500;
    public static int WIDTH = 500;
    public static Color color = Color.white;
    public static int foodStatic = 1;

    //Time parameters in ms
    public static int state_delay_ms = 1000;
    public static int confirmTime = state_delay_ms / 10;
    public static int aliveTime = 4 * state_delay_ms / 5;
}
