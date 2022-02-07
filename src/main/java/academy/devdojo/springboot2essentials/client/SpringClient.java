package academy.devdojo.springboot2essentials.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import academy.devdojo.springboot2essentials.domain.Anime;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SpringClient {
    public static void main(String[] args) {        
        ResponseEntity<Anime> entity = new RestTemplate().getForEntity("http://localhost:8080/animes/{id}", Anime.class, 3);
        log.info(entity);
        Anime object = new RestTemplate().getForObject("http://localhost:8080/animes/{id}", Anime.class, 3);

        log.info(object);
    }    
}
