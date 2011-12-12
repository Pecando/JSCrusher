package JSCrusher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.ArrayComprehension;
import org.mozilla.javascript.ast.ArrayComprehensionLoop;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.CatchClause;
import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.EmptyExpression;
import org.mozilla.javascript.ast.ErrorNode;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Jump;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Label;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.LetNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.RegExpLiteral;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;
import org.mozilla.javascript.ast.XmlDotQuery;
import org.mozilla.javascript.ast.XmlFragment;
import org.mozilla.javascript.ast.XmlLiteral;
import org.mozilla.javascript.ast.XmlMemberGet;
import org.mozilla.javascript.ast.XmlRef;
import org.mozilla.javascript.ast.Yield;

public class JSCrusher {

	private NameGenerator nameGen;
	private Stack<Integer> positionStack;
//	private FakeScope currentScope;
	private ArrayList<Name> modifierableNames;
	private Boolean skipName=false;
	private AstRoot currentRoot;
//	private String currentSrc;
	private HashSet<String> excludedName;
	
	

	public void setNameGen(NameGenerator nGen) {
		nameGen = nGen;
	}

	public JSCrusher() {
		modifierableNames=new ArrayList<Name>();
		excludedName=new HashSet<String>();
		nameGen = new BaseNameGenerator();
		positionStack = new Stack<Integer>();
	}
	public String crush(String src) {
		CompilerEnvirons compilerEnv = CompilerEnvirons.ideEnvirons();
		compilerEnv.setRecordingComments(true);
		compilerEnv.setRecordingLocalJsDocComments(true);
//		currentSrc=src;
		Parser p = new Parser(compilerEnv);
		AstRoot ast = p.parse(src, "noname",0);
		
		parseExclude(ast);
//		ScriptNode xast=new IRFactory().transformTree(ast);
		return crush(ast);		
	}

	private String crush(AstRoot root) {

//		currentScope = new FakeScope();
		currentRoot=root;

		for (Node subNode : root) {
			crushNode((AstNode) subNode);
		}
		HashMap<String, String> nameMapping=new HashMap<String, String>();
		for (Name subNode : modifierableNames) {
			if(!nameMapping.containsKey(subNode.getIdentifier()))
				nameMapping.put(subNode.getIdentifier(), nameGen.genName(subNode.getIdentifier()));
		}		
		for (Name subNode : modifierableNames) {
			subNode.setIdentifier(nameMapping.get(subNode.getIdentifier()));
		}
		
		String src=root.toSource();
		String newSrc="";
//		int length = src.length();
//		for(int i = 0;i<length;i++){
//			int c=src.codePointAt(i);
//			if( c ==0xa || c ==0xd || c ==0x2028 || c ==0x2029 )
//			{
//				System.out.println(Integer.toString(i));
//				continue;
//			}
//			newSrc+=src.charAt(i);
//		}
		Pattern p=Pattern.compile("[\\u000a\\u000d\\u2028\\u2029]");
		Matcher m=p.matcher(src);
		newSrc=m.replaceAll("");

		 p=Pattern.compile("\\s*([\\{\\}\\(\\)\\[\\]\\+\\-=\\*/;:,<>\\?\\.|&\\^%$#@!~`])\\s*");
		 m=p.matcher(newSrc);
		newSrc=m.replaceAll("$1");
		
		return newSrc;
	}
	
//	private void printModifierable() {
//		for (Name subNode : modifierableNames) {
//			System.out.println(subNode.getIdentifier());
//		}
//	}

