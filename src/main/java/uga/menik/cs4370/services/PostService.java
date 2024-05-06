package uga.menik.cs4370.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.ResultSet;

import uga.menik.cs4370.models.User;
import uga.menik.cs4370.models.Users;
import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.DiningHall;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.MealPlan;
import uga.menik.cs4370.models.Post;

@Service
public class PostService {

    private final DataSource dataSource;
    private final UserService userService;

    @Autowired
    public PostService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

/**
 * Creates a new meal plan.
 *
 * @param mealPlanName Name of the meal plan.
 * @param userId Identifier of the user associated with the meal plan.
 * @param diningHallId Identifier of the dining hall associated with the meal plan.
 * @return MealPlan The newly created meal plan object.
 * @throws SQLException If any SQL operations fail.
 */
public MealPlan createMealPlan(String mealPlanName, int userId, String diningHallName) throws SQLException {
    final String insertMealPlanSql = "INSERT INTO MealPlan (m_name, u_id, dh_id) VALUES (?, ?, ?)";
    final String userQuerySql = "SELECT * FROM user WHERE u_id = ?";
    final String diningHallQuerySql = "SELECT * FROM dininghall WHERE dh_name = ?";

    MealPlan newMealPlan = null;

    try (Connection conn = dataSource.getConnection()) {
        try (PreparedStatement insertMealPlanStmt = conn.prepareStatement(insertMealPlanSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertMealPlanStmt.setString(1, mealPlanName);
            insertMealPlanStmt.setInt(2, userId); // Changed to setString
            final String getId = "SELECT dh_id FROM dininghall WHERE dh_name = ?";
            int diningHallId;
            try(PreparedStatement getIdStmt = conn.prepareStatement(getId)) {
                getIdStmt.setString(1, diningHallName);
                try(ResultSet rs = getIdStmt.executeQuery()) {
                    rs.next();
                    diningHallId = rs.getInt("dh_id");
                }
            }
            insertMealPlanStmt.setInt(3, diningHallId);
            int affectedRows = insertMealPlanStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating meal plan failed, no rows affected.");
            }

            try (ResultSet generatedKeys = insertMealPlanStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int mealPlanId = generatedKeys.getInt(1);

                    // Fetch user and dining hall information to return a complete MealPlan object
                    User user = null;
                    DiningHall diningHall = null;
                    try (PreparedStatement userQueryStmt = conn.prepareStatement(userQuerySql)) {
                        userQueryStmt.setInt(1, userId); // Changed to setString
                        ResultSet userRs = userQueryStmt.executeQuery();
                        if (userRs.next()) {
                            user = new User(userId, userRs.getString("username"), userRs.getString("password"));
                        }
                    }
                    try (PreparedStatement diningHallQueryStmt = conn.prepareStatement(diningHallQuerySql)) {
                        diningHallQueryStmt.setString(1, diningHallName);
                        ResultSet dhRs = diningHallQueryStmt.executeQuery();
                        if (dhRs.next()) {
                            diningHall = new DiningHall(diningHallId, diningHallName);
                        }
                    }
                    if (user != null && diningHall != null) {
                        newMealPlan = new MealPlan(mealPlanId, mealPlanName, userId, diningHallId);
                    } else {
                        throw new SQLException("Creating meal plan failed, no ID obtained.");
                    }
                }
            }
        }
    }
    return newMealPlan;
}

    public List<MealPlan> getMyMealPlans(int loggedInUserId) {
        List<MealPlan> mealplans = new ArrayList<>();
        final String sql = 
            "SELECT m_id, m_name, dh_id FROM MealPlan where u_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedInUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MealPlan mealplan = mapRowToMealPlan(rs);
                    mealplans.add(mealplan);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(mealplans.isEmpty());
        return mealplans;
    }

    private MealPlan mapRowToMealPlan(ResultSet rs) throws SQLException {
        int mealPlanId = rs.getInt("m_id");
        String mealPlanName = rs.getString("m_name");
        int diningHallId = rs.getInt("dh_id");
        int userId = userService.getLoggedInUser().getUserId();
        return new MealPlan(mealPlanId, mealPlanName, userId, diningHallId);
    }
    public boolean addComment(String postId, int userId, String commentText) {
        final String sql = "INSERT INTO comment (postId, userId, commentText, commentDate) VALUES (?, ?, ?, NOW())";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, commentText);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public ExpandedPost getExpandedPostWithComments(String postId, int userId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        ExpandedPost expandedPost = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm");

        final String sql =
            "SELECT p.*, u.userId AS postUserId, u.firstName AS postFirstName, u.lastName AS postLastName, " +
            "COALESCE(bm.isBookmarked, 0) AS isBookmarked, COALESCE(lk.isLiked, 0) AS isLiked, " +
            "(SELECT COUNT(*) FROM heart WHERE postId = p.postId) AS heartsCount, " +
            "(SELECT COUNT(*) FROM comment WHERE postId = p.postId) AS commentsCount, " +
            "c.commentId, c.commentText, c.commentDate, " +
            "cu.userId AS commentUserId, cu.firstName AS commentFirstName, cu.lastName AS commentLastName " +
            "FROM post p " +
            "JOIN user u ON p.userId = u.userId " +
            "LEFT JOIN (SELECT postId, 1 AS isBookmarked FROM bookmark WHERE userId = ?) bm ON p.postId = bm.postId " +
            "LEFT JOIN (SELECT postId, 1 AS isLiked FROM heart WHERE userId = ?) lk ON p.postId = lk.postId " +
            "LEFT JOIN comment c ON p.postId = c.postId " +
            "LEFT JOIN user cu ON c.userId = cu.userId " +
            "WHERE p.postId = ? " +
            "ORDER BY c.commentDate DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (expandedPost == null) {
                        User postUser = new User(rs.getInt("postUserId"), rs.getString("postFirstName"), rs.getString("postLastName"));
                        Timestamp postDateTimestamp = rs.getTimestamp("postDate");
                        String formattedPostDate = dateFormat.format(postDateTimestamp);

                        expandedPost = new ExpandedPost(
                            rs.getString("postId"), rs.getString("postText"),
                            formattedPostDate,
                            postUser, rs.getInt("heartsCount"), rs.getInt("commentsCount"),
                            rs.getBoolean("isLiked"), rs.getBoolean("isBookmarked"),
                            comments
                        );
                    }

                    String commentId = rs.getString("commentId");
                    if (commentId != null) {
                        User commentUser = new User(rs.getInt("commentUserId"), rs.getString("commentFirstName"), rs.getString("commentLastName"));
                        Timestamp commentDateTimestamp = rs.getTimestamp("commentDate");
                        String formattedCommentDate = dateFormat.format(commentDateTimestamp);

                        Comment comment = new Comment(
                            commentId, rs.getString("commentText"), formattedCommentDate, commentUser
                        );
                        comments.add(comment);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expandedPost;
    }

    public List<Post> searchPostsByHashtags(List<String> hashtags, int userId) {
        List<Post> posts = new ArrayList<>();
        if (hashtags.isEmpty()) {
            return posts;
        }
    
        String sql = constructHashtagSearchSql(hashtags.size(), userId);
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            for (int i = 0; i < hashtags.size(); i++) {
                pstmt.setString(3 + i, hashtags.get(i));
            }
    
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(rs.getInt("userId"), rs.getString("firstName"), rs.getString("lastName"));
                    Post post = new Post(rs.getString("postId"), rs.getString("postText"), rs.getTimestamp("postDate").toString(),
                                         user, rs.getInt("heartsCount"), rs.getInt("commentsCount"), 
                                         rs.getBoolean("isLiked"), rs.getBoolean("isBookmarked"));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return posts;
    }
    private String constructHashtagSearchSql(int hashtagsCount, int userId) {
        String sql = "SELECT p.postId, p.postText, p.postDate, " +
                     "u.userId, u.firstName, u.lastName, " +
                     "(SELECT COUNT(*) FROM heart WHERE postId = p.postId) AS heartsCount, " +
                     "(SELECT COUNT(*) FROM comment WHERE postId = p.postId) AS commentsCount, " +
                     "EXISTS (SELECT 1 FROM heart WHERE postId = p.postId AND userId = ?) AS isLiked, " +
                     "EXISTS (SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) AS isBookmarked " +
                     "FROM post p JOIN user u ON p.userId = u.userId " +
                     "JOIN hashtag h ON p.postId = h.postId ";
        sql += "WHERE h.hashTag IN (";
        for (int i = 0; i < hashtagsCount; i++) {
            sql += "?";
            if (i < hashtagsCount - 1) {
                sql += ", ";
            }
        }
        sql += ") ";
        sql += "GROUP BY p.postId, u.userId, u.firstName, u.lastName, p.postText, p.postDate ";
        sql += "HAVING COUNT(DISTINCT h.hashTag) = " + hashtagsCount + " ";
        sql += "ORDER BY p.postDate DESC";
    
        return sql;
    }
    public boolean removeHeart(String postId, int userId) {
        final String deleteHeartSql = "DELETE FROM heart WHERE postId = ? AND userId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement deleteHeartStmt = conn.prepareStatement(deleteHeartSql)) {
            deleteHeartStmt.setString(1, postId);
            deleteHeartStmt.setInt(2, userId);
            int affectedRows = deleteHeartStmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean addHeart(String postId, int userId) {
        final String insertHeartSql = "INSERT INTO heart (postId, userId) VALUES (?, ?) ON DUPLICATE KEY UPDATE postId=postId;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement insertHeartStmt = conn.prepareStatement(insertHeartSql)) {
            insertHeartStmt.setString(1, postId);
            insertHeartStmt.setInt(2, userId);
            int affectedRows = insertHeartStmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}