package com.nlp.resumescanner_1;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class JobCriteriaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobCriteriaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save method to insert JobCriteria into the database
    public JobCriteria save(JobCriteria jobCriteria) {
        jdbcTemplate.update(
                "INSERT INTO job_criteria (job_name, coding_languages, gpa, prior_experience, preferred_locations, languages, weights,majors) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                jobCriteria.getJobName(),

                // Check if codingLanguages is null and use an empty list if it is
                jobCriteria.getCodingLanguages() != null ? String.join(", ", jobCriteria.getCodingLanguages()) : "",

                jobCriteria.getGpa(),
                jobCriteria.getPriorExperience(),

                // Check if preferredLocations is null and use an empty list if it is
                jobCriteria.getPreferredLocations() != null ? String.join(", ", jobCriteria.getPreferredLocations()) : "",

                // Check if languages is null and use an empty list if it is
                jobCriteria.getLanguages() != null ? String.join(", ", jobCriteria.getLanguages()) : "",
                jobCriteria.getWeights(),
                // Check if majors is null and use an empty list if it is
                jobCriteria.getMajors() != null ? String.join(", ", jobCriteria.getMajors()) : ""

        );
        return jobCriteria;
    }


    // Custom findAll method to get all JobCriteria from the database
    public List<JobCriteria> findAll() {
        String sql = "SELECT * FROM job_criteria";
        return jdbcTemplate.query(sql, this::mapRowToJobCriteria);
    }

    // Helper method to map SQL result set to JobCriteria object
    private JobCriteria mapRowToJobCriteria(ResultSet rs, int rowNum) throws SQLException {
        JobCriteria jobCriteria = new JobCriteria();
        jobCriteria.setJobName(rs.getString("job_name"));
        jobCriteria.setCodingLanguages(Arrays.asList(rs.getString("coding_languages").split(", ")));
        jobCriteria.setGpa(rs.getDouble("gpa"));
        jobCriteria.setPriorExperience(rs.getInt("prior_experience"));
        jobCriteria.setPreferredLocations(Arrays.asList(rs.getString("preferred_locations").split(", ")));
        jobCriteria.setLanguages(Arrays.asList(rs.getString("languages").split(", ")));

        // Handling null for the majors field
        String majors = rs.getString("majors");
        jobCriteria.setMajors(majors != null ? Arrays.asList(majors.split(", ")) : new ArrayList<>());

        jobCriteria.setWeights(rs.getDouble("weights"));
        return jobCriteria;
    }

}
