/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.services.PostService;
import uga.menik.cs4370.services.UserService;

/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {

@Autowired
private PostService postService;

@Autowired
private UserService userService;

@Autowired
private DataSource dataSource;
    /**
     * This function handles the /post/{postId} URL.
     * This handlers serves the web page for a specific post.
     * Note there is a path variable {postId}.
     * An example URL handled by this function looks like below:
     * http://localhost:8081/post/1
     * The above URL assigns 1 to postId.
     * 
     * See notes from HomeController.java regardig error URL parameter.
     */
@GetMapping("/{postId}")
public ModelAndView webpage(@PathVariable("postId") String postId,
                            @RequestParam(name = "error", required = false) String error,
                            RedirectAttributes redirectAttributes) {
    ModelAndView mv = new ModelAndView("posts_page");

    try {
        String userId = userService.getLoggedInUser().getUserId();

        ExpandedPost posts = postService.getExpandedPostWithComments(postId, userId);

        if (posts == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "The requested post does not exist.");
            return new ModelAndView("redirect:/errorPage"); 
        }

        mv.addObject("posts", posts);
    } catch (Exception e) {
        e.printStackTrace();
        String message = URLEncoder.encode("An error occurred while fetching the post. Please try again.", StandardCharsets.UTF_8);
        mv.addObject("errorMessage", message);
    }

    if (error != null && !error.isEmpty()) {
        mv.addObject("errorMessage", error);
    }

    return mv;
}

    /**
     * Handles comments added on posts.
     * See comments on webpage function to see how path variables work here.
     * This function handles form posts.
     * See comments in HomeController.java regarding form submissions.
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
                              @RequestParam("comment") String commentText,
                              RedirectAttributes redirectAttributes) {
        System.out.println("User is attempting to add a comment: postId=" + postId + ", comment=" + commentText);
    
        String userId = userService.getLoggedInUser().getUserId();
    
        boolean commentAdded = postService.addComment(postId, userId, commentText);
        if (commentAdded) {
            return "redirect:/post/" + postId;
        } else {
            String message = URLEncoder.encode("Failed to add comment. Please try again.", StandardCharsets.UTF_8);
            redirectAttributes.addFlashAttribute("error", message);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }
    /**
     * Handles likes added on posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions and how path variables work.
     */
    
     @GetMapping("/{postId}/heart/{isAdd}")
     public String addOrRemoveHeart(@PathVariable("postId") String postId, @PathVariable("isAdd") Boolean isAdd) {
         String loggedInUserId = userService.getLoggedInUser().getUserId();
     
         if (Boolean.TRUE.equals(isAdd)) {
             postService.addHeart(postId, loggedInUserId);
         } else {
             postService.removeHeart(postId, loggedInUserId);
         }
         return "redirect:/";
     }

    /**
     * Handles bookmarking posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions.
     */
    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId, @PathVariable("isAdd") Boolean isAdd) {
        String userId = userService.getLoggedInUser().getUserId(); 
        String sql;
    
        if (isAdd) {
            sql = "INSERT INTO bookmark (postId, userId) VALUES (?, ?) ON DUPLICATE KEY UPDATE postId=postId";
        } else {
            sql = "DELETE FROM bookmark WHERE postId = ? AND userId = ?";
        }
    
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.setString(2, userId);
    
            pstmt.executeUpdate();
            return "redirect:/bookmarks";
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.", StandardCharsets.UTF_8);
            return "redirect:/bookmarks?error=" + message;
        }
    }


}
