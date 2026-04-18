package jbailey.stu.mmu.bigbook.rest.controller;

import jbailey.stu.mmu.bigbook.commons.dao.BookDAO;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.rest.format.RESTFormatNegotiator;
import jbailey.stu.mmu.bigbook.rest.model.BookDTO;
import jbailey.stu.mmu.bigbook.rest.model.BookMapper;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;
import jbailey.stu.mmu.bigbook.rest.serializer.JsonSerializer;
import jbailey.stu.mmu.bigbook.rest.serializer.ResponseSerializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// todo: SORT THIS MESS OUT PLEASE

/**
 * Resource Handler for the {@link Book} REST API endpoint /books.
 * 
 * This servlet implements a RESTful API for managing book entities, adhering to REST principles
 * such as statelessness, uniform interface, and resource-based URLs. It handles CRUD operations
 * by mapping HTTP methods directly to business logic actions.
 * 
 * Key Features:
 * - Content Negotiation: Supports JSON, XML, and plain text responses based on {@code Accept} header.
 * - Error Handling: Provides consistent error responses with appropriate HTTP status codes.
 * - Input Validation: Leverages service-layer validation to ensure data integrity.
 * - Security: Enforces HTTPS and input sanitization to mitigate common vulnerabilities.
 * 
 * Architectural Role:
 * - Acts as the presentation layer for the REST API, translating HTTP requests to service calls.
 * - Uses {@link BookDTO}s (Data Transfer Objects) to decouple API contracts from internal domain models,
 *   allowing for API versioning and format flexibility without affecting business logic.
 * - Delegates to {@link BookService} for business rules, maintaining separation of concerns.
 * 
 * Separation of Concerns:
 * - HTTP protocol handling and request/response management (this servlet).
 * - Business logic and validation ({@link BookService} in commons).
 * - Data persistence and querying ({@link BookDAO} in commons).
 * - Serialization and format negotiation ({@link ResponseSerializer} and {@link RESTFormatNegotiator}).
 * 
 * Framework Agnosticism:
 * - No Spring annotations; dependencies injected via constructor to keep the adapter layer
 *   independent of the framework, adhering to hexagonal architecture.
 */
@Slf4j
public class BookResource extends HttpServlet {

    private final ServicePort servicePort;

    // Constructor injection for ServicePort to maintain framework agnosticism
    public BookResource(ServicePort servicePort) {
        this.servicePort = servicePort;
    }

