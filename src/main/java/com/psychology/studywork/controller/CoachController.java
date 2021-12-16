package com.psychology.studywork.controller;

import com.psychology.studywork.model.Event;
import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import com.psychology.studywork.repository.EventRepository;
import com.psychology.studywork.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.*;

@PreAuthorize("hasAuthority('COACH')")
@Controller
public class CoachController {
    @Autowired
    PersonRepository personRepository;
    @Autowired
    EventRepository eventRepository;

    @GetMapping("/coachSpace")
    public String getCoachSpace(Model model){
        Person coach = getAuthPerson();
        if(coach == null){
            return "redirect:/";
        }
        List<Event>coachEvents = getCoachEvents(coach.getId());
        model.addAttribute("events", coachEvents);
        model.addAttribute("person", coach);
            return "coachSpace";
        }

    @GetMapping("/clients")
    public String getClients(Model model){
        Person coach = getAuthPerson();
        if(coach==null){
            return "redirect:/";
        }
        List<Event> coachEvents = getCoachEvents(coach.getId());
        List<Person>clients = getClientsOfEvents(coachEvents);
        model.addAttribute("persons", clients);
        return "clients";

    }

    private List<Event> getCoachEvents(String idCoach){
        List<Event> events = eventRepository.findAll();
        List<Event> coachEvents = new ArrayList<>();
        for(int i = 0; i < events.size(); i++){
            if(events.get(i).getIdCoach().equals(idCoach)){
                coachEvents.add(events.get(i));
            }
        }
        return coachEvents;
    }

    private List<Person>getClientsOfEvents(List<Event>events){
        Set<String>clientsIdSet = new HashSet<>();
        for (int i = 0; i <events.size() ; i++) {
            clientsIdSet.add(events.get(i).getIdClient());
        }
        List<String> clientsId = new ArrayList<String>(clientsIdSet);
        List<Person>clients = new ArrayList<>();
        for(int i = 0; i < clientsId.size(); i++){
            Optional<Person> person = personRepository.findById(clientsId.get(i));
            if(person.isPresent()){
                clients.add(person.get());
            }
        }
        return clients;
    }

    @GetMapping("/clients/{Id}")
    public String getClientsForId(@PathVariable String Id, Model model){
        Person thisCoach = getAuthPerson();
        if(thisCoach == null){
            return "redirect:/";
        }
        Optional<Person> clientFromDB  = personRepository.findById(Id);
        if(!checkValidClientFromOptional(clientFromDB)){
            return "redirect:/";
        }
        Person client = clientFromDB.get();
        model.addAttribute("person", client);
        return "client";
    }

    @GetMapping("/makeConsultation/{Id}")
    public String makeConsultation(@PathVariable String Id, Model model){
        Person coach = getAuthPerson();
        if(coach == null){
            return "redirect:/";
        }
        Optional<Person> cl = personRepository.findById(Id);
        if(!checkValidClientFromOptional(cl)){
            return "redirect:/";
        }
        Person client = cl.get();
        model.addAttribute("coachId", coach.getId());
        makeModelForEvent(model, client);
        return "makeConsultation";
    }

    private Boolean checkValidClientFromOptional(Optional<Person> client){
        if(!client.isPresent()){
            return false;
        }
        Person person = client.get();
        if(!person.getRoles().contains(Role.CLIENT)){
            return false;
        }
        return true;
    }

    private Model makeModelForEvent(Model model, Person person){
        model.addAttribute("clientId", person.getId());
        model.addAttribute("name", person.getName());
        model.addAttribute("surname", person.getSurname());
        model.addAttribute("gender", person.getGender());
        model.addAttribute("email",person.getEmail());
        model.addAttribute("telephone", person.getTelephone());
        model.addAttribute("birthday", person.getBirthday());
        return model;
    }

    @PostMapping("/makeConsultation/{Id}")
    public String makeConsultation(@PathVariable String Id,
                                   @RequestParam String data,
                                   @RequestParam String description,
                                   @RequestParam String typeOfEvent){
        Event event = new Event();
        event.setIdClient(Id);
        try {
            event.setData(LocalDateTime.parse(data));
        }
        catch (Exception ex){
            System.out.println("Ошибка ввода даты при регистрации события");
            return "getConsultation";
        }
        Person coach = getAuthPerson();
        if(coach==null){return "redirect:/";}
        event.setIdCoach(coach.getId());
        event.setTypeOfEvent(typeOfEvent);
        event.setDescription(description);
        eventRepository.save(event);
        return "redirect:/coachSpace";
    }

    private Person getAuthPerson(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail =  auth.getName();
        Optional<Person> getPerson = Optional.ofNullable(personRepository.findByEmailIgnoreCase(clientEmail));
        if(!getPerson.isPresent()){
            auth.setAuthenticated(false);
            return null;
        }
        Person coach = getPerson.get();
        return coach;
    }

    @GetMapping("/deleteEvent/{Id}")
    public String deleteEvent(@PathVariable String Id){
        eventRepository.deleteById(Id);
        return "redirect:/coachSpace";
    }

    @GetMapping("/editEvent/{Id}")
    public String editEvent(@PathVariable String Id, Model model){
        Event event = getValidEvent(Id);
        if(event == null){
            return "redirect:coachSpace";
        }
        addAttributeForEventEditModel(model, event);
        return "editEventFromCoach";
    }

    private void addAttributeForEventEditModel(Model model,Event event){
        model.addAttribute("typeOfEvent", event.getTypeOfEvent());
        model.addAttribute("description", event.getDescription());
        model.addAttribute("data", event.getData());
        model.addAttribute("eventId", event.getId());
    }

    @PostMapping("/editEvent/{Id}")
    public String saveChangedEvent(@PathVariable String Id,
                                   @RequestParam String data,
                                   @RequestParam String description,
                                   @RequestParam String typeOfEvent){
        Event event = getValidEvent(Id);
        if(event == null){
            return "redirect:/";
        }
        event.setData(LocalDateTime.parse(data));
        event.setDescription(description);
        event.setTypeOfEvent(typeOfEvent);
        eventRepository.save(event);
        return "redirect:/coachSpace";
    }

    private Event getValidEvent(String id){
        Optional<Event> eventOptional = eventRepository.findById(id);
        if(!checkValidEvent(eventOptional)){
            return null;
        }
        Event event = eventOptional.get();
        return event;
    }

    private Boolean checkValidEvent(Optional<Event> event){
        if(!event.isPresent()){
            return false;
        }
        return true;
    }















}
