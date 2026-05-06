// src/components/HeaderBar.jsx
// Header with search, request/response toggles and an Add (+) button.

export default function HeaderBar({
                                      searchQuery,
                                      onSearchChange,
                                      requestType,
                                      responseType,
                                      onRequestTypeChange,
                                      onResponseTypeChange,
                                      sortMode,
                                      onSortChange,
                                      onAddBook,
                                  }) {
    return (
        <header className="header-bar">
            <div className="header-left">
                <h1>The Big Book Database</h1>
            </div>

            <div className="header-center">
                <input
                    type="text"
                    className="search-input"
                    placeholder="Search by title or author..."
                    value={searchQuery || ""}
                    onChange={(e) => onSearchChange(e.target.value)}
                />
            </div>

            <div className="header-right" style={{ display: "flex", gap: "0.75rem", alignItems: "center" }}>
                <div className="btn-group">
                    <span className="group-label">Request</span>
                    {["json", "xml", "yaml"].map((t) => (
                        <button
                            key={t}
                            className={`toggle-btn ${requestType === t ? "active" : ""}`}
                            onClick={() => onRequestTypeChange(t)}
                        >
                            {t.toUpperCase()}
                        </button>
                    ))}
                </div>

                <div className="btn-group">
                    <span className="group-label">Response</span>
                    {["json", "xml", "yaml"].map((t) => (
                        <button
                            key={t}
                            className={`toggle-btn ${responseType === t ? "active" : ""}`}
                            onClick={() => onResponseTypeChange(t)}
                        >
                            {t.toUpperCase()}
                        </button>
                    ))}
                </div>

                <div className="btn-group">
                    <span className="group-label">Sort</span>
                    <button className={`toggle-btn ${sortMode === "none" ? "active" : ""}`} onClick={() => onSortChange("none")}>None</button>
                    <button className={`toggle-btn ${sortMode === "title-asc" ? "active" : ""}`} onClick={() => onSortChange("title-asc")}>Title A–Z</button>
                    <button className={`toggle-btn ${sortMode === "date-desc" ? "active" : ""}`} onClick={() => onSortChange("date-desc")}>Date ↓</button>
                </div>

                <button
                    onClick={onAddBook}
                    title="Add Book"
                    style={{
                        display: "inline-flex",
                        alignItems: "center",
                        justifyContent: "center",
                        width: 40,
                        height: 40,
                        borderRadius: 999,
                        border: "none",
                        background: "var(--accent)",
                        color: "white",
                        fontSize: 20,
                        cursor: "pointer",
                        boxShadow: "0 4px 12px rgba(37,99,235,0.18)",
                    }}
                    aria-label="Add book"
                >
                    +
                </button>
            </div>
        </header>
    );
}
