package src;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {

	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */
	public Expression parse(ArrayList<String> tokens) throws ParseException {
		preparse(tokens);

		Variable var = new Variable(tokens.get(0));

		// This is nonsense code, just to show you how to thrown an Exception.
		// To throw it, type "error" at the console.
		if (var.toString().equals("error")) {
			throw new ParseException("User typed \"Error\" as the input!", 0);
		}

		return var;
	}

	private void preparse(ArrayList<String> tokens) {
		Stack<String> parenBalancer = new Stack<>();

		for (int i=0; i<tokens.size(); i++){
			if (tokens.get(i).equals("\\") && (i<1 || !tokens.get(i-1).equals("("))){
				tokens.add(i, "(");
				parenBalancer.add("a");
				i++;
			} else if (tokens.get(i).equals("(")){
				parenBalancer.add("(");
			} else if (tokens.get(i).equals(")") && parenBalancer.peek().equals("(")){
				parenBalancer.pop();
			} else if (tokens.get(i).equals(")") && parenBalancer.peek().equals("a")){
				tokens.add(i, ")");
				parenBalancer.pop();
				parenBalancer.pop();
				i++;
			}
		}

		while (!parenBalancer.isEmpty()){
			tokens.add(")");
			parenBalancer.pop();
		}
	}
}
