package records;

public class GeocodeRecord {
    private final double lat;
    private final double lng;
    private final String name;

    public GeocodeRecord(double lat, double lon, String  name){
        this.lat = lat;
        this.lng = lon;
        this.name = name;
    }

    public double getLat(){
        return lat;
    }

    public double getLng(){
        return lng;
    }

    public String getName() {
        return name;
    }
}
