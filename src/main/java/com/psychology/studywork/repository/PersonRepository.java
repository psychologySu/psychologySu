package com.psychology.studywork.repository;

import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends CrudRepository<Person, String> {
   Person findByEmailIgnoreCase(@Param("email") String email);
   List<Person> findAll();
}
