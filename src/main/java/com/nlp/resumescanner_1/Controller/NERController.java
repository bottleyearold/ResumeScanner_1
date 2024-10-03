package com.nlp.resumescanner_1.Controller;
import com.nlp.resumescanner_1.model.Type;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1")
public class NERController {

    @Autowired
    private StanfordCoreNLP stanfordCoreNLP;

    @PostMapping(value = "/analyze")
    public List<MatchResult> analyze(@RequestBody ResumeInput input) {
        CoreDocument coreDocument = new CoreDocument(input.getResume());
        stanfordCoreNLP.annotate(coreDocument);
        List<CoreLabel> coreLabels = coreDocument.tokens();

        // Analyze the resume against the criteria
        List<MatchResult> results = new ArrayList<>();

        results.add(matchPriorExperience(coreLabels, input.getCriteria().getPriorExperience()));

        results.add(matchGPA(coreLabels, input.getCriteria().getGpa()));

        results.addAll(matchCodingSkills(coreLabels, input.getCriteria().getCodingLanguages()));
        results.addAll(matchLocation(coreLabels, input.getCriteria().getPreferredLocations()));
        results.sort((r1, r2) -> Boolean.compare(r2.isMatched(), r1.isMatched()));  // Sort with true (matched) first

        return results;
    }



    // Method to match coding skills
    private List<MatchResult> matchCodingSkills(List<CoreLabel> coreLabels, List<String> requiredSkills) {
        // Lowercase all the skills in the resume and check if they match required skills
        Set<String> foundSkills = coreLabels.stream()
                .map(coreLabel -> coreLabel.originalText().toLowerCase())
                .filter(skill -> requiredSkills.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet())
                        .contains(skill))
                .collect(Collectors.toSet());

        // Prepare results based on matches
        List<MatchResult> results = new ArrayList<>();
        for (String skill : requiredSkills) {
            boolean matched = foundSkills.contains(skill.toLowerCase());
            results.add(new MatchResult("Coding Skill", skill, matched));
        }
        return results;
    }

    // Method to match GPA requirement
    private MatchResult matchGPA(List<CoreLabel> coreLabels, String requiredGPA) {
        double gpaRequirement = Double.parseDouble(requiredGPA);
        // Ensure the pattern captures GPAs more strictly
        Pattern gpaPattern = Pattern.compile("\\b([0-4](\\.\\d{1,2}))\\b");  // GPA format, e.g., 3.0, 3.5
        for (CoreLabel label : coreLabels) {
            String text = label.originalText();
            Matcher matcher = gpaPattern.matcher(text);
            if (matcher.matches()) {
                double foundGPA = Double.parseDouble(matcher.group(1));
                return new MatchResult("GPA", text, foundGPA >= gpaRequirement);
            }
        }
        return new MatchResult("GPA", "Not Found", false);
    }

    private MatchResult matchPriorExperience(List<CoreLabel> coreLabels, String requiredYears) {
        int requiredExperience = Integer.parseInt(requiredYears);

        // We will concatenate the tokens and then look for experience patterns
        StringBuilder resumeText = new StringBuilder();
        for (CoreLabel label : coreLabels) {
            resumeText.append(label.originalText()).append(" ");
        }

        // Handle both "year" and "years" properly
        Pattern experiencePattern = Pattern.compile("\\b(\\d{1,2})\\s+years?\\b");
        Matcher matcher = experiencePattern.matcher(resumeText.toString().toLowerCase());  // Convert resume to lowercase for case-insensitive matching

        if (matcher.find()) {
            int foundExperience = Integer.parseInt(matcher.group(1));  // Get the years from the capturing group
            return new MatchResult("Work Experience", matcher.group(), foundExperience >= requiredExperience);
        }

        return new MatchResult("Work Experience", "Not Found", false);
    }
    private List<MatchResult> matchLocation(List<CoreLabel> coreLabels, List<String> requiredLocations) {
        // Convert required locations to lowercase and trim spaces
        List<String> cleanedLocations = requiredLocations.stream()
                .map(location -> location.toLowerCase().trim())
                .collect(Collectors.toList());

        // Concatenate core labels into a single string for easier matching of multi-word phrases
        String resumeText = coreLabels.stream()
                .map(coreLabel -> coreLabel.originalText().toLowerCase())
                .collect(Collectors.joining(" "));  // Join all tokens with a space

        // Prepare results based on matches
        List<MatchResult> values = new ArrayList<>();
        for (String location : cleanedLocations) {
            boolean matched = resumeText.contains(location);  // Check if the location is present in the full resume text
            values.add(new MatchResult("Location", location, matched));
        }
        return values;
    }



}

// Class for the resume input from the admin form
class ResumeInput {
    private String resume;
    private Criteria criteria;

    // Getters and setters
    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }
}

// Class for the admin's criteria input (e.g., coding languages, GPA, etc.)
class Criteria {
    private List<String> codingLanguages;
    private String gpa;
    private String priorExperience;
    private List<String> preferredLocations;


    public List<String> getPreferredLocations() {
        return preferredLocations;
    }

    public void setPreferredLocations(List<String> preferredLocations) {
        this.preferredLocations = preferredLocations;
    }

    public List<String> getCodingLanguages() {
        return codingLanguages;
    }

    public void setCodingLanguages(List<String> codingLanguages) {
        this.codingLanguages = codingLanguages;
    }

    public String getGpa() {
        return gpa;
    }

    public void setGpa(String gpa) {
        this.gpa = gpa;
    }

    public String getPriorExperience() {
        return priorExperience;
    }

    public void setPriorExperience(String priorExperience) {
        this.priorExperience = priorExperience;
    }
}

// Class to represent the result of each match attempt
class MatchResult {
    private String field;
    private String value;
    private boolean matched;

    public MatchResult(String field, String value, boolean matched) {
        this.field = field;
        this.value = value;
        this.matched = matched;
    }

    // Getters and setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }
}
