package com.psychology.studywork.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Event implements Comparable<Event> {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid",strategy = "uuid")
    private String Id;
    private LocalDateTime createData;
    private LocalDateTime data;
    private String description;
    private String idCoach;
    private String idClient;
    private String typeOfEvent;

    Event(String idCoach, String idClient, String typeOfEvent, String description, LocalDateTime data){
        this.idCoach=idCoach;
        this.idClient = idClient;
        this.typeOfEvent= typeOfEvent;
        this.description = description;
        this.data= data;
    }
    Event(String idCoach, String idClient, String description, LocalDateTime data){
        this.idCoach=idCoach;
        this.idClient = idClient;
        this.description = description;
        this.data= data;
        this.typeOfEvent = "none";
    }
    public LocalDateTime getCreateData(){
        return createData;
    }

    public void setCreateData(LocalDateTime createData) {
        this.createData = createData;
    }
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdCoach() {
        return idCoach;
    }

    public void setIdCoach(String idCoach) {
        this.idCoach = idCoach;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getTypeOfEvent() {
        return typeOfEvent;
    }

    public void setTypeOfEvent(String typeOfEvent) {
        this.typeOfEvent = typeOfEvent;
    }

    @PrePersist
    public void onCreate(){
        this.setCreateData(LocalDateTime.now());
    }

    @Override
    public int compareTo(Event o) {
        return getData().compareTo(o.getData());
    }


}
