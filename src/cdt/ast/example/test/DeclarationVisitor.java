package cdt.ast.example.test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;

public class DeclarationVisitor extends ASTVisitor {
	private String prefix;
	public DeclarationVisitor(String prefix){
		this.prefix = prefix;
		this.shouldVisitDeclarations = true;
	}
	
	public int visit(IASTDeclaration inputDeclaration){
		if (!(inputDeclaration instanceof IASTSimpleDeclaration))
			return ASTVisitor.PROCESS_SKIP;
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)inputDeclaration;
		IASTDeclSpecifier specifier = declaration.getDeclSpecifier();
		System.out.print(inputDeclaration.getRawSignature());
		
		if(specifier instanceof CPPASTNamedTypeSpecifier){
			IASTName name = ((CPPASTNamedTypeSpecifier)specifier).getName();
			IBinding binding = name.resolveBinding();
			CPPTypedef typedef = (CPPTypedef)binding;
//			System.out.println(typedef.getType().getClass());
//			CPPClassType reftypedef = (CPPClassType) typedef.getType();
//			System.out.println(reftypedef.getCompositeTypeSpecifier().getRawSignature());
//			System.out.println(reftypedef.get)

//			System.out.println(binding.getClass());
//			IASTNode[] node = declaration.getTranslationUnit().getDefinitionsInAST(binding);
//			System.out.println((node[0].getParent()).getParent().getRawSignature());
		} else if(specifier instanceof CPPASTElaboratedTypeSpecifier){
			IASTName name = ((CPPASTElaboratedTypeSpecifier)specifier).getName();
			IBinding binding = name.resolveBinding();
			CPPClassType type = (CPPClassType)binding;
			System.out.println(type);
//			System.out.println(type.getCompositeTypeSpecifier().getRawSignature());
		}
		
		
//		System.out.println(specifier.getClass());
		
//		System.out.println(specifier);

		
		return ASTVisitor.PROCESS_CONTINUE;
	}

}
