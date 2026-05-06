// src/apiClient.js
// API client with JSON / XML / YAML support, logging and robust normalization.

import { mediaTypeToMime, serializeBook, parseResponse } from "./serialization";
import { BASE_URL } from "./config";

function logRequestResponse(label, req) {
    try {
        console.groupCollapsed(`[API] ${label}`);
        console.log("Request:", {
            method: req.method,
            url: req.url,
            headers: req.headers,
            bodyPreview: typeof req.body === "string" ? req.body.slice(0, 400) : req.body ?? null,
        });
        console.log("Response preview:", req.responsePreview ?? req.response);
        console.groupEnd();
    } catch (e) {
        console.warn("[API] logging failed", e);
    }
}

function buildHeaders(requestType, responseType) {
    const headers = { Accept: mediaTypeToMime(responseType) };
    if (requestType && requestType !== "text") headers["Content-Type"] = mediaTypeToMime(requestType);
    return headers;
}

/**
 * Try to extract an array of book objects from many possible payload shapes.
 * XML-to-JS conversions can produce many shapes; this function is intentionally
 * permissive and tries several heuristics.
 */
function normalizeListPayload(payload) {
    if (payload == null) return [];

    // Already an array
    if (Array.isArray(payload)) return payload;

    // Common wrappers
    if (payload.items && Array.isArray(payload.items)) return payload.items;
    if (payload.data && Array.isArray(payload.data)) return payload.data;
    if (payload.data && payload.data.items && Array.isArray(payload.data.items)) return payload.data.items;

    // XML -> JS common shapes:
    // - { BookDTO: {...} } or { BookDTO: [ {...}, {...} ] }
    // - { books: { BookDTO: [...] } } or { books: { book: [...] } }
    // - { BookList: { BookDTO: [...] } }
    // - { BookDTOs: { BookDTO: [...] } }
    // - { data: { BookDTO: [...] } }
    // - { book: [...] } or { books: [...] }
    if (payload.BookDTO) {
        if (Array.isArray(payload.BookDTO)) return payload.BookDTO;
        return [payload.BookDTO];
    }

    if (payload.book) {
        if (Array.isArray(payload.book)) return payload.book;
        return [payload.book];
    }

    if (payload.books) {
        // books could be an array or an object with BookDTO inside
        if (Array.isArray(payload.books)) return payload.books;
        if (payload.books.BookDTO && Array.isArray(payload.books.BookDTO)) return payload.books.BookDTO;
        if (payload.books.book && Array.isArray(payload.books.book)) return payload.books.book;
    }

    if (payload.BookList && payload.BookList.BookDTO) {
        if (Array.isArray(payload.BookList.BookDTO)) return payload.BookList.BookDTO;
        return [payload.BookList.BookDTO];
    }

    if (payload.BookDTOs && payload.BookDTOs.BookDTO) {
        if (Array.isArray(payload.BookDTOs.BookDTO)) return payload.BookDTOs.BookDTO;
        return [payload.BookDTOs.BookDTO];
    }

    if (payload.data && payload.data.BookDTO) {
        if (Array.isArray(payload.data.BookDTO)) return payload.data.BookDTO;
        return [payload.data.BookDTO];
    }

    // If payload looks like a single book object (has id or title), return single-element array
    if (typeof payload === "object" && (payload.id || payload.title)) return [payload];

    // If payload is a string, try to parse JSON and re-run heuristics
    if (typeof payload === "string") {
        try {
            const parsed = JSON.parse(payload);
            return normalizeListPayload(parsed);
        } catch (e) {
            // not JSON — give up
            return [];
        }
    }

    // As a last attempt, look for any array-valued property and return the first one
    if (typeof payload === "object") {
        const arrVal = Object.values(payload).find((v) => Array.isArray(v));
        if (arrVal) return arrVal;
    }

    return [];
}

async function doFetch(url, options, responseType) {
    const req = { method: options.method, url, headers: options.headers, body: options.body };
    const res = await fetch(url, options);

    const text = await res.text();
    req.responsePreview = text.slice(0, 400);

    let parsed;
    try {
        switch (responseType) {
            case "json":
                parsed = text ? JSON.parse(text) : null;
                break;
            case "xml":
                parsed = await parseResponse({ text: async () => text }, "xml");
                break;
            case "yaml":
                parsed = await parseResponse({ text: async () => text }, "yaml");
                break;
            default:
                // fallback: try JSON parse, otherwise raw text
                try {
                    parsed = text ? JSON.parse(text) : null;
                } catch (e) {
                    parsed = text;
                }
        }
    } catch (e) {
        // If parsing fails, keep raw text so logs show something useful
        parsed = text;
    }

    req.response = parsed;
    logRequestResponse(`${options.method} ${url}`, req);

    if (!res.ok) {
        const serverMsg =
            (parsed && (parsed.message || parsed.error || parsed.statusText)) ||
            (typeof parsed === "string" && parsed) ||
            `HTTP ${res.status}`;
        const err = new Error(serverMsg);
        err.status = res.status;
        err.response = parsed;
        throw err;
    }

    return parsed;
}

