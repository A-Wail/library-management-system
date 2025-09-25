-- Insert sample publishers
INSERT INTO publisher (id,name, address) VALUES
(1,'Penguin Random House', 'New York, NY 10019, USA'),
(2,'HarperCollins', 'New York, NY 10007, USA');

-- Insert hierarchical categories
INSERT INTO category (name, parent_category_id) VALUES ('Programming', NULL);
INSERT INTO category (name, parent_category_id) VALUES ('Fiction', NULL);


 Get the ID of 'Fiction' and use it as the parent for next inserts
INSERT INTO category (name, parent_category_id) VALUES ('Java', 1);
INSERT INTO category (name, parent_category_id) VALUES ('C++', 1);
INSERT INTO category (name, parent_category_id) VALUES ('Adventures',2);

-- Insert authors
INSERT INTO author (name, bio) VALUES
('Kathy Sierra', 'American science and Software Engineer best known for Java.'),
('Joli Tolkien', 'English writer, poet, philologist, and academic, best known for The Hobbit and The Lord of the Rings.');

-- Insert books
INSERT INTO book (title, isbn, publication_year, summary, language, cover_url, edition, publisher_id)
VALUES
('The C++ Programming Language', '9780321563842', 2013, 'A definitive guide to the C++ programming language.', 'English', 'https://example.com/cpp_book_cover.jpg', '4th', 1),
('Effective Java', '9780134685991', 2018, 'A must-read for any Java developer seeking to write high-quality code.', 'English', 'https://example.com/effective_java_cover.jpg', '3rd', 2),
('Introduction to Algorithms', '9780262033848', 2009, 'A comprehensive textbook on the modern study of computer algorithms.', 'English', 'https://example.com/algorithms_cover.jpg', '3rd', 1),
('The Lord of the Rings', '9780544003415', 2004, 'The epic fantasy novel by J.R.R. Tolkien, detailing the quest to destroy the One Ring.', 'English', 'https://example.com/lotr_cover.jpg', '50th Anniversary Edition', 2),
('Python Crash Course', '9781593279288', 2019, 'A fast-paced, thorough introduction to programming with Python.', 'English', 'https://example.com/python_crash_cover.jpg', '2nd', 1);
 Link books to authors
INSERT INTO book_author (book_id, author_id) VALUES
(
    (SELECT id FROM book WHERE title = 'Effective Java'),
    (SELECT id FROM author WHERE name = 'Kathy Sierra')
),
(
    (SELECT id FROM book WHERE title = 'The Lord of the Rings'),
    (SELECT id FROM author WHERE name = 'Joli Tolkien')
);

-- Link books to categories
INSERT INTO book_category (book_id, category_id) VALUES
(
    (SELECT id FROM book WHERE title = 'Effective Java'),
    (SELECT id FROM category WHERE name = 'Java')
),
(
    (SELECT id FROM book WHERE title = 'The Lord of the Rings'),
    (SELECT id FROM category WHERE name = 'Adventures')
);

-- Insert a sample member
INSERT INTO member (name, email, phone, membership_date) VALUES
('Ali Waled', 'aliwaleed@email.com', '555-123-4567', '2024-01-15');

-- Insert a sample ADMIN user. Password is 'admin123' but HASHED with BCrypt.
-- BCrypt hash for 'admin123'
INSERT INTO system_user (username, hash_pass, email, role) VALUES
('admin', '$2a$12$xyzXYZXYZXYZXYZXYZXYZ.1W0L1/7eC4WQ9lRcL9ZcB8QdK9V0S', 'admin@library.com', 'ADMIN');

-- Insert a sample borrowing transaction
INSERT INTO borrowing_transaction (book_id, member_id, borrow_date, due_date, status) VALUES
(
    (SELECT id FROM book WHERE title = 'The Lord of the Rings'),
    (SELECT id FROM member WHERE email = 'aliwaleed@email.com'),
    '2024-05-20',
    '2024-06-03',
    'BORROWED'
);