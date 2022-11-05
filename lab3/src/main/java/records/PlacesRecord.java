package records;

public class PlacesRecord {
    private final String name;
    private final String id;

    public PlacesRecord(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
