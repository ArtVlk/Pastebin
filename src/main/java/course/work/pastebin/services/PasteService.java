package course.work.pastebin.services;

import course.work.pastebin.crud.repositories.PasteRepository;
import course.work.pastebin.entities.AccessType;
import course.work.pastebin.entities.Paste;
import course.work.pastebin.entities.Role;
import course.work.pastebin.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasteService {
    @Autowired
    private PasteRepository pasteRepository;
    private static final String PASTES_CACHE = "pastes";

    @Transactional
    @CacheEvict(value = {"pastes", "userPastes"}, allEntries = true)
    public Paste createPaste(String content, User user, String title, AccessType accessType) {
        long start = System.nanoTime();

        String slug = UUID.randomUUID().toString();
        Date expirationDate = new Date(System.currentTimeMillis() + 3600 * 1000);

        Paste paste = Paste.builder()
                .slug(slug)
                .title(title != null && !title.isEmpty() ? title : "Паста")
                .content(content)
                .expirationDate(expirationDate)
                .accessType(accessType)
                .user(user)
                .build();

        Paste saved = pasteRepository.save(paste);

        long durationNs = System.nanoTime() - start;
        long durationMs = durationNs / 1_000_000;

        saved.setCreateTimeMs(durationMs);
        return saved;

    }

    @Transactional(readOnly = true)
    @Cacheable(value = PASTES_CACHE, key = "#slug")
    public Optional<Paste> getPasteBySlug(String slug) {
        return pasteRepository.findBySlug(slug)
                .filter(paste -> !paste.isExpired());
    }

    @Transactional(readOnly = true)
    public Optional<Paste> getPasteForUser(String slug, User currentUser) {
        return pasteRepository.findBySlug(slug)
                .filter(paste -> !paste.isExpired())
                .filter(paste -> hasAccess(paste, currentUser));
    }

    private boolean hasAccess(Paste paste, User currentUser) {
        return paste.getAccessType() == AccessType.PUBLIC
                || (currentUser != null && (currentUser.equals(paste.getUser())
                || currentUser.getRole() == Role.ADMIN));
    }

    @Transactional
    @CacheEvict(value = "userPastes", key = "#currentUser.id")
    public void deletePaste(String slug, User currentUser) {
        Paste paste = pasteRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Паста не найдена"));

        if (!hasDeletePermission(paste, currentUser)) {
            throw new AccessDeniedException("У вас нет прав на удаление этой пасты");
        }

        pasteRepository.delete(paste);
    }

    private boolean hasDeletePermission(Paste paste, User currentUser) {
        return currentUser.getRole() == Role.ADMIN ||
                paste.getUser().equals(currentUser);
    }

    @Cacheable(value = "userPastes", key = "#user.id")
    public List<Paste> getActivePastesByUser(User user) {
        Date now = new Date();
        return pasteRepository.findByUserAndExpirationDateAfterOrderByCreationDateDesc(user, now);
    }

}
