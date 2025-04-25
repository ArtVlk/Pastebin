package course.work.pastebin.services;

import course.work.pastebin.crud.repositories.PasteRepository;
import course.work.pastebin.entities.AccessType;
import course.work.pastebin.entities.Paste;
import course.work.pastebin.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasteService {
    @Autowired
    private PasteRepository pasteRepository;

    @Transactional
    public Paste createPaste(String content, User user, String title) {
        String slug = UUID.randomUUID().toString();
        Date expirationDate = new Date(System.currentTimeMillis() + 3600 * 1000);

        String finalTitle = (title == null || title.trim().isEmpty())
                ? "Паста"
                : title.trim();

        Paste paste = Paste.builder()
                .slug(slug)
                .title(title != null && !title.isEmpty() ? title : "Паста")
                .content(content)
                .expirationDate(expirationDate)
                .accessType(AccessType.PUBLIC)
                .user(user)
                .build();

        return pasteRepository.save(paste);
    }
    @Transactional(readOnly = true)
    public Optional<Paste> getPasteBySlug(String slug) {
        return pasteRepository.findBySlug(slug)
                .filter(paste -> !paste.isExpired());
    }

}
