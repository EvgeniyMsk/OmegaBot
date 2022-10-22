package ou.entities;

public class Person implements TextRequest {
    private String lastName;
    private String firstName;
    private String patronymic;
    private String dateOfBirth;
    private String textRequest;

    public Person(String textRequest) {
        this.textRequest = textRequest;
    }

    @Override
    public String getTextRequest() {
        return textRequest;
    }
}
