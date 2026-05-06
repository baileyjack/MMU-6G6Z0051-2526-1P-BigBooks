<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- ! Auto redirect when entering the web app to the home page though I tested this and I dont actually think it worked so idk -->
<%
    response.sendRedirect(request.getContextPath() + "/bigbookmvc/home");
%>
