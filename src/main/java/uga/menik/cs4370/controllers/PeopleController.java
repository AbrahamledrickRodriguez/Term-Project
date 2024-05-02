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

import uga.menik.cs4370.models.FollowableUser;
import uga.menik.cs4370.services.PeopleService;
import uga.menik.cs4370.services.UserService;

import java.sql.Connection;

/**
 * Handles /people URL and its sub URL paths.
 */
@Controller
@RequestMapping("/people")
public class PeopleController {
    /**
     * Serves the /people web page.
     * 
     * Note that this accepts a URL parameter called error.
     * The value to this parameter can be shown to the user as an error message.
     * See notes in HashtagSearchController.java regarding URL parameters.
     */

    private final PeopleService peopleService;
    private final UserService userService;
    private final DataSource dataSource;

    @Autowired
    public PeopleController(PeopleService peopleService, UserService userService, DataSource dataSource) {
        this.peopleService = peopleService;
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
    
        String loggedInUserId = userService.getLoggedInUser().getUserId();
        List<FollowableUser> followableUsers = peopleService.getFollowableUsers(loggedInUserId);
        mv.addObject("users", followableUsers);
    
        if (followableUsers.isEmpty()) {
            mv.addObject("isNoContent", true);
        }
    
        if (error != null && !error.isEmpty()) {
            mv.addObject("errorMessage", error);
        }
    
        return mv;
    }
   @GetMapping("{userId}/follow/{isFollow}")
    public String followUnfollowUser(@PathVariable("userId") String followeeUserId,
                                     @PathVariable("isFollow") Boolean isFollow,
                                     RedirectAttributes redirectAttributes) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }

        String loggedInUserId = userService.getLoggedInUser().getUserId();
        String sql = isFollow ? "INSERT INTO follow (followerUserId, followeeUserId) VALUES (?, ?)"
                              : "DELETE FROM follow WHERE followerUserId = ? AND followeeUserId = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loggedInUserId);
            pstmt.setString(2, followeeUserId);

            pstmt.executeUpdate();
            return "redirect:/people";
        } catch (Exception e) {
            String message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
                                               StandardCharsets.UTF_8);
            redirectAttributes.addAttribute("error", message);
            return "redirect:/people";
        }
    }

}
