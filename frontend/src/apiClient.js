import { mediaTypeSelector, serializeBook, parseResponse } from "./serialization";
import { BASE_URL } from "./config";

function logRequestResponse(loglabel, request) {
    try {
        console.groupCollapsed(`[API] ${loglabel}`);
        console.log("Request:", {
            method: request.method,
            url: request.url,
            headers: request.headers,
            bodyPreview: typeof request.body === "string" ? request.body.slice(0, 400) : request.body ?? null,
        });
        console.log("Response preview:", request.responsePreview ?? request.response);
        console.groupEnd();
    } catch (error) {
        console.warn("logging failed", error);
    }
}

function buildHeaders(requestType, responseType) {
    const headers = { Accept: mediaTypeSelector(responseType) };
    if (requestType && requestType !== "text") {
        headers["Content-Type"] = mediaTypeSelector(requestType);
    }
    return headers;
}

function normalizeListPayload(requestPayload) {
    if (requestPayload == null) return [];
    if (Array.isArray(requestPayload)) return requestPayload;

    if (requestPayload.items && Array.isArray(requestPayload.items)) return requestPayload.items;
    if (requestPayload.data && Array.isArray(requestPayload.data)) return requestPayload.data;
    if (requestPayload.data && requestPayload.data.items && Array.isArray(requestPayload.data.items)) {
        return requestPayload.data.items;
    }
    if (requestPayload.BookDTO) {
        return Array.isArray(requestPayload.BookDTO)
            ? requestPayload.BookDTO
            : [requestPayload.BookDTO];
    }
    if (requestPayload.book) {
        return Array.isArray(requestPayload.book)
            ? requestPayload.book
            : [requestPayload.book];
    }
    if (requestPayload.books) {
        if (Array.isArray(requestPayload.books)) return requestPayload.books;
        if (requestPayload.books.BookDTO && Array.isArray(requestPayload.books.BookDTO)) {
            return requestPayload.books.BookDTO;
        }
        if (requestPayload.books.book && Array.isArray(requestPayload.books.book)) {
            return requestPayload.books.book;
        }
    }
    if (requestPayload.BookList && requestPayload.BookList.BookDTO) {
        return Array.isArray(requestPayload.BookList.BookDTO)
            ? requestPayload.BookList.BookDTO
            : [requestPayload.BookList.BookDTO];
    }
    if (requestPayload.BookDTOs && requestPayload.BookDTOs.BookDTO) {
        return Array.isArray(requestPayload.BookDTOs.BookDTO)
            ? requestPayload.BookDTOs.BookDTO
            : [requestPayload.BookDTOs.BookDTO];
    }
    if (requestPayload.data && requestPayload.data.BookDTO) {
        return Array.isArray(requestPayload.data.BookDTO)
            ? requestPayload.data.BookDTO
            : [requestPayload.data.BookDTO];
    }
    if (typeof requestPayload === "object" && (requestPayload.id || requestPayload.title)) {
        return [requestPayload];
    }
    if (typeof requestPayload === "string") {
        try {
            const parsed = JSON.parse(requestPayload);
            return normalizeListPayload(parsed);
        } catch {
            return [];
        }
    }
    if (typeof requestPayload === "object") {
        const arrVal = Object.values(requestPayload).find((v) => Array.isArray(v));
        if (arrVal) return arrVal;
    }
    return [];
}

async function doFetch(url, options, responseType) {
    const request = { method: options.method, url, headers: options.headers, body: options.body };
    const response = await fetch(url, options);

    const responseText = await response.text();
    request.responsePreview = responseText.slice(0, 400);

    let parsedValue;
    try {
        switch (responseType) {
            case "json":
                parsedValue = responseText ? JSON.parse(responseText) : null;
                break;
            case "xml":
                parsedValue = await parseResponse({ text: async () => responseText }, "xml");
                break;
            case "yaml":
                parsedValue = await parseResponse({ text: async () => responseText }, "yaml");
                break;
            default:
                try {
                    parsedValue = responseText ? JSON.parse(responseText) : null;
                } catch {
                    parsedValue = responseText;
                }
        }
    } catch {
        parsedValue = responseText;
    }
    request.response = parsedValue;
    logRequestResponse(`${options.method} ${url}`, request);

    if (!response.ok) {
        const serverResponseMessage =
            (parsedValue && (parsedValue.message || parsedValue.error || parsedValue.statusText)) ||
            (typeof parsedValue === "string" && parsedValue) ||
            `HTTP ${response.status}`;

        const errorResponse = new Error(serverResponseMessage);
        errorResponse.status = response.status;
        errorResponse.response = parsedValue;
        throw errorResponse;
    }
    return parsedValue;
}

/**
 * fetchAllBooks(responseType) returns a array of books.
 */
export async function fetchAllBooks(responseType = "json") {
    const url = `${BASE_URL}/`;
    const headers = buildHeaders("json", responseType);
    const parsedRequest = await doFetch(url, { method: "GET", headers }, responseType);
    return normalizeListPayload(parsedRequest);
}

export async function fetchBookById(idOrObj, responseType = "json") {
    const id = typeof idOrObj === "object" ? idOrObj.id : idOrObj;
    if (id == null) throw new Error("fetchBookById: missing id");

    const params = new URLSearchParams();
    params.set("id", String(id));

    const url = `${BASE_URL}/?${params.toString()}`;
    const headers = buildHeaders("json", responseType);
    const parsedRequest = await doFetch(url, { method: "GET", headers }, responseType);

    if (Array.isArray(parsedRequest)) return parsedRequest[0] ?? null;
    if (parsedRequest && parsedRequest.data) return parsedRequest.data;
    return parsedRequest;
}

export async function createBook(book, requestType = "json", responseType = "json") {
    const requestBody = serializeBook(book, requestType);
    const url = `${BASE_URL}/`;
    const requestHeaders = buildHeaders(requestType, responseType);
    return await doFetch(url, { method: "POST", headers: requestHeaders, body: requestBody }, responseType);
}

export async function updateBook(book, requestType = "json", responseType = "json") {
    const requestBody = serializeBook(book, requestType);
    const url = `${BASE_URL}/`;
    const requestHeaders = buildHeaders(requestType, responseType);
    return await doFetch(url, { method: "PUT", headers: requestHeaders, body: requestBody }, responseType);
}

export async function deleteBook(idOrObj, responseType = "json") {
    const id = typeof idOrObj === "object" ? idOrObj.id : idOrObj;
    if (id == null) throw new Error("deleteBook: missing id");

    const params = new URLSearchParams();
    params.set("id", String(id));

    const url = `${BASE_URL}/?${params.toString()}`;
    const requestHeaders = buildHeaders("json", responseType);
    return await doFetch(url, { method: "DELETE", headers: requestHeaders }, responseType);
}