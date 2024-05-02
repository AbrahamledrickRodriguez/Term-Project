/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.services.PostService;
import uga.menik.cs4370.services.UserService;

/**
 * This controller handles the home page and some of it's sub URLs.
 */
@Controller
@RequestMapping
public class HomeController {
    
        private final PostService postService;
    private final UserService userService;

    @Autowired
    public HomeController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * This is the specific function that handles the root URL itself.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     */
    @GetMapping("/")
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("home_page");

        if (!userService.isAuthenticated()) {
            mv.setViewName("redirect:/login");
            return mv;
        }

        String loggedInUserId = userService.getLoggedInUser().getUserId();
        List<Post> posts = postService.getPostsFromFollowedUsers(loggedInUserId);
        mv.addObject("posts", posts);

        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

        if (error != null && !error.isEmpty()) {
            mv.addObject("errorMessage", error);
        }

        return mv;
    }
@PostMapping("/createpost")
public String createMealPlan(@RequestParam(name = "mealPlanName") String mealPlanName, 
                             @RequestParam(name = "diningHallId") int diningHallId,
                             RedirectAttributes redirectAttributes) {
    try {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = userService.getLoggedInUser().getUserId();  // Assuming getUserId returns a String
        postService.createMealPlan(mealPlanName, userId, diningHallId);  // Correct usage of the service instance
        return "redirect:/";
    } catch (SQLException e) {
        e.printStackTrace();
        String errorMessage = URLEncoder.encode("Failed to create the meal plan. Please try again.", StandardCharsets.UTF_8);
        redirectAttributes.addAttribute("error", errorMessage);
        return "redirect:/";
    }
}
}
