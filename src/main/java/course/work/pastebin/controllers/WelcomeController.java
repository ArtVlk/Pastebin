package course.work.pastebin.controllers;

import course.work.pastebin.crud.repositories.UserRepository;
import course.work.pastebin.entities.Paste;
import course.work.pastebin.entities.User;
import course.work.pastebin.services.PasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WelcomeController {

    private final PasteService pasteService;
    private final UserRepository userRepository;

    @Autowired
    public WelcomeController(PasteService pasteService, UserRepository userRepository) {
        this.pasteService = pasteService;
        this.userRepository = userRepository;
    }

    @GetMapping("/welcome")
    public String welcomePage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            List<Paste> activePastes = pasteService.getActivePastesByUser(user);
            model.addAttribute("pastes", activePastes);
        }
        return "welcomePage";
    }
}
