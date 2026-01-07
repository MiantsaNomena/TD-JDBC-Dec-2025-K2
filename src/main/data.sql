insert into dish (id, name, dish_type) values
    (1, 'Salade fraîche', 'START'),
    (2, 'Poulet grillé', 'MAIN'),
    (3, 'Riz aux légumes', 'MAIN'),
    (4, 'Gâteau au chocolat', 'DESSERT'),
    (5, 'Salade de fruits', 'DESSERT');

INSERT INTO Ingredient (name, price, category, id_dish, required_quantity) VALUES
    ('Laitue', 800.00, 'VEGETABLE', 1, 1),      -- 1 unité de laitue
    ('Tomate', 600.00, 'VEGETABLE', 1, 2),      -- 2 unités de tomate
    ('Poulet', 4500.00, 'ANIMAL', 2, 0.5),      -- 0.5 kg de poulet
    ('Chocolat', 3000.00, 'OTHER', 4, NULL),    -- quantité inconnue
    ('Beurre', 2500.00, 'DAIRY', 4, NULL
