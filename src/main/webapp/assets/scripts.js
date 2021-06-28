/*
 * Copyright 2010-2011, 2020-2021 Sven Strickroth <email@cs-ware.de>
 * Copyright 2019 Dustin Reineke <dustin.reineke@tu-clausthal.de>
 *
 * This file is part of the SubmissionInterface.
 *
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

function confirmLink(msg) {
	return confirm(msg);
}

function sendAsPost(e, msg) {
	if (!confirm(msg)) {
		return false;
	}
	let frm = document.createElement('FORM');
	frm.id = 'frm_' + Math.random();
	frm.method = 'POST';
	frm.action = e.href;
	document.body.appendChild(frm);
	frm.submit();
	return false;
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
var kaInterval = false;

function keepAlive(url, interval) {
	kaInterval = setInterval("kaAjax('" + url + "')", interval * 1000);
}

function kaAjax(url) {
	var kaHttpRequest = new XMLHttpRequest();
	if (kaHttpRequest.overrideMimeType) {
		kaHttpRequest.overrideMimeType('text/plain');
	}
	var ser = Math.round(Math.random() * 1000000); // Anti-caching random number
	kaHttpRequest.open('GET', url + '?random=' + ser, true);
	kaHttpRequest.onload = function() {
		if (kaHttpRequest.readyState === kaHttpRequest.DONE) {
			if (!(kaHttpRequest.status === 200 && kaHttpRequest.responseText == "still logged in")) {
				alert("Ihre Sitzung scheint nicht mehr aktiv zu sein. Bitte sichern Sie Ihre eingegebene Antwort z.B. in der Zwischenablage und melden sich erneut am GATE-System an (z.B. in einem neuen Tab)!");
				if (kaInterval) {
					clearInterval(kaInterval);
					kaInterval = false;
				}
			}
		}
	};
	kaHttpRequest.send(null);
}

function checkInternalComment() {
	var checkBox = document.getElementById('isdupe');
	var internalComment = document.getElementById('internalcomment');
	var submitButton = document.getElementById('submit');
	var duplicateTextbox = document.getElementById('duplicate');

	if (checkBox.checked) {
		if (internalComment.value.length >= 10) {
			submitButton.disabled = false;
		} else {
			submitButton.disabled = true;
		}
		duplicateTextbox.required = true;
	} else {
		submitButton.disabled = false;
		duplicateTextbox.required = false;
	}
}

function dodiff(id) {
	const one = document.getElementById('exp' + id).textContent;
	const other = document.getElementById('got' + id).textContent;

	const diff = Diff.diffChars(one, other);
	const display = document.getElementById('diff' + id);
	const fragment = document.createDocumentFragment();

	while (display.firstChild) { display.removeChild(display.firstChild); }
	for (var i = 0; i < diff.length; ++i) {
		if (diff[i].added && diff[i + 1] && diff[i + 1].removed) {
			var swap = diff[i];
			diff[i] = diff[i + 1];
			diff[i + 1] = swap;
		}

		var node;
		if (diff[i].removed) {
			node = document.createElement('del');
			node.appendChild(document.createTextNode(diff[i].value));
		} else if (diff[i].added) {
			node = document.createElement('ins');
			node.appendChild(document.createTextNode(diff[i].value));
		} else {
			node = document.createTextNode(diff[i].value);
		}
		fragment.appendChild(node);
	}

	display.appendChild(fragment);
	toggleVisibility('got' + id);
	toggleVisibility('diff' + id);
}

function toggleVisibility(id) {
	var element = document.getElementById(id);
	if (element.style.display === "none") {
		element.style.display = "block";
	} else {
		element.style.display = "none";
	}
}

function storeCheckbox() {
	var theform = document.getElementById('manualcheckform');

	var urlEncodedDataPairs = [];

	var fd = new FormData(theform);
	fd.append("ajax", "1");

	for (var pair of fd.entries()) {
		urlEncodedDataPairs.push(encodeURIComponent(pair[0]) + '=' + encodeURIComponent(pair[1]));
	}

	// Combine the pairs into a single string and replace all %-encoded spaces to
	// the '+' character; matches the behavior of browser form submissions.
	var urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');

	var request = new XMLHttpRequest();
	request.open("POST", theform.action);
	request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	request.send(urlEncodedData);
}
