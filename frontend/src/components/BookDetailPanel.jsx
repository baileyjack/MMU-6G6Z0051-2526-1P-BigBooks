export default function BookDetailPanel({ book, onClose, onEdit, onDelete }) {
    if (!book) return null;
    return (
        <aside className={`detail-panel open`}>
            <div className="detail-inner">
                <div className="detail-header">
                    <h2 style={{ margin: 0 }}>{book.title}</h2>
                    <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                        <button className="icon-btn" onClick={onClose} aria-label="Close">✕</button>
                    </div>
                </div>
                <div className="detail-body">
                    <p><strong>ID:</strong> {book.id}</p>
                    <p><strong>Author:</strong> {book.author}</p>
                    <p><strong>Date:</strong> {book.date}</p>
                    <p><strong>Genres:</strong> {(book.genres || []).join(", ")}</p>
                    <p><strong>Characters:</strong> {(book.characters || []).join(", ")}</p>
                    <div style={{ marginTop: "0.6rem" }}>
                        <strong>Synopsis</strong>
                        <p className="synopsis-text" style={{ marginTop: "0.4rem" }}>{book.synopsis}</p>
                    </div>
                </div>
                <div className="detail-footer">
                    <button className="secondary-btn" onClick={() => onEdit(book.id)}>Edit</button>
                    <button className="danger-btn" onClick={() => onDelete(book.id)}>Delete</button>
                </div>
            </div>
        </aside>
    );
}
