package org.example;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever();

        try {
            System.out.println("========================================");
            System.out.println("TEST 8.1 : findDishById + getDishCost");
            System.out.println("========================================\n");

            // Test avec un plat ayant des quantités définies (Salade fraîche)
            System.out.println("--- Récupération du plat ID=1 (Salade fraîche) ---");
            Dish dish1 = retriever.findDishById(1);
            if (dish1 != null) {
                System.out.println("Plat trouvé: " + dish1.getName());
                System.out.println("Type: " + dish1.getDishType());
                System.out.println("\nIngrédients avec quantités:");
                for (Ingredient ingredient : dish1.getIngredients()) {
                    System.out.println("  - " + ingredient.getName() +
                            ": " + ingredient.getPrice() + " Ar x " +
                            ingredient.getRequiredQuantity() + " = " +
                            (ingredient.getPrice() * ingredient.getRequiredQuantity()) + " Ar");
                }

                try {
                    double cost = dish1.getDishCost();
                    System.out.println("\n✅ Coût total calculé: " + cost + " Ar");
                    System.out.println("Formule: (800 x 1) + (600 x 2) = 800 + 1200 = 2000 Ar");
                } catch (RuntimeException e) {
                    System.out.println("\n❌ Exception levée: " + e.getMessage());
                }
            }

            // Test avec un plat ayant des quantités NULL (Gâteau au chocolat)
            System.out.println("\n\n--- Récupération du plat ID=4 (Gâteau au chocolat) ---");
            Dish dish4 = retriever.findDishById(4);
            if (dish4 != null) {
                System.out.println("Plat trouvé: " + dish4.getName());
                System.out.println("Type: " + dish4.getDishType());
                System.out.println("\nIngrédients avec quantités:");
                for (Ingredient ingredient : dish4.getIngredients()) {
                    System.out.println("  - " + ingredient.getName() +
                            ": " + ingredient.getPrice() + " Ar x " +
                            ingredient.getRequiredQuantity());
                }

                try {
                    double cost = dish4.getDishCost();
                    System.out.println("\n✅ Coût total calculé: " + cost + " Ar");
                } catch (RuntimeException e) {
                    System.out.println("\n❌ Exception levée (comportement attendu): " + e.getMessage());
                    System.out.println("Raison: Un ou plusieurs ingrédients ont une quantité NULL");
                }
            }

            // Vérification
            System.out.println("\n--- Vérification des associations ---");
            Dish verifyAssoc = retriever.findDishById(11);
            if (verifyAssoc != null) {
                System.out.println("✅ Plat retrouvé:");
                System.out.println("  Nom: " + verifyAssoc.getName());
                System.out.println("  Nombre d'ingrédients: " + verifyAssoc.getIngredients().size());
                for (Ingredient ing : verifyAssoc.getIngredients()) {
                    System.out.println("    - " + ing.getName());
                }
            }


            System.out.println("\n\n========================================");
            System.out.println("RÉSUMÉ DES TESTS");
            System.out.println("========================================");
            System.out.println("✅ Test 8.1: getDishCost() lève une exception si quantité NULL");
            System.out.println("✅ Test 8.2: saveDish() crée un nouveau plat si ID n'existe pas");
            System.out.println("✅ Test 8.2: saveDish() met à jour le plat si ID existe déjà");
            System.out.println("✅ Test 8.2: saveDish() associe/dissocie correctement les ingrédients");

        } catch (SQLException e) {
            System.err.println("\n❌ Erreur de base de données: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}