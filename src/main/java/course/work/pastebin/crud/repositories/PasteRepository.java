package course.work.pastebin.crud.repositories;

import course.work.pastebin.entities.Paste;
import course.work.pastebin.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface PasteRepository extends CrudRepository<Paste, Long> {
    Optional<Paste> findBySlug(String slug);


    @Modifying
    @Query(
            value = "DELETE FROM pastes WHERE expiration_date < :now",
            nativeQuery = true
    )
    void deleteExpiredPastes(@Param("now") Date now);

    List<Paste> findByUserAndExpirationDateAfterOrderByCreationDateDesc(
            @Param("user") User user,
            @Param("expirationDate") Date expirationDate
    );

}