	private void crushNode(AstNode node) {

//		System.out.println(node.shortName()+node.getLineno());
//		if(node.getLineno()==9265)
//			node.setLineno(node.getLineno());
		if (false) {
		} else if (node instanceof ArrayLiteral) {
			crushArrayLiteral((ArrayLiteral) node);
		} else if (node instanceof Block) {
			crushBlock((Block) node);
		} else if (node instanceof CatchClause) {
			crushCatchClause((CatchClause) node);
		} else if (node instanceof Comment) {
			crushComment((Comment) node);
		} else if (node instanceof ConditionalExpression) {
			crushConditionalExpression((ConditionalExpression) node);
		} else if (node instanceof ElementGet) {
			crushElementGet((ElementGet) node);
		} else if (node instanceof EmptyExpression) {
			crushEmptyExpression((EmptyExpression) node);
		} else if (node instanceof ErrorNode) {
			crushErrorNode((ErrorNode) node);
		} else if (node instanceof ExpressionStatement) {
			crushExpressionStatement((ExpressionStatement) node);
		} else if (node instanceof FunctionCall) {
			crushFunctionCall((FunctionCall) node);
		} else if (node instanceof IfStatement) {
			crushIfStatement((IfStatement) node);
		} else if (node instanceof InfixExpression) {
			crushInfixExpression((InfixExpression) node);
		} else if (node instanceof Jump) {
			crushJump((Jump) node);
		} else if (node instanceof KeywordLiteral) {
			crushKeywordLiteral((KeywordLiteral) node);
		} else if (node instanceof LabeledStatement) {
			crushLabeledStatement((LabeledStatement) node);
		} else if (node instanceof Name) {
			crushName((Name) node);
		} else if (node instanceof NumberLiteral) {
			crushNumberLiteral((NumberLiteral) node);
		} else if (node instanceof ObjectLiteral) {
			crushObjectLiteral((ObjectLiteral) node);
		} else if (node instanceof ParenthesizedExpression) {
			crushParenthesizedExpression((ParenthesizedExpression) node);
		} else if (node instanceof RegExpLiteral) {
			crushRegExpLiteral((RegExpLiteral) node);
		} else if (node instanceof ReturnStatement) {
			crushReturnStatement((ReturnStatement) node);
		} else if (node instanceof StringLiteral) {
			crushStringLiteral((StringLiteral) node);
		} else if (node instanceof SwitchCase) {
			crushSwitchCase((SwitchCase) node);
		} else if (node instanceof ThrowStatement) {
			crushThrowStatement((ThrowStatement) node);
		} else if (node instanceof TryStatement) {
			crushTryStatement((TryStatement) node);
		} else if (node instanceof UnaryExpression) {
			crushUnaryExpression((UnaryExpression) node);
		} else if (node instanceof VariableDeclaration) {
			crushVariableDeclaration((VariableDeclaration) node);
		} else if (node instanceof VariableInitializer) {
			crushVariableInitializer((VariableInitializer) node);
		} else if (node instanceof WithStatement) {
			crushWithStatement((WithStatement) node);
		} else if (node instanceof XmlFragment) {
			crushXmlFragment((XmlFragment) node);
		} else if (node instanceof XmlLiteral) {
			crushXmlLiteral((XmlLiteral) node);
		} else if (node instanceof XmlRef) {
			crushXmlRef((XmlRef) node);
		} else if (node instanceof Yield) {
			crushYield((Yield) node);
		}
//		else if (node instanceof Scope) {
//			crushScope((Scope) node);
//		}
	}

	private void crushArrayLiteral(ArrayLiteral node) {
		for (AstNode subNode : node.getElements()) {
			crushNode(subNode);
		}
	}

	private void crushBlock(Block node) {
		positionStack.push(node.getPosition());
		for (Node subNode : node) {
			crushNode((AstNode) subNode);
		}
		positionStack.pop();
	}

	private void crushCatchClause(CatchClause node) {
		modifierableNames.add(node.getVarName());
		if(node.getCatchCondition()!=null)
			crushNode(node.getCatchCondition());
		crushBlock(node.getBody());
	}
	
	private void crushComment(Comment node) {
	}
	
	private void crushConditionalExpression(ConditionalExpression node) {
		crushNode(node.getTestExpression());
		crushNode(node.getTrueExpression());
		crushNode(node.getFalseExpression());
	}
	
	private void crushElementGet(ElementGet node) {
		crushNode(node.getTarget());
		crushNode(node.getElement());		
	}
	
	private void crushEmptyExpression(EmptyExpression node) {
	}
	
	private void crushErrorNode(ErrorNode node) {
		System.out.println(node.getMessage());
		System.exit(0);
	}
	
	private void crushExpressionStatement(ExpressionStatement node) {
		crushNode(node.getExpression());		
	}
	
	private void crushFunctionCall(FunctionCall node) {
		crushNode(node.getTarget());
		for (AstNode subNode : node.getArguments()) {
			crushNode(subNode);
		}		
	}
	
	private void crushIfStatement(IfStatement node) {
		crushNode(node.getCondition());
		crushNode(node.getThenPart());	
		if(node.getElsePart()==null)
			return;
		crushNode(node.getElsePart());		
	}
	
	private void crushInfixExpression(InfixExpression node) {
		if(false){			
		} else if (node instanceof Assignment) {
			crushAssignment((Assignment) node);
		} else if (node instanceof ObjectProperty) {
			crushObjectProperty((ObjectProperty) node);
		} else if (node instanceof PropertyGet) {
			crushPropertyGet((PropertyGet) node);
		} else if (node instanceof XmlDotQuery) {
			crushXmlDotQuery((XmlDotQuery) node);
		} else if (node instanceof XmlMemberGet) {
			crushXmlMemberGet((XmlMemberGet) node);
		} else{
			crushNode(node.getLeft());
			crushNode(node.getRight());			
		}
	}
	
