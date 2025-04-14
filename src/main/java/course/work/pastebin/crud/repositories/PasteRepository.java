package course.work.pastebin.crud.repositories;

import course.work.pastebin.entities.Paste;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface PasteRepository extends CrudRepository<Paste, Long> {
    Optional<Paste> findBySlug(String slug);
}
