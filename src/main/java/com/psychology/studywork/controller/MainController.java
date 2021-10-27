package com.psychology.studywork.controller;
import com.psychology.studywork.model.Event;
import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import com.psychology.studywork.repository.EventRepository;
import com.psychology.studywork.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
public class MainController {


    @Autowired
    EventRepository eventRepository;
    @Autowired
    PersonRepository personRepository;

    @GetMapping("/")
    public String homepage(){
        return "index";
    }
    @GetMapping("/coaches")
    public String getCoaches(Model model){
        List<Person> coaches = findCoaches();
        model.addAttribute("coaches",coaches);
        return "coaches";
    }
    @GetMapping("/coaches/{id}")
    public String getCoach(@PathVariable String id, Model model){
        List<Person> result = findCoaches();
        if(result!=null){
            for (int i = 0; i < result.size() ; i++) {
                if(result.get(i).getId().equals(id)){

                    Person person = result.get(i);
                    model.addAttribute("person",person);
                    return "coach";

                }
            }
        } return "/coaches";


    }
    @GetMapping("/getConsultation/{Id}")
    public String getConsultation(@PathVariable String Id,
                                  Map<String,Object> model)
    {
        String coachId = Id;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail =  auth.getName();
        Optional<Person> getPerson = Optional.ofNullable(personRepository.findByEmailIgnoreCase(clientEmail));
        model.put("create",LocalDateTime.now());
        model.put("when", "");
        Optional<Person> coach = personRepository.findById(coachId);
        if(!coach.isPresent()){
            return "/coaches";  ///******!!!!СДЕЛАТЬ ОБРАБОТЧИК ОШИБОК!!!!****/////
        }
        model.put("clientEmail", clientEmail);
        model.put("coachId", coachId);
        model.put("description","" );
        if(getPerson.isPresent()){
            Person person = getPerson.get();
            model.put("clientId", person.getId());
            model.put("name", person.getName());
            model.put("surname", person.getSurname());
            model.put("gender", person.getGender());
            model.put("email",person.getEmail());
            model.put("telephone", person.getTelephone());
            return "getConsultation";
        }
        else{
            model.put("name", "");
            model.put("surname", "");
            model.put("gender", "");
            model.put("email","");
            model.put("telephone","");
            return "getConsultation";
            }
    }
    @PostMapping("/getConsultation/{Id}")
    public String createNewConsultation(
                                        @RequestParam String coachId,
                                        @RequestParam String when,
                                        @RequestParam String description,
                                        @RequestParam String clientId,
                                        @RequestParam String name,
                                        @RequestParam String surname,
                                        @RequestParam String gender,
                                        @RequestParam String email,
                                        @RequestParam String telephone,
                                        Map<String,Object> model )
    {
        Optional<Person>coach = personRepository.findById(coachId);
        if(!coach.isPresent()){
            HttpClientErrorException.BadRequest.create(HttpStatus.BAD_REQUEST,"Not Found this coach", new HttpHeaders(), new byte[128], Charset.defaultCharset());
        }
        Event event = new Event();
        event.setIdCoach(coachId);
        event.setData(LocalDateTime.parse(when));
        event.setTypeOfEvent("consultation");
        event.setDescription(description);
        Optional<Person> client = personRepository.findById(clientId);
        if(client.isPresent()){
            event.setIdClient(client.get().getId());
        }else{
            Person person = new Person();
            person.setEmail(email);
            person.setSurname(surname);
            person.setName(name);
            person.setGender(gender);
            person.setTelephone(telephone);
            personRepository.save(person);
            event.setIdClient(person.getId());
        }
        eventRepository.save(event);
        return "redirect:/main";


    }
    public List<Person> findCoaches(){
        List<Person> fullList = personRepository.findAll();
        List<Person> listCoaches = new ArrayList<Person>();
        for (int i = 0; i < fullList.size() ; i++) {
            if(fullList.get(i).getRole()== Role.COACH){
                listCoaches.add(fullList.get(i));
            }
        }
        return listCoaches;
    }


}
