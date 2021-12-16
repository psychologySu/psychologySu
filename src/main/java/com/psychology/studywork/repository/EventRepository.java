package com.psychology.studywork.repository;

import com.psychology.studywork.model.Event;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface EventRepository extends CrudRepository<Event, String> {
    List<Event> findAll();
}
