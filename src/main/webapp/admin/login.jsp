<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix='sec' uri='http://www.springframework.org/security/tags' %>
<html>
<head>
    <sec:authorize access="isAuthenticated()">
        <jsp:forward page="${pageContext.request.contextPath}/admin"/>
    </sec:authorize>
    <title>Админка</title>
    <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/favicon.ico">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.login.css"/>
    <meta charset="utf-8"/>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1, maximum-scale=1, userModel-scalable=no shrink-to-fit=no">
</head>
<body>
<form class="form-signin" id="login" action="${pageContext.request.contextPath}/admin/login" method="post">
    <div class="text-center mb-4">
        <%--<img class="mb-4" src="" alt="" width="72" height="72">--%>
        <h1 class="h3 mb-3 font-weight-normal">Вход в систему</h1>
    </div>

    <div class="form-label-group">
        <input value="admin" name="username" type="text" id="inputEmail" class="form-control" placeholder="Username" required
               autofocus>
        <label for="inputEmail">Имя пользователя</label>
    </div>

    <div class="form-label-group">
        <input value="1234" name="password" type="password" id="inputPassword" class="form-control" placeholder="Password" required>
        <label for="inputPassword">Пароль</label>
    </div>
    <button class="btn btn-lg btn-primary btn-block" type="submit">Вход</button>
</form>
</body>
</html>