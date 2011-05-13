$(function() {
	var studiengaenge = [
		'Angewandte Mathematik (Bachelor)',
		'Angewandte Mathematik (Master)',
		'Automatisierungstechnik (Master)',
		'Betriebswirtschaftslehre (Bachelor)',
		'Chemie (Bachelor)',
		'Chemie (Master)',
		'Chemieingenieurwesen (Diplom)',
		'Chemieingenieurwesen /Verfahrenstechnik (Ergänzungsstudiengang, Diplom)',
		'Energie und Rohstoffe (Bachelor)',
		'Energie- und Rohstoffversorgungstechnik (Master)',
		'Energiesystemtechnik (Diplom)',
		'Energiesystemtechnik (Ergänzungsstudiengang, Diplom)',
		'Energiesystemtechnik (Master)',
		'Energietechnologien (Bachelor)',
		'Energiewissenschaft (Master)',
		'Geoenvironmental Engineering (Bachelor)',
		'Geoenvironmental Engineering (Master)',
		'Informatik (Bachelor)',
		'Informatik (Master)',
		'Informationstechnik (Diplom)',
		'Internet Technologies and Information Systems (Master)',
		'Maschinenbau (Bachelor)',
		'Maschinenbau (Ergänzungsstudiengang, Diplom)',
		'Maschinenbau (Master)',
		'Maschinenbau/Mechatronik (Diplom)',
		'Materialwissenschaft (Master)',
		'Materialwissenschaft und Werkstofftechnik (Bachelor)',
		'Mechatronik (Master)',
		'Operations Research (Master)',
		'Petroleum Engineering (Master)',
		'Physik (Bachelor)',
		'Physik/Physikalische Technologien (Ergänzungsstudiengang, Diplom)',
		'Physikalische Technologien (Master)',
		'Radioactive and Hazardous Waste Management (Master)',
		'Rohstoff-Geowissenschaften (Bachelor)',
		'Rohstoff-Geowissenschaften (Master)',
		'Rohstoffversorgungstechnik (Weiterbildungsstudiengang, Master)',
		'Systems Engineering (Weiterbildungsstudiengang, Master)',
		'Technische Betriebswirtschaftslehre (Master)',
		'Technische Informatik (Bachelor)',
		'Umweltschutztechnik (Diplom)',
		'Umweltschutztechnik (Ergänzungsstudiengang, Diplom)',
		'Umweltverfahrenstechnik und Recycling (Master)',
		'Verfahrenstechnik (Diplom)',
		'Verfahrenstechnik/Chemieingenieurwesen (Master)',
		'Verfahrenstechnik/Chemieingenieurwesen (Bachelor)',
		'Werkstofftechnik (Master)',
		'Wirtschaftsinformatik (Bachelor)',
		'Wirtschaftsinformatik (Master)',
		'Wirtschaftsingenieurwesen (Bachelor)',
		'Wirtschaftsingenieurwesen (Diplom)',
		'Wirtschaftsingenieurwesen (Master)'
	];
	var minimumLength = 1;
	$('#studiengang').autocomplete({
		source: studiengaenge,
		minLength: minimumLength
	});
	$('#studiengang').butdton = $("<button type='button'>&nbsp;</button>")
		.attr("tabIndex", -1)
		.attr("title", "Alle anzeigen")
		.insertAfter($('#studiengang'))
		.button({
			icons: {
				primary: "ui-icon-triangle-1-s"
			},
			text: false
		})
		.removeClass("ui-corner-all")
		.addClass("ui-corner-right ui-button-icon")
		.click(function() {
			// close if already visible
			if ($('#studiengang').autocomplete("widget").is(":visible")) {
				$('#studiengang').autocomplete("close");
				return;
			}

			// pass empty string as value to search for, displaying all results
			$('#studiengang').autocomplete("option", "minLength", 0);
			$('#studiengang').autocomplete("search", "");
			$('#studiengang').autocomplete("option", "minLength", minimumLength);
			$('#studiengang').focus();
		});
});
