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
