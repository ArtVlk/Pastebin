package course.work.pastebin.controllers;

import course.work.pastebin.entities.User;
import course.work.pastebin.services.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RegistrationController {
    @Autowired
    UserDetailServiceImpl userDetailServiceImpl;

    @GetMapping("/registration")
    public String registration() {return "registration";}

    @PostMapping("/registration")
    public String addUser(User user, Model model) {
        if (userDetailServiceImpl.addUser(user)) {
            return "redirect:/login";
        } else {
            model.addAttribute("message", "User already exists");
            return "registration";
        }
    }

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (authentication != null) {
            return "redirect:/welcome";
        }
        return "login";
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "welcomePage";
    }
}
