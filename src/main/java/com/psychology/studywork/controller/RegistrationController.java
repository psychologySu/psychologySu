package com.psychology.studywork.controller;

import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import com.psychology.studywork.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class RegistrationController {
    @Autowired
    PersonRepository personRepository;

    @GetMapping("/registration")
    public String getRegistrationPage(){
        return "registration";
    }
    @PostMapping("/registration")
    public String addPerson(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String phone,
                            @RequestParam String gender,
                            @RequestParam String surname,
                            @RequestParam String birthday,
                            @RequestParam String password)
    {
        if(name==""&& surname==""&&password==""){
            return "registration";
        }
        Person person = new Person();
        person.setEnable(true);
        person.setTelephone(phone);
        person.setGender(gender);
        person.setName(name);
        person.setSurname(surname);
        person.setEmail(email);
        person.setBirthday(LocalDate.parse(birthday));
        person.setPassword(password);
        person.setRole(Role.CLIENT);
        personRepository.save(person);
        return "redirect:/login";
    }
}
