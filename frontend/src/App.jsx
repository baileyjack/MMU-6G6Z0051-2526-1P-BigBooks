import {useEffect, useState, useMemo, Component} from "react";
import {fetchAllBooks, fetchBookById, createBook, updateBook, deleteBook,} from "./apiClient";
import HeaderBar from "./components/HeaderBar";
import BookTable from "./components/BookTable";
import BookDetailPanel from "./components/BookDetailPanel";
import BookForm from "./components/BookForm.jsx";

function setCookie(cookieName, value, livenessDays = 365) {
    const expires = new Date(Date.now() + livenessDays * 864e5).toUTCString();
    document.cookie = `${cookieName}=${encodeURIComponent(value)}; expires=${expires}; path=/`;
}

/**
 * Would you like a cookie? :)
 * @param cookiedName
 * @returns {string}
 */
function getCookie(cookiedName) {
    return document.cookie.split("; ").reduce((r, v) => {
        const parts = v.split("=");
        return parts[0] === cookiedName ? decodeURIComponent(parts.slice(1).join("=")) : r;
    }, "");
}

export default function App() {
    const [allBooks, setAllBooks] = useState(null);
    const [isLoadingAll, setIsLoadingAll] = useState(false);
    const [loadingError, setLoadingError] = useState(null);

    const [pageNumber, setPageNumber] = useState(0);
    const [pageSize, setPageSize] = useState(50);
    const [total, setTotal] = useState(0);

    const [selectedBook, setSelectedBook] = useState(null);
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [formMode, setFormMode] = useState("create");
    const [HTTPRequestType, setHTTPRequestType] = useState(() => getCookie("requestType") || "json");
    const [HTTPResponseType, setHTTPResponseType] = useState(() => getCookie("responseType") || "json");

    const [searchQuery, setSearchQuery] = useState("");
    const [sortByMode, setSortByMode] = useState("none");

    // visible API errors
    const [apiErrors, setApiErrors] = useState([]);
    const pushApiError = (apiError, title = "API Error") => {
        const errorID = Date.now() + Math.random();
        const errorMessage = apiError?.message || (apiError?.response && JSON.stringify(apiError.response)) || "Unknown error";
        const errorDetails = apiError?.response ?? null;
        setApiErrors((errorStr) => [{ id: errorID, title, message: errorMessage, details: errorDetails }, ...errorStr]);
        console.error("[API ERROR]", title, apiError);
    };
    const dismissApiError = (id) => setApiErrors((errorStr) => errorStr.filter((errorStringFilterVal) => errorStringFilterVal.id !== id));

    useEffect(() => setCookie("requestType", HTTPRequestType), [HTTPRequestType]);
    useEffect(() => setCookie("responseType", HTTPResponseType), [HTTPResponseType]);

    useEffect(() => {
        let mountedValues = true;
        (async () => {
            try {
                setLoadingError(null);
                setIsLoadingAll(true);
                const items = await fetchAllBooks(HTTPResponseType);
                if (!mountedValues) return;
                setAllBooks(Array.isArray(items) ? items : []);
                setTotal(Array.isArray(items) ? items.length : 0);
                setPageNumber(0);
            } catch (exception) {
                if (!mountedValues) return;
                setAllBooks([]);
                setTotal(0);
                setLoadingError("Failed to load books");
                pushApiError(exception, "Failed to load books");
            } finally {
                if (!mountedValues) return;
                setIsLoadingAll(false);
            }
        })();
        return () => { mountedValues = false; };
    }, [HTTPResponseType]);

    const processedBooks = useMemo(class extends Component {
        render() {
            const base = Array.isArray(allBooks) ? allBooks : [];
            const searchQueryLowerase = (searchQuery || "").trim().toLowerCase();
            const searched = searchQueryLowerase
                ? base.filter((book) => {
                    const title = (book?.title || "").toString().toLowerCase();
                    const author = (book?.author || "").toString().toLowerCase();
                    return title.includes(searchQueryLowerase) || author.includes(searchQueryLowerase);
                }) : base;
            if (sortByMode === "none") return searched;

            return [...searched].sort((a, b) => {
                const aTitle = (a?.title || "").toString();
                const bTitle = (b?.title || "").toString();
                const aDate = a?.date ? new Date(a.date) : new Date(0);
                const bDate = b?.date ? new Date(b.date) : new Date(0);

                switch (sortByMode) {
                    case "title-asc":
                        return aTitle.localeCompare(bTitle);
                    case "title-desc":
                        return bTitle.localeCompare(aTitle);
                    case "date-asc":
                        return aDate - bDate;
                    case "date-desc":
                        return bDate - aDate;
                    default:
                        return 0;
                }
            });
        }
    }, [allBooks, searchQuery, sortByMode]);

    useEffect(() => {
        setTotal(processedBooks.length);
        const totalPages = Math.max(1, Math.ceil(processedBooks.length / pageSize));
        if (pageNumber >= totalPages) setPageNumber(0);
    }, [processedBooks, pageSize]);

    const paginatedBooks = useMemo(() => {
        const start = pageNumber * pageSize;
        return processedBooks.slice(start, start + pageSize);
    }, [processedBooks, pageNumber, pageSize]);

    const handleSizeChange = (newSize) => { setPageSize(newSize); setPageNumber(0); };
    const handleSearchChange = (v) => { setSearchQuery(v); setPageNumber(0); };

    const handleSelectBook = async (book) => {
        if (!book) return;
        const cached = Array.isArray(allBooks) ? allBooks.find((b) => b?.id === book.id) : null;
        if (cached && (cached.genres || cached.synopsis || cached.characters)) {
            setSelectedBook(cached);
            return;
        }
        try {
            const full = await fetchBookById(book.id, HTTPResponseType);
            setSelectedBook(full);
        } catch (ecception) {
            pushApiError(ecception, "Failed to fetch book");
            setSelectedBook(book);
        }
    };

    const handleOpenEdit = async (bookId) => {
        try {
            const full = await fetchBookById(bookId, HTTPResponseType);
            setSelectedBook(full);
            setFormMode("edit");
            setIsFormOpen(true);
        } catch (exception) {
            pushApiError(exception, "Failed to load book for editing");
            console.log("Failed to load book for editing");
        }
    };

    const handleCreateBook = async (book) => {
        try {
            await createBook(book, HTTPRequestType, HTTPResponseType);
            setIsLoadingAll(true);
            const items = await fetchAllBooks(HTTPResponseType);
            setAllBooks(Array.isArray(items) ? items : []);
            setIsLoadingAll(false);
            setIsFormOpen(false);
        } catch (exception) {
            pushApiError(exception, "Failed to create book");
            console.log("Failed to create book");
        }
    };

    const handleUpdateBook = async (book) => {
        try {
            console.log("[App] Updating book payload:", book, "requestType:", HTTPRequestType, "responseType:", HTTPResponseType);
            await updateBook(book, HTTPRequestType, HTTPResponseType);
            setIsFormOpen(false);
            setSelectedBook(null);
            setIsLoadingAll(true);
            const items = await fetchAllBooks(HTTPResponseType);
            setAllBooks(Array.isArray(items) ? items : []);
            setIsLoadingAll(false);
        } catch (exception) {
            pushApiError(exception, "Failed to update book");
            console.log("Failed to update book");
        }
    };

    const handleDeleteBook = async (id) => {
        try {
            await deleteBook(id, HTTPResponseType);
            setSelectedBook(null);
            setIsLoadingAll(true);
            const items = await fetchAllBooks(HTTPResponseType);
            setAllBooks(Array.isArray(items) ? items : []);
            setIsLoadingAll(false);
        } catch (exception) {
            pushApiError(exception, "Failed to delete book");
            console.log("Failed to delete book");
        }
    };

    return (
        <div className={`main-layout ${selectedBook ? "detail-open" : ""}`}>
            <HeaderBar
                searchQuery={searchQuery}
                onSearchChange={handleSearchChange}
                requestType={HTTPRequestType}
                responseType={HTTPResponseType}
                onRequestTypeChange={(t) => setHTTPRequestType(t)}
                onResponseTypeChange={(t) => setHTTPResponseType(t)}
                onAddBook={() => { setFormMode("create"); setIsFormOpen(true); }}
                sortMode={sortByMode}
                onSortChange={setSortByMode}
            />

            <div style={{
                position: "fixed",
                top: "calc(var(--header-height) + 12px)",
                right: "12px",
                zIndex: 1200,
                maxWidth: "420px",
            }}>
                {apiErrors.map((apiError) => (
                    <div key={apiError.id} style={{
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
                            <strong>{apiError.title}</strong>
                            <button onClick={() => dismissApiError(apiError.id)} style={{
                                border: "none",
                                background: "transparent",
                                cursor: "pointer",
                                color: "#7f1d1d",
                                fontWeight: 700,
                            }}>x</button>
                        </div>
                        <div style={{ marginTop: "0.45rem", color: "#4b1f1f" }}>{apiError.message}</div>
                        {apiError.details && typeof apiError.details === "object" && (
                            <details style={{ marginTop: "0.5rem", color: "#6b7280" }}>
                                <summary style={{ cursor: "pointer" }}>Details</summary>
                                <pre style={{ whiteSpace: "pre-wrap", fontSize: "0.85rem" }}>
                  {JSON.stringify(apiError.details, null, 2)}
                </pre></details>)}
                    </div>
                ))}
            </div>

            {loadingError && <div style={{ color: "red", marginBottom: "1rem" }}>{loadingError}</div>}
            {isLoadingAll && <div style={{ margin: "0.5rem 0", color: "#374151" }}>Loading all books...</div>}

            <BookTable
                books={paginatedBooks}
                page={pageNumber}
                size={pageSize}
                total={total}
                onPageChange={setPageNumber}
                onSizeChange={handleSizeChange}
                onSelectBook={handleSelectBook}/>

            <BookDetailPanel
                book={selectedBook}
                onClose={() => setSelectedBook(null)}
                onEdit={(id) => { if (id != null) handleOpenEdit(id); }}
                onDelete={(id) => { console.log("[App] onDelete called with id:", id); if (id != null) handleDeleteBook(id); }}/>

            {isFormOpen && (
                <BookForm
                    mode={formMode}
                    book={formMode === "edit" ? selectedBook : null}
                    onClose={() => setIsFormOpen(false)}
                    onSubmit={formMode === "edit" ? handleUpdateBook : handleCreateBook}/>
            )}
        </div>
    );
}
