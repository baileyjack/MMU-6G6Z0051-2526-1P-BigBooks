<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jbailey.stu.mmu.bigbook.commons.model.Book" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Edit Book</title>
    <style>
        <%@ include file="/WEB-INF/views/fragments/themes/themeVariables.jspf" %>

        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: var(--bg-color);
            color: var(--text-color);
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        h1 {
            color: var(--primary-color);
        }

        .error-box {
            background-color: #ffebee;
            border-left: 4px solid var(--accent-color);
            padding: 12px;
            margin-bottom: 20px;
            border-radius: 4px;
        }

        <%@ include file="/WEB-INF/views/fragments/layout/topButtonsStyles.jspf" %>
        <%@ include file="/WEB-INF/views/fragments/forms/bookFormStyles.jspf" %>
    </style>
</head>
<body>
<%
    request.setAttribute("primaryButtonLabel", "All Books");
    request.setAttribute("primaryButtonPath", "/bigbookmvc/books");
    request.setAttribute("secondaryButtonLabel", "Home");
    request.setAttribute("secondaryButtonPath", "/bigbookmvc/home");
    Book book = (Book) request.getAttribute("book");
%>
<%@ include file="/WEB-INF/views/fragments/layout/topButtons.jspf" %>

<h1>Edit Book</h1>

<div class="form-shell">
    <%@ include file="/WEB-INF/views/fragments/forms/errorBox.jspf" %>

    <form action="<%= request.getContextPath() %>/bigbookmvc/books/edit" method="post">
        <input type="hidden" name="id" value="<%= book != null ? book.getId() : 0 %>">

        <label for="title">Title:</label>
        <input type="text" id="title" name="title" value="<%= request.getAttribute("title") != null ? request.getAttribute("title") : (book != null ? book.getTitle() : "") %>" required>

        <label for="author">Author:</label>
        <input type="text" id="author" name="author" value="<%= request.getAttribute("author") != null ? request.getAttribute("author") : (book != null ? book.getAuthor() : "") %>" required>

        <label for="date">Date (ISO 8601 format):</label>
        <input type="text" id="date" name="date" value="<%= request.getAttribute("date") != null ? request.getAttribute("date") : "" %>" required>

        <label for="genres">Genres (comma-separated):</label>
        <textarea id="genres" name="genres" rows="3"><%= request.getAttribute("genresStr") != null ? request.getAttribute("genresStr") : "" %></textarea>

        <label for="characters">Characters (comma-separated):</label>
        <textarea id="characters" name="characters" rows="3"><%= request.getAttribute("charactersStr") != null ? request.getAttribute("charactersStr") : "" %></textarea>

        <label for="synopsis">Synopsis:</label>
        <textarea id="synopsis" name="synopsis" rows="5" required><%= request.getAttribute("synopsis") != null ? request.getAttribute("synopsis") : (book != null ? book.getSynopsis() : "") %></textarea>

        <button type="submit">Update Book</button>
    </form>
</div>
</body>
</html>
