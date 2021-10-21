package com.psychology.studywork.repository;

import com.psychology.studywork.model.MedicalCard;
import org.springframework.data.repository.CrudRepository;
public interface MedicalCardRepository extends CrudRepository <MedicalCard, String> {
}
