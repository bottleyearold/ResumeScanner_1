package com.nlp.resumescanner_1.Controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlp.resumescanner_1.JobCriteria;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1")
public class NERController {

  @Autowired private StanfordCoreNLP stanfordCoreNLP;
    @Qualifier("handlerExceptionResolver")
    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

  // Change method signature
  @PostMapping(value = "/analyze")
  public List<MatchResult> analyze(
      @RequestParam("resume") MultipartFile resumeFile,
      @RequestParam("criteria") String criteriaJson)
      throws IOException {
    String resumeText;
    try {
      // Read PDF file and extract text
      resumeText = extractTextFromPdf(resumeFile);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read the PDF file", e);
    }
    // Deserialize criteria JSON to a List of Criteria objects
    List<Criteria> criteriaList = new ObjectMapper().readValue(criteriaJson, new TypeReference<List<Criteria>>() {});
    // Static method

// Analyze the resume text with Stanford CoreNLP
    CoreDocument coreDocument = new CoreDocument(resumeText);
    stanfordCoreNLP.annotate(coreDocument);
    List<CoreLabel> coreLabels = coreDocument.tokens();  // <-- This is the definition of 'coreLabels'

// Iterate over the criteriaList and perform matching
    List<MatchResult> results = new ArrayList<>();

    for (Criteria criteria : criteriaList) {
      // Now you can access each criteria object in the list and use coreLabels for matching
      results.add(matchPriorExperience(coreLabels, criteria.getPriorExperience()));
      results.add(matchGPA(coreLabels, criteria.getGpa()));
      results.addAll(matchCodingSkills(coreLabels, criteria.getCodingLanguages()));
      results.addAll(matchMajor(coreLabels, criteria.getMajor() != null ? criteria.getMajor() : new ArrayList<>()));
      System.out.println("Majors to match: " + criteria.getMajor());
      results.addAll(matchLocation(coreLabels, criteria.getPreferredLocations()));
      results.addAll(matchLanguages(coreLabels, criteria.getLanguage()));
    }


// Sort the results
    results.sort((r1, r2) -> Boolean.compare(r2.isMatched(), r1.isMatched()));


    // Deserialize criteria JSON to Criteria object
//    Criteria criteria = new ObjectMapper().readValue(criteriaJson, Criteria.class);

    return results;
  }

  private String extractTextFromPdf(MultipartFile file) throws IOException {
    // Use Apache PDFBox or another library to extract text from the PDF
    PDDocument document = PDDocument.load(file.getInputStream());
    PDFTextStripper pdfStripper = new PDFTextStripper();
    String text = pdfStripper.getText(document);
    document.close();
    return text;
  }

  // Method to match languages
  private List<MatchResult> matchLanguages(
          List<CoreLabel> coreLabels, List<String> requiredLanguages) {
    Set<String> foundLanguages =
            coreLabels.stream()
                    .map(coreLabel -> coreLabel.originalText().toLowerCase())
                    .filter(
                            language ->
                                    requiredLanguages.stream()
                                            .map(String::toLowerCase)
                                            .collect(Collectors.toSet())
                                            .contains(language))
                    .collect(Collectors.toSet());

    List<MatchResult> res = new ArrayList<>();
    for (String language : requiredLanguages) {
      boolean matched = foundLanguages.contains(language.toLowerCase());
      res.add(new MatchResult("Language", language, matched));
    }
    return res;
  }

  // Method to match coding skills
  private List<MatchResult> matchCodingSkills(List<CoreLabel> coreLabels, List<String> requiredSkills) {
    // Use a Set to ensure no duplicate skills
    Set<String> foundSkills = coreLabels.stream()
            .map(coreLabel -> coreLabel.originalText().toLowerCase().trim())
            .filter(skill -> requiredSkills.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet())
                    .contains(skill))
            .collect(Collectors.toSet());

    List<MatchResult> res = new ArrayList<>();
    // Use another Set to keep track of added skills and prevent duplicates in results
    Set<String> uniqueSkills = new HashSet<>();

