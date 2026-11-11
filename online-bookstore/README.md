# Page & Co. – Online Bookstore

Full-stack bookstore: **vanilla HTML/CSS/JS** frontend, **Spring Web MVC (no Spring Boot)** REST API, **Spring JDBC** (`JdbcTemplate` → prepared statements) and **PostgreSQL**. Passwords are hashed with BCrypt (`spring-security-crypto` only), login uses an HTTP session cookie.

## Requirements
JDK 17+, Maven 3.9+, PostgreSQL 14+ (pgAdmin optional), **Tomcat 10.1+** (Jakarta Servlet 6 – Tomcat 9 will not work), IntelliJ IDEA Ultimate (Community works via the Maven `package` + manual Tomcat copy).

## 1. Create the database
1. In pgAdmin: right-click *Databases → Create → Database…* and name it `bookstore` (or run `CREATE DATABASE bookstore;`).
2. Open *Query Tool* on `bookstore`, load `database/bookstore.sql` and run it. It creates the five tables and inserts 24 sample books. It can be re-run any time to reset the data.
3. Edit `src/main/resources/application.properties` (`db.url`, `db.username`, `db.password`). Environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` override the file.

## 2. Run in IntelliJ IDEA
1. *File → Open* the `online-bookstore` folder; let Maven import `pom.xml`.
2. *Run → Edit Configurations → + → Tomcat Server → Local* (install Tomcat 10.1+ and select it).
3. *Deployment → + → Artifact → `online-bookstore:war exploded`*. Set *Application context* to `/online-bookstore`.
4. Run. Open <http://localhost:8080/online-bookstore/>.

Command-line alternative: `mvn clean package`, copy `target/online-bookstore.war` into Tomcat's `webapps/`, start Tomcat.

Open the site through Tomcat (not VS Code Live Server): the frontend and API must share one origin for the login cookie to work. Edit HTML/CSS/JS in VS Code and refresh; with the exploded artifact IntelliJ updates resources on frame deactivation.

## 3. Using the site
Browse, search and view books without an account. Sign up / log in to use the cart, checkout (Cash on Delivery, no payment gateway) and *My orders*. Checkout runs in one DB transaction: stock is locked and checked, the order and items are saved, stock is reduced, the cart is emptied. A reference such as `BK-20261110-000042` is shown on success.

## 4. REST API
| Method & path | Purpose | Auth |
|---|---|---|
| GET `/books` (`?keyword=&category=&sort=price_asc\|price_desc`) | All books, optionally filtered | – |
| GET `/books/{id}` | One book (404 if missing) | – |
| GET `/books/search?keyword=` | Title/author search | – |
| GET `/books/category/{category}` | By category | – |
| GET `/books/featured`, `/books/popular`, `/books/new`, `/books/categories` | Home page data | – |
| POST `/users/register` | Create account (201, 409 duplicate email, 400 validation) | – |
| POST `/users/login` · `/users/logout` · GET `/users/me` | Session | – |
| GET `/cart` · POST `/cart` · PUT `/cart/{id}` · DELETE `/cart/{id}` | Cart (stock limits enforced, duplicates merge) | session |
| POST `/checkout` | Place order (201) | session |
| GET `/orders/{id}` · GET `/users/{userId}/orders` | Own orders only (403 otherwise) | session |

Errors are JSON: `{"status":409,"error":"Conflict","message":"…","timestamp":"…"}`. A PostgreSQL outage returns 503.

## 5. Test with Postman
Import `postman/online-bookstore.postman_collection.json`. The `baseUrl` variable defaults to `http://localhost:8080/online-bookstore`. Run folders in order; Postman stores the session cookie automatically. Cart item and order ids in the URLs (`/cart/1`, `/orders/1`, `/users/1/orders`) must be replaced with ids returned by earlier calls if yours differ.

## 6. Verify in pgAdmin
```sql
SELECT * FROM cart;          -- rows appear / change quantity
SELECT * FROM orders;        -- one row per checkout
SELECT * FROM order_items;   -- price = unit price at purchase time
SELECT id, title, stock, sold_count FROM books ORDER BY id;   -- stock drops after an order
```

## Project layout
`src/main/java/com/bookstore/{config,controller,service,repository,model,dto,exception}` – layered backend (`AppInitializer` replaces web.xml) · `src/main/webapp` – pages, `css/style.css`, `js/{app,books,cart,checkout}.js`, `images/` (SVG covers) · `database/bookstore.sql` · `postman/`.

## Notes
- Demo-grade security: session cookie auth, no CSRF tokens, no rate limiting, HTTP only. Add these before real use.
- Cover images are generated SVGs so the project works offline; change `image_url` in `books` to use real photos.
