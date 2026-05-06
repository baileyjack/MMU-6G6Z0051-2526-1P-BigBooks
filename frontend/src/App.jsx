// src/App.jsx
// Main app: loads all books, client-side pagination/search, visible API errors, and viewport-aware detail panel.

import { useEffect, useState, useMemo } from "react";
import {
    fetchAllBooks,
    fetchBookById,
    createBook,
    updateBook,
    deleteBook,
} from "./apiClient";

import HeaderBar from "./components/HeaderBar";
import BookTable from "./components/BookTable";
import BookDetailPanel from "./components/BookDetailPanel";
import BookFormModal from "./components/BookFormModal";

function setCookie(name, value, days = 365) {
    const expires = new Date(Date.now() + days * 864e5).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires}; path=/`;
}
function getCookie(name) {
    return document.cookie.split("; ").reduce((r, v) => {
        const parts = v.split("=");
        return parts[0] === name ? decodeURIComponent(parts.slice(1).join("=")) : r;
    }, "");
}

export default function App() {
    const [allBooks, setAllBooks] = useState(null);
    const [isLoadingAll, setIsLoadingAll] = useState(false);
    const [loadError, setLoadError] = useState(null);

    const [page, setPage] = useState(0);
    const [size, setSize] = useState(50);
    const [total, setTotal] = useState(0);

    const [selectedBook, setSelectedBook] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState("create");

    const [requestType, setRequestType] = useState(() => getCookie("requestType") || "json");
    const [responseType, setResponseType] = useState(() => getCookie("responseType") || "json");

    const [searchQuery, setSearchQuery] = useState("");
    const [sortMode, setSortMode] = useState("none");

    // visible API errors
    const [apiErrors, setApiErrors] = useState([]);
    const pushApiError = (err, title = "API Error") => {
        const id = Date.now() + Math.random();
        const message = err?.message || (err?.response && JSON.stringify(err.response)) || "Unknown error";
        const details = err?.response ?? null;
        setApiErrors((s) => [{ id, title, message, details }, ...s]);
        console.error("[API ERROR]", title, err);
    };
    const dismissApiError = (id) => setApiErrors((s) => s.filter((x) => x.id !== id));

    useEffect(() => setCookie("requestType", requestType), [requestType]);
    useEffect(() => setCookie("responseType", responseType), [responseType]);

    useEffect(() => {
        let mounted = true;
        (async () => {
            try {
                setLoadError(null);
                setIsLoadingAll(true);
                const items = await fetchAllBooks(responseType, "");
                if (!mounted) return;
                setAllBooks(Array.isArray(items) ? items : []);
                setTotal(Array.isArray(items) ? items.length : 0);
                setPage(0);
            } catch (e) {
                if (!mounted) return;
                setAllBooks([]);
                setTotal(0);
                setLoadError("Failed to load books");
                pushApiError(e, "Failed to load books");
            } finally {
                if (!mounted) return;
                setIsLoadingAll(false);
            }
        })();
        return () => { mounted = false; };
    }, [responseType]);

    const processedBooks = useMemo(() => {
        const base = Array.isArray(allBooks) ? allBooks : [];
        const q = (searchQuery || "").trim().toLowerCase();
        const searched = q
            ? base.filter((b) => {
                const title = (b?.title || "").toString().toLowerCase();
                const author = (b?.author || "").toString().toLowerCase();
                return title.includes(q) || author.includes(q);
            })
            : base;

        if (sortMode === "none") return searched;

        return [...searched].sort((a, b) => {
            const aTitle = (a?.title || "").toString();
            const bTitle = (b?.title || "").toString();
            const aDate = a?.date ? new Date(a.date) : new Date(0);
            const bDate = b?.date ? new Date(b.date) : new Date(0);

            switch (sortMode) {
                case "title-asc": return aTitle.localeCompare(bTitle);
                case "title-desc": return bTitle.localeCompare(aTitle);
                case "date-asc": return aDate - bDate;
                case "date-desc": return bDate - aDate;
                default: return 0;
            }
        });
    }, [allBooks, searchQuery, sortMode]);

    useEffect(() => {
        setTotal(processedBooks.length);
        const totalPages = Math.max(1, Math.ceil(processedBooks.length / size));
        if (page >= totalPages) setPage(0);
    }, [processedBooks, size]);

    const pagedBooks = useMemo(() => {
        const start = page * size;
        return processedBooks.slice(start, start + size);
    }, [processedBooks, page, size]);

    const handleSizeChange = (newSize) => { setSize(newSize); setPage(0); };
    const handleSearchChange = (v) => { setSearchQuery(v); setPage(0); };

    const handleSelectBook = async (book) => {
        if (!book) return;
        const cached = Array.isArray(allBooks) ? allBooks.find((b) => b?.id === book.id) : null;
        if (cached && (cached.genres || cached.synopsis || cached.characters)) {
            setSelectedBook(cached);
            return;
        }
        try {
            const full = await fetchBookById(book.id, responseType);
            setSelectedBook(full);
        } catch (e) {
            pushApiError(e, "Failed to fetch book details");
            setSelectedBook(book);
        }
    };

    const handleOpenEdit = async (bookId) => {
        try {
            const full = await fetchBookById(bookId, responseType);
            setSelectedBook(full);
            setModalMode("edit");
            setIsModalOpen(true);
        } catch (e) {
            pushApiError(e, "Failed to load book for editing");
            console.log("Failed to load book for editing");
        }
    };

    const handleCreateBook = async (book) => {
        try {
            await createBook(book, requestType, responseType);
            setIsLoadingAll(true);
            const items = await fetchAllBooks(responseType, "");
            setAllBooks(Array.isArray(items) ? items : []);
            setIsLoadingAll(false);
            setIsModalOpen(false);
        } catch (e) {
            pushApiError(e, "Failed to create book");
            console.log("Failed to create book");
        }
    };

    const handleUpdateBook = async (book) => {
        try {
            console.log("[App] Updating book payload:", book, "requestType:", requestType, "responseType:", responseType);
            await updateBook(book, requestType, responseType);
            setIsModalOpen(false);
            setSelectedBook(null);
            setIsLoadingAll(true);
            const items = await fetchAllBooks(responseType, "");
            setAllBooks(Array.isArray(items) ? items : []);
            setIsLoadingAll(false);
        } catch (e) {
            pushApiError(e, "Failed to update book");
            console.log("Failed to update book");
        }
    };

    const handleDeleteBook = async (id) => {
        try {
            await deleteBook(id, responseType);
            setSelectedBook(null);
            setIsLoadingAll(true);
            const items = await fetchAllBooks(responseType, "");
            setAllBooks(Array.isArray(items) ? items : []);
            setIsLoadingAll(false);
        } catch (e) {
            pushApiError(e, "Failed to delete book");
            console.log("Failed to delete book");
        }
    };

    return (
        <div className={`main-layout ${selectedBook ? "detail-open" : ""}`}>
            <HeaderBar
                searchQuery={searchQuery}
                onSearchChange={handleSearchChange}
                requestType={requestType}
                responseType={responseType}
                onRequestTypeChange={(t) => setRequestType(t)}
                onResponseTypeChange={(t) => setResponseType(t)}
                onAddBook={() => { setModalMode("create"); setIsModalOpen(true); }}
                sortMode={sortMode}
                onSortChange={setSortMode}
            />

            {/* Floating API error list */}
            <div style={{
                position: "fixed",
                top: "calc(var(--header-height) + 12px)",
                right: "12px",
                zIndex: 1200,
                maxWidth: "420px",
            }}>
                {apiErrors.map((err) => (
                    <div key={err.id} style={{
                        background: "#fff7f7",
                        border: "1px solid #fecaca",
                        color: "#7f1d1d",
                        padding: "0.75rem 0.9rem",
                        borderRadius: 8,
                        boxShadow: "0 6px 18px rgba(15,23,42,0.08)",
                        marginBottom: "0.6rem",
                        fontSize: "0.95rem",
                    }}>
                        <div style={{ display: "flex", justifyContent: "space-between", gap: "0.5rem" }}>
                            <strong>{err.title}</strong>
                            <button onClick={() => dismissApiError(err.id)} style={{
                                border: "none",
                                background: "transparent",
                                cursor: "pointer",
                                color: "#7f1d1d",
                                fontWeight: 700,
                            }}>✕</button>
                        </div>
                        <div style={{ marginTop: "0.45rem", color: "#4b1f1f" }}>{err.message}</div>
                        {err.details && typeof err.details === "object" && (
                            <details style={{ marginTop: "0.5rem", color: "#6b7280" }}>
                                <summary style={{ cursor: "pointer" }}>Details</summary>
                                <pre style={{ whiteSpace: "pre-wrap", fontSize: "0.85rem" }}>
                  {JSON.stringify(err.details, null, 2)}
                </pre>
                            </details>
                        )}
                    </div>
                ))}
            </div>

            {loadError && <div style={{ color: "red", marginBottom: "1rem" }}>{loadError}</div>}

            {isLoadingAll && <div style={{ margin: "0.5rem 0", color: "#374151" }}>Loading all books...</div>}

            <BookTable
                books={pagedBooks}
                page={page}
                size={size}
                total={total}
                onPageChange={setPage}
                onSizeChange={handleSizeChange}
                onSelectBook={handleSelectBook}
            />

            <BookDetailPanel
                book={selectedBook}
                onClose={() => setSelectedBook(null)}
                onEdit={(id) => { if (id != null) handleOpenEdit(id); }}
                onDelete={(id) => { console.log("[App] onDelete called with id:", id); if (id != null) handleDeleteBook(id); }}
            />

            {isModalOpen && (
                <BookFormModal
                    mode={modalMode}
                    book={modalMode === "edit" ? selectedBook : null}
                    onClose={() => setIsModalOpen(false)}
                    onSubmit={modalMode === "edit" ? handleUpdateBook : handleCreateBook}
                />
            )}
        </div>
    );
}
