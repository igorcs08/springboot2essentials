package academy.devdojo.springboot2essentials.integration;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import academy.devdojo.springboot2essentials.domain.Anime;
import academy.devdojo.springboot2essentials.domain.DevDojoUser;
import academy.devdojo.springboot2essentials.repository.AnimeRepository;
import academy.devdojo.springboot2essentials.repository.DevDojoUserRepository;
import academy.devdojo.springboot2essentials.request.AnimePostRequestBody;
import academy.devdojo.springboot2essentials.util.AnimeCreator;
import academy.devdojo.springboot2essentials.util.AnimePostRequestBodyCreator;
import academy.devdojo.springboot2essentials.wrapper.PageableResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AnimeControllerIT {
        @Autowired
        @Qualifier(value = "testRestTemplateRoleUser")
        private TestRestTemplate testRestTemplateRoleUser;
        @Autowired
        @Qualifier(value = "testRestTemplateRoleAdmin")
        private TestRestTemplate testRestTemplateRoleAdmin;
        @Autowired
        private AnimeRepository animeRepository;
        @Autowired
        private DevDojoUserRepository devDojoUserRepository;

        private static final DevDojoUser USER = DevDojoUser.builder()
                        .name("DevDojo Academy")
                        .password("{bcrypt}$2a$10$q1OPl/nVTyRFlgQXTA.y4.VpqCw8mvg8wSEAeQzLriV65v8W5eDeK")
                        .username("devdojo")
                        .authorities("ROLE_USER")
                        .build();

        private static final DevDojoUser ADMIN = DevDojoUser.builder()
                        .name("Igor Cavalcante")
                        .password("{bcrypt}$2a$10$q1OPl/nVTyRFlgQXTA.y4.VpqCw8mvg8wSEAeQzLriV65v8W5eDeK")
                        .username("igor")
                        .authorities("ROLE_USER,ROLE_ADMIN")
                        .build();

        @TestConfiguration
        @Lazy
        static class Config {
                @Bean(name = "testRestTemplateRoleUser")
                public TestRestTemplate testRestTemplateRoleUserCreator(@Value("${local.server.port}") int port) {
                        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                                        .rootUri("http://localhost:" + port)
                                        .basicAuthentication("devdojo", "academy");

                        return new TestRestTemplate(restTemplateBuilder);
                }

                @Bean(name = "testRestTemplateRoleAdmin")
                public TestRestTemplate testRestTemplateRoleAdminCreator(@Value("${local.server.port}") int port) {
                        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                                        .rootUri("http://localhost:" + port)
                                        .basicAuthentication("igor", "academy");

                        return new TestRestTemplate(restTemplateBuilder);
                }
        }

        @Test
        @DisplayName("list returns list of anime inside page object when successful")
        void list_ReturnsListOfAnimesInsidePageObject_WhenSuccessful() {

                Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

                devDojoUserRepository.save(USER);

                String expectedName = savedAnime.getName();

                PageableResponse<Anime> animePage = testRestTemplateRoleUser.exchange("/animes", HttpMethod.GET, null,
                                new ParameterizedTypeReference<PageableResponse<Anime>>() {

                                }).getBody();

                Assertions.assertThat(animePage).isNotNull();

                Assertions.assertThat(animePage.toList())
                                .isNotEmpty()
                                .hasSize(1);

                Assertions.assertThat(animePage.toList().get(0).getName()).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("listAll returns list of anime when successful")
        void list_ReturnsListOfAnimes_WhenSuccessful() {
                Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

                devDojoUserRepository.save(USER);

                String expectedName = savedAnime.getName();

                List<Anime> animes = testRestTemplateRoleUser.exchange("/animes/all", HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<Anime>>() {
                                }).getBody();

                Assertions.assertThat(animes)
                                .isNotNull()
                                .isNotEmpty()
                                .hasSize(1);

                Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("findById returns anime when successful")
        void findById_ReturnsListOfAnimes_WhenSuccessful() {
                Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

                devDojoUserRepository.save(USER);

                Long expectedId = savedAnime.getId();

                Anime anime = testRestTemplateRoleUser.getForObject("/animes/{id}", Anime.class, expectedId);

                Assertions.assertThat(anime).isNotNull();

                Assertions.assertThat(anime.getId()).isNotNull().isEqualTo(expectedId);
        }

        @Test
        @DisplayName("findByName returns anime when successful")
        void findByName_ReturnsListOfAnimes_WhenSuccessful() {
                Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

                devDojoUserRepository.save(USER);

                String expectedName = savedAnime.getName();

                String url = String.format("/animes/find?name=%s", expectedName);

                List<Anime> animes = testRestTemplateRoleUser.exchange(url, HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<Anime>>() {
                                }).getBody();

                Assertions.assertThat(animes)
                                .isNotNull()
                                .isNotEmpty()
                                .hasSize(1);

                Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("findByName returns an empty list of anime when anime is not found")
        void findByName_ReturnsEmptyListOfAnimes_WhenAnimeIsNotFound() {
                devDojoUserRepository.save(USER);

                List<Anime> animes = testRestTemplateRoleUser.exchange("/animes/find?name=dbz", HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<Anime>>() {
                                }).getBody();

                Assertions.assertThat(animes)
                                .isNotNull()
                                .isEmpty();

        }

        @Test
        @DisplayName("save returns anime when successful")
        void save_ReturnsAnimes_WhenSuccessful() {
                AnimePostRequestBody animePostRequestBody = AnimePostRequestBodyCreator
                                .createAnimePostRequestBody();

                devDojoUserRepository.save(USER);

                ResponseEntity<Anime> animeResponseEntity = testRestTemplateRoleUser.postForEntity("/animes",
                                animePostRequestBody,
                                Anime.class);

                Assertions.assertThat(animeResponseEntity).isNotNull();

                Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                Assertions.assertThat(animeResponseEntity.getBody()).isNotNull();

                Assertions.assertThat(animeResponseEntity.getBody().getId()).isNotNull();

        }

        @Test
        @DisplayName("replace updates anime when successful")
        void replace_UpdatesAnimes_WhenSuccessful() {

                Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

                devDojoUserRepository.save(USER);

                savedAnime.setName("new name");

                ResponseEntity<Void> animeResponseEntity = testRestTemplateRoleUser.exchange("/animes", HttpMethod.PUT,
                                new HttpEntity<>(savedAnime),
                                Void.class);

                Assertions.assertThat(animeResponseEntity).isNotNull();

                Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        }

        @Test
        @DisplayName("delete removes anime when successful")
        void delete_RemovesAnimes_WhenSuccessful() {

                Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

                devDojoUserRepository.save(ADMIN);

                ResponseEntity<Void> animeResponseEntity = testRestTemplateRoleAdmin.exchange("/animes/admin/{id}",
                                HttpMethod.DELETE,
                                null, Void.class, savedAnime.getId());

                Assertions.assertThat(animeResponseEntity).isNotNull();

                Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        }

        @Test
        @DisplayName("delete returns 403 when user is not admin")
        void delete_Returns403_WhenUserIsNotAdmin() {

                Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

                devDojoUserRepository.save(USER);

                ResponseEntity<Void> animeResponseEntity = testRestTemplateRoleUser.exchange("/animes/admin/{id}",
                                HttpMethod.DELETE,
                                null, Void.class, savedAnime.getId());

                Assertions.assertThat(animeResponseEntity).isNotNull();

                Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        }

}
