package com.nlp.resumescanner_1;

import com.nlp.resumescanner_1.JobCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class JobCriteriaController {

    @Autowired
    private JobCriteriaRepository jobCriteriaRepository;

    @PostMapping("/addJob")
    public ResponseEntity<JobCriteria> addJob(@RequestBody JobCriteria jobCriteria) {
        JobCriteria savedCriteria = jobCriteriaRepository.save(jobCriteria);
        return ResponseEntity.ok(savedCriteria);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobCriteria>> getAllJobs() {
        List<JobCriteria> jobs = jobCriteriaRepository.findAll();
        return ResponseEntity.ok(jobs);
    }

}
