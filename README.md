# Library Management System

A Spring Boot REST API for managing library operations, including user authentication, member management, book cataloging, and borrowing/returning books. Secured with JSON Web Tokens (JWT) and Role-Based Access Control (RBAC) for roles `ADMIN`, `LIBRARIAN`, and `STAFF`.

## Features

- **User Authentication**: Admin-only registration and JWT-based login.
- **RBAC**:
  - `ADMIN`: Full access to all operations.
  - `LIBRARIAN`: Manage members, books, and borrowing transactions.
  - `STAFF`: View data and return books.
- **Member Management**: CRUD operations for library members.
- **Book Management**: Add books with ISBN, title, authors, categories, and publisher, with strict validation.
- **Borrowing/Returning**: Track book loans with due dates and overdue status.
- **Validation**: Enforce constraints (e.g., ISBN format, publication year 1450–2025).
- **Error Handling**: Custom exceptions for not found, book unavailability, and already returned books.
- **Testing**: Unit tests for services and repositories using JUnit and Mockito.

## Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, Hibernate
- **Database**: MySQL/PostgreSQL (configurable)
- **Build Tool**: Maven
- **Libraries**: Lombok, JJWT, Jakarta Validation
- **Logging**: SLF4J with Logback

## Prerequisites

- Java 17+
- Maven 3.8.x+
- MySQL/PostgreSQL (or H2 for testing)
- Git, cURL/Postman for API testing

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/A-Wail/library-management-system.git
cd library-management-system
```

### 2. Configure the Database

Edit `src/main/resources/application.properties` to address MySQL connection issues (e.g., public key retrieval):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
jwt.secret=your-very-long-base64-encoded-secret
jwt.expirationMs=86400000
```

Generate a JWT secret:

```bash
openssl rand -base64 32
```

### 3. Seed Initial Data

Seed an admin user with correct `@Id` annotation (from your Author entity discussion):

```sql
INSERT INTO system_user (id, email, username, hash_pass, role)
VALUES (1, 'admin@example.com', 'admin', '$2a$10$XURPShl9jSZVaJAG3TLF0.3H2L4o29N81TrQ6TqM0y1bLs.dm3E2i', 'ADMIN');
```

- `hash_pass` is BCrypt for `password123`.

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

API runs at `http://localhost:8080`.

## API Endpoints

All endpoints except `/api/v1/authentication/**` require `Authorization: Bearer <token>`.

## Testing

-Unit tests in src/test/java/com/task/library_managment_system/.

-Run tests:
```bash
mvn test
```

### Authentication

- **POST /api/v1/authentication/login**

  ```json
  {
    "username": "admin",
    "password": "password123"
  }
  ```

  **Response**:

  ```json
  {
    "token": "<jwt-token>"
  }
  ```
- **POST /api/v1/authentication/register** (ADMIN only)

  ```json
  {
    "email": "librarian@example.com",
    "username": "librarian1",
    "password": "librarian123",
    "role": "LIBRARIAN"
  }
  ```

### Members

