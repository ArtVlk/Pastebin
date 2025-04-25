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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasteController {
    @Autowired
    private PasteService pasteService;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/createPaste")
    public String createPaste(
            @RequestParam("pasteText") String text,
            @RequestParam(value = "pasteFile", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            Authentication authentication,
            RedirectAttributes redirectAttrs
    ) {
        try {
            System.out.println("Получено название: '" + title + "'");
            String username = authentication.getName();
            System.out.println(username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            String content = text;
            if (file != null && !file.isEmpty()) {
                content = new String(file.getBytes());
            }

            Paste paste = pasteService.createPaste(content, user, title);

            String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/paste/view/")
                    .path(paste.getSlug())
                    .toUriString();

            System.out.println(fullUrl);

            redirectAttrs.addFlashAttribute("pasteLink", fullUrl);
            redirectAttrs.addFlashAttribute("successMessage", "Паста успешно создана!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/";
    }



    @GetMapping("/paste/{slug}")
    public ResponseEntity<Resource> downloadPaste(@PathVariable String slug) {
        return pasteService.getPasteBySlug(slug)
                .map(paste -> {
                    Resource resource = new ByteArrayResource(paste.getContent().getBytes());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=paste.txt")
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paste/view/{slug}")
    public String viewPaste(@PathVariable String slug, Model model) {
        return pasteService.getPasteBySlug(slug)
                .map(paste -> {
                    model.addAttribute("paste", paste);
                    return "viewPaste";
                })
                .orElse("redirect:/error");
    }
}
