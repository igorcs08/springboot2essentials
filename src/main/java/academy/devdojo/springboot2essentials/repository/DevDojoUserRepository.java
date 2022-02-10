package academy.devdojo.springboot2essentials.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import academy.devdojo.springboot2essentials.domain.DevDojoUser;

public interface DevDojoUserRepository extends JpaRepository<DevDojoUser, Long> {
    
    DevDojoUser findByUsername(String username);
}
