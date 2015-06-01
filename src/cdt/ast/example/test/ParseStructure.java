package cdt.ast.example.test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.core.runtime.CoreException;

public class ParseStructure {
	
	static class PrintExpansionCommand implements ExtentionCommand{
		PrintWriter writer = null;
		
		public PrintExpansionCommand(PrintWriter writer){
			this.writer = writer;
		}

		@Override
		public void havingSimpleType(String c) {
//			String code = "fprintf(fp, \"" + c + " = 0x%x;\\n\", (long)" + c + ");";
			String code = "if((long)" + c + " != 0){\n"
						+ "    if(sizeof(" + c + ") == 1)\n"
					    + "        fprintf(fp, \"" + c + " = 0x%x;\\n\", " + c + "& 0xff);\n"
					    + "    else if(sizeof(" + c + ") == 2)\n"
					    + "        fprintf(fp, \"" + c + " = 0x%x;\\n\", " + c + "& 0xffff);\n"
					    + "    else if(sizeof(" + c + ") == 16)\n"
					    + "        fprintf(fp, \"" + c + " = 0x%llx;\\n\", " + c + ");\n"
					    + "    else\n"
					    + "        fprintf(fp, \"" + c + " = 0x%x;\\n\", " + c + ");\n"
					    + "}\n\n";
			writer.println(code);
		}};
		
	public static void main(String [] args) throws CoreException, FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(args[1]+"_buildBuilder.c", "UTF-8");
		writer.println("void buildBuilder_"+args[1] + "(FILE * fp, "+args[1]+" * ptr){\n");
		
//		FileContent fileContent = FileContent.createForExternalFileLocation("e:\\uls_l1_callp_msg_cat.prep");
		FileContent fileContent = FileContent.createForExternalFileLocation(args[0]);
		Map definedSymbols = new HashMap();
		String [] includePaths = new String[0];
		IScannerInfo scannerInfo = new ScannerInfo(definedSymbols, includePaths);
		IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();
		IParserLogService log = new DefaultLogService();
		IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, scannerInfo, emptyIncludes, null, 8, log);
		
		IScope scope = translationUnit.getScope();
//		IBinding [] bindings = scope.find("uls_l1_ue_context_config_req_t");
		IBinding [] bindings = scope.find(args[1]);
		IASTName[] name = translationUnit.getDeclarationsInAST(bindings[0]);
		IASTNode ast = name[0].getParent().getParent();
		
		
		ExtentionCommand command = new PrintExpansionCommand(writer);
		ExpansionVisitor visitor = new ExpansionVisitor("ptr->", command);
		ast.accept(visitor);
		
		writer.println("}\n");
		
		writer.close();
	}

}
