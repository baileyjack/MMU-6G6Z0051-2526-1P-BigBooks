<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jbailey.stu.mmu.bigbook.commons.model.Book" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Search Results</title>
    <style>
        <%@ include file="/WEB-INF/views/fragments/themes/themeVariables.jspf" %>

        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: var(--bg-color);
            color: var(--text-color);
        }

        h1 {
            color: var(--primary-color);
            margin-bottom: 20px;
        }

        .books-container {
            max-width: 900px;
            margin: 0 auto;
        }

        .search-info {
            background-color: var(--light-bg);
            padding: 15px;
            border-left: 4px solid var(--primary-color);
            margin-bottom: 20px;
            border-radius: 4px;
        }

        <%@ include file="/WEB-INF/views/fragments/layout/topButtonsStyles.jspf" %>
        <%@ include file="/WEB-INF/views/fragments/cards/bookItemStyles.jspf" %>
        <%@ include file="/WEB-INF/views/fragments/controls/listControlsStyles.jspf" %>
    </style>
    <%@ include file="/WEB-INF/views/fragments/scripts/toggleBookScript.jspf" %>
</head>
<body>
<%
    request.setAttribute("primaryButtonLabel", "Add New Book");
    request.setAttribute("primaryButtonPath", "/bigbookmvc/books/new");
    request.setAttribute("secondaryButtonLabel", "Home");
    request.setAttribute("secondaryButtonPath", "/bigbookmvc/home");
%>
<%@ include file="/WEB-INF/views/fragments/layout/topButtons.jspf" %>

<h1>Search Results</h1>

<%
    String searchQuery = (String) request.getAttribute("searchQuery");
%>
<div class="search-info">
    <strong>Search Query:</strong> <%= searchQuery != null ? searchQuery : "N/A" %>
</div>

<%
    request.setAttribute("pageSizeFormAction", request.getContextPath() + "/bigbookmvc/search");
    request.setAttribute("pageSizeHiddenFieldName", "search");
    request.setAttribute("pageSizeHiddenFieldValue", searchQuery != null ? searchQuery : "");
%>
<%@ include file="/WEB-INF/views/fragments/controls/pageSizeSelector.jspf" %>

<div class="books-container">
    <%
        List<Book> books = (List<Book>) request.getAttribute("books");
        if (books != null && !books.isEmpty()) {
            for (Book b : books) {
                request.setAttribute("bookItem", b);
                request.setAttribute("showBookActions", Boolean.TRUE);
    %>
    <%@ include file="/WEB-INF/views/fragments/cards/bookItem.jspf" %>
    <%
            }
            request.removeAttribute("bookItem");
            request.removeAttribute("showBookActions");
        } else {
    %>
    <div class="no-books">
        <h2>No books found.</h2>
        <p>Your search did not return any results.</p>
    </div>
    <%
        }
    %>
</div>

<%
    request.setAttribute("paginationBasePath", "/bigbookmvc/search");
    request.setAttribute("paginationQueryPrefix", "search=" + (searchQuery != null ? searchQuery : ""));
%>
<%@ include file="/WEB-INF/views/fragments/controls/pagination.jspf" %>

</body>
</html>
