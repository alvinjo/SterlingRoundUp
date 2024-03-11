package com.alvin.roundup.repo;

import com.alvin.roundup.repo.domain.RoundUpJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RoundUpRepo extends JpaRepository<RoundUpJob, String> {

    RoundUpJob findByJobId(String jobId);

}
