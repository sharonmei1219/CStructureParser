package cdt.ast.example.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.core.runtime.CoreException;

public class testCdtAst {
	private FileContent fileContent;
	private Map definedSymbols;
	private String[] includePaths;
	private IScannerInfo scannerInfo;
	private IParserLogService log;
	private IncludeFileContentProvider emptyIncludes;
	private IASTTranslationUnit translationUnit;
	IASTNode parent = null;
	@Before
	public void setUP() throws CoreException{
		fileContent = FileContent.createForExternalFileLocation("e:\\test.h");
		definedSymbols = new HashMap();
		includePaths = new String[0];
		scannerInfo = new ScannerInfo(definedSymbols, includePaths);
		log = new DefaultLogService();
		emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();
		int opts = 8;
		translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, scannerInfo, emptyIncludes, null, opts, log);
	}

	@Ignore
//	@Test
	public void testFirstTransverse() throws CoreException {
		IASTPreprocessorIncludeStatement[] includes = translationUnit.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement include : includes){
			System.out.println("include - " + include.getName());
		}
		
		ASTVisitor visitor = new ASTVisitor(){
			public int visit(IASTName name){
				System.out.println("IASTName: " + name.getClass().getSimpleName() + "(" + name.getRawSignature() + ") -> parent: " + name.getParent().getClass().getSimpleName());
				System.out.println("-- isVisible: " + testCdtAst.isVisible(name));
//				System.out.println(name.getRawSignature());
				return 3;
			}
			
			public int visit(IASTDeclSpecifier specifier){
				return 3;
			}
			
			public int visit(IASTDeclaration declaration){
				System.out.println("declaration: " + declaration + "->" + declaration.getRawSignature());
				if((declaration instanceof IASTSimpleDeclaration)){
					IASTSimpleDeclaration ast = (IASTSimpleDeclaration) declaration;
					try{
						System.out.println("--- type: " + ast.getSyntax() + " (childs: " + ast.getChildren().length + ")");
//						IASTNode typedef = ast.getChildren().length == 1 ? ast.getChildren()[0] : ast.getChildren()[1];
						IASTNode typedef = ast.getChildren()[0];
						System.out.println("------ typedef class: " + typedef.getClass());
						System.out.println("------ typedef: " + typedef);
						if(typedef instanceof CPPASTNamedTypeSpecifier){
							IASTName typeName = ((CPPASTNamedTypeSpecifier)typedef).getName();
							IBinding binding = typeName.resolveBinding();
							IName[] subDeclarations =  declaration.getTranslationUnit().getDeclarations(binding);
							System.out.println("--binding: " + ((IASTName)subDeclarations[0]).getParent().getRawSignature());
							System.out.println("--binding: " + declaration.getTranslationUnit().getDeclarations(binding)[0]);
						}
						
						IASTNode[] children = typedef.getChildren();
//						if((children != null) && (children.length > 0 )){
//							System.out.println("");
//						}
						
					}
					catch(ExpansionOverlapsBoundaryException e){
						e.printStackTrace();
					}
					
					IASTDeclarator[] declarators = ast.getDeclarators();
					for (IASTDeclarator declarator : declarators){
						System.out.println("--------declarator");
						System.out.println(declarator.getRawSignature());
					}
				}
				return 3;
			}
		};
		
		visitor.shouldVisitNames = true;
		visitor.shouldVisitDeclarations = true;
		
		System.out.println("-----------------------------------------");
		
		translationUnit.accept(visitor);
	}
	
	@Test
	public void testTransverseDeclarations(){
		ASTVisitor declaratorVisitor = new ASTVisitor(){
			private String prefix = "";	
			
			public int visit(IASTDeclarator declarator){
				System.out.println(prefix + declarator.getRawSignature());
				IASTDeclarator nsDeclarator = declarator.getNestedDeclarator();
				;
				IBinding binding = declarator.getName().resolveBinding();
				System.out.println("--binding--: " + declarator.getTranslationUnit().getDeclarations(binding)[0]);
//				System.out.println(binding.get);
//				System.out.println(declarator.getNestedDeclarator())
				prefix = prefix + declarator.getRawSignature() + ".";
				return 3;
			}
			
			public int leave(IASTDeclarator declarator){
				int prefixLen = prefix.length() - declarator.getRawSignature().length() - 1;
				prefix = prefix.substring(0, prefixLen);
				return 3;
			}
		};
		declaratorVisitor.shouldVisitDeclarators = true;
		

		
		ASTVisitor nameVisitor = new ASTVisitor(){
			public int visit(IASTName name){
				if(name.toString().equals("Outter")){
					System.out.println(name.toString());
					parent = (IASTNode)name.getParent();
				}
				return 3;
			}
		};
		
		ASTVisitor declarationVisitor = new ASTVisitor(){
			public int visit(IASTDeclaration declaration){
				if((declaration instanceof IASTSimpleDeclaration)){
					IASTSimpleDeclaration ast = (IASTSimpleDeclaration) declaration;
					IASTNode typedef = ast.getChildren()[0];
					if(typedef instanceof CPPASTNamedTypeSpecifier){
						IASTName typeName = ((CPPASTNamedTypeSpecifier)typedef).getName();
						IBinding binding = typeName.resolveBinding();
						IName[] subDeclarations =  declaration.getTranslationUnit().getDeclarations(binding);
						System.out.println("--binding: " + ((IASTName)subDeclarations[0]).getParent().getRawSignature());
//						System.out.println("--binding: " + declaration.getTranslationUnit().getDeclarations(binding)[0]);
					}else{
						System.out.println(ast.getChildren()[1].getRawSignature());
					}			
				}else{
					
				}
				return 3;
			}
		};

		class NamedASTVisitor extends ASTVisitor{
			private String prefix = "";
			public NamedASTVisitor(String prefix){
				this.prefix = prefix;
				shouldVisitDeclarations = true;
			}
			
			@Override
			public int visit(IASTDeclaration declaration){
				System.out.println(declaration.getRawSignature());
//				if((declaration instanceof IASTSimpleDeclaration)){
//					IASTSimpleDeclaration ast = (IASTSimpleDeclaration) declaration;
//					IASTDeclSpecifier specifier = ast.getDeclSpecifier();
//					
//					if(specifier instanceof CPPASTElaboratedTypeSpecifier){
//						IASTName typeName = ((CPPASTElaboratedTypeSpecifier)specifier).getName();
//						IBinding binding = typeName.resolveBinding();
//						IName[] subDeclarations =  declaration.getTranslationUnit().getDeclarations(binding);
//						IASTNode sub = ((IASTName)subDeclarations[0]).getParent();
//						NamedASTVisitor newVisitor = new NamedASTVisitor(prefix + ast.getChildren()[1].getRawSignature() + ".");
//						sub.accept(newVisitor);
//
//					}else{
//						System.out.println(specifier.getClass());
//						for (IASTDeclarator declarator : ast.getDeclarators()){
//							if(declarator instanceof IASTArrayDeclarator){
//								IASTArrayDeclarator array = (IASTArrayDeclarator)declarator;
//								IASTExpression dim0 = (array.getArrayModifiers()[0].getConstantExpression());
//								System.out.println(declarator.getName());
//								System.out.println(dim0);
//								System.out.println(array.getArrayModifiers()[1].getConstantExpression());
//							}else{
////								System.out.println(declarator.getRawSignature());
//								System.out.println(prefix + declarator.getName());
//							}
//						}
//					}			
//				}
				return 3;
			}
		};
		
		nameVisitor.shouldVisitNames = true;
		translationUnit.accept(nameVisitor);
		
		System.out.println("-----------------------------------");
		NamedASTVisitor namedAstVisitor = new NamedASTVisitor("");
		parent.accept(namedAstVisitor);
		
		System.out.println("------------------------------------");
		parent.accept(declaratorVisitor);
	}
	
	public static boolean isVisible(IASTNode current){
		return true;
	}
}