    @Override
    public void init() throws ServletException {
        log.info("BookResource initialized with ServicePort: {}", servicePort != null);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();
            RestResponse<?> restResponse;

            String idParam = request.getParameter("id");
            String searchParam = request.getParameter("search");
            String pageParam = request.getParameter("page");
            String sizeParam = request.getParameter("size");

            int page = 0;
            int size = Integer.MAX_VALUE; // Default to all for backward compatibility
            if (pageParam != null) {
                try {
                    page = Integer.parseInt(pageParam);
                } catch (NumberFormatException e) {
                    // Ignore, use default
                }
            }
            if (sizeParam != null) {
                try {
                    size = Integer.parseInt(sizeParam);
                } catch (NumberFormatException e) {
                    // Ignore, use default
                }
            }

            if (idParam != null && !idParam.isEmpty()) {
                restResponse = getBookById(bookService, idParam);
            } else if (searchParam != null && !searchParam.isEmpty()) {
                restResponse = searchBooks(bookService, searchParam, page, size);
            } else {
                restResponse = getAllBooks(bookService, page, size);
            }

            sendResponse(response, restResponse);

        } catch (Exception e) {
            log.error("Error in GET", e);
            try {
                sendErrorResponse(response, 500, "Error retrieving books");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();
            
            String requestBody = readRequestBody(request);
            if (requestBody.isEmpty()) {
                sendErrorResponse(response, 400, "Request body required");
                return;
            }

            JsonSerializer<BookDTO> serializer = new JsonSerializer<>();
            BookDTO newBookDTO = serializer.deserialize(requestBody, BookDTO.class);
            Book newBook = BookMapper.fromDTO(newBookDTO);

            List<String> errors = bookService.validateBook(
                newBook.getTitle(), newBook.getAuthor(), 
                newBook.getDate(), newBook.getSynopsis()
            );
            if (!errors.isEmpty()) {
                sendErrorResponse(response, 400, "Validation failed: " + String.join(", ", errors));
                return;
            }

            String genresStr = bookService.serialiseGenres(newBook.getGenres());
            String charactersStr = bookService.serialiseCharacters(newBook.getCharacters());
            
            bookService.addBook(
                newBook.getTitle(), newBook.getAuthor(), newBook.getDate(),
                genresStr, charactersStr, newBook.getSynopsis()
            );

            response.setStatus(HttpServletResponse.SC_CREATED);
            List<Book> allBooks = bookService.getAllBooks();
            Book createdBook = allBooks.isEmpty() ? null : allBooks.get(allBooks.size() - 1);
            BookDTO createdBookDTO = BookMapper.toDTO(createdBook);
            RestResponse<?> restResponse = RestResponse.builder()
                .status(201)
                .message("Book created")
                .data(createdBookDTO)
                .build();
            sendResponse(response, restResponse);

        } catch (Exception e) {
            log.error("Error in POST", e);
            try {
                sendErrorResponse(response, 500, "Error creating book");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();
            
            String requestBody = readRequestBody(request);
            if (requestBody.isEmpty()) {
                sendErrorResponse(response, 400, "Request body required");
                return;
            }

            JsonSerializer<BookDTO> serializer = new JsonSerializer<>();
            BookDTO updatedBookDTO = serializer.deserialize(requestBody, BookDTO.class);
            Book updatedBook = BookMapper.fromDTO(updatedBookDTO);

            if (updatedBook.getId() <= 0) {
                sendErrorResponse(response, 400, "Book ID required");
                return;
            }

            Book existing = bookService.getBookById(updatedBook.getId());
            if (existing == null) {
                sendErrorResponse(response, 404, "Book not found");
                return;
            }

            List<String> errors = bookService.validateBook(
                updatedBook.getTitle(), updatedBook.getAuthor(), 
                updatedBook.getDate(), updatedBook.getSynopsis()
            );
            if (!errors.isEmpty()) {
                sendErrorResponse(response, 400, "Validation failed: " + String.join(", ", errors));
                return;
            }

            String genresStr = bookService.serialiseGenres(updatedBook.getGenres());
            String charactersStr = bookService.serialiseCharacters(updatedBook.getCharacters());
            
            bookService.updateBook(
                updatedBook.getId(), updatedBook.getTitle(), updatedBook.getAuthor(),
                updatedBook.getDate(), genresStr, 
                charactersStr, updatedBook.getSynopsis()
            );

            Book resultBook = bookService.getBookById(updatedBook.getId());
            BookDTO resultBookDTO = BookMapper.toDTO(resultBook);
            RestResponse<?> restResponse = RestResponse.builder()
                .status(200)
                .message("Book updated")
                .data(resultBookDTO)
                .build();
            sendResponse(response, restResponse);

        } catch (Exception e) {
            log.error("Error in PUT", e);
            try {
                sendErrorResponse(response, 500, "Error updating book");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();
            
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                sendErrorResponse(response, 400, "Book ID required");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                sendErrorResponse(response, 400, "Invalid ID format");
                return;
            }

            Book existing = bookService.getBookById(id);
            if (existing == null) {
                sendErrorResponse(response, 404, "Book not found");
                return;
            }

            bookService.deleteBook(id);
            response.setStatus(HttpServletResponse.SC_OK);
            RestResponse<?> restResponse = RestResponse.builder()
                .status(200)
                .message("Book deleted")
                .data(null)
                .build();
            sendResponse(response, restResponse);

        } catch (Exception e) {
            log.error("Error in DELETE", e);
            try {
                sendErrorResponse(response, 500, "Error deleting book");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }

    private RestResponse<?> getBookById(BookService service, String idParam) {
        try {
            int id = Integer.parseInt(idParam);
            Book book = service.getBookById(id);
            if (book == null) {
                return RestResponse.builder()
                    .status(404).message("Not found").data(null).build();
            }
            BookDTO bookDTO = BookMapper.toDTO(book);
            return RestResponse.builder()
                .status(200).message("Book retrieved").data(bookDTO).build();
        } catch (NumberFormatException e) {
            return RestResponse.builder()
                .status(400).message("Invalid ID").data(null).build();
        }
    }

    private RestResponse<?> searchBooks(BookService service, String searchTerm, int page, int size) {
        List<Book> results;
        if (size < Integer.MAX_VALUE) {
            results = service.searchBookSummariesPaginated(searchTerm, page, size);
        } else {
            results = service.searchPaginatedBooksList(searchTerm, page, size);
        }
        List<BookDTO> resultsDTO = BookMapper.toDTOList(results);
        return RestResponse.builder()
            .status(200)
            .message("Found " + results.size() + " book(s)")
            .data(resultsDTO)
            .build();
    }

    private RestResponse<?> getAllBooks(BookService service, int page, int size) {
        List<Book> books;
        if (size < Integer.MAX_VALUE) {
            books = service.getPaginatedBookSummaries(page, size);
        } else {
            books = service.getAllBooks();
        }
        List<BookDTO> booksDTO = BookMapper.toDTOList(books);
        return RestResponse.builder()
            .status(200)
            .message("Books retrieved")
            .data(booksDTO)
            .build();
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void sendResponse(HttpServletResponse response, RestResponse<?> restResponse) throws IOException {
        String acceptHeader = response.getHeader("Accept");
        RESTFormatNegotiator.ResponseFormat format = RESTFormatNegotiator.negotiateFormat(acceptHeader);
        ResponseSerializer<Object> serializer = RESTFormatNegotiator.getSerializer(format);
        
        String body = serializer.serialise((RestResponse<Object>) restResponse);
        response.setContentType(format.getMimeType() + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(restResponse.getStatus());
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(body);
            writer.flush();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        RestResponse<?> errorResponse = RestResponse.builder()
            .status(status).message(message).data(null).build();
        sendResponse(response, errorResponse);
    }
}

