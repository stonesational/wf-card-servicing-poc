<DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>

<title>Test Page for OAUth Web Server Flow</title>

<script type="text/javascript" src="/sdk/js/canvas-all.js"></script>

</head>
<body>
<script>

    if (self === top) {
        // Not in Iframe
        alert("This canvas app must be included within an iframe");
    }

    function contextCallback(msg){
    var html,context;
    if(msg.status !== 200){
      console.log (msg.status);
      return;
    }
    Sfdc.canvas.client.autogrow(Sfdc.canvas.oauth.client());
        Sfdc.canvas.byId('canvas-request-string').innerHTML = jsonSyntaxHighlight(msg.payload);
    }

     function jsonSyntaxHighlight(json) {
        if (typeof json != 'string') {
           json = JSON.stringify(json, undefined, 2);
        }
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
            var cls = 'number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'key';
                } else {
                    cls = 'string';
                }
                } else if (/true|false/.test(match)) {
                    cls = 'boolean';
                } else if (/null/.test(match)) {
                    cls = 'null';
                }
                   return '<span class="' + cls + '">' + match + '</span>';
             });
    }
    
    function clickHandler(e) {
        var uri;

        if (! Sfdc.canvas.oauth.loggedin()) {
            uri = Sfdc.canvas.oauth.loginUrl();
            console.log("Login URI: " + uri);
            // This uri is the outer parent window.
       Sfdc.canvas.oauth.login(
        {uri : uri,
        params: {
            display : "touch",
            //response_type : "token",
            response_type : "code",
            client_id : "3MVG9A2kN3Bn17hudSDZs_Sd4e_URGVd3.aT9D9yvsykf9KR5683gzcLmzKXxOSyk7rVsewCN4DSdLbvBELi3", //Add Your Consumer Key
            redirect_uri : encodeURIComponent("https://wf-card-servicing-poc.herokuapp.com/oauth_callback") //Add your Callback URL
        }});
        }
        else {
            Sfdc.canvas.oauth.logout();
        }
        return false;
    }

    // Bootstrap the pae once the DOM is ready.
    Sfdc.canvas(function() {
        // On Ready...
        var login    = Sfdc.canvas.byId("login"),
            loggedIn = Sfdc.canvas.oauth.loggedin();
        login.innerHTML = (loggedIn) ? "Logout" : "login";
        Sfdc.canvas.byId("access-token").innerHTML = (loggedIn) ? Sfdc.canvas.oauth.token() : "No Token";
        login.onclick=clickHandler;
    if(loggedIn){
      Sfdc.canvas.client.ctx(contextCallback, Sfdc.canvas.oauth.client());
    }
    });

</script>

<style>
    pre {white-space: pre-wrap;padding: 5px; margin: 5px; overflow:auto;width:auto;height:80%;}
      .string { color: green; }
      .number { color: darkorange; }
      .boolean { color: blue; }
      .null { color: magenta; }
      .key { color: red; }
</style> 

<h1 id="header">OAuth Canvas App</h1>
<div>
 <div>
    <a id="login" href="#">Login</a>
  </div>
  <div>
     Access Token: <span id="access-token"></span>
  </div>
  <div id="canvas-content">
    <h4>Once logged in through OAuth, you can see the JSON representation of Canvas Request below:</h2>
    <pre><code id="canvas-request-string"></code></pre>
 </div> 
 </div>
</body>
</html>
