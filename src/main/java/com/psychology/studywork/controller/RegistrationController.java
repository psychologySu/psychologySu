package com.psychology.studywork.controller;

import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import com.psychology.studywork.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.HashSet;

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
                            @RequestParam String password){
        if(name == "" || surname == "" || password == ""){
            return "registration";
        }
        Person person = new Person();
        person.setEnable(true);
        person.setTelephone(phone);
        person.setGender(gender);
        person.setName(name);
        person.setSurname(surname);
        person.setEmail(email);
        try {
            person.setBirthday(LocalDate.parse(birthday));
        }catch (Exception ex){
            return "redirect:/registration";
        }
        person.setPassword(password);
        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.CLIENT);
        person.setRoles(roles);
        personRepository.save(person);
        return "redirect:/login";
    }
}
