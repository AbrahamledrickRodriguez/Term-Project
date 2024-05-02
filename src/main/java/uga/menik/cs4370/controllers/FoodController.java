/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uga.menik.cs4370.models.Food;
import uga.menik.cs4370.services.FoodService;
import uga.menik.cs4370.services.UserService;

import java.sql.Connection;

/**
 * Handles /people URL and its sub URL paths.
 */
@Controller
@RequestMapping("/food")
public class FoodController {
    /**
     * Serves the /food web page.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     */

    private final FoodService foodService;
    private final UserService userService;
    private final DataSource dataSource;

    @Autowired
    public FoodController(FoodService foodService, UserService userService, DataSource dataSource) {
        this.foodService = foodService;
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("people_page");
    
        if (!userService.isAuthenticated()) {
            mv.setViewName("redirect:/login");
            return mv;
        }

        List<Food> foods = foodService.getFoodList();
        mv.addObject("food", foods);
    
        if (foods.isEmpty()) {
            mv.addObject("isNoContent", true);
        }
    
        if (error != null && !error.isEmpty()) {
            mv.addObject("errorMessage", error);
        }
    
        return mv;
    }
}
