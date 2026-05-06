import { useState, useEffect } from "react";

export default function BookForm({ mode: formMode, book: startingBookVal, onClose, onSubmit }) {
    const [book, setBook] = useState({
        id: startingBookVal?.id ?? null,
        title: startingBookVal?.title ?? "",
        author: startingBookVal?.author ?? "",
        date: startingBookVal?.date ?? "",
        genres: (startingBookVal?.genres || []).join(", "),
        characters: (startingBookVal?.characters || []).join(", "),
        synopsis: startingBookVal?.synopsis ?? "",
    });
    const [error, setError] = useState("");

    useEffect(() => {
        setBook({
            id: startingBookVal?.id ?? null,
            title: startingBookVal?.title ?? "",
            author: startingBookVal?.author ?? "",
            date: startingBookVal?.date ?? "",
            genres: (startingBookVal?.genres || []).join(", "),
            characters: (startingBookVal?.characters || []).join(", "),
            synopsis: startingBookVal?.synopsis ?? "",
        });
        setError("");
    }, [startingBookVal]);

    const handleChange = (field) => (err) => setBook((p) => ({ ...p, [field]: err.target.value }));

    const handleSubmit = async (promise) => {
        promise.preventDefault();
        setError("");

        const confirmed = window.confirm(
            formMode === "create" ? "Create this book?" : "Save changes to this book?"
        );
        if (!confirmed) return;

        try {
            const payload = {
                id: book.id,
                title: book.title.trim(),
                author: book.author.trim(),
                date: book.date.trim(),
                genres: book.genres ? book.genres.split(",").map((g) => g.trim()).filter(Boolean) : [],
                characters: book.characters ? book.characters.split(",").map((c) => c.trim()).filter(Boolean) : [],
                synopsis: book.synopsis.trim(),
            };
            await onSubmit(payload);
        } catch (exception) {
            setError(exception?.message || "Failed to submit");
        }
    };

    /**
     * BUILD THE FORM!!! :)
     */
    return (
        <div className="form-backdrop">
            <div className="formview">
                <h2>{formMode === "create" ? "Add Book" : "Edit Book"}</h2>
                {error && <div className="error-banner">{error}</div>}
                <form onSubmit={handleSubmit} className="book-form">
                    {formMode === "edit" && (
                        <div className="form-row">
                            <label>ID</label>
                            <input type="text" value={book.id ?? ""} disabled />
                        </div>
                    )}
                    <div className="form-row">
                        <label>Title *</label>
                        <input type="text" value={book.title} onChange={handleChange("title")} required />
                    </div>
                    <div className="form-row">
                        <label>Author *</label>
                        <input type="text" value={book.author} onChange={handleChange("author")} required />
                    </div>
                    <div className="form-row">
                        <label>Date (YYYY-MM-DD)</label>
                        <input type="text" value={book.date} onChange={handleChange("date")} placeholder="YYYY-MM-DD" />
                    </div>
                    <div className="form-row">
                        <label>Genres (comma-separated)</label>
                        <input type="text" value={book.genres} onChange={handleChange("genres")} />
                    </div>
                    <div className="form-row">
                        <label>Characters (comma-separated)</label>
                        <input type="text" value={book.characters} onChange={handleChange("characters")} />
                    </div>
                    <div className="form-row">
                        <label>Synopsis</label>
                        <textarea value={book.synopsis} onChange={handleChange("synopsis")} rows={4} />
                    </div>
                    <div className="form-actions">
                        <button type="button" className="secondary-btn" onClick={onClose}>Cancel</button>
                        <button type="submit" className="primary-btn">{formMode === "create" ? "Add Book" : "Save Changes"}</button>
                    </div>
                </form>
            </div>
        </div>
    );
}