	private void crushJump(Jump node) {
		if(false){			
		} else if (node instanceof BreakStatement) {
			crushBreakStatement((BreakStatement) node);
		} else if (node instanceof ContinueStatement) {
			crushContinueStatement((ContinueStatement) node);
		} else if (node instanceof Label) {
			crushLabel((Label) node);
		} else if (node instanceof Scope) {
			crushScope((Scope) node);
		} else if (node instanceof SwitchStatement) {
			crushSwitchStatement((SwitchStatement) node);
		}else{
			
		}
	}
	private void crushKeywordLiteral(KeywordLiteral node) {
	}
	
	private void crushLabeledStatement(LabeledStatement node) {
		for (Label subNode : node.getLabels()) {
			crushLabel(subNode);
		}		
		crushNode(node.getStatement());
	}
	
	private void crushName(Name node) {
//		FakeScope scope = new FakeScope();
//		scope.addAssotiateName((Name) node);
//		currentScope.addSubScope(((Name) node).getIdentifier(), scope);
		if(node==null)
			return;
//		System.out.println(node.getIdentifier()+node.getLineno());
//		if(node.getLineno()==101)
//		node.setLineno(node.getLineno());
//		if(node.getIdentifier().equals("selector"))
//		{
//			node.setIdentifier("selector");
//		}
		
		Boolean isGlobal=currentRoot.getSymbol(node.getIdentifier())!=null;
		Boolean isLocal=false;
        for (AstNode s = node.getParent(); s != null; s = s.getParent()) {
           if(s instanceof Scope)
           {
        	   Scope ss=(Scope)s;
	            if (ss.getSymbol(node.getIdentifier())!=null) {
	            	isLocal=true;
	               break;
	            }
           }
        }		
		if(!skipName && (isGlobal || isLocal ) && !excludedName.contains(node.getIdentifier()) )
		{
			modifierableNames.add(node);
		}else{
			String name= node.getIdentifier();
			String newName="";
			int count =name.length();
			for(int i=0;i<count;i++){
				Double flag=Math.random();
				if(flag<0.0){
					newName+="\\u"+Integer.toHexString(name.codePointAt(i)+0xf0000).substring(1);								
				}else{
					newName+=name.charAt(i);
				}				
			}
			node.setIdentifier(newName);
		}
	}
	
	private void crushNumberLiteral(NumberLiteral node) {
	}
	
	private void crushObjectLiteral(ObjectLiteral node) {
		for (ObjectProperty subNode : node.getElements()) {
			crushObjectProperty(subNode);
		}
	}
	
	private void crushParenthesizedExpression(ParenthesizedExpression node) {
		crushNode(node.getExpression());
	}
	
	private void crushRegExpLiteral(RegExpLiteral node) {
	}
	
	private void crushReturnStatement(ReturnStatement node) {
		if(node.getReturnValue()==null)
			return;
		crushNode(node.getReturnValue());
	}
	
	private void crushStringLiteral(StringLiteral node) {
		
	}
	
	private void crushSwitchCase(SwitchCase node) {
		crushNode(node.getExpression());
		if(node.getStatements()==null)
			return;
		for (AstNode subNode : node.getStatements()) {
			crushNode(subNode);
		}
	}
	
	private void crushThrowStatement(ThrowStatement node) {
		crushNode(node.getExpression());
	}
	
	private void crushTryStatement(TryStatement node) {
		crushNode(node.getTryBlock());
		for (CatchClause subNode : node.getCatchClauses()) {
			crushCatchClause(subNode);
		}
		if(node.getFinallyBlock()==null)
			return;
		crushNode(node.getFinallyBlock());
	}
	
	private void crushUnaryExpression(UnaryExpression node) {
		crushNode(node.getOperand());
	}
	
	private void crushVariableDeclaration(VariableDeclaration node) {
		for (VariableInitializer subNode : node.getVariables()) {
			crushVariableInitializer(subNode);
		}
	}
	
	private void crushVariableInitializer(VariableInitializer node) {
		crushNode(node.getTarget());
		if(node.getInitializer()==null)
			return;
		crushNode(node.getInitializer());
	}
	
	private void crushWithStatement(WithStatement node) {
		crushNode(node.getExpression());
		crushNode(node.getStatement());
	}
	
	private void crushXmlFragment(XmlFragment node) {
	}
	
	private void crushXmlLiteral(XmlLiteral node) {
	}
	
	private void crushXmlRef(XmlRef node) {
	}
	
	private void crushYield(Yield node) {
		crushNode(node.getValue());
	}
	
	//infix node
	private void crushAssignment(Assignment node) {
		parseHint(node.getPosition());
		crushNode(node.getLeft());
		crushNode(node.getRight());
	}
	
