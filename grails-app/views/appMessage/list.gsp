<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'appMessage.label', default: 'Message')}"/>
    <title><g:message code="appMessage.list.title" args="[entityName]"/></title>
</head>

<body>

<h1><g:message code="appMessage.list.title" args="[entityName]"/></h1>

<table class="table table-striped">
    <thead>
    <tr>

        <g:sortableColumn property="code"
                          title="${message(code: 'appMessage.code.label', default: 'Code')}"/>

        <g:sortableColumn property="locale"
                          title="${message(code: 'appMessage.locale.label', default: 'Locale')}"/>

        <g:sortableColumn property="text"
                          title="${message(code: 'appMessage.text.label', default: 'Text')}"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${appMessageList}" var="appMessage">
        <tr>

            <td>
                <g:link action="edit" id="${appMessage.id}">
                    ${fieldValue(bean: appMessage, field: "code")}
                </g:link>
            </td>

            <td>
                ${fieldValue(bean: appMessage, field: "locale")}
            </td>

            <td>
                ${fieldValue(bean: appMessage, field: "text")}
            </td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="pagination">
    <bootstrap:paginate total="${appMessageTotal}"/>
</div>
</body>
</html>
