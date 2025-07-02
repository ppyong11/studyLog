package com.studylog.project.timer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Timer;

@Repository
public interface TimerRepository extends CrudRepository<Timer, Long> {
}
