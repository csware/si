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

import de.tuclausthal.submissioninterface.persistence.dao.CommonErrorDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError.Type;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import org.hibernate.Session;

public class RegexBasedHaskellClustering implements HaskellErrorClassifierIf {

    private final Session session;
    private final LinkedHashMap<String, Pattern> clusters;

    public RegexBasedHaskellClustering(Session session) {
        this.session = session;
        this.clusters = new LinkedHashMap<>();

        // Muster in Priorisierungsreihenfolge eintragen
        clusters.put("Parse-Fehler", Pattern.compile("\\bparse\\s+error\\b", Pattern.CASE_INSENSITIVE));
        clusters.put("Typenkonflikt", Pattern.compile("couldn'?t match (expected type|type)", Pattern.CASE_INSENSITIVE));
        clusters.put("Variable nicht im Gültigkeitsbereich", Pattern.compile("not in scope", Pattern.CASE_INSENSITIVE));
        clusters.put("Leerer do-Block", Pattern.compile("empty\\s+'do'\\s+block", Pattern.CASE_INSENSITIVE));
        clusters.put("Unendlicher Typ", Pattern.compile("occurs check:.*infinite type", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Modul nicht gefunden", Pattern.compile("could not find module", Pattern.CASE_INSENSITIVE));
        clusters.put("Fehlendes Binding", Pattern.compile("type signature.*lacks an accompanying binding", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Fehlende Instanz", Pattern.compile("no instance for", Pattern.CASE_INSENSITIVE));
        clusters.put("Abweichende Arity", Pattern.compile("equations for .* have different numbers of arguments", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Doppelte Signatur", Pattern.compile("duplicate type signatures?", Pattern.CASE_INSENSITIVE));
        clusters.put("Mehrdeutiger Bezeichner", Pattern.compile("ambiguous occurrence", Pattern.CASE_INSENSITIVE));
        clusters.put("Syntaxfehler", Pattern.compile("syntax error", Pattern.CASE_INSENSITIVE));
        clusters.put("Ungültige Typensignatur", Pattern.compile("invalid type signature", Pattern.CASE_INSENSITIVE));
        clusters.put("Mehrfache Deklarationen", Pattern.compile("multiple declarations", Pattern.CASE_INSENSITIVE));
        clusters.put("Fehlerhafter Datenkonstruktor", Pattern.compile("cannot parse data constructor in a data/newtype declaration", Pattern.CASE_INSENSITIVE));
        clusters.put("Fehlerhafter Typ-Header", Pattern.compile("malformed head of type or class declaration", Pattern.CASE_INSENSITIVE));
        clusters.put("Lexikalischer Fehler", Pattern.compile("lexical error at character", Pattern.CASE_INSENSITIVE));
        clusters.put("Kind-Konflikt", Pattern.compile("expected kind .* but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Mehrdeutiger Typ", Pattern.compile("ambiguous type variable", Pattern.CASE_INSENSITIVE));
        clusters.put("Doppelte Instanz", Pattern.compile("duplicate instance declarations", Pattern.CASE_INSENSITIVE));
        clusters.put("Fehlende Constraint", Pattern.compile("could not deduce.*\\(", Pattern.CASE_INSENSITIVE));
        clusters.put("Konfliktierende Bindings", Pattern.compile("conflicting definitions for", Pattern.CASE_INSENSITIVE));
        clusters.put("Methode nicht in Klasse", Pattern.compile("is not a \\(visible\\) method of class", Pattern.CASE_INSENSITIVE));
        clusters.put("Ungültige Instanz-Signatur", Pattern.compile("illegal type signature in instance declaration", Pattern.CASE_INSENSITIVE));
        clusters.put("Unvollständiger Typ", Pattern.compile("expecting one more argument to .*has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Constraint erwartet, aber Typ erhalten", Pattern.compile("expected a constraint, but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Pattern Binding in Instanz", Pattern.compile("pattern bindings.*not allowed in instance declaration", Pattern.CASE_INSENSITIVE));
        clusters.put("Falsche Konstruktor-Arity", Pattern.compile("the constructor ‘.*’ should have \\d+ argument[s]?, but has been given \\d+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Typed Hole", Pattern.compile("found hole: _ ::", Pattern.CASE_INSENSITIVE));
        clusters.put("Ungültige Binding-Syntax", Pattern.compile("illegal binding of built-in syntax", Pattern.CASE_INSENSITIVE));
        clusters.put("Letzte Anweisung im 'do'-Block", Pattern.compile("the last statement in a 'do' block must be an expression", Pattern.CASE_INSENSITIVE));
        clusters.put("Überlappende Instanzen", Pattern.compile("overlapping instances for", Pattern.CASE_INSENSITIVE));
        clusters.put("Ungültiges Enum-Deriving", Pattern.compile("can't make a derived instance of ['‘`]Enum", Pattern.CASE_INSENSITIVE));
        clusters.put("Ungültige Instanz-Form", Pattern.compile("illegal instance declaration.*flexibleinstances", Pattern.CASE_INSENSITIVE));
        clusters.put("Kein Datenkonstruktor", Pattern.compile("not a data constructor", Pattern.CASE_INSENSITIVE));
        clusters.put("Ungültiges Deriving", Pattern.compile("illegal deriving item", Pattern.CASE_INSENSITIVE));
        clusters.put("Flexible Kontexte benötigt", Pattern.compile("non type-variable argument in the constraint", Pattern.CASE_INSENSITIVE));
        clusters.put("Erneut ungültige Typensignatur", Pattern.compile("illegal type signature", Pattern.CASE_INSENSITIVE));
        clusters.put("Fehlende GADTs-Erweiterung", Pattern.compile("enable the GADTs extension", Pattern.CASE_INSENSITIVE));
        clusters.put("Kind-Konflikt (Constraint vs. Typ)", Pattern.compile("expected (a constraint|a type), but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        clusters.put("Ungültiger Typ-Operator", Pattern.compile("illegal operator .* in type .*", Pattern.CASE_INSENSITIVE));
        clusters.put("Fehlende Klammern im Range-Ausdruck", Pattern.compile("a section must be enclosed in parentheses", Pattern.CASE_INSENSITIVE));
        clusters.put("Warnung", Pattern.compile("warning", Pattern.CASE_INSENSITIVE));
        clusters.put("Sonstiger Fehler", Pattern.compile(".*", Pattern.DOTALL));
    }

    @Override
    public void classify(TestResult testResult, String stderr, String keyStr) {
        CommonErrorDAOIf commonErrorDAO = DAOFactory.CommonErrorDAOIf(session);
        List<String> matchedClusters = new ArrayList<>();

        for (var entry : clusters.entrySet()) {
            if (entry.getValue().matcher(stderr).find()) {
                matchedClusters.add(entry.getKey());
            }
        }


        for (String preferred : clusters.keySet()) {
            if (!preferred.equals("Sonstiger Fehler") && matchedClusters.contains(preferred)) {
                String fullKey = keyStr + preferred;
                CommonError commonError = commonErrorDAO.getCommonError(fullKey, testResult.getTest());
                if (commonError != null) {
                    commonError.getTestResults().add(testResult);
                } else {
                    commonErrorDAO.newCommonError(fullKey, preferred, testResult, Type.CompileTimeError);
                }
                return;
            }
        }

        if (matchedClusters.contains("Sonstiger Fehler")) {
            String fallbackKey = keyStr + "Sonstiger Fehler";
            CommonError commonError = commonErrorDAO.getCommonError(fallbackKey, testResult.getTest());
            if (commonError != null) {
                commonError.getTestResults().add(testResult);
            } else {
                commonErrorDAO.newCommonError(fallbackKey, "Sonstiger Fehler", testResult, Type.CompileTimeError);
            }
            return;
        }

        commonErrorDAO.newCommonError(keyStr + "Nicht klassifiziert", "Unklassifiziert", testResult, null);
    }
}