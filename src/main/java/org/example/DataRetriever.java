package org.example;

import java.sql.Connection;
import java.sql.Types;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private DBConnection dbConnection;

    public DataRetriever() {
        this.dbConnection = new DBConnection();
    }

    public Dish findDishById(int id) throws SQLException {
        Connection conn = dbConnection.getConnection();

        String query = "SELECT * FROM Dish WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();

        Dish dish = null;
        if (rs.next()) {
            dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));

            String ingredientQuery = "SELECT * FROM Ingredient WHERE id_dish = ?";
            PreparedStatement ingredientStmt = conn.prepareStatement(ingredientQuery);
            ingredientStmt.setInt(1, id);
            ResultSet ingredientRs = ingredientStmt.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (ingredientRs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(ingredientRs.getInt("id"));
                ingredient.setName(ingredientRs.getString("name"));
                ingredient.setPrice(ingredientRs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(ingredientRs.getString("category")));
                ingredient.setDish(dish);

                Double requiredQty = null;
                Object qtyObj = ingredientRs.getObject("required_quantity");
                if (qtyObj != null) {
                    requiredQty = ((Number) qtyObj).doubleValue();
                }
                ingredient.setRequiredQuantity(requiredQty);

                ingredients.add(ingredient);
            }
            dish.setIngredients(ingredients);

            ingredientRs.close();
            ingredientStmt.close();
        }

        rs.close();
        pstmt.close();
        conn.close();

        return dish;
    }

    public List<Ingredient> findIngredients(int page, int size) throws SQLException {
        Connection conn = dbConnection.getConnection();

        String query = "SELECT * FROM Ingredient LIMIT ? OFFSET ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, size);
        pstmt.setInt(2, page * size);
        ResultSet rs = pstmt.executeQuery();

        List<Ingredient> ingredients = new ArrayList<>();
        while (rs.next()) {
            Ingredient ingredient = new Ingredient();
            ingredient.setId(rs.getInt("id"));
            ingredient.setName(rs.getString("name"));
            ingredient.setPrice(rs.getDouble("price"));
            ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

            Double requiredQty = null;
            Object qtyObj = rs.getObject("required_quantity");
            if (qtyObj != null) {
                requiredQty = ((Number) qtyObj).doubleValue();
            }
            ingredient.setRequiredQuantity(requiredQty);

            int dishId = rs.getInt("id_dish");
            if (!rs.wasNull()) {
                Dish dish = findDishById(dishId);
                ingredient.setDish(dish);
            }

            ingredients.add(ingredient);
        }

        rs.close();
        pstmt.close();
        conn.close();

        return ingredients;
    }
    public void createIngredients(List<Ingredient> newIngredients) throws SQLException {
        Connection conn = dbConnection.getConnection();

        for (Ingredient ingredient : newIngredients) {
            String checkQuery = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, ingredient.getName());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                rs.close();
                checkStmt.close();
                conn.close();
                throw new RuntimeException("Ingredient '" + ingredient.getName() + "' existe déjà");
            }
            rs.close();
            checkStmt.close();
        }

        String query = "INSERT INTO Ingredient (name, price, category, id_dish, required_quantity) VALUES (?, ?, ?::category_enum, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(query);

        for (Ingredient ingredient : newIngredients) {
            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getPrice());
            pstmt.setString(3, ingredient.getCategory().name());

            if (ingredient.getDish() != null) {
                pstmt.setInt(4, ingredient.getDish().getId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            if (ingredient.getRequiredQuantity() != null) {
                pstmt.setDouble(5, ingredient.getRequiredQuantity());
            } else {
                pstmt.setNull(5, Types.NUMERIC);
            }

            pstmt.executeUpdate();
        }

        pstmt.close();
        conn.close();
    }
}
