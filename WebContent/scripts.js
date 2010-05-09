function confirmLink(msg) {
    if (typeof(window.opera) != 'undefined') {
        return true;
    }
    return confirm(msg);
}
function toggleComments(a) {
	if (a.href.toLowerCase().search("comments")>0) {
		a.href = a.href.replace("&comments=off","");
	} else {
		a.href+="&comments=off";
	}
}
