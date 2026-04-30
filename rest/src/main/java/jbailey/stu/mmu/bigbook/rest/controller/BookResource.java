package jbailey.stu.mmu.bigbook.rest.controller;

import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.rest.format.RESTFormatNegotiator;
import jbailey.stu.mmu.bigbook.rest.model.BookDTO;
import jbailey.stu.mmu.bigbook.rest.model.BookMapper;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;
import jbailey.stu.mmu.bigbook.rest.serialize.ResponseSerializer;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * REST API servlet for /books.
 *
 * Supports JSON, XML, YAML and TEXT depending on Accept and Content-Type headers.
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Attempting a GET request to get books from Book Resource API.");
        log.debug(request.getParameterMap().toString());
        try {
            BookService bookService = servicePort.getBookService();
            String body = readRequestBody(request);
            log.debug("Raw request body:\n{}", body);

            if (body == null || body.trim().isEmpty()) {
                sendErrorResponse(request, response, 400, "Request body required");
                return;
            }
            if (!body.contains("\n") && body.contains(":")) {
                sendErrorResponse(request, response, 400, "Malformed YAML: missing line breaks. Raw body: " + body.substring(0, Math.min(200, body.length())));
                return;
            }

            RestResponse<?> restResponse;

            String idParam = request.getParameter("id");
            String searchParam = request.getParameter("search");
            String pageParam = request.getParameter("page");
            String sizeParam = request.getParameter("size");

            int page = parseIntOrDefault(pageParam, 0);
            int size = parseIntOrDefault(sizeParam, Integer.MAX_VALUE);

            if (idParam != null && !idParam.isEmpty()) {
                log.info("GET requested with a given ID parameter: " + idParam);
                restResponse = getBookById(bookService, idParam);
            } else if (searchParam != null && !searchParam.isEmpty()) {
                log.info("GET requested with a search parameter: " + searchParam);
                restResponse = searchBooks(bookService, searchParam, page, size);
            } else {
                log.info("GET requested. Retrieving all books with pagination - page: {}, size: {}", page, size);
                restResponse = getAllBooks(bookService, page, size);
            }

            log.info("GET request successful. Sending response. Status code: {}", restResponse.getStatus());
            sendResponse(request, response, restResponse);

        } catch (Exception e) {
            log.error("Error on GET", e);
            sendErrorResponse(request, response, 500, "Error retrieving books");
        }
    }

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

            // Determine how to parse the incoming body
            log.info("POST requested body type: {}", request.getHeader("Content-Type"));
            ResponseSerializer<BookDTO> inputSerializer = getRequestSerializer(request);
            if (inputSerializer == null) {
                sendErrorResponse(request, response, 400, "Unsupported Content-Type");
                return;
            }

            log.info("Body will be deserialised to BookDTO: {}", body);
            BookDTO dto = inputSerializer.deserialize(body, BookDTO.class);
            Book newBook = BookMapper.fromDTO(dto);

            log.info("Checking book for errors: {}", newBook);
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

            // Retrieve the newly created book
            List<Book> all = bookService.getAllBooks();
            Book created = all.get(all.size() - 1);

            RestResponse<?> restResponse = RestResponse.builder()
                    .status(201)
                    .message("Book created")
                    .data(BookMapper.toDTO(created))
                    .build();

            sendResponse(request, response, restResponse);

        } catch (Exception e) {
            log.error("POST error", e);
            sendErrorResponse(request, response, 500, "Error creating book");
        }
    }

    // -------------------------------------------------------------------------
    // PUT
    // -------------------------------------------------------------------------
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();

            String body = readRequestBody(request);
            log.debug("Raw request body:\n{}", body);

            if (body == null || body.trim().isEmpty()) {
                sendErrorResponse(request, response, 400, "Request body required");
                return;
            }

            ResponseSerializer<BookDTO> inputSerializer = getRequestSerializer(request);
            if (inputSerializer == null) {
                sendErrorResponse(request, response, 400, "Unsupported Content-Type");
                return;
            }

            BookDTO dto = inputSerializer.deserialize(body, BookDTO.class);
            Book updated = BookMapper.fromDTO(dto);

            if (updated.getId() <= 0) {
                sendErrorResponse(request, response, 400, "Book ID required");
                return;
            }

            Book existing = bookService.getBookById(updated.getId());
            if (existing == null) {
                sendErrorResponse(request, response, 404, "Book not found");
                return;
            }

            List<String> errors = bookService.validateBook(
                    updated.getTitle(), updated.getAuthor(),
                    updated.getDate(), updated.getSynopsis()
            );

            if (!errors.isEmpty()) {
                sendErrorResponse(request, response, 400, "Validation failed: " + String.join(", ", errors));
                return;
            }

            bookService.updateBook(
                    updated.getId(),
                    updated.getTitle(),
                    updated.getAuthor(),
                    updated.getDate(),
                    bookService.serialiseGenres(updated.getGenres()),
                    bookService.serialiseCharacters(updated.getCharacters()),
                    updated.getSynopsis()
            );

            Book result = bookService.getBookById(updated.getId());

            RestResponse<?> restResponse = RestResponse.builder()
                    .status(200)
                    .message("Book updated")
                    .data(BookMapper.toDTO(result))
                    .build();

            sendResponse(request, response, restResponse);

        } catch (Exception e) {
            log.error("PUT error", e);
            sendErrorResponse(request, response, 500, "Error updating book");
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookService bookService = servicePort.getBookService();

            String idParam = request.getParameter("id");
            if (idParam == null) {
                sendErrorResponse(request, response, 400, "Book ID required");
                return;
            }

            int id = parseIntOrDefault(idParam, -1);
            if (id <= 0) {
                sendErrorResponse(request, response, 400, "Invalid ID");
                return;
            }

            Book existing = bookService.getBookById(id);
            if (existing == null) {
                sendErrorResponse(request, response, 404, "Book not found");
                return;
            }

            bookService.deleteBook(id);

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

    private ResponseSerializer<BookDTO> getRequestSerializer(HttpServletRequest request) {
        String contentType = request.getHeader("Content-Type");
        RESTFormatNegotiator.ResponseFormat format = RESTFormatNegotiator.negotiateFormat(contentType);
        return RESTFormatNegotiator.getSerializer(format);
    }

    private void sendResponse(HttpServletRequest request, HttpServletResponse response, RestResponse<?> restResponse)
            throws IOException {

        String accept = request.getHeader("Accept");
        RESTFormatNegotiator.ResponseFormat format = RESTFormatNegotiator.negotiateFormat(accept);

        ResponseSerializer<Object> serializer = RESTFormatNegotiator.getSerializer(format);
        String body = serializer.serialise((RestResponse<Object>) restResponse);

        response.setStatus(restResponse.getStatus());
        response.setContentType(format.getMimeType() + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(body);
        }
    }

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
