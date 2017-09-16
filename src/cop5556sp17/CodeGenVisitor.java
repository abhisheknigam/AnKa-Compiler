package cop5556sp17;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	int curr_slot = 1;
	HashSet<Dec> allVarDeclarationsInScope = new HashSet<Dec>();
	
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		int curr_slot_num = 0;
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		ArrayList<ParamDec> params = program.getParams();
		
		for (ParamDec dec : params) {
			dec.setSlot(curr_slot_num++);
			FieldVisitor fv = cw.visitField(0, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, null);
			fv.visitEnd();
		}
		
		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				new String[] { "java/net/MalformedURLException" });
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				 new String[] { "java/net/MalformedURLException" });
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, mv);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		
		for (Dec dec : allVarDeclarationsInScope) {
			mv.visitLocalVariable(dec.getIdent().getText(), classDesc, null, startRun, endRun, dec.getSlot());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		if (assignStatement.getVar().getDec().getTypeName().equals(TypeName.IMAGE) && assignStatement.getE().getType().equals(TypeName.IMAGE)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig,false);
		}
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		mv = (MethodVisitor) arg;
		List<Dec> decs = block.getDecs();
		List<Statement> statements = block.getStatements();
		
		if(decs != null){
			for(Dec dec : decs){
				dec.visit(this, mv);
			}
		}
		
		if(statements != null){
			for(Statement statement: statements){
				statement.visit(this, mv);
				if(statement instanceof BinaryChain && !((BinaryChain)statement).getType().equals(TypeName.INTEGER)){
					mv.visitInsn(POP);
				}
			}
		}
		return null;
	}
	
	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		MethodVisitor mv =  (MethodVisitor) arg;
		
		Chain e0 = binaryChain.getE0();
		Chain e1 = binaryChain.getE1();
		
		
		TypeName typeE0 = e0.getType();
		TypeName typeE1 = e1.getType();
		
		if(binaryChain.getArrow().isKind(Kind.BARARROW)){
			if(typeE0.equals(TypeName.IMAGE) && binaryChain.getE1() instanceof FilterOpChain) {
	        	e0.setLeft(true);
	            e0.visit(this, arg);
	            
	            e1.setLeft(false);
	    	 	e1.visit(this,arg);
	         }
		}else if(binaryChain.getArrow().isKind(Kind.ARROW)){
			 if(typeE0.equals(TypeName.IMAGE) && e1 instanceof FilterOpChain) {
	        	 e0.setLeft(true);
	        	 e0.visit(this, arg);
	        	 
	        	 e1.setLeft(false);
	    	 	 e1.visit(this,arg);
	         }else if(typeE0.equals(TypeName.IMAGE) && e1 instanceof IdentChain && ((IdentChain) binaryChain.getE1()).getDec().getTypeName().equals(TypeName.IMAGE)) {
	        	 e0.setLeft(true);
	        	 e0.visit(this, arg);
	        	 
	        	 e1.setLeft(false);
	    	 	 e1.visit(this,arg);
	         }else if(typeE0.equals(TypeName.INTEGER) && e1 instanceof IdentChain && ((IdentChain) binaryChain.getE1()).getDec().getTypeName().equals(TypeName.INTEGER)) {
	        	 e0.setLeft(true);
	        	 e0.visit(this, arg);
	        	 
	        	 e1.setLeft(false);
	    	 	 e1.visit(this,arg);
	        	 
	         }else if(typeE0.equals(TypeName.FILE) && typeE1.equals(TypeName.IMAGE)) {
				e0.setLeft(true);
				e0.visit(this, arg);
	            
				mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"readFromFile",PLPRuntimeImageIO.readFromFileDesc,false);
	            
	            e1.setLeft(false);
	    	 	e1.visit(this,arg);
	         }else if(typeE0.equals(TypeName.URL) && typeE1.equals(TypeName.IMAGE)) {
	        	e0.setLeft(true);
	        	e0.visit(this, arg);
	            
	        	mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"readFromURL",PLPRuntimeImageIO.readFromURLSig,false);
	            
	            e1.setLeft(false);
	    	 	e1.visit(this,arg);
	         }else if(typeE0.equals(TypeName.FRAME) && e1 instanceof  FrameOpChain) {
	        	 e0.setLeft(true);
	        	 e0.visit(this, arg);
	        	 
	        	 e1.setLeft(false);
	    	 	 e1.visit(this,arg);
	         }else if(typeE0.equals(TypeName.IMAGE) && e1 instanceof  ImageOpChain) {
	        	 e0.setLeft(true);
	        	 e0.visit(this, arg);
	        	 
	        	 e1.setLeft(false);
	    	 	 e1.visit(this,arg);
	         }else if(typeE0.equals(TypeName.IMAGE) && typeE1.equals(TypeName.FRAME)) {
	        	e0.setLeft(true);
	        	e0.visit(this, arg);
	        	e1.setLeft(true);
	        	e1.visit(this,arg);
	            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeFrame.JVMClassName,"createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig,false);
		        binaryChain.getE1().setLeft(false);
		        binaryChain.getE1().visit(this, arg);
		        
	         }else if(typeE0.equals(TypeName.IMAGE) && typeE1.equals(TypeName.FILE)) {
	        	e0.setLeft(true);
	        	e0.visit(this, arg);
	        	e1.setLeft(true);
	        	e1.visit(this,arg);
	            mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageIO.className,"write",PLPRuntimeImageIO.writeImageDesc,false);
	         }
		}
		
		return null;
	}

	
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		mv = (MethodVisitor)arg;
	
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		
		TypeName typeE0 = e0.getType();
		TypeName typeE1 = e1.getType();
		
		e0.visit(this, arg);
		e1.visit(this, arg);
		
		
		Kind kind = binaryExpression.getOp().kind;
		
		Label jump = new Label();
		Label endJump = new Label();
		
		switch(kind){
			case PLUS:
				if (typeE0.equals(TypeName.IMAGE) && typeE1.equals(TypeName.IMAGE)) {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
				} else {
					mv.visitInsn(IADD);
				}
				break;
			case MINUS:
				if (typeE0.equals(TypeName.IMAGE) && typeE1.equals(TypeName.IMAGE)) {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
				}else{
					mv.visitInsn(ISUB);
				}
				break;
			case TIMES:
				if (typeE0.equals(TypeName.IMAGE) || typeE1.equals(TypeName.IMAGE)) {
					if(binaryExpression.getE0().getType().equals(TypeName.INTEGER) && binaryExpression.getE1().getType().equals(TypeName.IMAGE)){
						mv.visitInsn(SWAP);
					}
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
				}else{
					mv.visitInsn(IMUL);
				}
				break;
			case DIV:
				if (typeE0.equals(TypeName.IMAGE)) {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
				}else{
					mv.visitInsn(IDIV);
				}
				break;
			case MOD:
				if (typeE0.equals(TypeName.IMAGE)) {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
				}else{
					mv.visitInsn(IREM);
				}
				break;
			case AND:
				mv.visitInsn(IAND);
				break;
			case OR:
				mv.visitInsn(IOR);
				break;
						
			case LE:	
				mv.visitJumpInsn(IF_ICMPLE, jump);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endJump);
				mv.visitLabel(jump);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endJump);
				break;
			case GE:	
				mv.visitJumpInsn(IF_ICMPGE, jump);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endJump);
				mv.visitLabel(jump);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endJump);
				break;
			case LT:	
				mv.visitJumpInsn(IF_ICMPLT, jump);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endJump);
				mv.visitLabel(jump);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endJump);
				break;
			case GT:	
				mv.visitJumpInsn(IF_ICMPGT, jump);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endJump);
				mv.visitLabel(jump);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endJump);
				break;
			case NOTEQUAL:
				if(typeE0.equals(TypeName.INTEGER)){
					mv.visitJumpInsn(IF_ICMPNE, jump);
				}else{
					mv.visitJumpInsn(IF_ACMPNE, jump);
				}
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endJump);
				mv.visitLabel(jump);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endJump);
				break;
			case EQUAL:	
				if(typeE0.equals(TypeName.INTEGER)){
					mv.visitJumpInsn(IF_ICMPEQ, jump);
				}else{
					mv.visitJumpInsn(IF_ACMPEQ, jump);
				}
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endJump);
				mv.visitLabel(jump);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endJump);
				break;
		}
		return null;
	}

	

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		mv = (MethodVisitor)arg;
		boolean bool = booleanLitExpression.getValue();
		mv.visitLdcInsn(bool);
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		MethodVisitor mv = (MethodVisitor) arg;
		
		
		if(constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}else if(constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		mv = (MethodVisitor)arg;
		
		declaration.setSlot(curr_slot);
		allVarDeclarationsInScope.add(declaration);
		curr_slot = curr_slot + 1;
		
		if(declaration.getTypeName().name().equals("FRAME") || declaration.getTypeName().name().equals("IMAGE")){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor) arg;
		filterOpChain.getArg().visit(this, arg);
		
		mv.visitInsn(ACONST_NULL);
		
		if(filterOpChain.getFirstToken().isKind(Kind.OP_BLUR)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		}else if(filterOpChain.getFirstToken().isKind(Kind.OP_GRAY)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}else if(filterOpChain.getFirstToken().isKind(Kind.OP_CONVOLVE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		} 		
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		
		frameOpChain.getArg().visit(this, arg);
		
		if(frameOpChain.getFirstToken().isKind(Kind.KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(Kind.KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(Kind.KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(Kind.KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		}else if(frameOpChain.getFirstToken().isKind(Kind.KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor) arg;
		boolean isLeft = identChain.isLeft();
		Dec identDec = identChain.getDec();
		TypeName type = identDec.getTypeName();
		
		if(!isLeft){
			if( identDec instanceof ParamDec){
				mv.visitVarInsn(ALOAD,0);
	        	mv.visitInsn(SWAP);
	            mv.visitFieldInsn(PUTFIELD, className,identChain.getFirstToken().getText(),type.getJVMTypeDesc());
			} else if(type.equals(TypeName.IMAGE)){
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identDec.getSlot());
			}else if(type.equals(TypeName.FRAME)){
				mv.visitInsn(DUP);
	            mv.visitVarInsn(ASTORE, identDec.getSlot());
			}else  {
				mv.visitVarInsn(ISTORE, identDec.getSlot());
			}
		}else{
			if(identDec instanceof ParamDec){
				mv.visitVarInsn(ALOAD,0);
	            mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),type.getJVMTypeDesc());
			}else if(type.equals(TypeName.IMAGE)){
				mv.visitVarInsn(ALOAD, identDec.getSlot());
			}else if(type.equals(TypeName.FRAME)){
	            mv.visitVarInsn(ALOAD, identDec.getSlot());
			}else {
				mv.visitVarInsn(ILOAD, identDec.getSlot());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		mv = (MethodVisitor)arg;
		Dec dec = identExpression.getDec();
		String type = null;
		TypeName name = dec.getTypeName();
		
		if(dec.getTypeName().equals(TypeName.INTEGER)){
			type = dec.getTypeName().getJVMTypeDesc();
		}else if(dec.getTypeName().equals(TypeName.BOOLEAN)){
			type = dec.getTypeName().getJVMTypeDesc();
		}
		
		if(dec instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());

		} else {
			if( name.equals(TypeName.INTEGER) || name.equals(TypeName.BOOLEAN)){
				mv.visitVarInsn(ILOAD, dec.getSlot());
			}else{
				mv.visitVarInsn(ALOAD, dec.getSlot());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor) arg;
		Dec dec = identX.getDec();
		
		String type = null;
		TypeName name = dec.getTypeName();
		if(name.equals(TypeName.INTEGER)){
			type = name.getJVMTypeDesc();
		}else if(name.equals(TypeName.BOOLEAN)){
			type = name.getJVMTypeDesc();
		}
		
		if (dec instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());
		} else {
			if (name.equals(TypeName.INTEGER) || name.equals(TypeName.BOOLEAN)) {
				mv.visitVarInsn(ISTORE, dec.getSlot());
			} else {
				mv.visitVarInsn(ASTORE, dec.getSlot());
			}
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		mv = (MethodVisitor)arg;
		ifStatement.getE().visit(this, arg);
		Label startIf = new Label();
		mv.visitJumpInsn(IFEQ, startIf);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(startIf);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		if(imageOpChain.getFirstToken().isKind(Kind.OP_WIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, "Ljava/awt/image/BufferedImage", "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		}else if(imageOpChain.getFirstToken().isKind(Kind.OP_HEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, "Ljava/awt/image/BufferedImage", "getHeight", PLPRuntimeImageOps.getHeightSig, false);
		}else if(imageOpChain.getFirstToken().isKind(Kind.KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		} 
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {		
		mv = (MethodVisitor)arg;
		mv.visitIntInsn(SIPUSH, intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		mv = (MethodVisitor)arg;
		
		mv.visitVarInsn(ALOAD, 0);
		
		if(paramDec.getTypeName().equals(TypeName.FILE)){
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
		}
			
		mv.visitVarInsn(ALOAD, 1);
		mv.visitIntInsn(SIPUSH, paramDec.getSlot());
		
		String type = null;
		TypeName name = paramDec.getTypeName();
		if(name.equals(TypeName.INTEGER)){
			mv.visitInsn(AALOAD);
			type = TypeName.INTEGER.getJVMTypeDesc();
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
		}else if(name.equals(TypeName.BOOLEAN)){
			mv.visitInsn(AALOAD);
			type = TypeName.BOOLEAN.getJVMTypeDesc();
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
		}else if(name.equals(TypeName.URL)){
			type = TypeName.URL.getJVMTypeDesc();
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
		}else if(name.equals(TypeName.FILE)){
			type = TypeName.FILE.getJVMTypeDesc();
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
		}
		
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), type);

		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression e : tuple.getExprList()) {
			e.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		mv = (MethodVisitor)arg;
		Label GUARD = new Label();
		mv.visitJumpInsn(GOTO, GUARD);
		
		Label BODY = new Label();
		mv.visitLabel(BODY);
		whileStatement.getB().visit(this, arg);
		
		mv.visitLabel(GUARD);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, BODY);
		
		return null;
	}
}
