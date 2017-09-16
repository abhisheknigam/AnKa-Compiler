package cop5556sp17;



import java.util.HashMap;
import java.util.Stack;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	private static int scope;
	private HashMap<Integer, HashMap<String, Dec>> scopes;
	private Stack<Integer> scopeNumbers = new Stack<Integer>();

	
	public void enterScope(){
		scope = scope + 1;
		scopeNumbers.push(scope);
	}
	
	
	
	public void leaveScope(){
		scopeNumbers.pop();
	}
	
	public boolean insert(String ident, Dec dec){
		if(scopes.get(scopeNumbers.peek()) != null && scopes.get(scopeNumbers.peek()).containsKey(ident)){
			return false;
		}else{
			HashMap<String, Dec> declarations = scopes.get(scopeNumbers.peek());
			if(declarations == null){
				declarations = new HashMap<String, Dec>();
			}
			declarations.put(ident, dec);
			scopes.put(scopeNumbers.peek(), declarations);
		}
		return true;
	}
		
	public SymbolTable() {
		scope = 0;
		scopes = new HashMap<Integer,HashMap<String, Dec>>();
		scopeNumbers.push(scope);
	}


	@Override
	public String toString() {	
		return this.toString();
	}

	public Dec lookup(String ident){
		int currentScope = scopeNumbers.peek();
		Stack<Integer> localScopeNumbers= new Stack<Integer>();
		localScopeNumbers.addAll(scopeNumbers);
		while(!localScopeNumbers.isEmpty() && (scopes.get(currentScope) == null || scopes.get(currentScope).get(ident) == null)){
			HashMap<String, Dec> declarations = scopes.get(currentScope);
			if(declarations != null && declarations.get(ident) != null){
				return declarations.get(ident);
			}else{
				localScopeNumbers.pop();
				if(!localScopeNumbers.isEmpty()){
					currentScope = localScopeNumbers.peek();
				}
			}
		}
		if(scopes.get(currentScope) != null && scopes.get(currentScope).get(ident) != null){
			return scopes.get(currentScope).get(ident);
		}
		return null;
	}
	

	public boolean isVisible(String ident) {
		int currentScope = scopeNumbers.peek();
		Stack<Integer> localScopeNumbers= new Stack<Integer>();
		localScopeNumbers.addAll(scopeNumbers);
		while(!localScopeNumbers.isEmpty() && (scopes.get(currentScope) == null || scopes.get(currentScope).get(ident) == null)){
			HashMap<String, Dec> declarations = scopes.get(currentScope);
			if(declarations !=null && declarations.get(ident) != null){
				return true;
			}else{
				localScopeNumbers.pop(); 
				if(!localScopeNumbers.isEmpty()){
					currentScope = localScopeNumbers.peek();
				}
			}
		}
		if(scopes.get(currentScope) != null && scopes.get(currentScope).get(ident) != null){
			return true;
		}else{
			return false;
		}
	}
}
