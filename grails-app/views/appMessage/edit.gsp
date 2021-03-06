<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'appMessage.label', default: 'Message')}"/>
    <title><g:message code="appMessage.edit.title" args="[entityName]"/></title>
</head>

<body>

<g:hasErrors bean="${appMessage}">
    <bootstrap:alert class="alert-error">
        <ul>
            <g:eachError bean="${appMessage}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </bootstrap:alert>
</g:hasErrors>

<h1><g:message code="appMessage.edit.title" args="[appMessage]"/></h1>

<fieldset>
    <g:form class="form-horizontal" action="edit"
            id="${appMessage?.id}">
        <g:hiddenField name="version" value="${appMessage?.version}"/>
        <fieldset>
            <f:with bean="appMessage">
                <f:field property="code" input-autofocus=""/>
                <f:field property="locale">
                    <g:localeSelect name="locale" value="${appMessage.locale}"
                                    noSelection="['':'']"/>
                </f:field>
                <f:field property="text"><g:textArea name="text" cols="70" rows="3" class="span6"
                                                     value="${appMessage.text}"/></f:field>
            </f:with>
            <div class="form-actions">
                <button type="submit" name="_action_edit" class="btn btn-primary"><i
                        class="icon-ok icon-white"></i> <g:message code="appMessage.button.update.label"/></button>
                <button type="submit" name="_action_delete" class="btn btn-danger"><i
                        class="icon-trash icon-white"></i> <g:message code="appMessage.button.delete.label"/></button>
            </div>
        </fieldset>
    </g:form>
</fieldset>

</body>
</html>
