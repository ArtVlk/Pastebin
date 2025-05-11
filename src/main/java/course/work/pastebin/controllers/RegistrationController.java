package course.work.pastebin.controllers;

import course.work.pastebin.entities.Role;
import course.work.pastebin.entities.User;
import course.work.pastebin.services.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {
    private final UserDetailServiceImpl userDetailServiceImpl;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationController(
            UserDetailServiceImpl userDetailServiceImpl,
            PasswordEncoder passwordEncoder
    ) {
        this.userDetailServiceImpl = userDetailServiceImpl;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registration")
    public String registration() {return "registration";}

    @PostMapping("/registration")
    public String addUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String role,
            Model model
    ) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());

            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .role(userRole)
                    .build();

            if (userDetailServiceImpl.addUser(user)) {
                return "redirect:/login";
            }
            model.addAttribute("message", "Пользователь уже существует");
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", "Некорректная роль! Используйте ADMIN или USER");
        }
        return "registration";
    }

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (authentication != null) {
            return "redirect:/welcome";
        }
        return "login";
    }

    @GetMapping("/register-success")
    public String welcome(){
        return "welcomePage";
    }
}
