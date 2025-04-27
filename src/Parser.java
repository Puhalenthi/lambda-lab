package src;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeSet;

import src.Variables.BoundVariable;
import src.Variables.FreeVariable;
import src.Variables.ParameterVariable;
import src.Variables.Variable;

public class Parser {

	/*
	 * Turns a set of tokens into an expression.  Comment this back in when you're ready.
	 */
	public Expression parse(ArrayList<String> tokens) throws ParseException {
		preparse(tokens);

		Expression expression = recursiveParse(tokens, null);

		Variable var = new FreeVariable(tokens.get(0));

		// This is nonsense code, just to show you how to thrown an Exception.
		// To throw it, type "error" at the console.
		if (var.toString().equals("error")) {
			throw new ParseException("User typed \"Error\" as the input!", 0);
		}

		return expression;
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


	private Expression recursiveParse(ArrayList<String> tokens, ArrayList<ParameterVariable> parameters){

		//the tokens describe a function
		if (tokens.get(0).equals("\\")){
			ParameterVariable parameterVariable = new ParameterVariable(tokens.get(1));
			ArrayList<ParameterVariable> newParameterList = addOrUpdateParameterList(parameters, parameterVariable);
			return new Function(parameterVariable, recursiveParse(new ArrayList<String>(tokens.subList(3, tokens.size())), newParameterList));
		}

		ArrayList<ArrayList<String>> topLevelItems = separateTopLevelItems(tokens);
		
		//the tokens describe a free variable
		if (topLevelItems.size() == 1 && topLevelItems.get(0).size() == 1){
			ParameterVariable matchingParameter = getMatchingParameter(parameters, topLevelItems.get(0).get(0));
			if (matchingParameter == null){
				return new FreeVariable(topLevelItems.get(0).get(0));
			} else {
				return matchingParameter.addBoundedVariable(topLevelItems.get(0).get(0));
			}
		}
		
		Expression head = null;

		for (int i=0; i<topLevelItems.size(); i++){
			Expression currentExpression = recursiveParse(topLevelItems.get(i), parameters);
			if (head == null){
				head = currentExpression;
			} else {
				head = new Application(head, currentExpression);
			}
		}
		
		return head;
	}

	private ArrayList<ArrayList<String>> separateTopLevelItems(ArrayList<String> tokens) {
		ArrayList<ArrayList<String>> topLevelItems = new ArrayList<>();

		ArrayList<String> currentItem = new ArrayList<>();
		int openingParenCount = 0;


		for (int i=0; i<tokens.size(); i++){
			String token = tokens.get(i);


			//currently inside a top level item
			if (openingParenCount != 0 && (!token.equals(")") || openingParenCount > 1)){
				currentItem.add(token);
			}

			if (token.equals("(")) {
				openingParenCount++;
			} else if (token.equals(")")){
				openingParenCount--;
				if (openingParenCount==0){
					topLevelItems.add(currentItem);
					currentItem = new ArrayList<>();
				}
			} else if (openingParenCount == 0) {
				currentItem.add(token);
				topLevelItems.add(currentItem);
				currentItem = new ArrayList<>();
			}
		}
		return topLevelItems;
	}

	private ArrayList<ParameterVariable> addOrUpdateParameterList(ArrayList<ParameterVariable> parameterList, ParameterVariable newParameter){
		if (parameterList == null) {
			parameterList = new ArrayList<>();
		}
		
		for (int i = 0; i < parameterList.size(); i++) {
			if (parameterList.get(i).toString().equals(newParameter.toString())) {
				parameterList.set(i, newParameter);
				return parameterList;
			}
		}
		
		parameterList.add(newParameter);
		return parameterList;
	}

	private ParameterVariable getMatchingParameter(ArrayList<ParameterVariable> parameterList, String token){
		if (parameterList == null){
			return null;
		}

		for (ParameterVariable p : parameterList) {
			if (p.toString().equals(token)){
				return p;
			}
		}
		return null;
	}
}