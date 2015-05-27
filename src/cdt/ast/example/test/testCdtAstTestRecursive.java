package cdt.ast.example.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;

public class testCdtAstTestRecursive {
	private FileContent fileContent;
	private Map definedSymbols;
	private String[] includePaths;
	private IScannerInfo scannerInfo;
	private IParserLogService log;
	private IncludeFileContentProvider emptyIncludes;
	private IASTCompletionNode translationUnit;
	private IASTNode declaration;

	@Before
	public void setUP() throws CoreException{
		fileContent = FileContent.createForExternalFileLocation("e:\\test.h");
		definedSymbols = new HashMap();
		includePaths = new String[] {"e:\\include\""};
		scannerInfo = new ScannerInfo(definedSymbols, includePaths);
		log = new DefaultLogService();
//		emptyIncludes = IncludeFileContentProvider.getSavedFilesProvider();
		emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();
		int opts = 8;
		translationUnit = GPPLanguage.getDefault().getCompletionNode(fileContent, scannerInfo, emptyIncludes, null, log, opts);
//		IScope scope = translationUnit.getScope();
//		IBinding [] bindings = scope.find("Outter");
//		IASTName[] name = translationUnit.getDeclarationsInAST(bindings[0]);
//		declaration = name[0].getParent();
	}
	
	@Test
	public void test() {
		DeclarationVisitor visitor = new DeclarationVisitor("");
		translationUnit.getTranslationUnit().accept(visitor);
	}

}
