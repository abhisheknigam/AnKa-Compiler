package cop5556sp17;
import static cop5556sp17.Scanner.Kind.ASSIGN;
import static cop5556sp17.Scanner.Kind.COMMA;
import static cop5556sp17.Scanner.Kind.EOF;
import static cop5556sp17.Scanner.Kind.IDENT;
import static cop5556sp17.Scanner.Kind.KW_IF;
import static cop5556sp17.Scanner.Kind.KW_WHILE;
import static cop5556sp17.Scanner.Kind.LBRACE;
import static cop5556sp17.Scanner.Kind.LPAREN;
import static cop5556sp17.Scanner.Kind.OP_SLEEP;
import static cop5556sp17.Scanner.Kind.RBRACE;
import static cop5556sp17.Scanner.Kind.RPAREN;
import static cop5556sp17.Scanner.Kind.SEMI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cop5556sp17.Scanner;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
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
import cop5556sp17.AST.WhileStatement;

public class Parser {

	static Map<String, ArrayList<Enum>> first = new HashMap<String, ArrayList<Enum>>();
	
	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		fillFirst();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();
		return program;
	}

	private void fillFirst() {
		ArrayList<Enum> dec = new ArrayList<Enum>();
		dec.add(Kind.KW_INTEGER);
		dec.add(Kind.KW_BOOLEAN);
		dec.add(Kind.KW_IMAGE);
		dec.add(Kind.KW_FRAME);
		
		
		first.put("dec", dec);
		
		ArrayList<Enum> paramDec = new ArrayList<Enum>();
		paramDec.add(Kind.KW_URL);
		paramDec.add(Kind.KW_FILE);
		paramDec.add(Kind.KW_INTEGER);
		paramDec.add(Kind.KW_BOOLEAN);
		
		first.put("paramDec", paramDec);
		
		ArrayList<Enum> statement = new ArrayList<Enum>();
		statement.add(Kind.OP_SLEEP);
		statement.add(Kind.KW_WHILE);
		statement.add(Kind.KW_IF);
		statement.add(Kind.IDENT);
		
		statement.add(Kind.KW_SHOW);
		statement.add(Kind.KW_HIDE);
		statement.add(Kind.KW_MOVE);
		statement.add(Kind.KW_XLOC);
		statement.add(Kind.KW_YLOC);
		
		statement.add(Kind.OP_BLUR);
		statement.add(Kind.OP_GRAY);
		statement.add(Kind.OP_CONVOLVE);
		
		statement.add(Kind.OP_WIDTH);
		statement.add(Kind.OP_HEIGHT);
		statement.add(Kind.KW_SCALE);
		
		
		
		first.put("statement", statement);
		
		ArrayList<Enum> arrowOp = new ArrayList<Enum>();
		arrowOp.add(Kind.ARROW);
		arrowOp.add(Kind.BARARROW);
		
		
		first.put("arrowOp", arrowOp);
		
		ArrayList<Enum> filterOp = new ArrayList<Enum>();
		
		filterOp.add(Kind.OP_BLUR);
		filterOp.add(Kind.OP_GRAY);
		filterOp.add(Kind.OP_CONVOLVE);
		
		
		first.put("filterOp", filterOp);
		
		ArrayList<Enum> frameOp = new ArrayList<Enum>();
		
		frameOp.add(Kind.KW_SHOW);
		frameOp.add(Kind.KW_HIDE);
		frameOp.add(Kind.KW_MOVE);
		frameOp.add(Kind.KW_XLOC);
		frameOp.add(Kind.KW_YLOC);
		
		
		first.put("frameOp", frameOp);
		
		ArrayList<Enum> imageOp = new ArrayList<Enum>();
		imageOp.add(Kind.OP_WIDTH);
		imageOp.add(Kind.OP_HEIGHT);
		imageOp.add(Kind.KW_SCALE);
		
		first.put("imageOp", imageOp);
		
		ArrayList<Enum> relOp = new ArrayList<Enum>();
		relOp.add(Kind.LT);
		relOp.add(Kind.LE);
		relOp.add(Kind.GT);
		relOp.add(Kind.GE);
		relOp.add(Kind.EQUAL);
		relOp.add(Kind.NOTEQUAL);
		
		first.put("relOp", relOp);
		
		ArrayList<Enum> weakOp = new ArrayList<Enum>();
		weakOp.add(Kind.PLUS);
		weakOp.add(Kind.MINUS);
		weakOp.add(Kind.OR);
		
		first.put("weakOp", weakOp);
		
		ArrayList<Enum> strongOp = new ArrayList<Enum>();
		strongOp.add(Kind.TIMES);
		strongOp.add(Kind.DIV);
		strongOp.add(Kind.AND);
		strongOp.add(Kind.MOD);
		
		first.put("strongOp", strongOp);
	}

	Expression expression() throws SyntaxException {
		Expression expression;
		Expression expression1;
		expression = term();
		Token token;
		while(first.get("relOp").contains(t.kind)){
			token = relOp();
			expression1 = term();
			expression = new BinaryExpression(expression.firstToken, expression, token, expression1);
		}
		return expression;
	}

	Expression term() throws SyntaxException {
		Expression expression;
		Expression expression1;
		Token token;
		expression = elem();
		while(first.get("weakOp").contains(t.kind)){
			token = weakOp();
			expression1 = elem();
			expression = new BinaryExpression(expression.firstToken, expression, token, expression1);
		}
		return expression;
	}

	Expression elem() throws SyntaxException {
		Expression expression;
		Expression expression1;
		Token token;
		expression = factor();
		while(first.get("strongOp").contains(t.kind)){
			token = strongOp();
			expression1 = factor();
			expression = new BinaryExpression(expression.firstToken, expression, token, expression1);
		}
		return expression;
	}
	
	IfStatement ifStatement() throws SyntaxException{
		Expression expression;
		Block block;
		Token token;
		if(t.kind.equals(KW_IF)){
			token = consume();
			if(t.kind.equals(LPAREN)){
				match(LPAREN);
				expression = expression();
				match(RPAREN);
				block = block();
			}else{
				throw new SyntaxException(" Invalid if Values" + scanner.getLinePos(t));
			}
		}else{
			throw new SyntaxException(" Invalid if Values" + scanner.getLinePos(t));
		}
		return new IfStatement(token, expression, block);
	}

	Expression factor() throws SyntaxException {
		Expression expression;
		switch (t.kind) {
		case IDENT: {
			return new IdentExpression(consume());
		}
			
		case INT_LIT: {
			return new IntLitExpression(consume());
		}
			
		case KW_TRUE:
		case KW_FALSE: {
			return new BooleanLitExpression(consume());
		}
			
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			return new ConstantExpression(consume());
		}
		
		case LPAREN: {
			consume();
			expression = expression();
			match(RPAREN);
			return expression;
		}
			
		default:
			throw new SyntaxException("illegal factor"  + scanner.getLinePos(t));
		}
	}

	Block block() throws SyntaxException {
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		Token token;
		if(t.kind.equals(LBRACE)){
			token = consume();
			while(first.get("dec").contains(t.kind) || first.get("statement").contains(t.kind)){
				if(first.get("dec").contains(t.kind)){
					decs.add(dec());
				}else if(first.get("statement").contains(t.kind)){
					statements.add(statement());
				}else{
					throw new SyntaxException(" Invalid block Values" + scanner.getLinePos(t));
				}
			}
			if(t.kind.equals(RBRACE)){
				consume();
			}else{
				throw new SyntaxException(" Invalid block Values" + scanner.getLinePos(t));
			}
		}else{
			throw new SyntaxException(" Invalid block Values" + scanner.getLinePos(t));
		}
		return new Block(token, decs, statements);
	}

	Program program() throws SyntaxException {
		ArrayList<ParamDec> paramDecs = new ArrayList<ParamDec>();
		Block block;
		Token token;
		if(t.kind.equals(IDENT)){
			token = consume();
			if(first.get("paramDec").contains(t.kind)){
				paramDecs.add(paramDec());
				while(t.kind.equals(COMMA)){
					consume();
					paramDecs.add(paramDec());
				}
				block = block();
			}else{
				block = block();
			}
		}else{
			throw new SyntaxException(" Invalid program Values" + scanner.getLinePos(t));
		}
		
		return new Program(token, paramDecs, block);
		
	}

	ParamDec paramDec() throws SyntaxException {
		Token token;
		Token ident;
		if(first.get("paramDec").contains(t.kind)){
			token = consume();
			if(t.kind.equals(IDENT)){
				ident = consume();
			}else{
				throw new SyntaxException(" Invalid paramDec Values" + scanner.getLinePos(t));
			}
		}else{
			throw new SyntaxException(" Invalid paramDec Values" + scanner.getLinePos(t));
		}
		return new ParamDec(token, ident);
	}

	Dec dec() throws SyntaxException {
		Token token;
		Token ident;
		if(first.get("dec").contains(t.kind)){
			token = consume();
			if(t.kind.equals(IDENT)){
				ident = consume();
			}else{
				throw new SyntaxException(" Invalid dec Values" + scanner.getLinePos(t));
			}
		}else{
			throw new SyntaxException(" Invalid dec Values" + scanner.getLinePos(t));
		}
		return new Dec(token, ident);
	}

	Statement statement() throws SyntaxException {
		Token token;
		Expression expression;
		Statement statement;
		if(t.kind.equals(OP_SLEEP)){
			token = consume();
			expression = expression();
			if(t.kind.equals(SEMI)){
				consume();
			}else{
				throw new SyntaxException(" Invalid statement Values" + scanner.getLinePos(t));
			}
			return new SleepStatement(token, expression);
		}else if(t.kind.equals(KW_WHILE)){
			return whileStatement();
		}else if(t.kind.equals(KW_IF)){
			return ifStatement();
		}else if(t.kind.equals(IDENT) && scanner.peek().kind.equals(ASSIGN)){
			statement = assign();
			if(t.kind.equals(SEMI)){
				consume();
			}else{
				throw new SyntaxException(" Invalid statement Values" + scanner.getLinePos(t));
			}
			return statement;
		}else if(t.kind.equals(IDENT) || first.get("filterOp").contains(t.kind) || first.get("frameOp").contains(t.kind) || first.get("imageOp").contains(t.kind)){
			statement = chain();
			if(t.kind.equals(SEMI)){
				consume();
			}else{
				throw new SyntaxException(" Invalid statement Values" + scanner.getLinePos(t));
			}
			return statement;
		}else{
			throw new SyntaxException(" Invalid statement Values" + scanner.getLinePos(t));
		}
	}
	
	AssignmentStatement assign() throws SyntaxException {
		Token ident = match(IDENT);
		IdentLValue identLValue = new IdentLValue(ident);
		match(ASSIGN);
		Expression e0 = expression();
		return new AssignmentStatement(ident, identLValue, e0);
	}

	BinaryChain chain() throws SyntaxException {
		ChainElem chainElement1 = chainElem();
		Token token1 = arrowOp();
		ChainElem chainElement2 = chainElem();
		BinaryChain binaryChain = new BinaryChain(chainElement1.firstToken, chainElement1, token1, chainElement2);
		while(first.get("arrowOp").contains(t.kind)){
			Token token2 = arrowOp();
			ChainElem chainElement3 = chainElem();
			binaryChain = new BinaryChain(chainElement1.firstToken, binaryChain, token2, chainElement3);
		}
		return binaryChain;
	}

	ChainElem chainElem() throws SyntaxException {
		Token token;
		Tuple tuple;
		if(t.kind.equals(IDENT)){
			token = consume();
			return new IdentChain(token);
		}else if(first.get("filterOp").contains(t.kind)){
			token = consume();
			tuple = arg();
			return new FilterOpChain(token, tuple);
		}else if(first.get("frameOp").contains(t.kind)){
			token = consume();
			tuple = arg();
			return new FrameOpChain(token, tuple);
		}else if(first.get("imageOp").contains(t.kind)){
			token = consume();
			tuple = arg();
			return new ImageOpChain(token, tuple);
		}else{
			throw new SyntaxException(" Incorrect chaining of element");
		} 
	}
	
	WhileStatement whileStatement() throws SyntaxException{
		Expression expression;
		Token token;
		Block block;
		if(t.kind.equals(KW_WHILE)){
			token = match(Kind.KW_WHILE);
			if(t.kind.equals(LPAREN)){
				match(Kind.LPAREN);
				expression = expression();
				match(Kind.RPAREN);
			}else{
				throw new SyntaxException(" Invalid whileStatement Values" + scanner.getLinePos(t));
			}
			block = block();
		}else{
			throw new SyntaxException(" Invalid whileStatement Values" + scanner.getLinePos(t));
		}
		return new WhileStatement(token, expression, block);
	}

	Tuple arg() throws SyntaxException {
		List<Expression> expressions = new ArrayList<Expression>();
		if(t.kind.equals(LPAREN)){
			Token lparen = consume();
			expressions.add(expression());
			while(t.kind.equals(COMMA)){
				consume();
				expressions.add(expression());
			}
			match(Kind.RPAREN);
			return new Tuple(lparen, expressions);
		}
		return new Tuple(null, expressions);
	}
	
	public Token strongOp() throws SyntaxException{
		switch(t.kind){
			case DIV:
			case AND:
			case TIMES:
			case MOD:
				return consume();
		}
		throw new SyntaxException(" Invalid strongOp Values" + scanner.getLinePos(t));
	}
	
	public Token weakOp() throws SyntaxException{
		switch(t.kind){
			case PLUS:
			case MINUS:
			case OR:
				return consume();
		}
		throw new SyntaxException(" Invalid weakOp Values" + scanner.getLinePos(t));
	}
	
	public Token relOp() throws SyntaxException{
		switch(t.kind){
			case LT:
			case LE:
			case GT:
			case GE:
			case EQUAL:
			case NOTEQUAL:
				return consume();
		}
		throw new SyntaxException(" Invalid relOp Values" + scanner.getLinePos(t));
	}
	
	public Token filterOp() throws SyntaxException{
		switch(t.kind){
			case OP_BLUR:
			case OP_CONVOLVE:
			case OP_GRAY:
				return consume();
		}
		throw new SyntaxException(" Invalid filterOp Values" + scanner.getLinePos(t));
	}
	
	public Token frameOp() throws SyntaxException{
		switch(t.kind){
			case KW_SHOW:
			case KW_HIDE:
			case KW_MOVE:
			case KW_XLOC:
			case KW_YLOC:
				return consume();
		}
		throw new SyntaxException(" Invalid frameOp Values" + scanner.getLinePos(t));
	}
	
	public Token imageOp() throws SyntaxException{
		switch(t.kind){
			case OP_WIDTH:
			case OP_HEIGHT:
			case KW_SCALE:
				return consume();
		}
		throw new SyntaxException(" Invalid imageOp Values" + scanner.getLinePos(t));
	}
	
	public Token arrowOp() throws SyntaxException{
		switch(t.kind){
			case ARROW:
			case BARARROW:
				return consume();
		}
		throw new SyntaxException(" Invalid arrow Values" + scanner.getLinePos(t));
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
