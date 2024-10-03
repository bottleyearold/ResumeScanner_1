package com.nlp.resumescanner_1.model;
public enum Type {

    PERSON("Person"),
    CITY("City"),
    STATE_OR_PROVINCE("State_Or_Province"),
    COUNTRY("Country"),
    EMAIL("Email"),
    TITLE("Title"),
    RANKING("Ranking"),
    LOCATION("Location"),
    GPA("GPA"),
    PRIOR_WORK_EXPERIENCE("Prior_Work_Experience"),
    CLEARANCE("Clearance"),
    CODING_LANGUAGES("Coding_Languages_Skills_Frameworks"),
    MAJOR("Major"),
    CERTIFICATIONS("Certifications"),
    LANGUAGES("Languages");

    private String type;

    Type(String type) {
        this.type = type;
    }

    public String getName() {
        return type;
    }
}
