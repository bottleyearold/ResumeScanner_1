package com.nlp.resumescanner_1;

import java.util.ArrayList;
import java.util.List;

public class JobCriteria {

    private String jobName;
    private List<String> codingLanguages;
    private Double gpa;
    private Integer priorExperience;
    private List<String> preferredLocations;
    private List<String> languages;
    private List<String> majors;  // New field for majors
    private Double weights;

    public JobCriteria() {
        this.codingLanguages = new ArrayList<>();
        this.preferredLocations = new ArrayList<>();
        this.languages = new ArrayList<>();
        this.majors = new ArrayList<>();  // Initialize majors to avoid null
    }


    // Getters and Setters for all fields
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<String> getCodingLanguages() {
        return codingLanguages;
    }

    public void setCodingLanguages(List<String> codingLanguages) {
        this.codingLanguages = codingLanguages;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public Integer getPriorExperience() {
        return priorExperience;
    }

    public void setPriorExperience(Integer priorExperience) {
        this.priorExperience = priorExperience;
    }

    public List<String> getPreferredLocations() {
        return preferredLocations;
    }

    public void setPreferredLocations(List<String> preferredLocations) {
        this.preferredLocations = preferredLocations;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getMajors() {  // Getter for majors
        return majors;
    }

    public void setMajors(List<String> majors) {  // Setter for majors
        this.majors = majors;
    }

    public Double getWeights() {
        return weights;
    }

    public void setWeights(Double weights) {
        this.weights = weights;
    }
}
