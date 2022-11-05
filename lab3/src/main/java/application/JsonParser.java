package application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import records.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<GeocodeRecord> parseGeocode(String json) {
        List<GeocodeRecord> res = new ArrayList<>();
        try {
            JsonNode jsonNode = objectMapper.readValue(json, JsonNode.class);
            JsonNode list = jsonNode.get("hits");
            for (JsonNode node : list) {
                String name = node.get("name").asText();
                double lat = node.get("point").get("lat").asDouble();
                double lng = node.get("point").get("lng").asDouble();
                res.add(new GeocodeRecord(lat, lng, name));
            }
            return res;
        } catch (IOException e){
            System.out.println("parsing fail");
            return null;
        }
    }

    public static WeatherRecord parseWeather(String json){
        try{
            JsonNode jsonNode = objectMapper.readValue(json, JsonNode.class);
            List<Weather> weatherList = new ArrayList<>();
            JsonNode list = jsonNode.get("weather");
            for (JsonNode node : list){
                String weather = node.get("main").asText();
                String description = node.get("description").asText();
                weatherList.add(new Weather(weather, description));
            }
            double temp = jsonNode.get("main").get("temp").asDouble();
            double speed = jsonNode.get("wind").get("speed").asDouble();
            return new WeatherRecord(temp, weatherList, speed);
        } catch (JsonProcessingException e) {
            System.out.println("parsing fail");
            return null;
        }
    }

    public static List<PlacesRecord> parsePlaces(String json){
        List<PlacesRecord> res = new ArrayList<>();
        try {
            JsonNode jsonNode = objectMapper.readValue(json, JsonNode.class);
            for (JsonNode node : jsonNode){
                String name = node.get("name").asText();
                String id = node.get("xid").asText();
                res.add(new PlacesRecord(name, id));
            }
            return res;
        } catch (IOException ignored){
            System.out.println("parsing fail");
            return null;
        }
    }

    public static DescriptionPlaceRecord parseDescription(String json){
        try {
            JsonNode jsonNode = objectMapper.readValue(json, JsonNode.class);
            String name = jsonNode.get("name").asText();
            String descr = "";
            JsonNode n = jsonNode.get("info");
            if (n != null && n != NullNode.getInstance())
                descr = n.get("descr").asText();
            return new DescriptionPlaceRecord(name, descr);
        } catch (JsonProcessingException e) {
            System.out.println("parsing fail");
            return null;
        }
    }
}
