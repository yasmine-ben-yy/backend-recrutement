// com.example.recrutement.interview.repository/InterviewQuestionRepository.java
package com.example.recrutement.interview.repository;

import com.example.recrutement.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByInterviewId(Long interviewId);
    void deleteByInterviewId(Long interviewId);
}