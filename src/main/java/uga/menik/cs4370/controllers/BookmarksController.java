/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.models.User;

/**
 * Handles /bookmarks and its sub URLs.
 * No other URLs at this point.
 * 
 * Learn more about @Controller here: 
 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {
@Autowired
private UserService userService;

@Autowired
private DataSource dataSource;
    /**
     * /bookmarks URL itself is handled by this.
     */
    @GetMapping
public ModelAndView webpage() {
    ModelAndView mv = new ModelAndView("posts_page");
    int userId = userService.getLoggedInUser().getUserId();
    List<Post> posts = getBookmarkedPosts(userId);

    mv.addObject("posts", posts);
    if (posts.isEmpty()) {
        mv.addObject("isNoContent", true);
    } else {
        mv.addObject("isNoContent", false);
    }

    return mv;
}


    private List<Post> getBookmarkedPosts(int userId) {
        List<Post> bookmarkedPosts = new ArrayList<>();
        final String sql = 
            "SELECT p.postId, p.postText, p.postDate, " +
            "u.userId, u.firstName, u.lastName, " +
            "(SELECT COUNT(*) FROM heart WHERE postId = p.postId AND userId = ?) AS isHearted, " +
            "(SELECT COUNT(*) FROM comment WHERE postId = p.postId) AS commentsCount, " +
            "1 AS isBookmarked " + 
            "FROM post p " +
            "JOIN user u ON p.userId = u.userId " +
            "JOIN bookmark b ON p.postId = b.postId AND b.userId = ? " +
            "ORDER BY p.postDate DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(rs.getInt("userId"), rs.getString("firstName"), rs.getString("lastName"));
                    Timestamp postDate = rs.getTimestamp("postDate");
                    String formattedDate = dateFormat.format(postDate);
                    Post post = new Post(
                        rs.getString("postId"), 
                        rs.getString("postText"), 
                        formattedDate, 
                        user, 
                        rs.getBoolean("isHearted") ? 1 : 0, 
                        rs.getInt("commentsCount"), 
                        rs.getBoolean("isHearted"), 
                        true 
                    );
                    bookmarkedPosts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookmarkedPosts;
    }
    
}
