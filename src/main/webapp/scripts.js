function confirmLink(msg) {
    if (typeof(window.opera) != 'undefined') {
        return true;
    }
    return confirm(msg);
}
function selectAll(divId) {
	if (document.selection) {
		var div = document.body.createTextRange();
		div.moveToElementText(document.getElementById(divId));
		div.select();
	} else { 
		var div = document.createRange();
		div.setStartBefore(document.getElementById(divId));
		div.setEndAfter(document.getElementById(divId));
		window.getSelection().addRange(div);
	}
}

// KeepAlive based on http://www.808.dk/?code-ajax-session-keepalive
var kaHttpRequest = false;

function keepAlive(url,interval)
{
  setInterval("kaAjax('"+url+"')", interval * 1000);
}

function kaAjax(url)
{
  kaHttpRequest = false;
  if (window.XMLHttpRequest)
  { // For Mozilla, Safari, Opera, IE7+
    kaHttpRequest = new XMLHttpRequest();
    if (kaHttpRequest.overrideMimeType)
    {
      kaHttpRequest.overrideMimeType('text/plain');
    }
  }
  else if (window.ActiveXObject)
  { // For IE6
    try
    {
      kaHttpRequest = new ActiveXObject("Msxml2.XMLHTTP");
    }
    catch (e)
    {
      try
      {
        kaHttpRequest = new ActiveXObject("Microsoft.XMLHTTP");
      }
      catch (e)
      {}
    }
  }
  if (!kaHttpRequest)
  {
    alert('Giving up :( Cannot create an XMLHTTP instance');
    return false;
  }
  var ser = Math.round(Math.random()*1000000); // Anti-caching random number
  kaHttpRequest.open('GET', url + '?random=' + ser, true);
  kaHttpRequest.send(null);
}
