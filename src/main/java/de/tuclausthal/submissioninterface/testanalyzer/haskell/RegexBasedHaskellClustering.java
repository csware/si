package de.tuclausthal.submissioninterface.testanalyzer.haskell;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.tuclausthal.submissioninterface.persistence.dao.CommonErrorDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError;
import de.tuclausthal.submissioninterface.persistence.datamodel.CommonError.Type;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import org.hibernate.Session;

public class RegexBasedHaskellClustering implements HaskellErrorClassifierIf {

    private final Session session;
    private final Map<String, Pattern> clusters;

    public RegexBasedHaskellClustering(Session session) {
        this.session = session;
        // HINWEIS: Deutsche Beschreibungen der Fehlerarten:
        this.clusters = new LinkedHashMap<>(Map.ofEntries(
                Map.entry("Parse-Fehler", Pattern.compile("\\bparse\\s+error\\b", Pattern.CASE_INSENSITIVE)),
                Map.entry("Typenkonflikt", Pattern.compile("couldn'?t match (expected type|type)", Pattern.CASE_INSENSITIVE)),
                Map.entry("Variable nicht im Gültigkeitsbereich", Pattern.compile("not in scope", Pattern.CASE_INSENSITIVE)),
                Map.entry("Leerer do-Block", Pattern.compile("empty\\s+'do'\\s+block", Pattern.CASE_INSENSITIVE)),
                Map.entry("Unendlicher Typ", Pattern.compile("occurs check:.*infinite type", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Modul nicht gefunden", Pattern.compile("could not find module", Pattern.CASE_INSENSITIVE)),
                Map.entry("Fehlendes Binding", Pattern.compile("type signature.*lacks an accompanying binding", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Fehlende Instanz", Pattern.compile("no instance for", Pattern.CASE_INSENSITIVE)),
                Map.entry("Abweichende Arity", Pattern.compile("equations for .* have different numbers of arguments", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Doppelte Signatur", Pattern.compile("duplicate type signatures?", Pattern.CASE_INSENSITIVE)),
                Map.entry("Mehrdeutiger Bezeichner", Pattern.compile("ambiguous occurrence", Pattern.CASE_INSENSITIVE)),
                Map.entry("Syntaxfehler", Pattern.compile("syntax error", Pattern.CASE_INSENSITIVE)),
                Map.entry("Ungültige Typensignatur", Pattern.compile("invalid type signature", Pattern.CASE_INSENSITIVE)),
                Map.entry("Mehrfache Deklarationen", Pattern.compile("multiple declarations", Pattern.CASE_INSENSITIVE)),
                Map.entry("Fehlerhafter Datenkonstruktor", Pattern.compile("cannot parse data constructor in a data/newtype declaration", Pattern.CASE_INSENSITIVE)),
                Map.entry("Fehlerhafter Typ-Header", Pattern.compile("malformed head of type or class declaration", Pattern.CASE_INSENSITIVE)),
                Map.entry("Lexikalischer Fehler", Pattern.compile("lexical error at character", Pattern.CASE_INSENSITIVE)),
                Map.entry("Kind-Konflikt", Pattern.compile("expected kind .* but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Mehrdeutiger Typ", Pattern.compile("ambiguous type variable", Pattern.CASE_INSENSITIVE)),
                Map.entry("Doppelte Instanz", Pattern.compile("duplicate instance declarations", Pattern.CASE_INSENSITIVE)),
                Map.entry("Fehlende Constraint", Pattern.compile("could not deduce.*\\(", Pattern.CASE_INSENSITIVE)),
                Map.entry("Konfliktierende Bindings", Pattern.compile("conflicting definitions for", Pattern.CASE_INSENSITIVE)),
                Map.entry("Methode nicht in Klasse", Pattern.compile("is not a \\(visible\\) method of class", Pattern.CASE_INSENSITIVE)),
                Map.entry("Ungültige Instanz-Signatur", Pattern.compile("illegal type signature in instance declaration", Pattern.CASE_INSENSITIVE)),
                Map.entry("Unvollständiger Typ", Pattern.compile("expecting one more argument to .*has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Constraint erwartet, aber Typ erhalten", Pattern.compile("expected a constraint, but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Pattern Binding in Instanz", Pattern.compile("pattern bindings.*not allowed in instance declaration", Pattern.CASE_INSENSITIVE)),
                Map.entry("Falsche Konstruktor-Arity", Pattern.compile("the constructor ‘.*’ should have \\d+ argument[s]?, but has been given \\d+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Typed Hole", Pattern.compile("found hole: _ ::", Pattern.CASE_INSENSITIVE)),
                Map.entry("Ungültige Binding-Syntax", Pattern.compile("illegal binding of built-in syntax", Pattern.CASE_INSENSITIVE)),
                Map.entry("Letzte Anweisung im 'do'-Block", Pattern.compile("the last statement in a 'do' block must be an expression", Pattern.CASE_INSENSITIVE)),
                Map.entry("Überlappende Instanzen", Pattern.compile("overlapping instances for", Pattern.CASE_INSENSITIVE)),
                Map.entry("Ungültiges Enum-Deriving", Pattern.compile("can't make a derived instance of ['‘`]Enum", Pattern.CASE_INSENSITIVE)),
                Map.entry("Ungültige Instanz-Form", Pattern.compile("illegal instance declaration.*flexibleinstances", Pattern.CASE_INSENSITIVE)),
                Map.entry("Kein Datenkonstruktor", Pattern.compile("not a data constructor", Pattern.CASE_INSENSITIVE)),
                Map.entry("Ungültiges Deriving", Pattern.compile("illegal deriving item", Pattern.CASE_INSENSITIVE)),
                Map.entry("Flexible Kontexte benötigt", Pattern.compile("non type-variable argument in the constraint", Pattern.CASE_INSENSITIVE)),
                Map.entry("Erneut ungültige Typensignatur", Pattern.compile("illegal type signature", Pattern.CASE_INSENSITIVE)),
                Map.entry("Fehlende GADTs-Erweiterung", Pattern.compile("enable the GADTs extension", Pattern.CASE_INSENSITIVE)),
                Map.entry("Kind-Konflikt (Constraint vs. Typ)", Pattern.compile("expected (a constraint|a type), but .* has kind", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)),
                Map.entry("Ungültiger Typ-Operator", Pattern.compile("illegal operator .* in type .*", Pattern.CASE_INSENSITIVE)),
                Map.entry("Fehlende Klammern im Range-Ausdruck", Pattern.compile("a section must be enclosed in parentheses", Pattern.CASE_INSENSITIVE)),
                Map.entry("Warnung", Pattern.compile("warning", Pattern.CASE_INSENSITIVE)),
                Map.entry("Sonstiger Fehler", Pattern.compile("."))
        ));
    }

    @Override
    public void classify(TestResult testResult, String stderr, String keyStr) {
        CommonErrorDAOIf commonErrorDAO = DAOFactory.CommonErrorDAOIf(session);
        boolean assigned = false;

        for (var entry : clusters.entrySet()) {
            if (entry.getValue().matcher(stderr).find()) {
                String clusterName = entry.getKey();
                CommonError commonError = commonErrorDAO.getCommonError(keyStr + clusterName, testResult.getTest());
                if (commonError != null) {
                    commonError.getTestResults().add(testResult);
                } else {
                    commonErrorDAO.newCommonError(keyStr + clusterName, clusterName, testResult, Type.CompileTimeError);
                }
                assigned = true;
                break;
            }
        }

        if (!assigned) {
            commonErrorDAO.newCommonError(keyStr + "Nicht klassifiziert", "Unklassifiziert", testResult, null);
        }
    }
}
