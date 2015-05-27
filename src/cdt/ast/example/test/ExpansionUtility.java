package cdt.ast.example.test;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;

public class ExpansionUtility {

	static public IType specificationToType(IASTDeclSpecifier specifier){
		if (specifier instanceof CPPASTNamedTypeSpecifier){
			IASTName name = ((CPPASTNamedTypeSpecifier)specifier).getName();
			IBinding binding = name.resolveBinding();
			IType typedef = (CPPTypedef)binding;
			while (typedef instanceof CPPTypedef){
				typedef = ((CPPTypedef) typedef).getType();
			}
			return typedef;
		}
		
		if(specifier instanceof CPPASTElaboratedTypeSpecifier){
			IASTName name = ((CPPASTElaboratedTypeSpecifier)specifier).getName();
			IBinding binding = name.resolveBinding();
			return (CPPClassType)binding;
		}
		
		if(specifier instanceof CPPASTSimpleDeclSpecifier){
			return new CPPBasicType(null, ((CPPASTSimpleDeclSpecifier) specifier).getType());
		}
		
		if(specifier instanceof CPPASTCompositeTypeSpecifier){
			return null;
		}
		System.out.println(specifier.getClass());
		return null;
	}

	public static long getArrayDimension(CPPASTArrayDeclarator arrayDeclarator) {
		IASTExpression expression = arrayDeclarator.getArrayModifiers()[0].getConstantExpression();
		if (expression instanceof CPPASTLiteralExpression){
			return ((CPPASTLiteralExpression)expression).getEvaluation().getValue(arrayDeclarator).numericalValue();
		}
		
		if (expression instanceof CPPASTIdExpression){
			return ((CPPASTIdExpression)expression).getEvaluation().getValue(arrayDeclarator).numericalValue();
		}
		
		if (expression instanceof CPPASTBinaryExpression){
			return ((CPPASTBinaryExpression)expression).getEvaluation().getValue(arrayDeclarator).numericalValue();
		}
		
		System.out.println(expression.getClass());
		return 0;

	}

}
