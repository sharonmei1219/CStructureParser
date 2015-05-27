package cdt.ast.example.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;

public class TestParseWithScanner {
	private FileContent fileContent;
	private Map definedSymbols;
	private String[] includePaths;
	private IScannerInfo scannerInfo;
	private IParserLogService log;
	private IncludeFileContentProvider emptyIncludes;
	private IASTTranslationUnit translationUnit;
	private IASTNode declaration;
	private GNUCPPSourceParser parser;

	@Before
	public void setUP() throws CoreException{
		fileContent = FileContent.createForExternalFileLocation("e:\\test.h");
		definedSymbols = new HashMap();
		includePaths = new String[] {"e:\\include\\*"};
		scannerInfo = new ScannerInfo(definedSymbols, includePaths);
		
		IScanner scanner = createScanner(fileContent, ParserLanguage.C, scannerInfo);
		parser = new GNUCPPSourceParser(scanner,
				                        ParserMode.STRUCTURAL_PARSE,
				                        new NullLogService(),
				                        new GPPParserExtensionConfiguration());
		
	}
	
	public static IScanner createScanner(FileContent codeReader,
			                             ParserLanguage language,
			                             IScannerInfo scannerInfo){
		return new CPreprocessor(codeReader,
				                 scannerInfo,
				                 language,
				                 new DefaultLogService(),
				                 GCCScannerExtensionConfiguration.getInstance(),
				                 IncludeFileContentProvider.getSavedFilesProvider());
	}
	
	@Test
	public void test() {
		IASTTranslationUnit ast = parser.parse();
		TVisitor visitor = new TVisitor();
		ast.accept(visitor);
	}
	
	class TVisitor extends ASTVisitor{
		public TVisitor(){
			this.shouldVisitDeclarations = true;
		}
		public int visit(IASTDeclaration declr){
			CPPASTSimpleDeclaration sd = (CPPASTSimpleDeclaration)declr;
			IASTDeclSpecifier specifier = sd.getDeclSpecifier();
			if(specifier instanceof CPPASTElaboratedTypeSpecifier){
				IASTName name = ((CPPASTElaboratedTypeSpecifier)specifier).getName();
				IBinding binding = name.resolveBinding();
				CPPClassType type = (CPPClassType)binding;
				System.out.println("---------Specifier---------");
				System.out.println(binding);
//				System.out.println("---------Definition---------");
//				System.out.println(type.getCompositeTypeSpecifier().getRawSignature());
				System.out.println();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}
	};

}
