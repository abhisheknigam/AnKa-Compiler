# AnKa-Compiler
Designing and Implementing of a Compiler for a small programming language

## Implemented  a scanner for the programming language with the following lexical structure.

    comment ::= /* NOT( */ )* */
    token ::= ident | keyword | frame_op_keyword | filter_op_keyword | image_op_keyword | boolean_literal
    | int_literal | separator | operator
    ident ::= ident_start ident_part* (but not reserved)
    ident_start ::= A .. Z | a .. z | $ | _
    ident_part ::= ident_start | ( 0 .. 9 )
    int_literal ::= 0 | ( 1 .. 9 ) ( 0 .. 9 )*
    keyword ::= integer | boolean | image | url | file | frame | while | if | sleep | screenheight | screenwidth
    filter_op_keyword ∷ = gray | convolve | blur | scale
    image_op_keyword ∷ = width | height
    frame_op_keyword ∷ = xloc | yloc | hide | show | move
    boolean_literal ::= true | false
    separator ::= ; | , | ( | ) | { | }
    operator ::= | | & | == | != | < | > | <= | >= | + | - | * | / | % | ! | -> | |-> | <-
 
 
## Implemented a recursive descent parser for the following context-free grammar:

    program ::= IDENT block
    program ::= IDENT param_dec ( , param_dec )* block
    paramDec ::= ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN ) IDENT
    block ::= { ( dec | statement) * }
    dec ::= ( KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME ) IDENT
    statement ::= OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
    assign ::= IDENT ASSIGN expression
    chain ::= chainElem arrowOp chainElem ( arrowOp chainElem)*
    whileStatement ::= KW_WHILE ( expression ) block
    ifStatement ::= KW_IF ( expression ) block
    arrowOp ∷= ARROW | BARARROW
    chainElem ::= IDENT | filterOp arg | frameOp arg | imageOp arg
    filterOp ::= OP_BLUR | OP_GRAY | OP_CONVOLVE
    frameOp ::= KW_SHOW | KW_HIDE | KW_MOVE | KW_XLOC | KW_YLOC
    imageOp ::= OP_WIDTH | OP_HEIGHT | KW_SCALE
    arg ::= ε | ( expression ( , expression)* )
    expression ∷= term ( relOp term)*
    term ∷= elem ( weakOp elem)*
    elem ∷= factor ( strongOp factor)*
    factor ∷= IDENT | INT_LIT | KW_TRUE | KW_FALSE
    | KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
    relOp ∷= LT | LE | GT | GE | EQUAL | NOTEQUAL
    weakOp ∷= PLUS | MINUS | OR
    strongOp ∷= TIMES | DIV | AND | MOD
    
