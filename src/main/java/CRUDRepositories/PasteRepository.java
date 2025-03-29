package CRUDRepositories;

import course.work.pastebin.Entities.Paste;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasteRepository extends CrudRepository<Paste, Long> {
}
