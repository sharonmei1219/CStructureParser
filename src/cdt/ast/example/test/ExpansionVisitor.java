package cdt.ast.example.test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;

public class ExpansionVisitor extends ASTVisitor {

	private ExtentionCommand singleExpansionCommand;
	private String prefix = "";

	public ExpansionVisitor(String prefix, ExtentionCommand command) {
		this.prefix = prefix;
		this.singleExpansionCommand = command;
		this.shouldVisitDeclarations = true;
	}
	
	public int visit(IASTDeclaration inputDeclaration) {
		
		if (!(inputDeclaration instanceof IASTSimpleDeclaration))
			return ASTVisitor.PROCESS_ABORT;
		
		IASTSimpleDeclaration ast = (IASTSimpleDeclaration) inputDeclaration;
		IASTDeclSpecifier specifier = ast.getDeclSpecifier();
		IType type = ExpansionUtility.specificationToType(specifier);
		
		if(type instanceof CPPClassType){
			expandAllDeclarators(ast, type);
		}else if (type instanceof CPPBasicType){
			handleAllFullExpansion(ast);
		}else{
//			TODO: errorMessageIndicate some fields failed to expand
		}

		return ASTVisitor.PROCESS_CONTINUE;
	}



	private void passFullExpansionToCommand(IASTDeclarator declarator) {
		String fullExpansion = prefix  + declarator.getName();
		singleExpansionCommand.havingSimpleType(fullExpansion);
	}

	private void expandAllDeclarators(IASTSimpleDeclaration ast, IType type) {
		for (IASTDeclarator declarator : ast.getDeclarators()) {
			if (type instanceof CPPClassType) {
				expandDeclarator((CPPClassType) type, declarator);
			}
		}
	}

	private void expandDeclarator(CPPClassType typedef,	IASTDeclarator declarator) {
		
		if (declarator instanceof CPPASTArrayDeclarator){
			long dim = ExpansionUtility.getArrayDimension((CPPASTArrayDeclarator)declarator);
			for (int i = 0; i < dim; i++){
				String nextLevelPrefix = prefix  + declarator.getName() + "[" + Integer.toString(i) + "].";
				ExpansionVisitor expandNextLevel = new ExpansionVisitor(nextLevelPrefix, singleExpansionCommand);
				ICPPASTCompositeTypeSpecifier nextLevelStructure = typedef.getCompositeTypeSpecifier();
				nextLevelStructure.accept(expandNextLevel);
			}
		}else{
			String nextLevelPrefix = prefix + declarator.getName() + ".";
			ExpansionVisitor expandNextLevel = new ExpansionVisitor(nextLevelPrefix, singleExpansionCommand);
			ICPPASTCompositeTypeSpecifier nextLevelStructure = typedef.getCompositeTypeSpecifier();
			nextLevelStructure.accept(expandNextLevel);
		}
	}
	
	private void handleAllFullExpansion(IASTSimpleDeclaration ast) {
		for (IASTDeclarator declarator : ast.getDeclarators()) {
			if (declarator instanceof CPPASTArrayDeclarator){
				long dim = ExpansionUtility.getArrayDimension((CPPASTArrayDeclarator)declarator);
				for (int i = 0; i < dim; i++){
					String fullExpansion = prefix  + declarator.getName() + "[" + Integer.toString(i) + "]";
					singleExpansionCommand.havingSimpleType(fullExpansion);
				}
			}else{
				passFullExpansionToCommand(declarator);
			}
		}
	}

}
