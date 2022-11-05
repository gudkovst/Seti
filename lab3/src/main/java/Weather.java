package records;

public class Weather {
    private final String weather;
    private final String description;

    public Weather(String weather, String description){
        this.weather = weather;
        this.description = description;
    }

    public String getWeather() {
        return weather;
    }

    public String getDescription() {
        return description;
    }
}