## An abstract syntax tree specified by the following abstract syntax:

    Program ∷ = List<ParamDec> Block
    ParamDec ∷ = type ident
    Block ∷ = List<Dec> List<Statement>
    Dec ∷ = type ident
    Statement ∷ = SleepStatement | WhileStatement | IfStatement | Chain
    | AssignmentStatement
    SleepStatement ∷ = Expression
    AssignmentStatement ∷ = IdentLValue Expression
    Chain ∷ = ChainElem | BinaryChain
    ChainElem ::= IdentChain | FilterOpChain | FrameOpChain | ImageOpChain
    IdentChain ∷ = ident
    FilterOpChain ∷ = filterOp Tuple
    FrameOpChain ∷ = frameOp Tuple
    ImageOpChain ∷ = imageOp Tuple
    BinaryChain ∷ = Chain (arrow | bararrow) ChainElem
    WhileStatement ∷ = Expression Block
    IfStatement ∷ = Expression Block
    Expression ∷ = IdentExpression | IntLitExpression | BooleanLitExpression
    | ConstantExpression | BinaryExpression
    IdentExpression ∷ = ident
    IdentLValue ∷ = ident
    IntLitExpression ∷ = intLit
    BooleanLitExpression ∷ = booleanLiteral
    ConstantExpression ∷ = screenWidth | screenHeight
    BinaryExpression ∷ = Expression op Expression
    Tuple : ≔ List<Expression>
    op ∷ = relOp | weakOp | strongOp
    type ∷ = integer | image | frame | file | boolean | url
    
 ## Implemented a LeBlanc-Cook symbol table
 
    SymbolTable symbab = new SymbolTable()
    Program ∷ = List<ParamDec> Block
    ParamDec ∷ = type ident symtab.insert(ident.getText(), ParamDec);
    Block ∷ = symtab.enterScope() List<Dec> List<Statement> symtab.leaveScope()
    Dec ∷ = type ident symtab.insert(ident.getText(), Dec);
    Statement ∷ = SleepStatement | WhileStatement | IfStatement | Chain
    | AssignmentStatement
    SleepStatement ∷ = Expression condition: Expression.type==INTEGER
    AssignmentStatement ∷ = IdentLValue Expression
    condition: IdentLValue.type== Expression.type
    Chain ∷ = ChainElem | BinaryChain
    ChainElem ::= IdentChain | FilterOpChain | FrameOpChain | ImageOpChain
    IdentChain ∷ = ident
    condition: ident has been declared and is visible in the current scope
    IdentChain.type <- ident.type
    ident.type <- symtab.lookup(ident.getText()).getType()
    FilterOpChain ∷ = filterOp Tuple
    condition: Tuple.length == 0
    FilterOpChain.type <- IMAGE
    FrameOpChain ∷ = frameOp Tuple
    if (FrameOP.isKind(KW_SHOW, KW_HIDE)
    {
        condition: Tuple.length == 0
        FrameOpChain.type <- NONE
    }

    else if (FrameOp.isKind(KW_XLOC, KW_YLOC)
    {
        condition: Tuple.length == 0
        FrameOpChain.type <- INTEGER
    }

    else if(FrameOp.isKind(KW_MOVE)
    {
        condition: Tuple.length == 2
        FrameOpChain.type <- NONE
    }

    else there is a bug in your parser
    ImageOpChain ∷ = imageOp Tuple
    if (imageOp.isKind(OP_WIDTH, OP_HEIGHT)
    {
        condition: Tuple.length == 0
        ImageOpChain.type <- INTEGER
    }

    else if (imageOP.isKind(KW_SCALE))
    {
        condition: Tuple.length==1
        ImageOpChain.type <- IMAGE
    }

    BinaryChain ∷ = Chain (arrow | bararrow) ChainElem
    
## Implemented code generation

    Program ∷= Name List<ParamDec> Block
    class Name implements Runnable
    {
        variables declared in List<ParamDec> are instance variables of the class
        public Name(String[] args)
        {
            initialize instance variables with values from args.
        }

        public static void main(String[] args)
        {
            Name instance = new Name(args);
            instance.run();
        }

        public void run()
        {
            declarations and statements from block
        }

    }

    ParamDec ∷= type ident
    instance variable in class, initialized with values from arg array
    Block ∷= List<Dec> List<Statement>
    
    Decs are local variables in current scope of run method
    
    Statements are executed in run method
    Must label beginning and end of scope, and keep track of local variables, their slot in the local variable array, and     their range of visibility.
    
    Dec ∷= type ident
    
    Assign a slot in the local variable array to this variable and save it in the new slot attribute in the Dec class.
    Statement ∷= SleepStatement | WhileStatement | IfStatement | Chain
    | AssignmentStatement
    
    SleepStatement ∷= Expression
    
    AssignmentStatement ∷= IdentLValue Expression
    
    store value of Expression into location indicated by IdentLValue
    
    IMPORTANT:
    insert the following statement into your code for an Assignment Statement
    after value of expression is put on top of stack and before it is written into the
    IdentLValue
    
    CodeGenUtils.genPrintTOS(GRADE, mv,assignStatement.getE().getType());
    
    Chain ∷= ChainElem | BinaryChain
    
    ChainElem ::= IdentChain | FilterOpChain | FrameOpChain | ImageOpChain
    
    IdentChain ∷= ident
    FilterOpChain ∷= filterOp Tuple
    FrameOpChain ∷= frameOp Tuple
    ImageOpChain ∷= imageOp Tuple
    BinaryChain ∷= Chain (arrow | bararrow) ChainElem
    WhileStatement ∷= Expression Block
    goto GUARD
    BODY Block
    GUARD Expression
    IFNE BODY
    
    IfStatement ∷= Expression Block
    Expression
    IFEQ AFTER
    Block
    AFTER …
    Expression ∷= IdentExpression | IntLitExpression | BooleanLitExpression
    | ConstantExpression | BinaryExpression
    
    always generate code to leave value of expression on top of stack.
    IdentExpression ∷= ident
    load value of variable (this could be a field or a local var)
    IdentLValue ∷= ident
    
    store value on top of stack to this variable (which could be a field or local var)
    IntLitExpression ∷= intLit
    load constant
    
    BooleanLitExpression ∷= booleanLiteral
    load constant
    ConstantExpression ∷= screenWidth | screenHeight
    BinaryExpression ∷= Expression op Expression
    
    Visit children to generate code to leave values of arguments on stack
    perform operation, leaving result on top of the stack. Expressions should
    be evaluated from left to write consistent with the structure of the AST.
    
    Tuple :≔ List<Expression>
    
    op ∷= relOp | weakOp | strongOp
    
    type ∷= integer | image | frame | file | boolean | url
