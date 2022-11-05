package application;

import config.Config;
import config.Keys;
import config.URLs;
import records.DescriptionPlaceRecord;
import records.GeocodeRecord;
import records.PlacesRecord;
import records.WeatherRecord;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Requester {
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public CompletableFuture<List<GeocodeRecord>> requestGeocode(String namePlace) {
        String requestString = String.format(URLs.GEOCODE, namePlace, Keys.KEY_GEOCODE);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(requestString)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).
                thenApply(HttpResponse::body).
                thenApply(JsonParser::parseGeocode);
    }

    public CompletableFuture<WeatherRecord> requestWeather(double lat, double lng){
        String requestString = String.format(URLs.WEATHER, lat, lng, Keys.KEY_WEATHER);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(requestString)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).
                thenApply(HttpResponse::body).
                thenApply(JsonParser::parseWeather);
    }

    public CompletableFuture<List<PlacesRecord>> requestPlaces(double lat, double lng){
        String requestString = String.format(URLs.INTERESTING_PLACES, lat, lng, Config.RADIUS, Keys.KEY_PLACES);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(requestString)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).
                thenApply(HttpResponse::body).
                thenApply(JsonParser::parsePlaces);
    }

    public CompletableFuture<DescriptionPlaceRecord> requestDescription(String id){
        String requestString = String.format(URLs.DESCRIPTION_PLACE, id, Keys.KEY_PLACES);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(requestString)).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).
                thenApply(HttpResponse::body).
                thenApply(JsonParser::parseDescription);
    }
}
