package records;

public class DescriptionPlaceRecord {
    private final String name;
    private final String description;

    public DescriptionPlaceRecord(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
