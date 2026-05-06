package jbailey.stu.mmu.bigbook.rest.controller;

// INternal Imports
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.rest.format.RESTFormatNegotiator;
import jbailey.stu.mmu.bigbook.rest.model.BookDTO;
import jbailey.stu.mmu.bigbook.rest.model.BookMapper;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;
import jbailey.stu.mmu.bigbook.rest.serialize.ResponseSerializer;

// Java Imports
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

// External Imports
import lombok.extern.slf4j.Slf4j;


/**
 * REST API servlet for /books.
 *
 * Supports JSON, XML, YAML depending on Accept and Content-Type headers.
 */
@Slf4j
public class BookResource extends HttpServlet {

    private final ServicePort servicePort;

    public BookResource(ServicePort servicePort) {
        this.servicePort = servicePort;
    }

    @Override
    public void init() throws ServletException {
        log.info("BookResource API Controller Handler has been initialised successfully.");
    }

    /**
     * Overrides GET with a request and a response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Attempting a GET request to get books from the Books API.");
        log.debug(request.getParameterMap().toString());

        try {
            BookService bookService = servicePort.getBookService();
            RestResponse<?> restResponse;
            String idParameter = request.getParameter("id");
            String searchParameter = request.getParameter("search");
            String pageParameter = request.getParameter("page");
            String sizeParameter = request.getParameter("size");

            int pageNo = parseIntOrDefault(pageParameter, 0);
            int sizeOfPages = parseIntOrDefault(sizeParameter, Integer.MAX_VALUE);

            if (idParameter != null && !idParameter.isEmpty()) {
                log.info("GET requested with a given ID parameter: {}", idParameter);
                restResponse = getBookById(bookService, idParameter);
            } else if (searchParameter != null && !searchParameter.isEmpty()) {
                log.info("GET requested with a search: {}", searchParameter);
                restResponse = searchBooks(bookService, searchParameter, pageNo, sizeOfPages);
            } else {
                log.info("GET requested. Retrieving all books with pagination with details: page: {}, size: {}", pageNo, sizeOfPages);
                restResponse = getAllBooks(bookService, pageNo, sizeOfPages);
            }
            log.info("GET request successful. Sending response. Status code: {}", restResponse.getStatus());
            sendResponse(request, response, restResponse);

        } catch (Exception exception) {
            log.error("Error on GET", exception);
            sendErrorResponse(request, response, 500, "Error retrieving books");
        }
    }

    /**
     * Overrides POST with a request and a response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Attempting a POST request to get books from Book Resource API.");
        try {
            BookService bookService = servicePort.getBookService();
            String body = readRequestBody(request);
            log.debug("Raw request body:\n{}", body);
            log.debug("Raw bytes: {}", Arrays.toString(body.getBytes(StandardCharsets.UTF_8)));

            if (body == null || body.trim().isEmpty()) {
                sendErrorResponse(request, response, 400, "Request body required");
                return;
            }

            // ? How to parse the incoming body
            log.info("POST requested body type: {}", request.getHeader("Content-Type"));
            ResponseSerializer<BookDTO> inputSerializer = getRequestSerializer(request);
            if (inputSerializer == null) {
                // Throw an error if we provided an incorrect content type which cannot be used.
                sendErrorResponse(request, response, 400, "Unsupported Content-Type");
                return;
            }

            log.info("Body will be deserialised to BookDTO: {}", body);
            BookDTO dto = inputSerializer.deserialize(body, BookDTO.class);
            Book newBook = BookMapper.fromDTO(dto);
            log.info("Checking book for errors: {}", newBook);

            // Get a list of errors and shwo them if there are, otherwise add the book to the DB.
            List<String> errors = bookService.validateBook(
                    newBook.getTitle(), newBook.getAuthor(),
                    newBook.getDate(), newBook.getSynopsis()
            );
            if (!errors.isEmpty()) {
                sendErrorResponse(request, response, 400, "Validation failed: " + String.join(", ", errors));
                return;
            }
            bookService.addBook(
                    newBook.getTitle(),
                    newBook.getAuthor(),
                    newBook.getDate(),
                    bookService.serialiseGenres(newBook.getGenres()),
                    bookService.serialiseCharacters(newBook.getCharacters()),
                    newBook.getSynopsis()
            );

            // Retrieve the created book
            List<Book> allBooks = bookService.getAllBooks();
            Book newCreatedBook = allBooks.get(allBooks.size() - 1); // Just get the last item int he list

            RestResponse<?> restResponse = RestResponse.builder()
                    .status(201)
                    .message("Book created")
                    .data(BookMapper.toDTO(newCreatedBook))
                    .build();

            sendResponse(request, response, restResponse);

        } catch (Exception e) {
            log.error("POST error", e);
            sendErrorResponse(request, response, 500, "Error creating book");
        }
    }

    /**
     * Overrides PUT with a request and a response
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();
            String body = readRequestBody(request);
            log.debug("Raw request body:\n{}", body);
            if (body == null || body.trim().isEmpty()) {
                // We require a body
                sendErrorResponse(request, response, 400, "Request body required");
                return;
            }

            ResponseSerializer<BookDTO> inputSerializer = getRequestSerializer(request);
            if (inputSerializer == null) {
                sendErrorResponse(request, response, 400, "Unsupported Content-Type");
                return;
            }

            BookDTO dtoObjectToUpdate = inputSerializer.deserialize(body, BookDTO.class);
            Book updatedBookDetail = BookMapper.fromDTO(dtoObjectToUpdate);

            if (updatedBookDetail.getId() <= 0) {
                sendErrorResponse(request, response, 400, "Book ID required");
                return;
            }
            Book bookToUpdate = bookService.getBookById(updatedBookDetail.getId());
            if (bookToUpdate == null) {
                sendErrorResponse(request, response, 404, "Book not found");
                return;
            }
            List<String> errors = bookService.validateBook(
                    updatedBookDetail.getTitle(), updatedBookDetail.getAuthor(),
                    updatedBookDetail.getDate(), updatedBookDetail.getSynopsis()
            );
            if (!errors.isEmpty()) {
                sendErrorResponse(request, response, 400, "Validation failed: " + String.join(", ", errors));
                return;
            }

            bookService.updateBook(
                    updatedBookDetail.getId(),
                    updatedBookDetail.getTitle(),
                    updatedBookDetail.getAuthor(),
                    updatedBookDetail.getDate(),
                    bookService.serialiseGenres(updatedBookDetail.getGenres()),
                    bookService.serialiseCharacters(updatedBookDetail.getCharacters()),
                    updatedBookDetail.getSynopsis()
            );
            Book resultingBookObject = bookService.getBookById(updatedBookDetail.getId());
            RestResponse<?> restResponse = RestResponse.builder()
                    .status(200)
                    .message("Book updated")
                    .data(BookMapper.toDTO(resultingBookObject))
                    .build();
            sendResponse(request, response, restResponse);
        } catch (Exception e) {
            log.error("PUT error", e);
            sendErrorResponse(request, response, 500, "Error updating book");
        }
    }

    /**
     * Overrides DELETE with a request and a response
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();

            String idParam = request.getParameter("id");
            if (idParam == null) {
                // Can't delete something that doesnt exist
                sendErrorResponse(request, response, 400, "Book ID required");
                return;
            }
            int id = parseIntOrDefault(idParam, -1);
            if (id <= 0) {
                // ID can't be negative
                sendErrorResponse(request, response, 400, "Invalid ID");
                return;
            }
            Book existing = bookService.getBookById(id);
            if (existing == null) {
                sendErrorResponse(request, response, 404, "Book not found");
                return;
            }
            bookService.deleteBook(id); // Simple deletion call
            RestResponse<?> restResponse = RestResponse.builder()
                    .status(200)
                    .message("Book deleted")
                    .data(null)
                    .build();
            sendResponse(request, response, restResponse);

        } catch (Exception e) {
            log.error("DELETE error", e);
            sendErrorResponse(request, response, 500, "Error deleting book");
        }
    }

    /**
     * Gets the serialiser based on the request header
     * @param request as the request to check the header from
     * @return a serialised for the bookDTO object
     */
    private ResponseSerializer<BookDTO> getRequestSerializer(HttpServletRequest request) {
        String contentType = request.getHeader("Content-Type");
        RESTFormatNegotiator.ResponseFormat format = RESTFormatNegotiator.negotiateFormat(contentType);
        return RESTFormatNegotiator.getSerializer(format);
    }

