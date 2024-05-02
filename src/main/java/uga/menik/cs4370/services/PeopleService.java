/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.FollowableUser;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {
    private final DataSource dataSource;

    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FollowableUser> getFollowableUsers(String currentUserId) {
        List<FollowableUser> followableUsers = new ArrayList<>();
        final String sql = 
            "SELECT u.userId, u.firstName, u.lastName, " +
            "(SELECT COUNT(*) FROM follow WHERE followerUserId = ? AND followeeUserId = u.userId) > 0 AS isFollowed, " +
            "GREATEST(MAX(p.postDate), MAX(c.commentDate)) AS lastActiveDate " +
            "FROM user u " +
            "LEFT JOIN post p ON u.userId = p.userId " +
            "LEFT JOIN comment c ON u.userId = c.userId " +
            "WHERE u.userId != ? " +
            "GROUP BY u.userId, u.firstName, u.lastName";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUserId);
            pstmt.setString(2, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm");
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    boolean isFollowed = rs.getBoolean("isFollowed");
                    Timestamp lastActiveTimestamp = rs.getTimestamp("lastActiveDate");
                    String lastActiveDate = lastActiveTimestamp != null ? dateFormat.format(lastActiveTimestamp) : "Never Active";

                    FollowableUser user = new FollowableUser(userId, firstName, lastName, isFollowed, lastActiveDate);
                    followableUsers.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return followableUsers;
    }
}

