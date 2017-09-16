package cop5556sp17;

import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain chain1 = binaryChain.getE0();
		ChainElem chainElem = binaryChain.getE1();
		
		Token token = binaryChain.getArrow();
		
		chain1.visit(this, arg);
		chainElem.visit(this, arg);
		
		if(chain1.getType().equals(TypeName.URL) && token.isKind(Kind.ARROW) && chainElem.getType().equals(TypeName.IMAGE)){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain1.getType().equals(TypeName.FILE) && token.isKind(Kind.ARROW) && chainElem.getType().equals(TypeName.IMAGE)){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain1.getType().equals(TypeName.FRAME) && token.isKind(Kind.ARROW) && chainElem.getClass().equals(FrameOpChain.class) && (chainElem.getFirstToken().isKind(Kind.KW_XLOC) || chainElem.getFirstToken().isKind(Kind.KW_YLOC))){
			binaryChain.setType(TypeName.INTEGER);
		}else if(chain1.getType().equals(TypeName.FRAME) && token.isKind(Kind.ARROW) && chainElem.getClass().equals(FrameOpChain.class) && (chainElem.getFirstToken().isKind(Kind.KW_SHOW) || chainElem.getFirstToken().isKind(Kind.KW_HIDE)|| chainElem.getFirstToken().isKind(Kind.KW_MOVE))){
			binaryChain.setType(TypeName.FRAME);
		}else if(chain1.getType().equals(TypeName.IMAGE) && token.isKind(Kind.ARROW) && chainElem.getClass().equals(ImageOpChain.class) && (chainElem.getFirstToken().isKind(Kind.OP_WIDTH) || chainElem.getFirstToken().isKind(Kind.OP_HEIGHT))){
			binaryChain.setType(TypeName.INTEGER);
		}else if(chain1.getType().equals(TypeName.IMAGE) && token.isKind(Kind.ARROW) && chainElem.getType().equals(TypeName.FRAME)){
			binaryChain.setType(TypeName.FRAME);
		}else if(chain1.getType().equals(TypeName.IMAGE) && token.isKind(Kind.ARROW) && chainElem.getType().equals(TypeName.FILE)){
			binaryChain.setType(TypeName.NONE);
		}else if(chain1.getType().equals(TypeName.IMAGE) && (token.isKind(Kind.ARROW) || token.isKind(Kind.BARARROW)) && chainElem.getClass().equals(FilterOpChain.class) && (chainElem.getFirstToken().isKind(Kind.OP_GRAY) || chainElem.getFirstToken().isKind(Kind.OP_BLUR)|| chainElem.getFirstToken().isKind(Kind.OP_CONVOLVE))){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain1.getType().equals(TypeName.IMAGE) && token.isKind(Kind.ARROW) && chainElem.getClass().equals(ImageOpChain.class) && (chainElem.getFirstToken().isKind(Kind.KW_SCALE))){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain1.getType().equals(TypeName.IMAGE) && token.isKind(Kind.ARROW) && chainElem.getType().equals(TypeName.IMAGE) && chainElem.getClass().equals(IdentChain.class) ){
			binaryChain.setType(TypeName.IMAGE);
		}else if(chain1.getType().equals(TypeName.INTEGER) && token.isKind(Kind.ARROW) && chainElem.getType().equals(TypeName.INTEGER) && chainElem.getClass().equals(IdentChain.class) ){
			binaryChain.setType(TypeName.INTEGER);
		}else{
			throw new TypeCheckException("Error in visit Binary Chain");
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression expression1 = binaryExpression.getE0();
		Expression expression2 = binaryExpression.getE1();
		Token token = binaryExpression.getOp();
		
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		

		if(expression1.getType().equals(TypeName.INTEGER) && (token.isKind(Kind.PLUS) || token.isKind(Kind.MINUS)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.INTEGER);
		}else if(expression1.getType().equals(TypeName.IMAGE) && (token.isKind(Kind.PLUS) || token.isKind(Kind.MINUS)) && expression2.getType().equals(TypeName.IMAGE)){
			binaryExpression.setType(TypeName.IMAGE);
		}else if(expression1.getType().equals(TypeName.INTEGER) && (token.isKind(Kind.TIMES) || token.isKind(Kind.DIV) || token.isKind(Kind.MOD)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.INTEGER);
		}else if(expression1.getType().equals(TypeName.INTEGER) && ((token.isKind(Kind.TIMES))) && expression2.getType().equals(TypeName.IMAGE)){
			binaryExpression.setType(TypeName.IMAGE);
		}else if(expression1.getType().equals(TypeName.IMAGE) && ((token.isKind(Kind.TIMES)) || token.isKind(Kind.DIV) || token.isKind(Kind.MOD)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.IMAGE);
		}else if(expression1.getType().equals(TypeName.INTEGER) && (token.isKind(Kind.LT) || token.isKind(Kind.GT) || token.isKind(Kind.LE) || token.isKind(Kind.GE)) && expression2.getType().equals(TypeName.INTEGER)){
			binaryExpression.setType(TypeName.BOOLEAN);
		}else if(expression1.getType().equals(TypeName.BOOLEAN) && (token.isKind(Kind.LT) || token.isKind(Kind.GT) || token.isKind(Kind.LE) || token.isKind(Kind.GE) || token.isKind(Kind.AND) || token.isKind(Kind.OR)) && expression2.getType().equals(TypeName.BOOLEAN)){
			binaryExpression.setType(TypeName.BOOLEAN);
		}else if((token.isKind(Kind.EQUAL) || token.isKind(Kind.NOTEQUAL)) && expression1.getType() == expression2.getType()){
			binaryExpression.setType(TypeName.BOOLEAN);
		}else{
			throw new TypeCheckException("Error in visit Binary Expression");
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		List<Dec> decs = block.getDecs();
		List<Statement> statements = block.getStatements();
		
		if(decs != null){
			for(Dec dec : decs){
				dec.visit(this, arg);
			}
		}
		
		if(statements != null){
			for(Statement statement: statements){
				statement.visit(this, arg);
			}
		}
		symtab.leaveScope();
		return null;
	}

	private Object visitChain(Statement chain, Object arg) throws Exception {
		if(chain.getClass().equals(BinaryChain.class)){
			visitBinaryChain((BinaryChain) chain, arg);
		}else{
			visitChainElement(chain,arg);
		}
		return null;
	}

	private Object visitChainElement(Statement chainElem, Object arg) throws Exception {
		if(chainElem.getClass().equals(FilterOpChain.class)){
			visitFilterOpChain((FilterOpChain)chainElem, arg);
		}else if(chainElem.getClass().equals(FrameOpChain.class)){
			visitFrameOpChain((FrameOpChain) chainElem, arg);
		}else if(chainElem.getClass().equals(ImageOpChain.class)){
			visitImageOpChain((ImageOpChain) chainElem, arg);
		}else if(chainElem.getClass().equals(IdentChain.class)){
			visitIdentChain((IdentChain) chainElem, arg);
		}else{
			new TypeCheckException("Error in visit Block");
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setType(TypeName.BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		Tuple tuple = filterOpChain.getArg();
		tuple.visit(this, arg);
		if(tuple != null && tuple.getExprList().size() == 0){
			filterOpChain.setType(TypeName.IMAGE);
		}else{
			throw new TypeCheckException("Failed to match condition in Visit Filter Op Chain");
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Token firstToken = frameOpChain.getFirstToken();
		Tuple tuple = frameOpChain.getArg();
		tuple.visit(this, arg);
		if(firstToken.isKind(Kind.KW_SHOW) || firstToken.isKind(Kind.KW_HIDE)){
			if(tuple.getExprList().size() == 0){
				frameOpChain.setType(TypeName.NONE);
			}else{
				throw new TypeCheckException("Failed to match condition in Visit Frame Op Chain");
			}
		}else if(firstToken.isKind(Kind.KW_XLOC) || firstToken.isKind(Kind.KW_YLOC)){
			if(tuple.getExprList().size() == 0){
				frameOpChain.setType(TypeName.INTEGER);
				
			}else{
				throw new TypeCheckException("Failed to match condition in Visit Frame Op Chain");
			}
		}else if(firstToken.isKind(Kind.KW_MOVE)){
			if(tuple.getExprList().size() == 2){
				frameOpChain.setType(TypeName.NONE);
			}else{
				throw new TypeCheckException("Failed to match condition in Visit Frame Op Chain");
			}
		}else{
			throw new TypeCheckException("Failed to match condition in Visit Frame Op Chain");
		}
		return null;
	}

	@Override
	// TODO Match with Grammer
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Token token = identChain.getFirstToken();
		Dec dec = symtab.lookup(token.getText());
		if(symtab.isVisible(token.getText())){
			identChain.setDec(dec);
			identChain.setType(dec.getTypeName());
		}else{
			throw new TypeCheckException("Illegal construct identChain:"+identChain+" as could not find a declaration in symboltable"); 
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Token firstToken = identExpression.getFirstToken();
		if(symtab.isVisible(firstToken.getText())){
			identExpression.setType(symtab.lookup(firstToken.getText()).getTypeName());
			identExpression.setDec(symtab.lookup(firstToken.getText()));
		}else{
			throw new TypeCheckException("Ident has not been declared or it is visible in the current scope");
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Expression expression = ifStatement.getE();
		Block block = ifStatement.getB();
		expression.visit(this, arg);
		
		if(!expression.getType().equals(TypeName.BOOLEAN)){
			throw new TypeCheckException("Expected boolean expression");
		}
		block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setType(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression expression = sleepStatement.getE();
		expression.visit(this, arg);
		if(!expression.getType().equals(TypeName.INTEGER)){
			throw new TypeCheckException("Type mismatch in Sleep Statement valuation");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Expression expression = whileStatement.getE();
		Block block = whileStatement.getB();
		expression.visit(this, arg);
		
		if(!expression.getType().equals(TypeName.BOOLEAN)){
			throw new TypeCheckException("Expected boolean expression");
		}
		block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setTypeName(Type.getTypeName(declaration.getFirstToken()));
		Token ident  = declaration.getIdent();
		boolean isSuccessful = symtab.insert(ident.getText(), declaration);
		if(!isSuccessful)
			throw new TypeCheckException("Variable already presnt in current scope.");
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		List<ParamDec> paramDecs = program.getParams();
		Block block  = program.getB();
		Object object = null;
		if(paramDecs != null){
			for(ParamDec paramDec: paramDecs){
				object = paramDec.visit(this, arg);
			}
		}
		if(block != null){
			object = block.visit(this, arg);
		}
		return object;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		IdentLValue lvalue= assignStatement.getVar();
		Expression expression = assignStatement.getE();
		
		lvalue.visit(this, arg);
		expression.visit(this, arg);
		
		if(!lvalue.getDec().getTypeName().equals(expression.getType())){
			throw new TypeCheckException("Incompatible type assignment");
		}	
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identLValue, Object arg) throws Exception {
		if(symtab.isVisible(identLValue.getText())){	
			identLValue.setDec(symtab.lookup(identLValue.getText()));
		}else{
			throw new TypeCheckException("Ident has not been declared or it is visible in the current scope");
		}
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		Token ident  = paramDec.getIdent();
		paramDec.setTypeName(Type.getTypeName(paramDec.firstToken));
		boolean isSuccessful = symtab.insert(ident.getText(), paramDec);
		if(!isSuccessful)
			throw new TypeCheckException("Variable already presnt in current scope.");
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setType(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Token firstToken = imageOpChain.getFirstToken();
		Tuple tuple = imageOpChain.getArg();
		tuple.visit(this, arg);
		if(firstToken.isKind(Kind.OP_WIDTH) || firstToken.isKind(Kind.OP_HEIGHT)){
			if(tuple.getExprList().size() == 0){
				imageOpChain.setType(TypeName.INTEGER);
			}else{
				throw new TypeCheckException("Failed to match condition in Image Frame Op Chain");
			}
		}else if(firstToken.isKind(Kind.KW_SCALE)){
			if(tuple.getExprList().size() == 1){
				imageOpChain.setType(TypeName.IMAGE);
			}else{
				throw new TypeCheckException("Failed to match condition in Image Frame Op Chain");
			}
		}else{
			throw new TypeCheckException("Failed to match condition in Image Frame Op Chain");
		}

		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> expressions = tuple.getExprList();
		for(Expression expression:expressions){
			expression.visit(this, arg);
			if(!expression.getType().equals(TypeName.INTEGER)){
				throw new TypeCheckException("Exception while visiting Tuple");
			}
		}
		return null;
	}

}
