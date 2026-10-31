package restaurantrush.controller;

public enum TurnStep {
    ARRIVALS("1", "Arrivals"),
    MANAGER_CHOICES("2-3", "Manager choices, kitchen and serving"),
    EATING_AND_PAYMENT("4", "Eating and payment"),
    WAITING_AND_ABANDONMENT("5", "Waiting and abandonment");

    private final String number;

    private final String title;

    TurnStep(String number, String title) {
        this.number = number;
        this.title = title;
    }

    public String getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }
}
