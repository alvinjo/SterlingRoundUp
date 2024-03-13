package com.alvin.roundup.repo;

import com.alvin.roundup.domain.RoundUpJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundUpRepo extends JpaRepository<RoundUpJob, String> {

}
