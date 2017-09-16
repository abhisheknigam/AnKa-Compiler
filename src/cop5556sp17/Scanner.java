package cop5556sp17;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner {
	
	
	static final String LINEBREAK = "[\\n]";
	static final String STARTCOMMENT = "[/][\\*]";
	static final String ENDCOMMENT = "[\\*][/]";
	static final String SPACE = "[ \\t\\r\\f]";

	static final String format  ="|(?<%s>%s)";
	static final String format1 ="|(?<A%s>%s)";
	
	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	public int lines;
	
	
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
		
	public Token peek() {
	    if (tokenNum >= tokens.size())
	        return null;
	    return tokens.get(tokenNum);
	}

	public static enum Kind { 
		
		IDENT("","([a-zA-Z$_]+)([a-zA-Z$_0-9]*)"),INT_LIT("","(0|[1-9][0-9]*)"),
		KW_INTEGER("integer","integer"), KW_BOOLEAN("boolean","boolean"), 
		KW_IMAGE("image","image"), KW_URL("url","url"), 
		KW_FILE("file","file"), KW_FRAME("frame","frame"), 
		KW_WHILE("while","while"), KW_IF("if","if"), 
		KW_TRUE("true","true"), KW_FALSE("false","false"),
		SEMI(";",";"), COMMA(",",","), 
		LPAREN("(","[\\(]"), RPAREN(")","[\\)]") , LBRACE("{","[\\{]"), 
		RBRACE("}","[}]"), ASSIGN("<-","[<][-]"), 
		ARROW("->","[-][>]"), BARARROW("|->","[|][-][>]"), 
		OR("|","[|]"), AND("&","[&]"), 
		EQUAL("==","[=][=]"), NOTEQUAL("!=","[!][=]"), 
		LE("<=","[<][=]"), GE(">=","[>][=]"),
		LT("<","[<]"), GT(">","[>]"), 
		PLUS("+","[+]"), MINUS("-","[-]"), 
		TIMES("*","[*]"), DIV("/","[/]"), 
		MOD("%","[%]"), NOT("!","[!]"), 
		OP_BLUR("blur","blur"), OP_GRAY("gray","gray"), 
		OP_CONVOLVE("convolve","convolve"), KW_SCREENHEIGHT("screenheight","screenheight"), 
		KW_SCREENWIDTH("screenwidth","screenwidth"), OP_WIDTH("width","width"), 
		OP_HEIGHT("height","height"), KW_XLOC("xloc","xloc"), 
		KW_YLOC("yloc","yloc"), KW_HIDE("hide","hide"), 
		KW_SHOW("show","show"), KW_MOVE("move","move"), 
		OP_SLEEP("sleep","sleep"), KW_SCALE("scale","scale"), EOF("eof","$");
		
		final String text;
		final String regularExpression;

		

		Kind(String text, String regularExpression) {
			this.text = text;
			this.regularExpression = regularExpression;
		}
	
		String getText() {
			return text;
		}
		
		String getRegularExpression() {
			return regularExpression;
		}
	}
	
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	public class Token {
		
		public final int length;
		public final Kind kind;
		public int line;
		public int linePosition;
		public final int pos;  
		
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		
		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 * @throws IllegalNumberException 
		 */
		public int intVal() {
			if(this.kind.equals(Kind.INT_LIT)){
					 Integer paresedInteger = Integer.parseInt(getText());
					 return paresedInteger;
			}else{
				throw new NumberFormatException(); 
			}
		}
		
		public String getText() {
			return chars.substring(pos, pos+length);
		}
		
		LinePos getLinePos(){
			LinePos linePos= new LinePos(line, linePosition);
			return linePos;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}
		
		public void setLinePosition(int line, int position){
			this.line=line;
			this.linePosition = position;
		}
		
		public boolean isKind(Kind kind){
			return this.kind.equals(kind);
		}
		
		public Kind kind(){
			return this.kind;
		}
		
	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
	}
	
	private StringBuilder addToPattern() {
		StringBuilder patternMatcher = new StringBuilder();
		
		patternMatcher.append(String.format(format, "SPACE",SPACE));
		patternMatcher.append(String.format(format, "STARTCOMMENT", STARTCOMMENT));
		patternMatcher.append(String.format(format, "ENDCOMMENT", ENDCOMMENT));
		patternMatcher.append(String.format(format, "LINEBREAK", LINEBREAK));
		
		return patternMatcher;
	}
	
	private void checkIllegalCharacter(int lastPostion) throws IllegalCharException {
		if(lastPostion != chars.length()){
			throw new IllegalCharException("Error - end of file occurred");
		}
	}

	private void checkIllegalidentifier(int lastPostion, int startPosition) throws IllegalCharException {
		if(startPosition != lastPostion){
			throw new IllegalCharException("Illegal identifier - "+chars.substring(lastPostion, startPosition)+" encountered at pos:"+ lastPostion);
		}
	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		
		boolean isIdentifiedToken = false;
		int lastLinePosition = 0;
		int lastPostion = 0;
		boolean commentStart = false;
		
		// Building the matching pattern using patternMatcher
		StringBuffer handleComment = new StringBuffer();
		handleComment.append(String.format("|(?<%s>%s)", "ENDCOMMENT", ENDCOMMENT));
		handleComment.append(String.format("|(?<%s>%s)", "LINEBREAK", LINEBREAK));
		
		Pattern tokenInsideComment = Pattern.compile(new String(handleComment.substring(1)));
		
		Matcher tokenizerMatch = generatePatternMatcher();
		
		while(tokenizerMatch.find()){
			if(commentStart){
				if(tokenizerMatch.group().matches(ENDCOMMENT)){
					tokenizerMatch.usePattern(generatePattern());
					lastPostion = tokenizerMatch.end();
					commentStart = false;
					continue;
				}
				
				if(tokenizerMatch.group().matches(LINEBREAK)){
					lastLinePosition = tokenizerMatch.end();
					lastPostion = tokenizerMatch.end();
					lines++;
					continue;
				}
			}else{
				Token token = null;
				int startPosition = tokenizerMatch.start();
				
				checkIllegalidentifier(lastPostion, startPosition);
				
				lastPostion = tokenizerMatch.end();
				isIdentifiedToken = false;
				
				if(tokenizerMatch.group().matches(STARTCOMMENT)){
					commentStart = true;
					tokenizerMatch.usePattern(tokenInsideComment);
					continue;
				}else if(tokenizerMatch.group().matches(LINEBREAK)){
					lines++;
					lastLinePosition = tokenizerMatch.end();
					continue;
				}else if(tokenizerMatch.group().matches(SPACE)){
					continue;
				}else{
					
					for(Kind identifier : Kind.values()){
						if(tokenizerMatch.group().matches(identifier.regularExpression)){
							token = new Token(identifier, tokenizerMatch.start(), tokenizerMatch.end()-tokenizerMatch.start());
							isIdentifiedToken = true;
							token.setLinePosition(lines, (tokenizerMatch.start()-lastLinePosition));
							
							if(identifier.equals(Kind.INT_LIT)){
								token.intVal();
							}
						}
					}
					
					if(isIdentifiedToken){
						tokens.add(token);
						continue;
					}else{
						throw new IllegalCharException("Illegal identifier - "+ tokenizerMatch.group());
					}
				}
			}
		}
		
		checkIllegalCharacter(lastPostion);
		
		return this;  
	}

	private Matcher generatePatternMatcher() {
		
		Pattern tokenPatterns = generatePattern();
		Matcher tokenizerMatch = tokenPatterns.matcher(chars);
		
		return tokenizerMatch;
	}

	private Pattern generatePattern() {
		StringBuilder patternMatcher = addToPattern();
		
		int indexOfKind = 0;
		
		for(Kind k : Kind.values()){
			patternMatcher.append(String.format(format1,indexOfKind++ ,k.getRegularExpression()));
		}
		
		Pattern tokenPatterns = Pattern.compile(new String(patternMatcher.substring(1)));
		return tokenPatterns;
	}

	public LinePos getLinePos(Token token) {
		LinePos linePos= new LinePos(token.line, token.linePosition);
		return linePos;
	}
}
