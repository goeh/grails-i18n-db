<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'appMessage.label', default: 'Message')}"/>
    <title><g:message code="appMessage.create.title" args="[entityName]"/></title>
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

<h1><g:message code="appMessage.create.title" args="[entityName]"/></h1>

<fieldset>
    <g:form class="form-horizontal" action="create">
        <fieldset>
            <f:with bean="appMessage">
                <f:field property="code" input-autofocus=""/>
                <f:field property="locale">
                    <g:localeSelect name="locale" value="${appMessage.locale}" sort="displayCountry"
                                    noSelection="['':'']"/>
                </f:field>
                <f:field property="text"><g:textArea name="text" cols="70" rows="3" class="span6"
                                                     value="${appMessage.text}"/></f:field>
            </f:with>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary"><i class="icon-ok icon-white"></i> <g:message
                        code="appMessage.button.create.label"/></button>
            </div>
        </fieldset>
    </g:form>
</fieldset>
</body>
</html>
