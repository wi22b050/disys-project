import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Stations {

    @Id
    private int id;

    private String dbUrl;

    private Double lat;

    private Double lng;

    public int getId() {
        return id;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
