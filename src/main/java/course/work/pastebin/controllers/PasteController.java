package course.work.pastebin.controllers;

import course.work.pastebin.crud.repositories.UserRepository;
import course.work.pastebin.entities.AccessType;
import course.work.pastebin.entities.Paste;
import course.work.pastebin.entities.User;
import course.work.pastebin.services.PasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
            @RequestParam("accessType") AccessType accessType,
            Authentication authentication,
            RedirectAttributes redirectAttrs
    ) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            String content = text;
            if (file != null && !file.isEmpty()) {
                content = new String(file.getBytes(),  StandardCharsets.UTF_8);
            }

            Paste paste = pasteService.createPaste(content, user, title, accessType);


            String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/paste/view/")
                    .path(paste.getSlug())
                    .toUriString();

            System.out.println(fullUrl);

            //redirectAttrs.addFlashAttribute("createTimeMs", paste.getCreateTimeMs());
            return "redirect:/paste/view/" + paste.getSlug();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/paste/view/{slug}")
    public String viewPaste(@PathVariable String slug,
                            Authentication authentication,
                            Model model) {
        User currentUser = getCurrentUser(authentication);
        Optional<Paste> pasteOpt = pasteService.getPasteForUser(slug, currentUser);

        if (pasteOpt.isPresent()) {
            Paste paste = pasteOpt.get();
            model.addAttribute("paste", paste);

            String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/paste/view/")
                    .path(paste.getSlug())
                    .toUriString();
            model.addAttribute("pasteUrl", fullUrl);

            return "viewPaste";
        } else {
            return "redirect:/access-denied";
        }
    }

    private User getCurrentUser(Authentication authentication) {
        return authentication != null
                ? userRepository.findByUsername(authentication.getName()).orElse(null)
                : null;
    }

    @GetMapping("/paste/{slug}")
    public ResponseEntity<Resource> downloadPaste(@PathVariable String slug,
                                                  Authentication authentication) {
        User currentUser = authentication != null
                ? userRepository.findByUsername(authentication.getName()).orElse(null)
                : null;

        return pasteService.getPasteForUser(slug, currentUser)
                .map(paste -> {
                    String fileName = generateFileName(paste.getTitle(), paste.getSlug());
                    Resource resource = new ByteArrayResource(paste.getContent().getBytes());

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(resource);
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    private String generateFileName(String title, String slug) {
        String baseName = (title != null && !title.isEmpty())
                ? title
                : "paste_" + slug;

        String normalized = baseName
                .replaceAll("[^\\p{L}\\p{Nd}\\s-]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();

        String fileName = URLEncoder.encode(normalized + ".txt", StandardCharsets.UTF_8)
                .replace("+", "%20");

        return fileName.startsWith(".")
                ? "paste.txt"
                : fileName;
    }

    @PostMapping("/paste/{slug}/delete")
    public String deletePaste(@PathVariable String slug,
                              Authentication authentication,
                              RedirectAttributes redirectAttrs) {
        User currentUser = getCurrentUser(authentication);

        try {
            pasteService.deletePaste(slug, currentUser);
            redirectAttrs.addFlashAttribute("successMessage", "Паста успешно удалена");
        } catch (AccessDeniedException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Ошибка при удалении пасты");
        }

        return "redirect:/welcome";
    }
}
