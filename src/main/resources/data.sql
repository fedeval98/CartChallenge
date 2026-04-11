-- =========================
-- CLIENTS (USUARIOS)
-- password = 1234 (BCrypt)
-- =========================
INSERT INTO client (id, first_name, last_name, password, email, role) VALUES
                                                                          (1, 'Federico', 'Valdez', '{bcrypt}$2a$12$RWQL8QReiBAQs5FFoqwY.uEtgGdr65BQih8PhqbbClFelOT/110k2', 'fede@mail.com', 'CLIENT'),
                                                                          (2, 'Ana', 'Gomez', '{bcrypt}$2a$12$RWQL8QReiBAQs5FFoqwY.uEtgGdr65BQih8PhqbbClFelOT/110k2', 'ana@mail.com', 'CLIENT'),
                                                                          (3, 'Admin', 'System', '{bcrypt}$2a$12$RWQL8QReiBAQs5FFoqwY.uEtgGdr65BQih8PhqbbClFelOT/110k2', 'admin@mail.com', 'ADMIN');

-- =========================
-- CATEGORY (DESCUENTOS)
-- =========================
INSERT INTO category (id, name, discount_rate) VALUES
                                                   (1, 'TECH', 0.10),
                                                   (2, 'SPORT', 0.15),
                                                   (3, 'FOOD', 0.05);

-- =========================
-- PRODUCTS
-- =========================
INSERT INTO product (id, name, category_id, stock, price, code) VALUES
                                                                    (1, 'Notebook Lenovo', 1, 10, 1200.00, 'TECH-001'),
                                                                    (2, 'Mouse Logitech', 1, 50, 25.00, 'TECH-002'),
                                                                    (3, 'Teclado Redragon', 1, 30, 45.00, 'TECH-003'),
                                                                    (4, 'Zapatillas Nike', 2, 20, 80.00, 'SPORT-001'),
                                                                    (5, 'Remera Adidas', 2, 40, 20.00, 'SPORT-002'),
                                                                    (6, 'Cafe Molido 500g', 3, 100, 10.00, 'FOOD-001'),
                                                                    (7, 'Galletitas Chocolate', 3, 200, 5.00, 'FOOD-002');