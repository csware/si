// Copyright (c) 2011, Mike Samuel
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//
// Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// Neither the name of the OWASP nor the names of its contributors may
// be used to endorse or promote products derived from this software
// without specific prior written permission.
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
// ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

// "EbayPolicyExample" of the OWASP HTML Satinizer project extended by:
// Copyright (c) 2021-2024 Sven Strickroth <email@cs-ware.de>

package de.tuclausthal.submissioninterface.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Based on the
 * <a href="http://www.owasp.org/index.php/Category:OWASP_AntiSamy_Project#Stage_2_-_Choosing_a_base_policy_file">AntiSamy EBay example</a>.
 * <blockquote>
 * eBay (http://www.ebay.com/) is the most popular online auction site in the
 * universe, as far as I can tell. It is a public site so anyone is allowed to
 * post listings with rich HTML content. It's not surprising that given the
 * attractiveness of eBay as a target that it has been subject to a few complex
 * XSS attacks. Listings are allowed to contain much more rich content than,
 * say, Slashdot- so it's attack surface is considerably larger. The following
 * tags appear to be accepted by eBay (they don't publish rules):
 * {@code <a>},...
 * </blockquote>
 */
public class HTMLSanitizerPolicy {
	// Some common regular expression definitions.

	// The 16 colors defined by the HTML Spec (also used by the CSS Spec)
	private static final Pattern COLOR_NAME = Pattern.compile("(?:aqua|black|blue|fuchsia|gray|grey|green|lime|maroon|navy|olive|purple|red|silver|teal|white|yellow)");

	// HTML/CSS Spec allows 3 or 6 digit hex to specify color
	private static final Pattern COLOR_CODE = Pattern.compile("(?:#(?:[0-9a-fA-F]{3}(?:[0-9a-fA-F]{3})?))");

	private static final Pattern NUMBER_OR_PERCENT = Pattern.compile("[0-9]+%?");
	private static final Pattern PARAGRAPH = Pattern.compile("(?:[\\p{L}\\p{N},'\\.\\s\\-_\\(\\)]|&[0-9]{2};)*");
	private static final Pattern HTML_ID = Pattern.compile("[a-zA-Z0-9\\:\\-_\\.]+");
	// force non-empty with a '+' at the end instead of '*'
	private static final Pattern HTML_TITLE = Pattern.compile("[\\p{L}\\p{N}\\s\\-_',:\\[\\]!\\./\\\\\\(\\)&]*");
	private static final Pattern HTML_CLASS = Pattern.compile("[a-zA-Z0-9\\s\\-_]+");

	private static final Pattern ONSITE_URL = Pattern.compile("(?:[\\p{L}\\p{N}\\\\\\.\\#@\\$%\\+&;\\-_~,\\?=/!]+|\\#(\\w)+)");
	private static final Pattern OFFSITE_URL = Pattern.compile("\\s*(?:https?://|mailto:)[\\p{L}\\p{N}][\\p{L}\\p{N}\\p{Zs}\\.\\#@\\$%\\+&;:\\-_~,\\?=/!\\(\\)]*+\\s*");
	private static final Pattern DATA_URL = Pattern.compile("data:image/[^,]+,.+");//

	private static final Pattern NUMBER = Pattern.compile("[0-9]+");

	private static final Pattern NAME = Pattern.compile("[a-zA-Z0-9\\-_\\$]+");

	private static final Pattern ALIGN = Pattern.compile("(?i)center|left|right|justify|char");

	private static final Pattern VALIGN = Pattern.compile("(?i)baseline|bottom|middle|top");

	private static final Predicate<String> COLOR_NAME_OR_COLOR_CODE = matchesEither(COLOR_NAME, COLOR_CODE);

	private static final Predicate<String> ONSITE_OR_OFFSITE_URL = matchesEither(ONSITE_URL, OFFSITE_URL);
	private static final Predicate<String> ONSITE_OR_OFFSITE_OR_DATA_URL = matchesEither(ONSITE_URL, OFFSITE_URL, DATA_URL);
	


	private static final Pattern ONE_CHAR = Pattern.compile(".?", Pattern.DOTALL);

	/**
	 * A policy that can be used to produce policies that sanitize to HTML sinks
	 * via {@link PolicyFactory#apply}.
	 */
	public static final PolicyFactory POLICY_DEFINITION = new HtmlPolicyBuilder()
			.allowAttributes("id").matching(HTML_ID).globally()
			.allowAttributes("class").matching(HTML_CLASS).globally()
			.allowAttributes("lang").matching(Pattern.compile("[a-zA-Z]{2,20}")).globally()
			.allowAttributes("title").matching(HTML_TITLE).globally()
			.allowStyling()
			.allowAttributes("align").matching(ALIGN).onElements("p")
			.allowAttributes("color").matching(COLOR_NAME_OR_COLOR_CODE).onElements("font")
			.allowAttributes("face").matching(Pattern.compile("[\\w;, \\-]+")).onElements("font")
			.allowAttributes("size").matching(NUMBER).onElements("font")
			.allowAttributes("href").matching(ONSITE_OR_OFFSITE_URL).onElements("a")
			.allowUrlProtocols("mailto", "data", "http", "https")
			.allowAttributes("target").matching(false, "_blank", "_top", "_self").onElements("a")
			.allowAttributes("nohref").onElements("a")
			.allowAttributes("name").matching(NAME).onElements("a")
			.requireRelNofollowOnLinks()
			.allowAttributes("src").matching(ONSITE_OR_OFFSITE_OR_DATA_URL).onElements("img")
			.allowAttributes("name").matching(NAME).onElements("img")
			.allowAttributes("alt").matching(PARAGRAPH).onElements("img")
			.allowAttributes("border", "hspace", "vspace").matching(NUMBER).onElements("img")
			.allowAttributes("border", "cellpadding", "cellspacing").matching(NUMBER).onElements("table")
			.allowAttributes("bgcolor").matching(COLOR_NAME_OR_COLOR_CODE).onElements("table")
			.allowAttributes("background").matching(ONSITE_URL).onElements("table")
			.allowAttributes("align").matching(ALIGN).onElements("table")
			.allowAttributes("noresize").matching(Pattern.compile("(?i)noresize")).onElements("table")
			.allowAttributes("background").matching(ONSITE_URL).onElements("td", "th", "tr")
			.allowAttributes("bgcolor").matching(COLOR_NAME_OR_COLOR_CODE).onElements("td", "th")
			.allowAttributes("abbr").matching(PARAGRAPH).onElements("td", "th")
			.allowAttributes("axis", "headers").matching(NAME).onElements("td", "th")
			.allowAttributes("scope").matching(Pattern.compile("(?i)(?:row|col)(?:group)?")).onElements("td", "th")
			.allowAttributes("nowrap").onElements("td", "th")
			.allowAttributes("height", "width").matching(NUMBER_OR_PERCENT).onElements("table", "td", "th", "tr", "img")
			.allowAttributes("align").matching(ALIGN).onElements("thead", "tbody", "tfoot", "img", "td", "th", "tr", "colgroup", "col")
			.allowAttributes("valign").matching(VALIGN).onElements("thead", "tbody", "tfoot", "td", "th", "tr", "colgroup", "col")
			.allowAttributes("charoff").matching(NUMBER_OR_PERCENT).onElements("td", "th", "tr", "colgroup", "col", "thead", "tbody", "tfoot")
			.allowAttributes("char").matching(ONE_CHAR).onElements("td", "th", "tr", "colgroup", "col", "thead", "tbody", "tfoot")
			.allowAttributes("colspan", "rowspan").matching(NUMBER).onElements("td", "th")
			.allowAttributes("span", "width").matching(NUMBER_OR_PERCENT).onElements("colgroup", "col")
			.allowAttributes("size", "name", "disabled").onElements("select")
			.allowAttributes("selected", "value", "autocomplete").onElements("option")
			.allowAttributes("type", "name", "disabled", "value", "autocomplete", "pattern", "size", "maxlength").onElements("input")
			.allowAttributes("type").onElements("ol")
			.allowElements("a", "h1", "h2", "h3", "h4", "h5", "h6", "p", "i", "b", "u", "strong", "em", "small", "big", "pre", "code", "cite", "samp", "sub", "sup", "strike", "center", "blockquote", "hr", "br", "col", "font", "map", "span", "div", "img", "ul", "ol", "li", "dd", "dt", "dl", "tbody", "thead", "tfoot", "table", "td", "th", "tr", "colgroup", "fieldset", "legend", "input", "select", "option")
			.toFactory();

	private static Predicate<String> matchesEither(final Pattern a, final Pattern b) {
		return new Predicate<>() {
			public boolean test(String s) {
				return a.matcher(s).matches() || b.matcher(s).matches();
			}
		};
	}

	private static Predicate<String> matchesEither(final Pattern a, final Pattern b, final Pattern c) {
		return new Predicate<>() {
			public boolean test(String s) {
				return a.matcher(s).matches() || b.matcher(s).matches() || c.matcher(s).matches();
			}
		};
	}
}
