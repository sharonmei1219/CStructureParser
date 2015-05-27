package cdt.ast.example.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Expectation;

public class testExpansion {
	private FileContent fileContent;
	private Map definedSymbols;
	private String[] includePaths;
	private IScannerInfo scannerInfo;
	private IParserLogService log;
	private IncludeFileContentProvider emptyIncludes;
	private IASTTranslationUnit translationUnit;
	private Mockery context;
	private ExtentionCommand command;
	private ExpansionVisitor visitor;

	@Before
	public void setUP() throws CoreException{
		definedSymbols = new HashMap();
		includePaths = new String[0];
		scannerInfo = new ScannerInfo(definedSymbols, includePaths);
		log = new DefaultLogService();
		emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();
		context = new Mockery();
		command = context.mock(ExtentionCommand.class);
		visitor = new ExpansionVisitor("", command);
	}

	@Test
	public void testTypedefineFromNameToConceretePrimaryDefinition() throws CoreException {
		String code = "typedef int tmp;\n"
				    + "typedef tmp u32;"
				    + "struct A { u32 a;}";
		
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		IASTDeclSpecifier specifier = simpleDeclaration.getDeclSpecifier();
		IType type = ExpansionUtility.specificationToType(specifier);
		assertEquals("int", type.toString());

	}
	
	@Test
	public void testTypedefineFromNameToConcereteStructureDefinition() throws CoreException {
		String code = "struct B {int a}; \n"
				    + "typedef struct B tmp;\n"
				    + "typedef tmp u32;"
				    + "struct A { u32 a;}";
		
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		IASTDeclSpecifier specifier = simpleDeclaration.getDeclSpecifier();
		IType type = ExpansionUtility.specificationToType(specifier);
		assertEquals("B", type.toString());
	}
	
	@Test
	public void testArrayDimensionOfLiteralExpression() throws CoreException{
		String code = "struct A { int a[10];}";
		CPPASTArrayDeclarator arrayDeclarator = (CPPASTArrayDeclarator)getFirstArrayDeclarator(code, "A");
		assertEquals(10, ExpansionUtility.getArrayDimension(arrayDeclarator));
	}
	
	@Test
	public void testArrayDimensionOfLiteralDefinedByMacroExpression() throws CoreException{
		String code = "#define maxSize 10\n"
				    + "struct A { int a[maxSize];}";
		CPPASTArrayDeclarator arrayDeclarator = (CPPASTArrayDeclarator)getFirstArrayDeclarator(code, "A");
		assertEquals(10, ExpansionUtility.getArrayDimension(arrayDeclarator));
	}
	
	@Test
	public void testArrayDimensionOfIDExpression() throws CoreException{
		String code = "const int maxSize = 10;\n"
				    + "struct A { int a[maxSize];}";
		CPPASTArrayDeclarator arrayDeclarator = (CPPASTArrayDeclarator)getFirstArrayDeclarator(code, "A");
		assertEquals(10, ExpansionUtility.getArrayDimension(arrayDeclarator));
	}

	@Test
	public void testArrayDimensionOfBinaryExpression() throws CoreException{
		String code = "struct A { int a[10 + 2 + 3];}";
		CPPASTArrayDeclarator arrayDeclarator = (CPPASTArrayDeclarator)getFirstArrayDeclarator(code, "A");
		assertEquals(15, ExpansionUtility.getArrayDimension(arrayDeclarator));
	}

	@Test
	public void testSpecificationToTypeCPPASTElaboratedTypeSpecifierToCppClassType() throws CoreException {
		String code = "struct B {int b}; \n"
				    + "struct A {struct B a;}";
		
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		IASTDeclSpecifier specifier = simpleDeclaration.getDeclSpecifier();
		IType type = ExpansionUtility.specificationToType(specifier);
		assertEquals("B", type.toString());
	}
	
	@Test
	public void testExpandStructureOfAllPrimaryTypeElement() throws CoreException{
		String code = "struct A {\n"
				        + "int a;};";
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		context.checking(new Expectations(){{
			oneOf(command).havingSimpleType("a");
		}});
		visitor.visit(simpleDeclaration);
		context.assertIsSatisfied();
	}
	
