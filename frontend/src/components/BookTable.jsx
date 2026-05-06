
export default function BookTable({
                                      books,
                                      page,
                                      size,
                                      total,
                                      onPageChange,
                                      onSizeChange,
                                      onSelectBook,
                                  }) {
    const totalPages = Math.max(1, Math.ceil(total / size));
    return (
        <div className="book-table-wrapper">
            <table className="book-table">
                <thead>
                <tr>
                    <th>ID</th><th>Title</th><th>Author</th><th>Date</th>
                </tr>
                </thead>
                <tbody>
                {books.length === 0 ? (
                    <tr><td colSpan={4}>No books found.</td></tr>) : (
                    books.map((book) => (
                        <tr
                            key={book.id}
                            className="clickable-row"
                            onClick={() => onSelectBook(book)}>
                            <td>{book.id}</td><td>{book.title}</td><td>{book.author}</td><td>{book.date}</td>
                        </tr>
                    ))
                )}
                </tbody>
            </table>

            <div className="pagination-bar">
                <div>
                    Rows per page:{" "}
                    <select
                        value={size}
                        onChange={(e) => onSizeChange(Number(e.target.value))}>
                        {[10, 25, 50, 100].map((n) => (
                            <option key={n} value={n}>{n}</option>))}
                    </select>
                </div>

                <div>
                    <button disabled={page === 0} onClick={() => onPageChange(page - 1)}>Prev</button>
                    <span style={{ margin: "0 0.5rem" }}>
            {page + 1} / {totalPages}
          </span>
                    <button
                        disabled={page + 1 >= totalPages}
                        onClick={() => onPageChange(page + 1)}>Next</button>
                </div>
            </div>
        </div>
    );
}
