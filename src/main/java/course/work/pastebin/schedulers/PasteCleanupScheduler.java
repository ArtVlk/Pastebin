package course.work.pastebin.schedulers;

import course.work.pastebin.crud.repositories.PasteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Component
public class PasteCleanupScheduler {

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteCleanupScheduler(PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    // Удаляет пасты каждую минуту (60,000 мс)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteExpiredPastes() {
        pasteRepository.deleteExpiredPastes(new Date());
    }
}