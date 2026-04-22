-- =========================
-- CLIENTS (USUARIOS)
-- password = 1234 (BCrypt)
-- =========================
INSERT INTO client (first_name, last_name, password, email, role) VALUES
                                                                          ('Federico', 'Valdez', '{bcrypt}$2a$12$RWQL8QReiBAQs5FFoqwY.uEtgGdr65BQih8PhqbbClFelOT/110k2', 'fede@mail.com', 'CLIENT'),
                                                                          ('Ana', 'Gomez', '{bcrypt}$2a$12$RWQL8QReiBAQs5FFoqwY.uEtgGdr65BQih8PhqbbClFelOT/110k2', 'ana@mail.com', 'CLIENT'),
                                                                          ('Admin', 'System', '{bcrypt}$2a$12$RWQL8QReiBAQs5FFoqwY.uEtgGdr65BQih8PhqbbClFelOT/110k2', 'admin@mail.com', 'ADMIN');

-- =========================
-- CATEGORY (DESCUENTOS)
-- =========================
INSERT INTO category (name, discount_rate) VALUES
                                                   ('TECH', 0.10),
                                                   ('SPORT', 0.15),
                                                   ('FOOD', 0.05);

-- =========================
-- PRODUCTS
-- =========================
INSERT INTO product (name, category_id, stock, price, code) VALUES
                                                                    ('Notebook Lenovo', 1, 10, 1200.00, 'TECH-001'),
                                                                    ('Mouse Logitech', 1, 50, 25.00, 'TECH-002'),
                                                                    ('Teclado Redragon', 1, 30, 45.00, 'TECH-003'),
                                                                    ('Zapatillas Nike', 2, 20, 80.00, 'SPORT-001'),
                                                                    ('Remera Adidas', 2, 40, 20.00, 'SPORT-002'),
                                                                    ('Cafe Molido 500g', 3, 100, 10.00, 'FOOD-001'),
                                                                    ('Galletitas Chocolate', 3, 200, 5.00, 'FOOD-002');