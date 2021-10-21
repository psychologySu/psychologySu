package com.psychology.studywork.repository;

import com.psychology.studywork.model.Event;
import org.springframework.data.repository.CrudRepository;
public interface EventRepository extends CrudRepository<Event, String> {

}