/**
 * fetchBooks(page, size, responseType, q)
 * - If page is null/undefined -> GET /books/ (all books)
 * - If page is provided -> GET /books?page=X&size=Y (paginated)
 * Returns { items, total } for paginated calls, and { items, total } for all-books call too.
 */
export async function fetchBooks(page, size, responseType = "json", q = "") {
    if (page === undefined || page === null) {
        const url = `${BASE_URL}/`;
        const headers = buildHeaders("json", responseType);
        const parsed = await doFetch(url, { method: "GET", headers }, responseType);
        const items = normalizeListPayload(parsed);
        return { items, total: items.length };
    }

    const params = new URLSearchParams();
    params.set("page", page);
    params.set("size", size);
    if (q) params.set("q", q);

    const url = `${BASE_URL}/?${params.toString()}`;
    const headers = buildHeaders("json", responseType);
    const parsed = await doFetch(url, { method: "GET", headers }, responseType);

    if (parsed && typeof parsed === "object") {
        if (Array.isArray(parsed.items) || typeof parsed.total === "number") {
            return {
                items: Array.isArray(parsed.items) ? parsed.items : [],
                total: typeof parsed.total === "number" ? parsed.total : parsed.items?.length ?? null,
            };
        }
        if (parsed.data) {
            const items = Array.isArray(parsed.data) ? parsed.data : parsed.data.items ?? [];
            const total = typeof parsed.total === "number" ? parsed.total : parsed.data.total ?? null;
            return { items, total };
        }
    }

    if (Array.isArray(parsed)) return { items: parsed, total: parsed.length };

    // If parsed is a single object, try to normalize it into an array
    const items = normalizeListPayload(parsed);
    return { items, total: items.length };
}

export async function fetchAllBooks(responseType = "json", q = "") {
    const url = `${BASE_URL}/`;
    const headers = buildHeaders("json", responseType);
    const parsed = await doFetch(url, { method: "GET", headers }, responseType);
    let items = normalizeListPayload(parsed);
    if (q) {
        const ql = q.toLowerCase();
        items = items.filter(
            (b) =>
                (b?.title || "").toString().toLowerCase().includes(ql) ||
                (b?.author || "").toString().toLowerCase().includes(ql)
        );
    }
    return items;
}

export async function fetchBookById(idOrObj, responseType = "json") {
    const id = idOrObj && typeof idOrObj === "object" ? idOrObj.id : idOrObj;
    if (id == null) throw new Error("fetchBookById: missing id");
    const params = new URLSearchParams();
    params.set("id", String(id));
    const url = `${BASE_URL}/?${params.toString()}`;
    const headers = buildHeaders("json", responseType);
    const parsed = await doFetch(url, { method: "GET", headers }, responseType);
    if (Array.isArray(parsed)) return parsed[0] ?? null;
    if (parsed && parsed.data) return parsed.data;
    return parsed;
}

export async function createBook(book, requestType = "json", responseType = "json") {
    const body = serializeBook(book, requestType);
    const url = `${BASE_URL}/`;
    const headers = buildHeaders(requestType, responseType);
    const parsed = await doFetch(url, { method: "POST", headers, body }, responseType);
    return parsed;
}

export async function updateBook(book, requestType = "json", responseType = "json") {
    const body = serializeBook(book, requestType);
    const url = `${BASE_URL}/`;
    const headers = buildHeaders(requestType, responseType);
    const parsed = await doFetch(url, { method: "PUT", headers, body }, responseType);
    return parsed;
}

export async function deleteBook(idOrObj, responseType = "json") {
    const id = idOrObj && typeof idOrObj === "object" ? idOrObj.id : idOrObj;
    if (id == null) throw new Error("deleteBook: missing id");
    const params = new URLSearchParams();
    params.set("id", String(id));
    const url = `${BASE_URL}/?${params.toString()}`;
    const headers = buildHeaders("json", responseType);
    const parsed = await doFetch(url, { method: "DELETE", headers }, responseType);
    return parsed;
}
