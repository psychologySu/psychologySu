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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PreAuthorize("hasAuthority('COACH')")
@Controller
public class CoachController {
    @Autowired
    PersonRepository personRepository;
    @Autowired
    EventRepository eventRepository;
    @GetMapping("/coachSpace")
    public String getCoachSpace(Model model){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String clientEmail =  auth.getName();
            Optional<Person> getPerson = Optional.ofNullable(personRepository.findByEmailIgnoreCase(clientEmail));
        if(!getPerson.isPresent()){
            auth.setAuthenticated(false);
            return "redirect:/";
        }
        Person coach = getPerson.get();
        List<Event>coachEvents = getCoachEvents(coach.getId());
        model.addAttribute("events", coachEvents);
        model.addAttribute("coach", coach);
            return "coachSpace";
        }
        private List<Event> getCoachEvents(String idCoach){
            List<Event> events = eventRepository.findAll();
            List<Event> coachEvents = new ArrayList<>();
            for(int i = 0; i<events.size();i++){
                if(events.get(i).getIdCoach().equals(idCoach)){
                    coachEvents.add(events.get(i));
                }
            }
            return coachEvents;

        }
}