	private void crushObjectProperty(ObjectProperty node) {
		crushNode(node.getLeft());
		crushNode(node.getRight());
	}
	
	private void crushPropertyGet(PropertyGet node) {
		Boolean nameState=skipName;
		skipName=true;
		crushName(node.getProperty());
		skipName=nameState;
		crushNode(node.getTarget());
	}
	
	private void crushXmlDotQuery(XmlDotQuery node) {
	}
	
	private void crushXmlMemberGet(XmlMemberGet node) {
	}
	
//jump node
	private void crushBreakStatement(BreakStatement node) {
		crushName(node.getBreakLabel());
	}
	
	private void crushContinueStatement(ContinueStatement node) {
		crushName(node.getLabel());
	}
	
	private void crushLabel(Label node) {
	}
	
	private void crushScope(Scope node) {
		if(false){			
		} else if (node instanceof ArrayComprehension) {
			crushArrayComprehension((ArrayComprehension) node);
		} else if (node instanceof LetNode) {
			crushLetNode((LetNode) node);
		} else if (node instanceof DoLoop) {
			crushDoLoop((DoLoop) node);
		} else if (node instanceof ForInLoop) {
			crushForInLoop((ForInLoop) node);
		} else if (node instanceof ForLoop) {
			crushForLoop((ForLoop) node);
		} else if (node instanceof WhileLoop) {
			crushWhileLoop((WhileLoop) node);
//		} else if (node instanceof AstRoot) {
//			crushAstRoot((AstRoot) node);
		} else if (node instanceof FunctionNode) {
			crushFunctionNode((FunctionNode) node);
		}else{
			if(node.getStatements()==null)
				return;			
			for (AstNode subNode : node.getStatements()) {
				crushNode(subNode);
			}			
		}
	}
	
	private void crushSwitchStatement(SwitchStatement node) {
		crushNode(node.getExpression());
		for (SwitchCase subNode : node.getCases()) {
			crushSwitchCase(subNode);
		}
	}
	
//scope node
	
	private void crushArrayComprehension(ArrayComprehension node) {
		crushNode(node.getResult());
		for (ArrayComprehensionLoop subNode : node.getLoops()) {
			crushArrayComprehensionLoop(subNode);
		}		
		crushNode(node.getFilter());
	}
	
	private void crushLetNode(LetNode node) {
		crushVariableDeclaration(node.getVariables());	
		crushNode(node.getBody());
	}
	
//	private void crushLoop(Loop node) {
//	}
//	private void crushScriptNode(ScriptNode node) {
//	}
	//loop
	
	private void crushDoLoop(DoLoop node) {
		crushNode(node.getCondition());
		crushNode(node.getBody());
	}
	
	private void crushForInLoop(ForInLoop node) {
		crushNode(node.getIteratedObject());
		crushNode(node.getIterator());
		crushNode(node.getBody());
	}
	
	private void crushForLoop(ForLoop node) {
		crushNode(node.getInitializer());
		crushNode(node.getCondition());
		crushNode(node.getIncrement());
		crushNode(node.getBody());
	}
	
	private void crushWhileLoop(WhileLoop node) {
		crushNode(node.getCondition());
		crushNode(node.getBody());
	}
	
//script
//	private void crushAstRoot(AstRoot node) {
//	}
	private void crushFunctionNode(FunctionNode node) {
//		FakeScope scope;
//		scope = new FakeScope();
//		scope.addAssotiateName(((FunctionNode) node).getFunctionName());
//		currentScope.addSubScope(((FunctionNode) node).getName(), scope);
//
//		scopeStack.push(currentScope);
//		currentScope = scope;
//
		crushName(node.getFunctionName());
		for (AstNode subNode : node.getParams()) {
			crushNode(subNode);
		}
		crushNode(node.getBody());
//		currentScope = scopeStack.pop();
		
	}
	//forin
	private void crushArrayComprehensionLoop(ArrayComprehensionLoop node) {
		crushNode(node.getIteratedObject());
		crushNode(node.getIterator());
	}
	private void parseHint(Integer nodeStart) {
//		String hintStr=currentSrc.substring(positionStack.lastElement(), nodeStart);
//		System.out.println("hint : "+hintStr);
	}
	private void parseExclude(AstRoot root) {
		SortedSet<Comment> cms=root.getComments();
		Pattern p=Pattern.compile("@expose\\{([^\\{\\}]*)\\}");
		for(Comment cm:cms){
			String content = cm.getValue();
			Matcher m=p.matcher(content);
			if(!m.find())
				continue;
			String[] exStr=m.group(1).split(",");
			for(int i=0;i<exStr.length;i++){
				if(!excludedName.contains(exStr[i])){
					excludedName.add(exStr[i]);
				}				
			}			
		}

	}
	
}
