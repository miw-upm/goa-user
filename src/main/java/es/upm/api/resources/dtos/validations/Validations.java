package es.upm.api.resources.dtos.validations;

public class Validations {
    public static final String MOBILE = "\\+\\d{8,15}|\\d{9}|\\d{1}|\\d{2}";
    public static final String MOBILE_RX = "^" + MOBILE + "$";
    public static final String ID_WITH_UUID = "/{id:[0-9a-fA-F\\-]{36}}";
    public static final String ID_WITH_UUID_BASE64 = "/{id:[A-Za-z0-9_-]{22}}";
    public static final String ID_WITH_MOBILE = "/{id:(?:" + MOBILE + ")}";

    private Validations() {
        //Empty
    }
}

