package org.example;

import java.sql.Connection;
import java.sql.Types;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection dbConnection;

    public DataRetriever() {
        this.dbConnection = new DBConnection();
    }


    public Dish findDishById(int id) throws SQLException {
        String dishQuery = "SELECT * FROM Dish WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement dishStmt = conn.prepareStatement(dishQuery)) {

            dishStmt.setInt(1, id);
            try (ResultSet rs = dishStmt.executeQuery()) {

                Dish dish = null;
                if (rs.next()) {
                    dish = new Dish();
                    dish.setId(rs.getInt("id"));
                    dish.setName(rs.getString("name"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));

                    String ingQuery = "SELECT * FROM Ingredient WHERE id_dish = ?";
                    try (PreparedStatement ingStmt = conn.prepareStatement(ingQuery)) {
                        ingStmt.setInt(1, id);
                        try (ResultSet ingRs = ingStmt.executeQuery()) {

                            List<Ingredient> ingredients = new ArrayList<>();
                            while (ingRs.next()) {
                                Ingredient ing = new Ingredient();
                                ing.setId(ingRs.getInt("id"));
                                ing.setName(ingRs.getString("name"));
                                ing.setPrice(ingRs.getDouble("price"));
                                ing.setCategory(CategoryEnum.valueOf(ingRs.getString("category")));
                                ing.setDish(dish);
                                ingredients.add(ing);
                            }
                            dish.setIngredients(ingredients);
                        }
                    }
                }return dish;
            }
        }
    }

    public List<Ingredient> findIngredients(int page, int size) throws SQLException {
        String query = "SELECT * FROM Ingredient LIMIT ? OFFSET ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, size);
            stmt.setInt(2, page * size);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Ingredient> ingredients = new ArrayList<>();
                while (rs.next()) {
                    Ingredient ing = new Ingredient();
                    ing.setId(rs.getInt("id"));
                    ing.setName(rs.getString("name"));
                    ing.setPrice(rs.getDouble("price"));
                    ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                    int dishId = rs.getInt("id_dish");
                    if (!rs.wasNull()) {
                        ing.setDish(findDishById(dishId));
                    }

                    ingredients.add(ing);
                }return ingredients;
            }
        }
    }



    public void createIngredients(List<Ingredient> newIngredients) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {

            for (Ingredient ing : newIngredients) {
                String checkQuery = "SELECT COUNT(*) FROM Ingredient WHERE name = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                     ResultSet rs = checkStmt.executeQuery()) {

                    checkStmt.setString(1, ing.getName());
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        throw new RuntimeException("Ingredient '" + ing.getName() + "' existe déjà");
                    }
                }
            }

            String insertQuery = "INSERT INTO Ingredient (name, price, category, id_dish) VALUES (?, ?, ?::category_enum, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                for (Ingredient ing : newIngredients) {
                    stmt.setString(1, ing.getName());
                    stmt.setDouble(2, ing.getPrice());
                    stmt.setString(3, ing.getCategory().name());

                    if (ing.getDish() != null) {
                        stmt.setInt(4, ing.getDish().getId());
                    } else {
                        stmt.setNull(4, Types.INTEGER);
                    }

                    stmt.executeUpdate();
                }
            }
        }
    }


    public void saveDish(Dish dish) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {

            String checkQuery = "SELECT id FROM Dish WHERE id = ?";
            boolean exists;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                 ResultSet rs = checkStmt.executeQuery()) {

                checkStmt.setInt(1, dish.getId());
                exists = rs.next();
            }

            if (!exists) {
                String insertQuery = "INSERT INTO Dish (name, dish_type) VALUES (?, ?::dish_type_enum)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setString(1, dish.getName());
                    stmt.setString(2, dish.getDishType().name());
                    stmt.executeUpdate();
                }
            } else {
                String updateQuery = "UPDATE Dish SET name = ?, dish_type = ?::dish_type_enum WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setString(1, dish.getName());
                    stmt.setString(2, dish.getDishType().name());
                    stmt.setInt(3, dish.getId());
                    stmt.executeUpdate();
                }
            }

            String clearQuery = "UPDATE Ingredient SET id_dish = NULL WHERE id_dish = ?";
            try (PreparedStatement stmt = conn.prepareStatement(clearQuery)) {
                stmt.setInt(1, dish.getId());
                stmt.executeUpdate();
            }

            if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
                String attachQuery = "UPDATE Ingredient SET id_dish = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(attachQuery)) {
                    for (Ingredient ing : dish.getIngredients()) {
                        stmt.setInt(1, dish.getId());
                        stmt.setInt(2, ing.getId());
                        stmt.executeUpdate();
                    }
                }
            }
        }
    }


    public List<Dish> findDishesByIngredientName(String ingredientName) throws SQLException {
        String query = "SELECT DISTINCT d.id FROM Dish d " +
                "JOIN Ingredient i ON d.id = i.id_dish " +
                "WHERE i.name = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ingredientName);
            try (ResultSet rs = stmt.executeQuery()) {

                List<Dish> dishes = new ArrayList<>();
                while (rs.next()) {
                    dishes.add(findDishById(rs.getInt("id")));
                }return dishes;
            }
        }
    }



    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size) throws SQLException {

        StringBuilder query = new StringBuilder(
                "SELECT i.* FROM Ingredient i LEFT JOIN Dish d ON i.id_dish = d.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (ingredientName != null && !ingredientName.isEmpty()) {
            query.append(" AND i.name = ?");
            params.add(ingredientName);
        }

        if (category != null) {
            query.append(" AND i.category = ?::category_enum");
            params.add(category.name());
        }

        if (dishName != null && !dishName.isEmpty()) {
            query.append(" AND d.name = ?");
            params.add(dishName);
        }

        query.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Ingredient> ingredients = new ArrayList<>();
                while (rs.next()) {
                    Ingredient ing = new Ingredient();
                    ing.setId(rs.getInt("id"));
                    ing.setName(rs.getString("name"));
                    ing.setPrice(rs.getDouble("price"));
                    ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                    int dishId = rs.getInt("id_dish");
                    if (!rs.wasNull()) {
                        ing.setDish(findDishById(dishId));
                    }

                    ingredients.add(ing);
                }return ingredients;
            }
        }
    }
}
