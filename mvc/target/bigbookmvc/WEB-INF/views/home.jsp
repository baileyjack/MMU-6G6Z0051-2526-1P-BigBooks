<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>BigBook Database Management Portal</title>
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
            text-align: center;
        }

        h1 { color: var(--primary-color); }
        <%@ include file="/WEB-INF/views/fragments/layout/topButtonsStyles.jspf" %>
        <%@ include file="/WEB-INF/views/fragments/forms/homeSearchFormStyles.jspf" %>
        <%@ include file="/WEB-INF/views/fragments/cards/bookItemStyles.jspf" %>
        <%@ include file="/WEB-INF/views/fragments/cards/recentBooksStyles.jspf" %>
    </style>
    <%@ include file="/WEB-INF/views/fragments/scripts/toggleBookScript.jspf" %>
</head>
<body>
<%
    request.setAttribute("primaryButtonLabel", "View All Books");
    request.setAttribute("primaryButtonPath", "/bigbookmvc/books");
    request.setAttribute("secondaryButtonLabel", "Add New Book");
    request.setAttribute("secondaryButtonPath", "/bigbookmvc/books/new");
%>
<%@ include file="/WEB-INF/views/fragments/layout/topButtons.jspf" %>

<h1>Welcome to The Big Book Database (MVC)</h1>

<%@ include file="/WEB-INF/views/fragments/forms/homeSearchForm.jspf" %>
<%@ include file="/WEB-INF/views/fragments/cards/recentBooksList.jspf" %>

</body>
</html>
