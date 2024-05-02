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

import uga.menik.cs4370.models.Food;

/**
 * This service contains food related functions.
 */
@Service
public class FoodService {
    private final DataSource dataSource;

    @Autowired
    public FoodService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Food> getFoodList() {
        List<Food> foods = new ArrayList<>();
        final String sql = 
            "SELECT * FROM food";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int foodId = rs.getInt("foodId");
                    String foodName = rs.getString("foodName");
                    int calories = rs.getInt("calories");

                    Food thisFood = new Food(foodId, foodName, calories);
                    foods.add(thisFood);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foods;
    }
}

