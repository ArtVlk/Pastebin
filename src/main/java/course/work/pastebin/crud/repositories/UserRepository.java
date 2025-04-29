package course.work.pastebin.crud.repositories;

import course.work.pastebin.entities.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface UserRepository extends CrudRepository<User, Long> {

    @Cacheable(value = "users", key = "#username")
    Optional<User> findByUsername(String username);
}
