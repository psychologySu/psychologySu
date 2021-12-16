package com.psychology.studywork.controller;

import com.psychology.studywork.model.Event;
import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import com.psychology.studywork.repository.EventRepository;
import com.psychology.studywork.repository.PersonRepository;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class MainController {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    PersonRepository personRepository;

    @GetMapping("/")
    public String homepage(Model model){
        if(isAuthenticated()) {
            model.addAttribute("stateOfLogin",true);
            model.addAttribute("loginValue", "/myspace");
            model.addAttribute("loginText","Личный кабинет");
        }else{model.addAttribute("loginValue","/login");
            model.addAttribute("stateOfLogin", false);
            model.addAttribute("loginText","Авторизация");
        }
        model.addAttribute("stateForGetWeather", true);
        try{
            fillWeatherForm(model);
        }catch(Exception ex){
            model.addAttribute("stateForGetWeather", false);
            System.out.println("Ошибка при получении данных о погоде");
        }

        return "index";
    }

    private Boolean isAuthenticated(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail =  auth.getName();
        Optional<Person> getPerson = Optional.ofNullable(personRepository.findByEmailIgnoreCase(clientEmail));
        return getPerson.isPresent();
    }

    private void fillWeatherForm(Model model){
        HashMap<String,String> weather = getWeather();
        model.addAttribute("now",weather.get("now"));
        model.addAttribute("now_gt",weather.get("now_gt"));
        model.addAttribute("icon",weather.get("icon"));
        model.addAttribute("wind_speed",weather.get("wind_speed"));
        model.addAttribute("wind_gust",weather.get("wind_gust"));
        model.addAttribute("pressure_mm",weather.get("pressure_mm"));
        model.addAttribute("temp",weather.get("temp"));
        model.addAttribute("feels_like",weather.get("feels_like"));
        model.addAttribute("humidity",weather.get("humidity"));
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
        if(result != null){
            for (Person value : result) {
                if (value.getId().equals(id)) {
                    model.addAttribute("person", value);
                    return "coach";
                }
            }
        } return "redirect:/coaches";

    }

    public List<Person> findCoaches(){
        List<Person> fullList = personRepository.findAll();
        List<Person> listCoaches = new ArrayList<>();
        for (Person person : fullList) {
            if (person.getRoles().contains(Role.COACH)) {
                listCoaches.add(person);
            }
        }
        return listCoaches;
    }

    @GetMapping("/myspace")
    public String getMySpace(){
       Person person = getValidAuthPerson();
       if(person == null){
           return "redirect:/";
       }
       Set<Role>roles = person.getRoles();
       if(roles.contains(Role.ADMIN)){
            return "redirect:adminSpace";
       }
       if(roles.contains(Role.COACH)){
            return "redirect:coachSpace";
       }
       return "redirect:clientSpace";
    }

    @GetMapping("/getConsultation/{Id}")
    public String getConsultation(@PathVariable String Id, Map<String,Object> model){
        Person person = getValidAuthPerson();
        model.put("create",LocalDateTime.now());
        model.put("when", "");
        model.put("coachId", Id);
        model.put("description","" );
        if(person != null){
            model.put("clientEmail", person.getEmail());
            model.put("clientId", person.getId());
            model.put("name", person.getName());
            model.put("surname", person.getSurname());
            model.put("gender", person.getGender());
            model.put("email","");
            model.put("telephone", person.getTelephone());
            model.put("birthday", person.getBirthday());
            model.put("personState",false);
        }
        else{
            return "redirect:/login";
        }
        return "getConsultation";
    }

    @PostMapping("/getConsultation/{Id}")
    public String createNewConsultation(
                                        @PathVariable String Id,
                                        @RequestParam String data,
                                        @RequestParam String description,
                                        @RequestParam String name,
                                        @RequestParam String surname,
                                        @RequestParam String typeOfEvent,
                                        @RequestParam String gender,
                                        @RequestParam String telephone)
    {
        Optional<Person>coach = personRepository.findById(Id);
        if(!coach.isPresent()){
            return "redirect:/mySpace";
        }
        Event event = new Event();
        event.setIdCoach(Id);
        try {
            event.setData(LocalDateTime.parse(data));
        }
        catch (Exception ex){
            return "redirect:/getConsultation/{Id}";
        }
        event.setTypeOfEvent(typeOfEvent);
        event.setDescription(description);
        Person client = getValidAuthPerson();
        if(client != null){
            client.setName(name);
            client.setGender(gender);
            client.setSurname(surname);
            client.setTelephone(telephone);
            event.setIdClient(client.getId());
        }else{
            return "redirect:/login";
        }
        eventRepository.save(event);
        return "redirect:/";
    }

    private Person getValidAuthPerson(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail =  auth.getName();
        Optional<Person> getPerson = Optional.ofNullable(personRepository.findByEmailIgnoreCase(clientEmail));
        if (!getPerson.isPresent()){
            return null;
        }
        return getPerson.get();
    }

    private HashMap<String, String> getWeather(){
        JSONObject ob= getJSONObject();
        HashMap<String, String > result = new HashMap<>();
        result.put("now",ob.get("now").toString());
        result.put("now_gt",(String) ob.get("now_gt"));
        LinkedHashMap<String,Object> fact =(LinkedHashMap<String, Object>) ob.get("fact");
        result.put("icon", (String)fact.get("icon"));
        result.put("wind_speed", fact.get("wind_speed").toString());
        result.put("wind_gust", fact.get("wind_gust").toString());
        result.put("pressure_mm", fact.get("pressure_mm").toString());
        result.put("temp", fact.get("temp").toString());
        result.put("feels_like", fact.get("feels_like").toString());
        result.put("humidity",fact.get("humidity").toString());
        return result;
    }

    private JSONObject getJSONObject(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("X-Yandex-API-Key", "b1546c10-03aa-41aa-9d7a-6ecc3782dc1d");
        HttpEntity<String> entity = new HttpEntity<String>( headers);
        ResponseEntity<JSONObject> res = restTemplate.exchange("https://api.weather.yandex.ru/v2/informers?lat=56.8519&lon=60.6122&lang=ru_RU", HttpMethod.GET,entity,JSONObject.class);
        JSONObject result  = res.getBody();
        return result;
    }



}
