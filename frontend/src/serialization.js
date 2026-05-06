import YAML from "js-yaml";

export function mediaTypeSelector(givenType) {
    switch (givenType) {
        case "json": return "application/json";
        case "xml": return "application/xml";
        case "yaml": return "application/x-yaml";
        default: return "application/json";
    }
}

function escapeCharsForXml(string = "") {
    return String(string)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&apos;");
}

export function serializeBook(book, type) {
    switch (type) {
        case "json": return JSON.stringify(book);
        case "xml": return bookToXml(book);
        case "yaml": return YAML.dump(book);
        default: throw new Error("Unsupported request serialization type");
    }
}

/**
 * Returns the full XML Book Instance for one BookDTO
 * @param book the book...
 * @returns {string}
 */
function bookToXml(book) {
    const genres = (book.genres || []).map((g) => `<genre>${escapeCharsForXml(g)}</genre>`).join("");
    const characters = (book.characters || []).map((c) => `<character>${escapeCharsForXml(c)}</character>`).join("");
    return `
        <BookDTO>
          ${book.id != null ? `<id>${escapeCharsForXml(book.id)}</id>` : ""}
          <title>${escapeCharsForXml(book.title)}</title>
          <author>${escapeCharsForXml(book.author)}</author>
          <date>${escapeCharsForXml(book.date)}</date>
          <genres>${genres}</genres>
          <characters>${characters}</characters>
          <synopsis>${escapeCharsForXml(book.synopsis || "")}</synopsis>
        </BookDTO>`.trim();
}


/**
 * Expects a type of "json", "xml", or "yaml" and attempts to parse the text accordingly.
 */
export async function parseResponse(response, type) {
    const text = await response.text();
    switch (type) {
        case "json":
            try { return JSON.parse(text); } catch (e) { return text; }
        case "xml":
            return xmlToJs(text);
        case "yaml":
            try { return YAML.load(text); } catch (e) { return text; }
        default:
            try { return JSON.parse(text); } catch (e) { return text; }
    }
}

/**
 * xmlToJs
 *
 * Converts XML string into:
 *  - an array of book objects when multiple books are present, or
 *  - a single book object when only one is present, or
 *  - an empty object when nothing recognizable is found.
 *
 * Handles:
 *  - multiple <data> siblings (each representing a book)
 *  - <data><BookDTO>...</BookDTO>...</data>
 *  - <books><BookDTO>...</BookDTO></books>
 *  - flattened repeated tags inside <data>
 *  - RestResponse wrappers
 */
