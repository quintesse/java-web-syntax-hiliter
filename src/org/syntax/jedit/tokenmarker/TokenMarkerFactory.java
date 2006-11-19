package org.syntax.jedit.tokenmarker;

public class TokenMarkerFactory {

	private static String[] markerNames = new String[] {
		"batchfile",	"org.syntax.jedit.tokenmarker.BatchFileTokenMarker",
		"bat",			"org.syntax.jedit.tokenmarker.BatchFileTokenMarker",
		"c++",			"org.syntax.jedit.tokenmarker.CCTokenMarker",
		"cpp",			"org.syntax.jedit.tokenmarker.CCTokenMarker",
		"cplusplus",	"org.syntax.jedit.tokenmarker.CCTokenMarker",
		"c",			"org.syntax.jedit.tokenmarker.CTokenMarker",
		"eiffel",		"org.syntax.jedit.tokenmarker.EiffelTokenMarker",
		"html",			"org.syntax.jedit.tokenmarker.HTMLTokenMarker",
		"idl",			"org.syntax.jedit.tokenmarker.IDLTokenMarker",
		"javascript",	"org.syntax.jedit.tokenmarker.JavaScriptTokenMarker",
		"jscript",		"org.syntax.jedit.tokenmarker.JavaScriptTokenMarker",
		"ecmascript",	"org.syntax.jedit.tokenmarker.JavaScriptTokenMarker",
		"java",			"org.syntax.jedit.tokenmarker.JavaTokenMarker",
		"makefile",		"org.syntax.jedit.tokenmarker.MakefileTokenMarker",
		"patch",		"org.syntax.jedit.tokenmarker.PatchTokenMarker",
		"perl",			"org.syntax.jedit.tokenmarker.PerlTokenMarker",
		"pl",			"org.syntax.jedit.tokenmarker.PerlTokenMarker",
		"php",			"org.syntax.jedit.tokenmarker.PHPTokenMarker",
		"plsql",		"org.syntax.jedit.tokenmarker.PLSQLTokenMarker",
		"pl/sql",		"org.syntax.jedit.tokenmarker.PLSQLTokenMarker",
		"properties",	"org.syntax.jedit.tokenmarker.PropsTokenMarker",
		"ini",			"org.syntax.jedit.tokenmarker.PropsTokenMarker",
		"python",		"org.syntax.jedit.tokenmarker.PythonTokenMarker",
		"shellscript",	"org.syntax.jedit.tokenmarker.ShellScriptTokenMarker",
		"shell",		"org.syntax.jedit.tokenmarker.ShellScriptTokenMarker",
		"sh",			"org.syntax.jedit.tokenmarker.ShellScriptTokenMarker",
		"sql",			"org.syntax.jedit.tokenmarker.SQLTokenMarker",
		"tex",			"org.syntax.jedit.tokenmarker.TexTokenMarker",
		"tsql",			"org.syntax.jedit.tokenmarker.TSQLTokenMarker",
		"t-sql",		"org.syntax.jedit.tokenmarker.TSQLTokenMarker",
		"xml",			"org.syntax.jedit.tokenmarker.XMLTokenMarker"
	};

	public static TokenMarker create(String syntax) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		for (int i = 0; i < markerNames.length; i += 2) {
			if (markerNames[i].equalsIgnoreCase(syntax)) {
				return (TokenMarker) Class.forName(markerNames[i + 1]).newInstance();
			}
		}
		return null;
	}
}
