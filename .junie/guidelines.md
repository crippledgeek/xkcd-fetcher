# XKCD Fetcher - Development Guidelines

This document provides essential information for developers working on the XKCD Fetcher project.

## Project Overview

XKCD Fetcher is a Spring Boot reactive application that fetches comics from the XKCD API and stores them in a PostgreSQL database. The application uses:

- Spring WebFlux for reactive programming
- Spring Data JPA for database access
- Liquibase for database schema management
- MapStruct for object mapping
- Lombok for reducing boilerplate code
- BlockHound for detecting blocking calls in reactive code

## Build and Configuration Instructions

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- PostgreSQL 14 or higher

### Database Setup

1. Create a PostgreSQL database named `xkcd`:

```sql
CREATE DATABASE xkcd;
CREATE USER xkcd WITH ENCRYPTED PASSWORD 'xkcd';
GRANT ALL PRIVILEGES ON DATABASE xkcd TO xkcd;
```

2. The application uses Liquibase to manage the database schema. The schema will be automatically created when the application starts.

### Building the Project

To build the project, run:

```bash
mvn clean package
```

To skip tests during the build:

```bash
mvn clean package -DskipTests
```

### Running the Application

To run the application:

```bash
java -jar target/xkcd-fetcher-0.0.1-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

## Testing Information

### Testing Framework

The project uses:
- JUnit 5 for unit tests
- Mockito for mocking dependencies
- StepVerifier from reactor-test for testing reactive streams

### Running Tests

To run all tests:

```bash
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=XKCDServiceTest
```

### Writing Tests

#### Service Tests

For testing services that interact with external APIs (like XKCDService), use Mockito to mock the service interface:

```java
@Test
void getLatestComic_shouldReturnComic() {
    // Create a mock Xkcd object
    Xkcd mockComic = new Xkcd(
        "7",                                           // month
        "",                                            // link
        "2025",                                        // year
        "",                                            // news
        "Test Comic",                                  // safe_title
        "",                                            // transcript
        "This is a test comic",                        // alt
        "Test Comic",                                  // title
        "27",                                          // day
        2000,                                          // num
        "https://imgs.xkcd.com/comics/test_comic.png"  // img
    );
    
    // Configure the mock to return our mock comic
    when(xkcdService.getLatestComic()).thenReturn(Mono.just(mockComic));

    // Test the service
    StepVerifier.create(xkcdService.getLatestComic())
            .assertNext(comic -> {
                assertNotNull(comic);
                assertEquals(2000, comic.num());
                assertEquals("Test Comic", comic.title());
                assertEquals("This is a test comic", comic.alt());
                assertEquals("https://imgs.xkcd.com/comics/test_comic.png", comic.img());
            })
            .verifyComplete();
}
```

#### Repository Tests

For testing repositories, use the `@DataJpaTest` annotation and an in-memory database:

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ComicRepositoryTest {

    @Autowired
    private ComicRepository comicRepository;

    @Test
    void findByTitle_shouldReturnComicsWithMatchingTitle() {
        // Given
        Comic comic1 = new Comic();
        comic1.setTitle("Test Comic");
        comic1.setImg("https://example.com/comic1.png");
        comic1.setAlt("Test Alt 1");
        comic1.setDate(LocalDate.of(2025, 7, 28));
        
        Comic comic2 = new Comic();
        comic2.setTitle("Test Comic");
        comic2.setImg("https://example.com/comic2.png");
        comic2.setAlt("Test Alt 2");
        comic2.setDate(LocalDate.of(2025, 7, 29));
        
        Comic comic3 = new Comic();
        comic3.setTitle("Different Title");
        comic3.setImg("https://example.com/comic3.png");
        comic3.setAlt("Test Alt 3");
        comic3.setDate(LocalDate.of(2025, 7, 30));
        
        comicRepository.saveAll(Arrays.asList(comic1, comic2, comic3));
        
        // When
        List<Comic> foundComics = comicRepository.findByTitle("Test Comic");
        
        // Then
        assertEquals(2, foundComics.size());
        assertTrue(foundComics.stream().allMatch(comic -> "Test Comic".equals(comic.getTitle())));
    }
    
    @Test
    void findByDate_shouldReturnComicsWithMatchingDate() {
        // Given
        LocalDate testDate = LocalDate.of(2025, 7, 28);
        
        Comic comic1 = new Comic();
        comic1.setTitle("Test Comic 1");
        comic1.setImg("https://example.com/comic1.png");
        comic1.setAlt("Test Alt 1");
        comic1.setDate(testDate);
        
        Comic comic2 = new Comic();
        comic2.setTitle("Test Comic 2");
        comic2.setImg("https://example.com/comic2.png");
        comic2.setAlt("Test Alt 2");
        comic2.setDate(testDate);
        
        Comic comic3 = new Comic();
        comic3.setTitle("Test Comic 3");
        comic3.setImg("https://example.com/comic3.png");
        comic3.setAlt("Test Alt 3");
        comic3.setDate(LocalDate.of(2025, 7, 29));
        
        comicRepository.saveAll(Arrays.asList(comic1, comic2, comic3));
        
        // When
        List<Comic> foundComics = comicRepository.findByDate(testDate);
        
        // Then
        assertEquals(2, foundComics.size());
        assertTrue(foundComics.stream().allMatch(comic -> testDate.equals(comic.getDate())));
    }
}
```

