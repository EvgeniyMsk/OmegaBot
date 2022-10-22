package ou.entities;

public class Car implements TextRequest{
    private String textRequest;
    private String GRZ;

    public Car(String textRequest) {
        this.textRequest = textRequest;
        this.GRZ = textRequest;
    }

    @Override
    public String getTextRequest() {
        return GRZ;
    }
}
