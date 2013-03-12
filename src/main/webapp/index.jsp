<%@page contentType="application/xhtml+xml" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jspf" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<link type="text/css" rel="stylesheet"
      href="<c:url value='/css/bootstrap.min.css'/>" />
<link type="text/css" rel="stylesheet"
      href="<c:url value='/css/bootstrap-responsive.min.css'/>" />
<link type="text/css" rel="stylesheet"
      href="<c:url value='/css/specs/welcome.css'/>" />
<link rel="shortcut icon" href="http://oauth.net/images/oauth-2-sm.png"/>

</head>

<body>
    <div class="container marketing">


        <div class="alert alert-info" style="margin-top: 40px">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            <h4>Chcete to skusit?</h4>
            - Na vyskusanie mozme zadat do prehliadaca:<br/>
            <a href="oauth/authorize?client_id=foodlovers&amp;redirect_uri=http%3A%2F%2Fexample.org&amp;response_type=code&amp;scope=read&amp;state=qB4ND7">priklad 1</a>
            <a href="http://localhost:8081/auth/oauth/authorize?client_id=92y75NqNqyO1eo3euGQcZY2qKjyVoq8T3aahuMTnzj4CxQBmir61pjkg504BIwm4&amp;redirect_uri=http://en.wikipedia.org/wiki/Broccoli&amp;response_type=code&amp;scope=read&amp;state=qB4ND7">priklad 2</a>
            <br />

            - Zadanie takehto url do prehliadaca bude viest k chybe, pretoze client z id "veryverybadclient" neexistuje:<br/>
            <a href="oauth/authorize?client_id=veryverybadclient&amp;redirect_uri=http%3A%2F%2Fexample.org&amp;response_type=code&amp;scope=read&amp;state=qB4ND7">priklad 3</a>
            <br />
        </div>

        <hr class="featurette-divider"/>

        <div class="featurette">
            <a href="http://oauth.net/2/" target="_blank">
                <img class="featurette-image pull-left" src="img/oauth2logo_big.png" height="512" width="512"/>
            </a>
            <h2 class="featurette-heading">OAuth2 Autorizační server ČVUT. <span class="muted">Je to jednoduché.</span></h2>
            <p class="lead">Donec ullamcorper nulla non metus auctor fringilla. Vestibulum id ligula porta felis euismod semper. Praesent commodo cursus magna, vel scelerisque nisl consectetur. Fusce dapibus, tellus ac cursus commodo.</p>
        </div>

        <hr class="featurette-divider"/>

        <!-- FOOTER -->
        <footer>
            <p class="pull-right"><a href="#">Nahoru</a></p>
            <p>&copy; 2012 České vysoké učení technické v Praze. &middot; <a href="#">Soukromí</a> &middot; <a href="#">Podmínky</a></p>
        </footer>
    </div>

    <script src="js/jquery-1.8.3.min.js"></script>
    <script src="js/bootstrap.min.js"></script>

    <%@ include file="/WEB-INF/jspf/footer.jspf" %>
