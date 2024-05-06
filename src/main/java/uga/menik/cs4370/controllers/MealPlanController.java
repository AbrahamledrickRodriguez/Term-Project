/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import javax.sql.DataSource;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.services.PostService;


/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/createmealplan")
public class MealPlanController {

    private final UserService userService;
    private final DataSource dataSource;
    private final PostService postService;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public MealPlanController(PostService postService, UserService userService,DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
        this.postService = postService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("mp_page");
        if (!userService.isAuthenticated()) {
            mv.setViewName("redirect:/login");
            return mv;
        }
        mv.addObject("errorMessage", error);

        return mv;
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @PostMapping
    public String createMealPlan(@RequestParam(name = "mealPlanName") String mealPlanName, 
                             @RequestParam(name = "diningHallName") String diningHallName,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!userService.isAuthenticated()) {
                return "redirect:/login";
            }
            int userId = userService.getLoggedInUser().getUserId();  // Assuming getUserId returns a String
            postService.createMealPlan(mealPlanName, userId, diningHallName);  // Correct usage of the service instance
            return "redirect:/";
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMessage = URLEncoder.encode("Failed to create the meal plan. Please try again.", StandardCharsets.UTF_8);
            redirectAttributes.addAttribute("error", errorMessage);
            return "redirect:/";
        }
    }
    
}
