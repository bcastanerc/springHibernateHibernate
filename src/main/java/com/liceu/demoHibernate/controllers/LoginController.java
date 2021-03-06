package com.liceu.demoHibernate.controllers;

import com.liceu.demoHibernate.entities.User;
import com.liceu.demoHibernate.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    LoginService loginService;

    @Autowired
    UserService userService;

    @Autowired
    RegisterService registerService;

    @GetMapping("/login")
    public String getLogin(@RequestParam Optional<Boolean> error, Model model){
        if ((error.isPresent())) model.addAttribute("error", true);
        return "/login";
    }

    @PostMapping("/login")
    public String postLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model, @RequestParam String _csrftoken) throws NoSuchAlgorithmException {
        User logedUSer = userService.findUserByEmailEquals(email);
        String encryptedPassword = userService.encryptPassword(password);
        if (logedUSer != null && !logedUSer.getLoggedByOauth()){
            if (logedUSer.getPassword().equals(encryptedPassword)){
                session.setAttribute("user_email", logedUSer.getEmail());
                return "redirect:/createNotes";
            }
        }
        model.addAttribute("error", true);
        model.addAttribute("csrfToken", _csrftoken);
        return "/login";
    }

    @GetMapping("/logingoogle")
    public String app() throws Exception  {
        URL url = loginService.getGoogleRedirectURL();
        return "redirect:" + url;
    }

    @GetMapping("/auth/oauth2callback/")
    public String oauthCallback(@RequestParam String code, HttpSession session) throws Exception {
        String accessToken = loginService.getAccessToken(code);
        Map<String,String> userDetails = loginService.getUserDetails(accessToken);
        String picture = userDetails.get("picture");
        String email = userDetails.get("email");
        String[] cuts = email.split("@");
        String username = cuts[0];
        if(userService.findUserByEmailEquals(email) == null) registerService.register(username,email,null);

        if (userService.findUserByEmailEquals(email).getLoggedByOauth()){
            session.setAttribute("picture",picture);
            session.setAttribute("user_email",email);
            return "redirect:/userNotes";
        }
        return "redirect:/login?error=true";
    }



}
