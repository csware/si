/*
 * Copyright 2025 Sven Strickroth <email@cs-ware.de>
 * Copyright 2025 Esat Avci <e.avci@campus.lmu.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tuclausthal.submissioninterface.testanalyzer.haskell.syntax;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class RegexBasedHaskellClustering {
	private static final LinkedHashMap<String, Pattern> CLUSTERS = new LinkedHashMap<>();

	private RegexBasedHaskellClustering() {}

	static {
		// Parse-Errors
		CLUSTERS.put("GHCi Kontext in Abgabe", Pattern.compile("(^|\\n).*?(ghci>|Prelude>|parse error on input\\s+‘(:\\{|}:)’|:\\{|}:)", Pattern.MULTILINE));
		CLUSTERS.put("Ungültige Top-Level-Deklaration", Pattern.compile("Parse error: module header, import declaration\\s+or\\s+top-level declaration expected\\.", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Parse-Fehler durch Import-Fehler", Pattern.compile("(parse error on input)[\\s\\S]+?‘?import", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Parse-Fehler in 'let'-Binding", Pattern.compile("\\(let.*in.*\\)-syntax\\s+in\\s+pattern|parse\\s+error\\s*\\(possibly\\s+incorrect\\s+indentation[^)]*\\)[\\s\\S]*?\\n\\s*\\d+\\s*\\|\\s+.*\\blet\\b[^\\n]*=", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Parse-Fehler in Funktionsdeklaration", Pattern.compile("parse error.*?\\n\\s*\\|\\s*(\\d+)\\s*\\|\\s([a-z]\\w*)\\s*::", Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Parse-Fehler", Pattern.compile("\\bparse\\s+error\\b", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Typed Hole", Pattern.compile("found hole: _ ::", Pattern.CASE_INSENSITIVE));

		// Type Errors
		CLUSTERS.put("Falsche Funktionsarität", Pattern.compile("applied to too (?:few|many) value arguments|applied to \\w+ value arguments,.*?\\bbut its type.*?has only \\w+|\\bhas \\w+ arguments, but its type .*? has only \\w+|is applied to .*? (?:visible )?arguments,.*?but its type .*? has only", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Inkonsistenter Rückgabetyp", Pattern.compile("Couldn't match type[:\\s]*.*with[:\\s]*.*In a case alternative", Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Implementierung verletzt Typsignatur", Pattern.compile("is a rigid type variable bound by", Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Numerischer Typenkonflikt", Pattern.compile("No instance for .*Num .*|No instance for .*Fractional .*|Couldn't match expected type\\s+.(Double|Float|Rational|Int|Integer|Num\\s+a\\d*).\\s+with actual type\\s+.(Double|Float|Rational|Int|Integer|Num\\s+a\\d*).", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Doppelte Signatur", Pattern.compile("duplicate type signatures?", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Typenkonflikt", Pattern.compile("couldn'?t match (expected type|type)", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Unendlicher Typ", Pattern.compile("occurs check:.*infinite type", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Mehrfache Deklarationen", Pattern.compile("multiple declarations", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Mehrdeutiger Bezeichner", Pattern.compile("ambiguous occurrence", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Ungültiger Typ-Operator", Pattern.compile("illegal operator .* in type .*", Pattern.CASE_INSENSITIVE));

		// Not in scope
		CLUSTERS.put("Typenkonstruktor oder Klasse nicht definiert", Pattern.compile("Not in scope: type constructor or class ‘[A-Z][a-zA-Z0-9_']*’", Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Nicht definierter Datenkonstruktor", Pattern.compile("Not in scope: data constructor ‘[A-Z][a-zA-Z0-9_']*’", Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Funktion nicht definiert", Pattern.compile("Variable not in scope: ([a-zA-Z_][a-zA-Z0-9_']*)\\s*::\\s*[^:\\n]+->.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Variable nicht im Gültigkeitsbereich", Pattern.compile("not in scope", Pattern.CASE_INSENSITIVE));

		// Binding and Signature
		CLUSTERS.put("Pattern Binding in Instanz", Pattern.compile("pattern bindings.*not allowed in instance declaration", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Fehlendes Binding", Pattern.compile("type signature.*lacks an accompanying binding", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Falsche Arität für Konstruktor", Pattern.compile("the (?:data )?constructor .* should have \\d+ argument[s]?, but has been given \\d+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Abweichende Arity", Pattern.compile("equations for .* have different numbers of arguments", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Constraint erwartet, aber Typ erhalten", Pattern.compile("expected a constraint, but .*(has kind|is a type)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Ungültige Instanz-Signatur", Pattern.compile("illegal type signature in instance declaration", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Ungültige Typensignatur", Pattern.compile("((invalid|illegal) type signature|Invalid data constructor .* in type signature)", Pattern.CASE_INSENSITIVE));

		// instance and class
		CLUSTERS.put("Überlappende Instanzen", Pattern.compile("overlapping instances for", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Fehlende Constraint bei Funktionssignatur", Pattern.compile("No instance for [(‘]\\w+ [a-z][)’] arising from a use of", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Fehlende Superklassen-Instanz", Pattern.compile("no\\s+instance\\s+for.*arising\\s+from\\s+the\\s+superclasses", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Fehlende Instanz bei 'deriving'", Pattern.compile("When deriving the instance for", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Fehlende Instanz", Pattern.compile("no instance for", Pattern.CASE_INSENSITIVE));

		// definition and declaration
		CLUSTERS.put("Konflikt in 'data'-Deklaration", Pattern.compile("Conflicting definitions for\\s+['‘`]?.+?['’`]?\\s+.*?\\n\\s*\\|\\s*\\n\\s*\\d+\\s*\\|\\s*data", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Mehrfachdefinition in Funktionsgleichung", Pattern.compile("conflicting definitions for.*in an equation for", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Konfliktierende Bindings", Pattern.compile("conflicting definitions for", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Modul nicht gefunden", Pattern.compile("could not find module", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Fehler mit Datenkonstruktoren", Pattern.compile("(cannot parse data constructor in a data/newtype declaration|not a data constructor)", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Mehrfache Instanzdeklaration", Pattern.compile("duplicate instance declarations", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Methode nicht in Klasse", Pattern.compile("is not a \\(visible\\) method of class", Pattern.CASE_INSENSITIVE));

		// others
		CLUSTERS.put("Ungültige Instanz-Form", Pattern.compile("illegal instance declaration|Use FlexibleInstances", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Falsche Anzahl von Typ-Argumenten", Pattern.compile("expecting one more argument to .*has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Kind-Konflikt", Pattern.compile("expected kind .* but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Kind-Konflikt (Constraint vs. Typ)", Pattern.compile("expected (a constraint|a type), but .* (?:has kind|is a (?:constraint|type))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
		CLUSTERS.put("Mehrdeutiger Typ", Pattern.compile("ambiguous type variable", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Constraint nicht erfüllbar", Pattern.compile("could not deduce", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Flexible Kontexte benötigt", Pattern.compile("non type-variable argument in the constraint", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Fehlende GADTs-Erweiterung", Pattern.compile("enable the GADTs extension", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Ungültiges Zeichen", Pattern.compile("syntax error", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Lexikalischer Fehler", Pattern.compile("(lexical error at character|Unicode character .+ looks like .+ but it is not)", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Fehlerhafter Typ-Header", Pattern.compile("malformed head of type or class declaration", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Leerer do-Block", Pattern.compile("empty\\s+'do'\\s+block", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Letzte Anweisung im 'do'-Block", Pattern.compile("the last statement in a 'do' block must be an expression", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Ungültige Binding-Syntax", Pattern.compile("illegal binding of (?:built-in syntax|an existing name)", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Fehlende Klammern im Range-Ausdruck", Pattern.compile("a section must be enclosed in parentheses", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Ungültiges Enum-Deriving", Pattern.compile("(?:can't|Can't) make a derived instance of [‘'`]Enum\\b", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Ungültiges Deriving", Pattern.compile("illegal deriving item", Pattern.CASE_INSENSITIVE));

		// fallback
		CLUSTERS.put("Warnung", Pattern.compile("warning", Pattern.CASE_INSENSITIVE));
		CLUSTERS.put("Sonstiger Fehler", Pattern.compile(".*", Pattern.DOTALL));
	}

	public static String classify(String stderr) {
		for (Map.Entry<String, Pattern> entry : CLUSTERS.entrySet()) {
			if (entry.getValue().matcher(stderr).find()) {
				return entry.getKey();
			}
		}
		return "Sonstige Fehler";
	}
}