function xmlToJs(xmlString) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xmlString, "application/xml");

    if (doc.querySelector("parsererror")) {
        throw new Error("Invalid XML received from server");
    }

    // Helper: convert a node that contains book fields into a JS object
    function nodeToBook(node) {
        const getText = (sel) => node.querySelector(sel)?.textContent?.trim() ?? "";

        const genreNodes = node.querySelectorAll("genres > genre, genres > genres, genres > *");
        const genres = Array.from(genreNodes).map((n) => n.textContent.trim()).filter(Boolean);

        const charNodes = node.querySelectorAll("characters > character, characters > *");
        const characters = Array.from(charNodes).map((n) => n.textContent.trim()).filter(Boolean);

        const idText = getText("id");
        return {
            id: idText ? Number(idText) : undefined,
            title: getText("title"),
            author: getText("author"),
            date: getText("date"),
            genres,
            characters,
            synopsis: getText("synopsis"),
        };
    }
    
    //* Maps objects to Books
    function mapNodesToBooks(nodes) {
        const arr = Array.from(nodes).map(nodeToBook);
        return arr;
    }

    // 0) If there are multiple top-level <data> siblings, treat each as a book
    const topDataNodes = Array.from(doc.querySelectorAll(":root > data, RestResponse > data, /* fallback */ data"));
    if (topDataNodes.length > 1) {
        const books = topDataNodes.map((n) => {
            // If this data node contains BookDTO children, prefer those
            const dtoChildren = n.querySelectorAll("BookDTO");
            if (dtoChildren.length > 0) return mapNodesToBooks(dtoChildren)[0];
            // Otherwise convert the data node itself
            return nodeToBook(n);
        });
        return books;
    }

    // 1) Look for a <data> node (single)
    const dataNode = doc.querySelector("data");
    if (dataNode) {
        // 1a) explicit BookDTO children
        const bookDtoNodes = Array.from(dataNode.querySelectorAll("BookDTO"));
        if (bookDtoNodes.length > 0) {
            const books = mapNodesToBooks(bookDtoNodes);
            return books.length === 1 ? books[0] : books;
        }

        // 1b) repeated child nodes like <book> or <item>
        const repeatedNodes = Array.from(dataNode.querySelectorAll("book, Book, item"));
        if (repeatedNodes.length > 0) {
            const books = mapNodesToBooks(repeatedNodes);
            return books.length === 1 ? books[0] : books;
        }

        // 1c) flattened repeated tags inside <data>
        // e.g. <data><id>1001</id><title>...</title><id>1002</id><title>...</title>...</data>
        const idNodes = Array.from(dataNode.querySelectorAll("id"));
        const titleNodes = Array.from(dataNode.querySelectorAll("title"));
        const authorNodes = Array.from(dataNode.querySelectorAll("author"));
        const dateNodes = Array.from(dataNode.querySelectorAll("date"));
        const synopsisNodes = Array.from(dataNode.querySelectorAll("synopsis"));

        const counts = [idNodes.length, titleNodes.length, authorNodes.length].filter(n => n > 0);
        const maxCount = counts.length ? Math.max(...counts) : 0;

        if (maxCount > 1) {
            const allGenreItems = Array.from(dataNode.querySelectorAll("genres > genre, genres > genres, genres > *")).map(n => n.textContent.trim()).filter(Boolean);
            const allCharItems = Array.from(dataNode.querySelectorAll("characters > character, characters > *")).map(n => n.textContent.trim()).filter(Boolean);

            const books = [];
            for (let i = 0; i < maxCount; i++) {
                const idText = idNodes[i]?.textContent?.trim();
                const titleText = titleNodes[i]?.textContent?.trim();
                const authorText = authorNodes[i]?.textContent?.trim();
                const dateText = dateNodes[i]?.textContent?.trim();
                const synopsisText = synopsisNodes[i]?.textContent?.trim();

                let genres = [];
                if (allGenreItems.length && allGenreItems.length % maxCount === 0) {
                    const per = allGenreItems.length / maxCount;
                    genres = allGenreItems.slice(i * per, (i + 1) * per);
                }

                let characters = [];
                if (allCharItems.length && allCharItems.length % maxCount === 0) {
                    const per = allCharItems.length / maxCount;
                    characters = allCharItems.slice(i * per, (i + 1) * per);
                }

                books.push({
                    id: idText ? Number(idText) : undefined,
                    title: titleText || "",
                    author: authorText || "",
                    date: dateText || "",
                    genres,
                    characters,
                    synopsis: synopsisText || "",
                });
            }
            return books;
        }

        // 1d) single book fields inside <data>
        const hasFields = dataNode.querySelector("id, title, author, synopsis, genres, characters");
        if (hasFields) {
            return nodeToBook(dataNode);
        }
    }

    // 2) Root-level BookDTO nodes
    const rootBookNodes = Array.from(doc.querySelectorAll("BookDTO"));
    if (rootBookNodes.length > 0) {
        const books = mapNodesToBooks(rootBookNodes);
        return books.length === 1 ? books[0] : books;
    }

    // 3) Root-level wrapper like <books>, <BookList>, <BookDTOs>
    const wrapper = doc.querySelector("books, Books, BookList, BookDTOs");
    if (wrapper) {
        const inner = Array.from(wrapper.querySelectorAll("BookDTO, book, item"));
        if (inner.length > 0) {
            const books = mapNodesToBooks(inner);
            return books.length === 1 ? books[0] : books;
        }
    }

    // 4) RestResponse flattened case: <RestResponse><data>...</data></RestResponse>
    const restData = doc.querySelector("RestResponse > data");
    if (restData) {
        const ids = restData.querySelectorAll("id");
        if (ids.length > 1) {
            const innerXml = `<data>${restData.innerHTML}</data>`;
            return xmlToJs(innerXml);
        }
        const has = restData.querySelector("id, title, author, synopsis, genres, characters");
        if (has) return nodeToBook(restData);
    }

    // 5) Last attempt: multiple id/title pairs anywhere -> build array by index
    const allIds = Array.from(doc.querySelectorAll("id"));
    const allTitles = Array.from(doc.querySelectorAll("title"));
    if (allIds.length > 1 && allTitles.length >= allIds.length) {
        const count = allIds.length;
        const books = [];
        for (let i = 0; i < count; i++) {
            books.push({
                id: allIds[i]?.textContent?.trim() ? Number(allIds[i].textContent.trim()) : undefined,
                title: allTitles[i]?.textContent?.trim() || "",
                author: (Array.from(doc.querySelectorAll("author"))[i]?.textContent?.trim()) || "",
                date: (Array.from(doc.querySelectorAll("date"))[i]?.textContent?.trim()) || "",
                genres: [],
                characters: [],
                synopsis: (Array.from(doc.querySelectorAll("synopsis"))[i]?.textContent?.trim()) || "",
            });
        }
        return books;
    }

    // Nothing matched — return empty object
    return {};
}
