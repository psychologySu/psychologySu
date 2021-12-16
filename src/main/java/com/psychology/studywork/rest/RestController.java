package com.psychology.studywork.REST;

import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import com.psychology.studywork.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")

public class RestController {
    @Autowired
    PersonRepository personRepository;

    @GetMapping("/coaches")
    public List<Person> getCoaches(){
        List<Person> coaches = findCoaches();
        return coaches;
    }

    @GetMapping("/coaches/{Id}")
    public Person getCoach(@PathVariable String Id){
        Optional<Person> person = personRepository.findById(Id);
        if(person.isPresent()){
            return person.get();
        }
       return new Person("Not Found this coach", LocalDate.now(),"Not Found this coach","Not Found this coach","Not Found this coach","Not Found this coach","Not Found this coach","Not Found this coach");
    }

    public List<Person> findCoaches(){
        List<Person> fullList = personRepository.findAll();
        List<Person> listCoaches = new ArrayList<Person>();
        for (int i = 0; i < fullList.size() ; i++) {
            if(fullList.get(i).getRoles().contains(Role.COACH)){
                listCoaches.add(fullList.get(i));
            }
        }
        return listCoaches;
    }


}
