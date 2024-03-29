package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * This controller will deal with all but security (login & register)
 */
@Controller
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    /*
     * after login has been validated, it will come here
     */
    @RequestMapping("/")
    public String homepg(Model model) {
        model.addAttribute("allmsg", messageRepository.findAll());

        User crntuser = userService.getUser();
        if (crntuser != null)
            model.addAttribute("loginuser", crntuser.getUsername());
        else
            model.addAttribute("loginuser", "none");
        return "index";

    }

    @RequestMapping("/addmsg")
    public String addMsg(Model model){
        Message newmsg = new Message();

        model.addAttribute("newmsg", newmsg);
        return "addmsg";
    }

    @RequestMapping("/processmsg")
    public String processMsg(@Valid @ModelAttribute("newmsg") Message newmsg,
                             BindingResult result, Model model){
        if (result.hasErrors())
            return "addmsg";
        else {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            String todaysdate = dateFormat.format(date);
            newmsg.setPostedDate(todaysdate);

            User currentUser = userService.getUser();
            newmsg.setPostedBy(currentUser.getUsername());
            newmsg.setUsers(Arrays.asList(currentUser));

            messageRepository.save(newmsg);
            return "redirect:/";
        }
    }


    @RequestMapping("/detailmsg/{id}")
    public String showMsg(@PathVariable("id") long id, Model model){
        model.addAttribute("onemsg", messageRepository.findById(id).get());

        for (User user : messageRepository.findById(id).get().getUsers()) {
            model.addAttribute("user", user);
            model.addAttribute("allmsg", user.getMessages());
            break;
        }

        return "detailmsg";
    }

    @RequestMapping("/updatemsg/{id}")
    public String updateMsg(@PathVariable("id") long id, Model model){
        model.addAttribute("onemsg", messageRepository.findById(id).get());
        return "updatemsg";
    }

//    @RequestMapping("/processupdatemsg")
    @PostMapping("/updateprocess")
    public String processUpdateMsg(@Valid @ModelAttribute("onemsg") Message onemsg,
                             BindingResult result, Model model){
        if (result.hasErrors())
            return "updatemsg";
        else {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            String todaysdate = dateFormat.format(date);
            onemsg.setPostedDate(todaysdate);

            User currentUser = userService.getUser();
            onemsg.setPostedBy(currentUser.getUsername());
            onemsg.setUsers(Arrays.asList(currentUser));

            messageRepository.save(onemsg);
            return "redirect:/";
        }
    }

    @RequestMapping("/deletemsg/{id}")
    public String deleteMsg(@PathVariable("id") long id, Model model) {
        messageRepository.deleteById(id);
        return "redirect:/";
    }


    @RequestMapping("/admin")
    public String admin(Model model) {

        //pass currently logged-in user information to index.html
        User crntuser = userService.getUser();
        if (crntuser != null)
            model.addAttribute("crntuser", crntuser);

        return "admin";
    }

    @RequestMapping("/secure")
    public String secure(Principal principal, Model model) {
        String username=principal.getName();
        model.addAttribute("crntuser", userRepository.findByUsername(username));
        return "secure";
    }

//    @RequestMapping("/index")
//    public String index() {
//        return "index";
//    }
}
