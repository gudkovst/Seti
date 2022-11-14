package config;

public class URLs {
    public static String GEOCODE = "https://graphhopper.com/api/1/geocode?q=%s&key=%s";
    public static String WEATHER = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric";
    public static String INTERESTING_PLACES = "https://api.opentripmap.com/0.1/ru/places/radius?" +
            "lat=%s&lon=%s&radius=%s&format=json&apikey=%s";
    public static String DESCRIPTION_PLACE = "https://api.opentripmap.com/0.1/ru/places/xid/%s?apikey=%s";
}
