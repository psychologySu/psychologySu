package com.psychology.studywork.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class MedicalCard {
    @Id
    @GeneratedValue( generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;
    private String gender;
    private String story;
    private String yearsOld;
    private boolean isInRelationship;

    public MedicalCard(String id, String gender, String yearsOld, boolean isInRelationship, String story ) {
        this.id = id;
        this.gender = gender;
        this.yearsOld = yearsOld;
        this.isInRelationship = isInRelationship;
        this.story = story;
    }

    public MedicalCard(String gender, String yearsOld, boolean isInRelationship, String story) {
        this.gender = gender;
        this.yearsOld = yearsOld;
        this.isInRelationship = isInRelationship;
        this.story = story;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getYearsOld() {
        return yearsOld;
    }

    public void setYearsOld(String yearsOld) {
        this.yearsOld = yearsOld;
    }

    public boolean isInRelationship() {
        return isInRelationship;
    }

    public void setInRelationship(boolean inRelationship) {
        isInRelationship = inRelationship;
    }
}
