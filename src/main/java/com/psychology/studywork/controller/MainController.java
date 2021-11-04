package com.psychology.studywork.controller;
import com.psychology.studywork.model.Event;
import com.psychology.studywork.model.Person;
import com.psychology.studywork.model.Role;
import com.psychology.studywork.repository.EventRepository;
import com.psychology.studywork.repository.PersonRepository;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.http.*;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDate;
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
    public String homepage(Model model)
    {
        HashMap<String,String> m = getWeather();
        model.addAttribute("now",m.get("now"));
        model.addAttribute("now_gt",m.get("now_gt"));
        model.addAttribute("icon",m.get("icon"));
        model.addAttribute("wind_speed",m.get("wind_speed"));
        model.addAttribute("wind_gust",m.get("wind_gust"));
        model.addAttribute("pressure_mm",m.get("pressure_mm"));
        model.addAttribute("temp",m.get("temp"));
        model.addAttribute("feels_like",m.get("feels_like"));
        model.addAttribute("humidity",m.get("humidity"));
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
                                        @PathVariable String Id,
                                        @RequestParam String data,
                                        @RequestParam String description,
                                        @RequestParam String name,
                                        @RequestParam String surname,
                                        @RequestParam String birthday,
                                        @RequestParam String typeOfEvent,
                                        @RequestParam String gender,
                                        @RequestParam String email,
                                        @RequestParam String telephone,
                                        Map<String,Object> model )
    {
        Optional<Person>coach = personRepository.findById(Id);
        if(!coach.isPresent()){
            HttpClientErrorException.BadRequest.create(HttpStatus.BAD_REQUEST,"Not Found this coach", new HttpHeaders(), new byte[128], Charset.defaultCharset());
        }
        Event event = new Event();
        event.setIdCoach(Id);
        event.setData(LocalDateTime.parse(data));
        event.setTypeOfEvent(typeOfEvent);
        event.setDescription(description);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String clientEmail =  auth.getName();
        Optional<Person> client = Optional.ofNullable(personRepository.findByEmailIgnoreCase(clientEmail));
        if(client.isPresent()){
            event.setIdClient(client.get().getId());
        }else{
            Person person = new Person();
            person.setEmail(email);
            person.setSurname(surname);
            person.setName(name);
            person.setGender(gender);
            person.setTelephone(telephone);
            person.setBirthday(LocalDate.parse(birthday));
            personRepository.save(person);
            event.setIdClient(person.getId());
        }
        eventRepository.save(event);
        return "redirect:/";


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
    private HashMap<String, String> getWeather() {

        JSONObject ob= getJSONObject();
        HashMap<String, String > result = new HashMap<>();
        result.put("now",ob.get("now").toString());
        result.put("now_gt",(String) ob.get("now_gt"));
        LinkedHashMap<String,Object> fact =(LinkedHashMap<String, Object>) ob.get("fact");
        result.put("icon", (String)fact.get("icon"));
        String f = fact.get("wind_speed").getClass().toString();
        if(f.equals("class java.lang.Integer")){
            result.put("wind_speed", Integer.toString((Integer)fact.get("wind_speed")));
        }
        if(f.equals("class java.lang.double")){
            result.put("wind_speed", Double.toString((Double)fact.get("wind_speed")));
        }
        result.put("wind_gust", Double.toString((Double)fact.get("wind_gust")));
        result.put("pressure_mm", Integer.toString((Integer)fact.get("pressure_mm")));
        result.put("temp", Integer.toString((Integer)fact.get("temp")));
        result.put("feels_like", Integer.toString((Integer)fact.get("feels_like")));
        result.put("humidity",Integer.toString((Integer)fact.get("humidity")));
        return result;
    }
    private JSONObject getJSONObject(){
        String key = "X-Yandex-API-Key: b1546c10-03aa-41aa-9d7a-6ecc3782dc1d";
        final String uri = "https://api.weather.yandex.ru/v2/informers?lat=56.8519&lon=60.6122&lang=ru_RU X-Yandex-API-Key:b1546c10-03aa-41aa-9d7a-6ecc3782dc1d" ;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        headers.set("X-Yandex-API-Key", "b1546c10-03aa-41aa-9d7a-6ecc3782dc1d");
        HttpEntity<String> entity = new HttpEntity<String>( headers);
        ResponseEntity<JSONObject> res = restTemplate.exchange("https://api.weather.yandex.ru/v2/informers?lat=56.8519&lon=60.6122&lang=ru_RU", HttpMethod.GET,entity,JSONObject.class);
        JSONObject result  = res.getBody();
        return result;
    }



}
