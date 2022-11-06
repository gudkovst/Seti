package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Keys {
    public static String KEY_PLACES;
    public static String KEY_GEOCODE;
    public static String KEY_WEATHER;

    public static void loadKeys(){
        try {
            Scanner scanner = new Scanner(new File(Config.fileKeys));
            KEY_PLACES = scanner.nextLine();
            KEY_GEOCODE = scanner.nextLine();
            KEY_WEATHER = scanner.nextLine();
        }catch (FileNotFoundException e){
            System.out.println("Keys not load");
            System.exit(-1);
        }
    }
}