- **POST /api/v1/members** (ADMIN, LIBRARIAN)

  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "membershipDate": "2025-09-25"
  }
  ```
- **GET /api/v1/members** (ADMIN, LIBRARIAN, STAFF)
- **GET /api/v1/members/{id}** (ADMIN, LIBRARIAN, STAFF)
- **PUT /api/v1/members/{id}** (ADMIN, LIBRARIAN)
- **DELETE /api/v1/members/{id}** (ADMIN)

### Books

- **POST /api/v1/books** (ADMIN, LIBRARIAN)

  ```json
  {
    "bookDetails": {
      "isbn": "978-3-16-148410-0",
      "title": "The Great Gatsby",
      "publicationYear": 1925,
      "edition": "First Edition",
      "summary": "A novel about the American Dream.",
      "language": "English",
      "coverUrl": "https://example.com/gatsby-cover.jpg"
    },
    "categoryIds": [1, 2],
    "publisherId": 1,
    "authorIds": [1]
  }
  ```
- **GET /api/v1/books** (ADMIN, LIBRARIAN, STAFF)
- **GET /api/v1/books/{id}** (ADMIN, LIBRARIAN, STAFF)
- **PUT /api/v1/books/{id}** (ADMIN, LIBRARIAN)
- **DELETE /api/v1/books/{id}** (ADMIN)

### Borrowing

- **POST /api/v1/borrowings/{memberId}/{bookId}** (ADMIN, LIBRARIAN)

  ```bash
  curl -X POST http://localhost:8080/api/v1/borrowings/1/1 -H "Authorization: Bearer <librarian-token>"
  ```

  **Response**:

  ```json
  {
    "transactionId": 1,
    "bookTitle": "The Great Gatsby",
    "memberName": "John Doe",
    "borrowDate": "2025-09-25",
    "dueDate": "2025-10-09",
    "message": "Book borrowed successfully. Due date: 2025-10-09"
  }
  ```
- **PUT /api/v1/borrowings/{transactionId}/return** (ADMIN, LIBRARIAN, STAFF)

  ```bash
  curl -X PUT http://localhost:8080/api/v1/borrowings/1/return -H "Authorization: Bearer <staff-token>"
  ```

## Advanced Topics

### Security

- **JWT Authentication**: Tokens are generated in `JwtService` using JJWT, signed with a secure HMAC-SHA key, and validated in `JwtAuthenticationFilter`. Tokens include `username` and `roles`:

  ```java
  public String generateToken(SystemUser user) {
      return Jwts.builder()
          .setSubject(user.getUsername())
          .claim("roles", user.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(",")))
          .setIssuedAt(new Date())
          .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
          .signWith(signingKey)
          .compact();
  }
  ```
- **RBAC**: `@PreAuthorize` enforces role-based access:

  ```java
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public BorrowingResponse borrowBook(Long memberId, Long bookId) { ... }
  ```
- **Security Best Practices**:
  - Passwords hashed with BCrypt (`PasswordEncoder`).
  - CSRF disabled for stateless API.
  - Use HTTPS in production.
  - Rotate `jwt.secret` via environment variables:

    ```bash
    export JWT_SECRET=your-very-long-base64-encoded-secret
    ```

### Performance Optimization

- **JPA Query Optimization**: Optimize `BorrowingServiceImpl.findActiveTransactionsByBookId`:

  ```java
  @Query("SELECT t FROM BorrowingTransaction t WHERE t.book.id = :bookId AND t.returnDate IS NULL")
  List<BorrowingTransaction> findActiveTransactionsByBookId(@Param("bookId") Long bookId);
  ```
- **Indexing**: Add to `borrowing_transaction`:

  ```sql
  CREATE INDEX idx_book_id_return_date ON borrowing_transaction (book_id, return_date);
  ```
- **Caching**: Cache book availability:

  ```java
  @Cacheable("bookAvailability")
  public boolean isBookCurrentlyBorrowed(Long bookId) { ... }
  ```
- **Pagination**: Add to `MemberServiceImpl.viewAllMembers`:

  ```java
  Page<MemberResponse> viewAllMembers(Pageable pageable);
  ```

### Testing

- **Unit Tests**: Test `BorrowingServiceImpl` with JUnit/Mockito:

  ```java
  @Test
  void borrowBook_throwsBookNotAvailableException_whenBookIsBorrowed() {
      when(bookRepo.findById(1L)).thenReturn(Optional.of(Book.builder().title("Test Book").build()));
      when(borrowingTransRepo.findByBookId(1L)).thenReturn(List.of(BorrowingTransaction.builder().returnDate(null).build()));
      assertThrows(BookNotAvailableNowException.class, () -> borrowingService.borrowBook(1L, 1L));
  }
  ```
- **Integration Tests**: Use `@SpringBootTest` with `@WithMockUser`:

  ```java
  @Test
  @WithMockUser(roles = "LIBRARIAN")
  void borrowBook_succeeds_forLibrarian() throws Exception {
      mockMvc.perform(post("/api/v1/borrowings/1/1")).andExpect(status().isOk());
  }
  ```
- **Test Database**: Use H2:

  ```properties
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.jpa.hibernate.ddl-auto=create-drop
  ```

### Future Extensions

- **Fine Calculation**: Add to `BorrowingServiceImpl.getBorrowingById`:

  ```java
  if (transaction.getReturnDate().isAfter(transaction.getDueDate())) {
      long daysOverdue = ChronoUnit.DAYS.between(transaction.getDueDate(), transaction.getReturnDate());
      double fine = daysOverdue * 1.0; // $1 per day
      transaction.setFine(fine);
  }
  ```
- **Search API**: Add `GET /api/v1/books/search?title={query}`:

  ```java
  @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<Book> searchByTitle(@Param("query") String query);
  ```
- **Author-Book Relationship**: Ensure bidirectional `@ManyToMany` mapping (from past discussion):

  ```java
  @Entity
  public class Book {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      @ManyToMany
      @JoinTable(
          name = "book_author",
          joinColumns = @JoinColumn(name = "book_id"),
          inverseJoinColumns = @JoinColumn(name = "author_id")
      )
      private Set<Author> authors = new HashSet<>();
  }
  ```
- **Email Notifications**: Use Spring Mail:

  ```java
  @Autowired
  private JavaMailSender mailSender;
  public void sendOverdueReminder(BorrowingTransaction transaction) {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(transaction.getMember().getEmail());
      message.setSubject("Overdue Book Reminder");
      message.setText("Please return " + transaction.getBook().getTitle() + " due on " + transaction.getDueDate());
      mailSender.send(message);
  }
  ```

### Deployment

- **Docker**:

  ```dockerfile
  FROM openjdk:17-jdk-slim
  COPY target/library-management-system.jar app.jar
  ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/library_db?allowPublicKeyRetrieval=true
  ENTRYPOINT ["java", "-jar", "/app.jar"]
  ```

  ```bash
  docker build -t library-management-system .
  docker run -p 8080:8080 --link mysql-container:db library-management-system
  ```
- **GitHub Actions CI**:

  ```yaml
  name: CI
  on: [push]
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - uses: actions/setup-java@v3
          with:
            java-version: '17'
        - run: mvn test
  ```

### Exporting Data to JSON

To export entities (e.g., books) to JSON (from past discussion):

```java
ObjectMapper mapper = new ObjectMapper();
List<Book> books = bookRepo.findAll();
mapper.writeValue(new File("books.json"), books);
```

Handle circular references in `@ManyToMany`:

```java
mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
```

## Project Structure

```
library-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/task/library_managment_system/
│   │   │   ├── controller/    # BorrowingController, BookController...etc
│   │   │   ├── controlAdvice/ # controllerAdvice
│   │   │   ├── dto/           # CreateBookRequest, BorrowingResponse...etc
│   │   │   ├── exception/     # EntityNotFoundException, BookNotAvailableNowException...etc
│   │   │   ├── models/        # SystemUser, Book, BorrowingTransaction, Member, Author...etc
│   │   │   ├── repository/    # BookRepo, BorrowingTransRepo, MemberRepo...etc
│   │   │   ├── security/      # JwtService, SecurityConfiguration...etc
│   │   │   ├── service/       # BorrowingServiceImpl, UserServiceImpl...etc
│   │   ├── resources/
│   │       ├── application.properties
├── pom.xml
├── .gitignore
├── README.md
├── LICENSE
```

## Contributing

1. Fork the repository.
2. Create a branch: `git checkout -b feature/your-feature`.
3. Commit: `git commit -m "Add your feature"`.
4. Push: `git push origin feature/your-feature`.
5. Open a pull request.

## License

MIT License - see LICENSE.

## Contact

Open an issue on GitHub or email abdelrhmanwramadan@gmail.com.
