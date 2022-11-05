package records;

import java.util.List;

public class WeatherRecord {
    private final double temperature;
    private final List<Weather> weather;
    private final double windSpeed;

    public WeatherRecord(double temp, List<Weather> list, double speed){
        temperature = temp;
        weather = list;
        windSpeed = speed;
    }

    public double getTemperature() {
        return temperature;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}