    /**
     * formats a RestResponse into JSON/XML/YAML based on the header +
     * sets the HTTP status and content type then writes the serialised body to the HTTP response.
     */
    private void sendResponse(HttpServletRequest request, HttpServletResponse response, RestResponse<?> restResponse)
            throws IOException {

        String accept = request.getHeader("Accept");
        RESTFormatNegotiator.ResponseFormat format = RESTFormatNegotiator.negotiateFormat(accept);
        ResponseSerializer<Object> serializer = RESTFormatNegotiator.getSerializer(format);
        String body = serializer.serialise((RestResponse<Object>) restResponse);
        response.setStatus(restResponse.getStatus());
        response.setContentType(format.getNegotiatedContentTyoe() + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(body);
        }
    }

    // Send error response :)
    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, int status, String message)
            throws IOException {

        RestResponse<?> error = RestResponse.builder()
                .status(status)
                .message(message)
                .data(null)
                .build();

        sendResponse(request, response, error);
    }

    private int parseIntOrDefault(String value, int def) {
        try {
            return value == null ? def : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Used to be a BufferedReader but it strips newlines which caused YAML issues so it now reads raw bytes.
     * @param request the HTTP Request to read body from
     * @return a String with the request body
     * @throws IOException if the request cannot be read.
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        byte[] bytes = request.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private RestResponse<?> getBookById(BookService service, String idParam) {
        int id = parseIntOrDefault(idParam, -1);
        if (id <= 0) {
            return RestResponse.builder().status(400).message("Invalid ID").data(null).build();
        }
        Book book = service.getBookById(id);
        if (book == null) {
            return RestResponse.builder().status(404).message("Not found").data(null).build();
        }
        return RestResponse.builder()
                .status(200)
                .message("Book retrieved")
                .data(BookMapper.toDTO(book))
                .build();
    }

    private RestResponse<?> searchBooks(BookService service, String term, int page, int size) {
        List<Book> results = (size < Integer.MAX_VALUE)
                ? service.searchBookSummariesPaginated(term, page, size)
                : service.searchPaginatedBooksList(term, page, size);
        return RestResponse.builder()
                .status(200)
                .message("Found " + results.size() + " book(s)")
                .data(BookMapper.toDTOList(results))
                .build();
    }

    private RestResponse<?> getAllBooks(BookService service, int page, int size) {
        List<Book> books = (size < Integer.MAX_VALUE)
                ? service.getPaginatedBookSummaries(page, size)
                : service.getAllBooks();
        return RestResponse.builder()
                .status(200)
                .message("Books retrieved")
                .data(BookMapper.toDTOList(books))
                .build();
    }
}