#### Controller Tests

For testing controllers, use WebTestClient:

```java
@WebFluxTest(ComicController.class)
class ComicControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private ComicService comicService;
    
    @Test
    void getComicById_shouldReturnComic() {
        // Given
        int comicId = 2000;
        Xkcd mockComic = new Xkcd(
            "7",                                           // month
            "",                                            // link
            "2025",                                        // year
            "",                                            // news
            "Test Comic",                                  // safe_title
            "",                                            // transcript
            "This is a test comic",                        // alt
            "Test Comic",                                  // title
            "27",                                          // day
            comicId,                                       // num
            "https://imgs.xkcd.com/comics/test_comic.png"  // img
        );
        
        when(comicService.getComicById(comicId)).thenReturn(Mono.just(mockComic));
        
        // When/Then
        webTestClient.get()
            .uri("/comics/{comicId}", comicId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Xkcd.class)
            .value(comic -> {
                assertEquals(comicId, comic.num());
                assertEquals("Test Comic", comic.title());
                assertEquals("This is a test comic", comic.alt());
                assertEquals("https://imgs.xkcd.com/comics/test_comic.png", comic.img());
            });
    }
    
    @Test
    void getAllComics_shouldReturnAllComics() {
        // Given
        Comics mockComics = new Comics(List.of(
            new se.disabledsecurity.xkcd.fetcher.internal.model.Comic(
                2000,
                "Test Comic 1",
                "https://imgs.xkcd.com/comics/test_comic1.png",
                "This is test comic 1",
                LocalDate.of(2025, 7, 27)
            ),
            new se.disabledsecurity.xkcd.fetcher.internal.model.Comic(
                2001,
                "Test Comic 2",
                "https://imgs.xkcd.com/comics/test_comic2.png",
                "This is test comic 2",
                LocalDate.of(2025, 7, 28)
            )
        ));
        
        when(comicService.getAllComics()).thenReturn(Mono.just(mockComics));
        
        // When/Then
        webTestClient.get()
            .uri("/comics")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Comics.class)
            .value(comics -> {
                assertEquals(2, comics.comics().size());
                assertEquals("Test Comic 1", comics.comics().get(0).title());
                assertEquals("Test Comic 2", comics.comics().get(1).title());
            });
    }
}
```


## Additional Development Information

### Project Structure

- `src/main/java/se/disabledsecurity/xkcd/fetcher/`
  - `configuration/`: Configuration classes
  - `controller/`: REST controllers
  - `entity/`: JPA entities
  - `exception/`: Exception handling
  - `external/model/`: Models for external API responses
  - `functions/`: Utility functions
  - `internal/model/`: Internal models
  - `mapper/`: MapStruct mappers
  - `repository/`: Spring Data repositories
  - `service/`: Service interfaces and implementations

### Key Components

- `XKCDService`: Interface for fetching comics from the XKCD API
- `ComicFetcher`: Implementation of ComicService that uses XKCDService
- `ComicController`: REST controller for exposing comic endpoints
- `Comic` and `ComicDate` entities: JPA entities for storing comics and their dates

### Reactive Programming

The application uses Spring WebFlux for reactive programming. Key points:

- Use `Mono` for single values and `Flux` for streams of values
- Avoid blocking operations in the reactive pipeline
- Use `subscribeOn(Schedulers.boundedElastic())` for potentially blocking operations
- BlockHound is installed to detect blocking calls in reactive code
- Replace imperative if-null checks with reactive approaches like `Mono.justOrEmpty()`

#### Reactive Null Handling

The code was refactored to replace an imperative if-null check with a reactive approach using `Mono.justOrEmpty()` in three methods: `deleteComicByTitle`, `deleteComicById`, and `deleteComicByDate`. This approach maintains the reactive flow without breaking the chain with imperative code.

In the traditional imperative approach, you would check if an object is null before performing operations on it:

```
if (object != null) {
    performOperation(object);
}
```

With the reactive approach using `Mono.justOrEmpty()`, you can handle this more elegantly:

```
Mono.justOrEmpty(object)
    .doOnNext(obj -> performOperation(obj))
```

This creates a Mono that:
- Emits the object if it's non-null
- Completes empty if the object is null
- Processes the object only if it exists (using `doOnNext`)
- Continues the reactive chain regardless of whether the object exists

In the PostgresDatabaseService implementation, this pattern is used to handle potential null comic dates when deleting comics. All tests pass successfully, confirming that the refactored implementation maintains the same functionality as before.

### Database Schema

The database has two tables:
- `comic`: Stores comic information (id, title, img, alt, date)
- `comic_date`: Stores the relationship between comics and dates (comic_id, date)

### Code Style

- Use Lombok annotations to reduce boilerplate code
- Use MapStruct for object mapping
- Follow reactive programming best practices
- Write comprehensive tests for all components