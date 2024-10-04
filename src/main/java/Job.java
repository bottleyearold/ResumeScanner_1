import java.util.List;

public class Job {
    private String title;
    private double gpa;
    private List<String> majors;
    private List<String> locations;
    private List<String> skills;

    // Constructor
    public Job(String title, double gpa, List<String> majors, List<String> locations, List<String> skills) {
        this.title = title;
        this.gpa = gpa;
        this.majors = majors;
        this.locations = locations;
        this.skills = skills;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public List<String> getMajors() {
        return majors;
    }

    public void setMajors(List<String> majors) {
        this.majors = majors;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }


}
