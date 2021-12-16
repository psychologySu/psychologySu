package com.psychology.studywork.controller;

import com.psychology.studywork.model.Event;
import com.psychology.studywork.model.Person;
import com.psychology.studywork.repository.EventRepository;
import com.psychology.studywork.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.*;

@PreAuthorize("hasAuthority('CLIENT')")
@Controller
public class ClientController {
    @Autowired
    PersonRepository personRepository;
    @Autowired
    EventRepository eventRepository;

    @GetMapping("/clientSpace")
    public String getClientSpace(Model model){
        Person person = getValidPerson();
        if(person == null){
            return "redirect:/";
        }
        model.addAttribute( "person",person);
        List<Event>clientEvents = getClientEvents(person.getId());
        Collections.sort(clientEvents);
        Collections.reverse(clientEvents);
        model.addAttribute("events",clientEvents);
        return "clientSpace";
    }

    private List<Event> getClientEvents(String idClient){
        List<Event> events = eventRepository.findAll();
        List<Event> clientEvents = new ArrayList<Event>() ;
        for (int i = 0; i < events.size() ; i++) {
            if(events.get(i).getIdClient().equals(idClient)){
                clientEvents.add(events.get(i));
            }
        }
        return clientEvents;
    }

    @GetMapping("/editClientPage")
    public String EditClientPage(Model model){
        Person person = getValidPerson();
        if(person == null){
            return "redirect:/";
        }
        model.addAttribute("name",person.getName());
        model.addAttribute("surname", person.getSurname());
        model.addAttribute("email", person.getEmail());
        model.addAttribute("birthday", person.getBirthday());
        model.addAttribute("telephone",person.getTelephone());
        model.addAttribute("gender", person.getGender());
        return "editClientPage";
    }

    @PostMapping("/editClientPage")
    public String SaveEditClientPage(@RequestParam String name,
                                     @RequestParam String surname,
                                     @RequestParam String email,
                                     @RequestParam String birthday,
                                     @RequestParam String telephone,
                                     @RequestParam String gender){
        Person person = getValidPerson();
        if(person == null){
            return "redirect:/";
        }
        person.setName(name);
        person.setSurname(surname);
        person.setEmail(email);
        person.setBirthday(LocalDate.parse(birthday));
        person.setTelephone(telephone);
        person.setGender(gender);
        personRepository.save(person);
        return "redirect:clientSpace";
    }

    public Person getValidPerson(){
        Person result;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail =  auth.getName();
        Optional<Person> getPerson = Optional.ofNullable(personRepository.findByEmailIgnoreCase(clientEmail));
        if(!getPerson.isPresent()){
            auth.setAuthenticated(false);
            return null;
        }
        result = getPerson.get();
        return result;
    }

}
