

DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders      CASCADE;
DROP TABLE IF EXISTS cart        CASCADE;
DROP TABLE IF EXISTS books       CASCADE;
DROP TABLE IF EXISTS users       CASCADE;

CREATE TABLE users (
    id          BIGSERIAL     PRIMARY KEY,
    full_name   VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(100)  NOT NULL,              -- BCrypt hash, never plain text
    phone       VARCHAR(20),
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE books (
    id           BIGSERIAL      PRIMARY KEY,
    title        VARCHAR(200)   NOT NULL CHECK (length(trim(title)) > 0),
    author       VARCHAR(150)   NOT NULL,
    category     VARCHAR(60)    NOT NULL,
    description  TEXT           NOT NULL,
    price        NUMERIC(10,2)  NOT NULL CHECK (price > 0),
    stock        INTEGER        NOT NULL CHECK (stock >= 0),
    image_url    VARCHAR(255)   NOT NULL,
    featured     BOOLEAN        NOT NULL DEFAULT FALSE,
    sold_count   INTEGER        NOT NULL DEFAULT 0 CHECK (sold_count >= 0),   -- drives the "Popular" section
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_books_category ON books (category);
CREATE INDEX idx_books_title    ON books (lower(title));
CREATE INDEX idx_books_author   ON books (lower(author));

CREATE TABLE cart (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id   BIGINT  NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    quantity  INTEGER NOT NULL CHECK (quantity > 0),
    CONSTRAINT uq_cart_user_book UNIQUE (user_id, book_id)   -- one row per book per user
);

CREATE TABLE orders (
    id               BIGSERIAL      PRIMARY KEY,
    user_id          BIGINT         NOT NULL REFERENCES users(id),
    total_amount     NUMERIC(12,2)  NOT NULL CHECK (total_amount >= 0),
    delivery_address TEXT           NOT NULL,   -- name, address, city, pincode, phone, email
    order_date       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    order_status     VARCHAR(30)    NOT NULL DEFAULT 'PLACED'
);
CREATE INDEX idx_orders_user ON orders (user_id);

CREATE TABLE order_items (
    id        BIGSERIAL      PRIMARY KEY,
    order_id  BIGINT         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id   BIGINT         NOT NULL REFERENCES books(id),
    quantity  INTEGER        NOT NULL CHECK (quantity > 0),
    price     NUMERIC(10,2)  NOT NULL CHECK (price > 0)       -- unit price at time of purchase
);
CREATE INDEX idx_order_items_order ON order_items (order_id);


INSERT INTO books (title, author, category, description, price, stock, image_url, featured, sold_count, created_at) VALUES
('The Alchemist', 'Paulo Coelho', 'Fiction', 'A young Andalusian shepherd follows a recurring dream across the desert in search of treasure, and learns that the journey matters more than the destination. A short, luminous fable about listening to your own heart.', 299.00, 40, 'images/books/01-the-alchemist.svg', TRUE, 180, CURRENT_TIMESTAMP - INTERVAL '24 days'),
('To Kill a Mockingbird', 'Harper Lee', 'Fiction', 'Scout Finch grows up in a sleepy Alabama town where her father, a principled lawyer, defends a Black man falsely accused of a crime. A moving look at conscience, courage and childhood.', 349.00, 25, 'images/books/02-to-kill-a-mockingbird.svg', FALSE, 199, CURRENT_TIMESTAMP - INTERVAL '23 days'),
('The Kite Runner', 'Khaled Hosseini', 'Fiction', 'Amir betrays his closest friend in Kabul and spends decades trying to make amends. A story of guilt, loyalty and redemption set against the turmoil of Afghanistan.', 399.00, 18, 'images/books/03-the-kite-runner.svg', FALSE, 198, CURRENT_TIMESTAMP - INTERVAL '22 days'),
('The God of Small Things', 'Arundhati Roy', 'Fiction', 'Twins Estha and Rahel return to their family home in Kerala, where the events of one fateful season in 1969 still shape everything. Lush, lyrical and unforgettable.', 379.00, 12, 'images/books/04-the-god-of-small-things.svg', FALSE, 189, CURRENT_TIMESTAMP - INTERVAL '21 days'),
('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 'Fantasy', 'An orphan boy discovers on his eleventh birthday that he is a wizard, and is whisked off to Hogwarts School of Witchcraft and Wizardry. The first adventure in the most loved fantasy series of a generation.', 499.00, 60, 'images/books/05-harry-potter-and-the-sorcerer-s-stone.svg', TRUE, 201, CURRENT_TIMESTAMP - INTERVAL '20 days'),
('The Hobbit', 'J.R.R. Tolkien', 'Fantasy', 'Bilbo Baggins would rather stay home with his books and his pipe, until a wizard and thirteen dwarves invite him on a quest to reclaim a mountain of gold from a dragon.', 450.00, 30, 'images/books/06-the-hobbit.svg', TRUE, 200, CURRENT_TIMESTAMP - INTERVAL '19 days'),
('A Game of Thrones', 'George R.R. Martin', 'Fantasy', 'Noble families scheme for the Iron Throne while an ancient threat gathers beyond the Wall. The first volume of a sprawling saga of power, loyalty and betrayal.', 599.00, 15, 'images/books/07-a-game-of-thrones.svg', FALSE, 203, CURRENT_TIMESTAMP - INTERVAL '18 days'),
('The Girl with the Dragon Tattoo', 'Stieg Larsson', 'Mystery', 'A disgraced journalist and a brilliant, troubled hacker team up to solve a forty-year-old disappearance inside one of Sweden''s wealthiest families.', 429.00, 20, 'images/books/08-the-girl-with-the-dragon-tattoo.svg', FALSE, 179, CURRENT_TIMESTAMP - INTERVAL '17 days'),
('And Then There Were None', 'Agatha Christie', 'Mystery', 'Ten strangers are lured to an island off the coast of Devon, and one by one they begin to die. The best-selling crime novel of all time, and still a perfect puzzle.', 299.00, 35, 'images/books/09-and-then-there-were-none.svg', TRUE, 198, CURRENT_TIMESTAMP - INTERVAL '16 days'),
('Gone Girl', 'Gillian Flynn', 'Mystery', 'On the morning of their fifth anniversary, Amy Dunne vanishes. As the police close in on her husband, nothing about the marriage is quite what it seemed.', 399.00, 0, 'images/books/10-gone-girl.svg', FALSE, 189, CURRENT_TIMESTAMP - INTERVAL '15 days'),
('Atomic Habits', 'James Clear', 'Self-Help', 'A practical, science-backed system for building good habits and breaking bad ones. Small changes, repeated daily, compound into remarkable results.', 499.00, 80, 'images/books/11-atomic-habits.svg', TRUE, 193, CURRENT_TIMESTAMP - INTERVAL '14 days'),
('The Power of Now', 'Eckhart Tolle', 'Self-Help', 'A guide to spiritual enlightenment that invites you to step out of anxious thinking and live fully in the present moment.', 349.00, 22, 'images/books/12-the-power-of-now.svg', FALSE, 180, CURRENT_TIMESTAMP - INTERVAL '13 days'),
('Rich Dad Poor Dad', 'Robert T. Kiyosaki', 'Self-Help', 'Two father figures, two very different attitudes to money. Kiyosaki''s plain-spoken lessons on assets, liabilities and financial independence.', 325.00, 45, 'images/books/13-rich-dad-poor-dad.svg', FALSE, 191, CURRENT_TIMESTAMP - INTERVAL '12 days'),
('Deep Work', 'Cal Newport', 'Self-Help', 'The ability to focus without distraction is becoming rarer and more valuable. Newport lays out rules for training your attention and producing your best work.', 450.00, 5, 'images/books/14-deep-work.svg', FALSE, 202, CURRENT_TIMESTAMP - INTERVAL '11 days'),
('Clean Code', 'Robert C. Martin', 'Technology', 'A handbook of agile software craftsmanship. Learn to tell good code from bad, and how to write code that is readable, maintainable and a pleasure to work with.', 899.00, 28, 'images/books/15-clean-code.svg', TRUE, 194, CURRENT_TIMESTAMP - INTERVAL '10 days'),
('A Brief History of Time', 'Stephen Hawking', 'Science', 'From the Big Bang to black holes, Hawking explains the biggest questions about the universe in language anyone can follow.', 399.00, 33, 'images/books/16-a-brief-history-of-time.svg', FALSE, 197, CURRENT_TIMESTAMP - INTERVAL '9 days'),
('The Pragmatic Programmer', 'Andrew Hunt & David Thomas', 'Technology', 'Timeless advice on becoming a better, more productive developer: take responsibility, build adaptable systems, and never stop learning your craft.', 799.00, 14, 'images/books/17-the-pragmatic-programmer.svg', FALSE, 208, CURRENT_TIMESTAMP - INTERVAL '8 days'),
('Cosmos', 'Carl Sagan', 'Science', 'A personal voyage through 15 billion years of cosmic evolution, from the origin of life to the farthest galaxies, told with wonder and poetry.', 499.00, 26, 'images/books/18-cosmos.svg', FALSE, 196, CURRENT_TIMESTAMP - INTERVAL '7 days'),
('Wings of Fire', 'A.P.J. Abdul Kalam', 'Biography', 'The autobiography of India''s Missile Man and former President, tracing his path from a small town in Rameswaram to the heart of the nation''s space programme.', 250.00, 50, 'images/books/19-wings-of-fire.svg', TRUE, 199, CURRENT_TIMESTAMP - INTERVAL '6 days'),
('Steve Jobs', 'Walter Isaacson', 'Biography', 'Based on more than forty interviews with Jobs himself, an intimate and unflinching portrait of the creative, driven and demanding co-founder of Apple.', 599.00, 19, 'images/books/20-steve-jobs.svg', FALSE, 198, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('The Story of My Experiments with Truth', 'M.K. Gandhi', 'Biography', 'Gandhi''s candid account of his life up to 1921: the ideas, mistakes and convictions that shaped his philosophy of truth and non-violence.', 199.00, 38, 'images/books/21-the-story-of-my-experiments-with-truth.svg', FALSE, 182, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('Sapiens', 'Yuval Noah Harari', 'History', 'A sweeping history of humankind, from the first stone tools to the age of artificial intelligence, and the stories we tell that hold societies together.', 549.00, 55, 'images/books/22-sapiens.svg', TRUE, 197, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('Guns, Germs, and Steel', 'Jared Diamond', 'History', 'Why did some societies come to dominate others? Diamond traces the answer to geography, farming and disease rather than to any difference between peoples.', 499.00, 16, 'images/books/23-guns-germs-and-steel.svg', FALSE, 188, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('The Discovery of India', 'Jawaharlal Nehru', 'History', 'Written in prison in 1944, Nehru''s reflection on India''s long history, cultures and philosophies, and the question of what the nation means.', 399.00, 21, 'images/books/24-the-discovery-of-india.svg', FALSE, 176, CURRENT_TIMESTAMP - INTERVAL '1 days');
