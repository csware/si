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

import java.util.*;
import java.util.regex.Pattern;

public class RegexBasedHaskellClustering {
    private static final LinkedHashMap<String, Pattern> CLUSTERS = new LinkedHashMap<>();

    static {
        CLUSTERS.put("Parse-Fehler", Pattern.compile("\\bparse\\s+error\\b", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Typenkonflikt", Pattern.compile("couldn'?t match (expected type|type)", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Variable nicht im Gültigkeitsbereich", Pattern.compile("not in scope", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Leerer do-Block", Pattern.compile("empty\\s+'do'\\s+block", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Unendlicher Typ", Pattern.compile("occurs check:.*infinite type", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Modul nicht gefunden", Pattern.compile("could not find module", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Fehlendes Binding", Pattern.compile("type signature.*lacks an accompanying binding", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Fehlende Instanz", Pattern.compile("no instance for", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Abweichende Arity", Pattern.compile("equations for .* have different numbers of arguments", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Doppelte Signatur", Pattern.compile("duplicate type signatures?", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Mehrdeutiger Bezeichner", Pattern.compile("ambiguous occurrence", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Syntaxfehler", Pattern.compile("syntax error", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Ungültige Typensignatur", Pattern.compile("invalid type signature", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Mehrfache Deklarationen", Pattern.compile("multiple declarations", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Fehlerhafter Datenkonstruktor", Pattern.compile("cannot parse data constructor in a data/newtype declaration", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Fehlerhafter Typ-Header", Pattern.compile("malformed head of type or class declaration", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Lexikalischer Fehler", Pattern.compile("lexical error at character", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Kind-Konflikt", Pattern.compile("expected kind .* but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Mehrdeutiger Typ", Pattern.compile("ambiguous type variable", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Doppelte Instanz", Pattern.compile("duplicate instance declarations", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Fehlende Constraint", Pattern.compile("could not deduce.*\\(", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Konfliktierende Bindings", Pattern.compile("conflicting definitions for", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Methode nicht in Klasse", Pattern.compile("is not a \\(visible\\) method of class", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Ungültige Instanz-Signatur", Pattern.compile("illegal type signature in instance declaration", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Unvollständiger Typ", Pattern.compile("expecting one more argument to .*has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Constraint erwartet, aber Typ erhalten", Pattern.compile("expected a constraint, but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Pattern Binding in Instanz", Pattern.compile("pattern bindings.*not allowed in instance declaration", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Falsche Konstruktor-Arity", Pattern.compile("the constructor ‘.*’ should have \\d+ argument[s]?, but has been given \\d+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Typed Hole", Pattern.compile("found hole: _ ::", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Ungültige Binding-Syntax", Pattern.compile("illegal binding of built-in syntax", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Letzte Anweisung im 'do'-Block", Pattern.compile("the last statement in a 'do' block must be an expression", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Überlappende Instanzen", Pattern.compile("overlapping instances for", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Ungültiges Enum-Deriving", Pattern.compile("can't make a derived instance of ['‘`]Enum", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Ungültige Instanz-Form", Pattern.compile("illegal instance declaration.*flexibleinstances", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Kein Datenkonstruktor", Pattern.compile("not a data constructor", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Ungültiges Deriving", Pattern.compile("illegal deriving item", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Flexible Kontexte benötigt", Pattern.compile("non type-variable argument in the constraint", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Erneut ungültige Typensignatur", Pattern.compile("illegal type signature", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Fehlende GADTs-Erweiterung", Pattern.compile("enable the GADTs extension", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Kind-Konflikt (Constraint vs. Typ)", Pattern.compile("expected (a constraint|a type), but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        CLUSTERS.put("Ungültiger Typ-Operator", Pattern.compile("illegal operator .* in type .*", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Fehlende Klammern im Range-Ausdruck", Pattern.compile("a section must be enclosed in parentheses", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Warnung", Pattern.compile("warning", Pattern.CASE_INSENSITIVE));
        CLUSTERS.put("Sonstiger Fehler", Pattern.compile(".*", Pattern.DOTALL));
    }
    public final static String classify(String stderr) {

        for (Map.Entry<String, Pattern> entry : CLUSTERS.entrySet()) {
            if (entry.getValue().matcher(stderr).find()) {
                return entry.getKey();
            }
        }
        return "Sonstige Fehler";
    }
}
