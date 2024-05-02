/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.sql.Connection;
import javax.sql.DataSource;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;


/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final DataSource dataSource;

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public ProfileController(UserService userService,DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public String profileOfLoggedInUser() {
        System.out.println("User is attempting to view the profile of the logged-in user.");
        String loggedInUserId = userService.getLoggedInUser().getUserId();
        return "redirect:/profile/" + loggedInUserId;
    }

    /**
     * This function handles /profile/{userId} URL.
     * This serves the webpage that shows posts of a speific user given by userId.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * how path variables work.
     */
   @GetMapping("/{userId}")
    public ModelAndView profileOfSpecificUser(@PathVariable("userId") String userId,
                                          @RequestParam(name = "loggedInUserId", required = false) String loggedInUserId) {
    ModelAndView mv = new ModelAndView("profile_page");

    List<Post> posts = new ArrayList<>();
    final String sql = 
        "SELECT p.postId, p.postText, p.postDate, u.userId, u.firstName, u.lastName, " +
        "(SELECT COUNT(*) FROM heart WHERE postId = p.postId) AS heartsCount, " +
        "(SELECT COUNT(*) FROM comment WHERE postId = p.postId) AS commentsCount, " + 
        "EXISTS(SELECT 1 FROM heart WHERE postId = p.postId AND userId = ?) AS isLiked, " +
        "EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) AS isBookmarked " +
        "FROM post p JOIN user u ON p.userId = u.userId " +
        "WHERE u.userId = ? " +
        "ORDER BY p.postDate DESC";

    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, loggedInUserId);
        pstmt.setString(2, loggedInUserId);
        pstmt.setString(3, userId);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                    rs.getString("userId"), 
                    rs.getString("firstName"), 
                    rs.getString("lastName")
                );
                Post post = new Post(
                    rs.getString("postId"), 
                    rs.getString("postText"), 
                    rs.getTimestamp("postDate").toString(), 
                    user, 
                    rs.getInt("heartsCount"), 
                    rs.getInt("commentsCount"),
                    rs.getBoolean("isLiked"), 
                    rs.getBoolean("isBookmarked")
                );
                posts.add(post);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        mv.addObject("errorMessage", "Error fetching user posts.");
    }

    return mv;
}
    
}