    for (String skill : requiredSkills) {
      String skillLower = skill.toLowerCase();
      if (!uniqueSkills.contains(skillLower)) {
        boolean matched = foundSkills.contains(skillLower);
        res.add(new MatchResult("Coding Skill", skill, matched));
        uniqueSkills.add(skillLower); // Add to the set to prevent future duplicates
      }
    }

    return res;
  }



  // Method to match GPA requirement
  private MatchResult matchGPA(List<CoreLabel> coreLabels, String requiredGPA) {
    double gpaRequirement = Double.parseDouble(requiredGPA);
    Pattern gpaPattern =
            Pattern.compile("\\b([0-4](\\.\\d{1,2}))\\b"); // GPA format, e.g., 3.0, 3.5
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

  // Method to match prior experience
  private MatchResult matchPriorExperience(List<CoreLabel> coreLabels, String requiredYears) {
    int requiredExperience = Integer.parseInt(requiredYears);
    StringBuilder resumeText = new StringBuilder();
    for (CoreLabel label : coreLabels) {
      resumeText.append(label.originalText()).append(" ");
    }

    Pattern experiencePattern = Pattern.compile("\\b(\\d{1,2})\\s+years?\\b");
    Matcher matcher = experiencePattern.matcher(resumeText.toString().toLowerCase());

    if (matcher.find()) {
      int foundExperience = Integer.parseInt(matcher.group(1));
      return new MatchResult("Work Experience", matcher.group(), foundExperience >= requiredExperience);
    }

    return new MatchResult("Work Experience", "Not Found", false);
  }

  // Method to match locations
  private List<MatchResult> matchLocation(
          List<CoreLabel> coreLabels, List<String> requiredLocations) {
    // Convert required locations to lowercase and trim spaces
    List<String> cleanedLocations =
            requiredLocations.stream()
                    .map(location -> location.toLowerCase().trim())
                    .collect(Collectors.toList());

    String resumeText =
            coreLabels.stream()
                    .map(coreLabel -> coreLabel.originalText().toLowerCase())
                    .collect(Collectors.joining(" ")); // Join all tokens with a space

    // Prepare results based on matches
    List<MatchResult> values = new ArrayList<>();
    for (String location : cleanedLocations) {
      boolean matched =
              resumeText.contains(location); // Check if the location is present in the full resume text
      values.add(new MatchResult("State", location, matched));
    }
    return values;
  }



  // Method to match majors
  private List<MatchResult> matchMajor(List<CoreLabel> coreLabels, List<String> requiredMajors) {
    Set<String> foundMajors = coreLabels.stream()
            .map(coreLabel -> coreLabel.originalText().toLowerCase().trim())
            .filter(major -> requiredMajors.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet())
                    .contains(major))
            .collect(Collectors.toSet());

    List<MatchResult> res = new ArrayList<>();
    Set<String> uniqueMajors = new HashSet<>();

    for (String major : requiredMajors) {
      String majorLower = major.toLowerCase();
      if (!uniqueMajors.contains(majorLower)) {
        boolean matched = foundMajors.contains(majorLower);
        res.add(new MatchResult("Major", major, matched));
        uniqueMajors.add(majorLower); // Add to set to avoid duplicates
      }
    }

    return res;
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
  public static class Criteria {
    private List<String> codingLanguages;
    private String gpa;
    private String priorExperience;
    private List<String> preferredLocations;
    private List<String> language;
    @JsonProperty("majors")
    private List<String> majors;
    private String jobName;

    public String getJobName() {
      return jobName;
    }

    public void setJobName(String jobName) {
      this.jobName = jobName;
    }

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
    public List<String> getLanguage() { return language;}
    public List<String> getMajor() {
      return majors != null ? majors : new ArrayList<>();  // Ensure major is never null
    }

    public void setMajor(List<String> majors) {
      this.majors = majors != null ? majors : new ArrayList<>();  // Ensure majors is never null
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
}