	@Test
	public void testExpandStructureOfStructure() throws CoreException{
		String code = "struct B{\n"
					+ "    int b;"
					+ "};"
				    + "struct A {\n" 
	                + "    struct B a;"
	                + "};";
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		context.checking(new Expectations() {{
				oneOf(command).havingSimpleType("a.b");
			}});
		visitor.visit(simpleDeclaration);
		context.assertIsSatisfied();
	}
	
	@Test
	public void testExpandStructureOfStructureDefinedInARow() throws CoreException{
		String code = "struct B{\n"
					+ "    int b;"
					+ "};"
				    + "struct A {\n" 
	                + "    struct B a, c;"
	                + "};";
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		context.checking(new Expectations() {{
				oneOf(command).havingSimpleType("a.b");
				oneOf(command).havingSimpleType("c.b");
			}});
		visitor.visit(simpleDeclaration);
		context.assertIsSatisfied();
	}

	@Test
	public void testExpandStructureOfStructureRenamedByTypeDef() throws CoreException{
		String code = "struct B{\n"
					+ "    int b;"
					+ "};"
					+ "typedef struct B renamedB;"
				    + "struct A {\n" 
	                + "    renamedB a;"
	                + "};";
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		context.checking(new Expectations() {{
				oneOf(command).havingSimpleType("a.b");
			}});
		visitor.visit(simpleDeclaration);
		context.assertIsSatisfied();
	}
	
	@Test
	public void testExpandStructureOfPrimaryTypeRenamedByTypeDef() throws CoreException{
		String code = "typedef int renamedInt;"
				    + "struct A {\n" 
	                + "    renamedInt a;"
	                + "};";
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		context.checking(new Expectations() {{
				oneOf(command).havingSimpleType("a");
			}});
		visitor.visit(simpleDeclaration);
		context.assertIsSatisfied();
	}
	
	@Test
	public void testExpandArrayOfPrimaryType() throws CoreException{
		String code = "struct A {\n" 
	                + "    int a[2]; \n"
	                + "};";
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		context.checking(new Expectations() {{
				oneOf(command).havingSimpleType("a[0]");
				oneOf(command).havingSimpleType("a[1]");
			}});
		visitor.visit(simpleDeclaration);
		context.assertIsSatisfied();
	}
	
	@Test
	public void testExpandArrayOfStructureType() throws CoreException{
		String code = "struct B {int b;}; \n"
					+ "struct A {\n" 
	                + "    struct B a[2]; \n"
	                + "};";
		IASTSimpleDeclaration simpleDeclaration = getFirstSimpleDeclaration(code, "A");
		context.checking(new Expectations() {{
				oneOf(command).havingSimpleType("a[0].b");
				oneOf(command).havingSimpleType("a[1].b");
			}});
		visitor.visit(simpleDeclaration);
		context.assertIsSatisfied();
	}

	private IASTSimpleDeclaration getFirstSimpleDeclaration(String code, String specName)
			throws CoreException {
		CPPASTCompositeTypeSpecifier declaration = (CPPASTCompositeTypeSpecifier)findDeclaration(code, specName);
		IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)declaration.getDeclarations(true)[0];
		return simpleDeclaration;
	}
	
	private CPPASTArrayDeclarator getFirstArrayDeclarator(String code, String specName) throws CoreException{
		IASTSimpleDeclaration declaration = getFirstSimpleDeclaration(code, specName);
		return (CPPASTArrayDeclarator)(declaration.getDeclarators()[0]);
	}

	private IASTNode findDeclaration(String code, String declarationName) throws CoreException {
		fileContent = FileContent.create("<test-code>", code.toCharArray());
		translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, scannerInfo, emptyIncludes, null, 8, log);
		IScope scope = translationUnit.getScope();
		IBinding [] bindings = scope.find(declarationName);
		IASTName[] name = translationUnit.getDeclarationsInAST(bindings[0]);
		return name[0].getParent();
	}

}
