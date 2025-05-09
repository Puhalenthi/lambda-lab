package src;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import src.variables.FreeVariable;
import src.variables.ParameterVariable;

public class Parser {

	ArrayList<ParameterVariable> debugParameterList = new ArrayList<>();

	/*
	 * Turns a set of tokens into an expression. Comment this back in when you're
	 * ready.
	 */
	public Expression parse(ArrayList<String> tokens) throws ParseException, DuplicateKeyException {
		preparse(tokens);

		// setting an expression
		if (tokens.size() > 2 && tokens.get(1).equals("=")) {
			Expression expression = Memory.add(tokens.get(0),
					parse(new ArrayList<String>(tokens.subList(2, tokens.size()))));
			System.out.println("Added " + expression + " as " + tokens.get(0));
			return null;
		}

		// running an expression
		if (tokens.size() > 1 && tokens.get(0).equals("run")) {
			return Runner.runWithDeepCopy(recursiveParse(new ArrayList<String>(tokens.subList(1, tokens.size())), null));
		}

		Expression expression = recursiveParse(tokens, null);

		return expression;
	}

	private void preparse(ArrayList<String> tokens) {
		Stack<String> parenBalancer = new Stack<>();

		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("\\") && (i < 1 || !tokens.get(i - 1).equals("("))) {
				tokens.add(i, "(");
				parenBalancer.add("a");
				i++;
			} else if (tokens.get(i).equals("(")) {
				parenBalancer.add("(");
			} else if (tokens.get(i).equals(")") && parenBalancer.peek().equals("(")) {
				parenBalancer.pop();
			} else if (tokens.get(i).equals(")") && parenBalancer.peek().equals("a")) {
				tokens.add(i, ")");
				parenBalancer.pop();
				parenBalancer.pop();
				i++;
			}
		}

		while (!parenBalancer.isEmpty()) {
			tokens.add(")");
			parenBalancer.pop();
		}
	}

	private Expression recursiveParse(ArrayList<String> tokens, ArrayList<ParameterVariable> parameters) {

		// the tokens describe a function
		if (tokens.get(0).equals("\\")) {

			ParameterVariable parameterVariable = new ParameterVariable(tokens.get(1));
			ArrayList<ParameterVariable> newParameterList = addOrUpdateParameterList(parameters, parameterVariable);

			Expression parsedExpression = recursiveParse(new ArrayList<String>(tokens.subList(3, tokens.size())),
					newParameterList);
			Function newFunction = new Function(parameterVariable, parsedExpression);

			if (parsedExpression instanceof Application parsedApplication) {
				parsedApplication.setParent(newFunction);
			}

			return newFunction;
		}

		ArrayList<ArrayList<String>> topLevelItems = separateTopLevelItems(tokens);

		// the tokens describe a single variable
		if (topLevelItems.size() == 1 && topLevelItems.get(0).size() == 1) {
			ParameterVariable matchingParameter = getMatchingParameter(parameters, topLevelItems.get(0).get(0));
			if (matchingParameter == null) {
				Expression memoryItem = Memory.get(topLevelItems.get(0).get(0));
				if (memoryItem == null) {
					return new FreeVariable(topLevelItems.get(0).get(0));
				} else {
					return memoryItem;
				}
			} else {
				return matchingParameter.addBoundedVariable(topLevelItems.get(0).get(0));
			}
		}

		Expression head = null;

		for (int i = 0; i < topLevelItems.size(); i++) {
			Expression currentExpression = recursiveParse(topLevelItems.get(i), parameters);
			if (head == null) {
				head = currentExpression;
			} else {
				if (head instanceof Application) {
					Application newApplication = new Application(head, currentExpression);
					((Application) head).parent = newApplication;
					if (currentExpression instanceof Application) {
						((Application) currentExpression).setParent(newApplication);
					}
					head = ((Application) head).parent;
				} else {
					Application newApplication = new Application(head, currentExpression);
					if (currentExpression instanceof Application) {
						((Application) currentExpression).setParent(newApplication);
					}
					head = newApplication;
				}
			}
		}

		return head;
	}

	private ArrayList<ArrayList<String>> separateTopLevelItems(ArrayList<String> tokens) {
		ArrayList<ArrayList<String>> topLevelItems = new ArrayList<>();

		ArrayList<String> currentItem = new ArrayList<>();
		int openingParenCount = 0;

		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);

			// currently inside a top level item
			if (openingParenCount != 0 && (!token.equals(")") || openingParenCount > 1)) {
				currentItem.add(token);
			}

			if (token.equals("(")) {
				openingParenCount++;
			} else if (token.equals(")")) {
				openingParenCount--;
				if (openingParenCount == 0) {
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

	private ArrayList<ParameterVariable> addOrUpdateParameterList(ArrayList<ParameterVariable> parameterList,
			ParameterVariable newParameter) {

		ArrayList<ParameterVariable> newParameterList = new ArrayList<>();
		if (parameterList == null){
			newParameterList.add(newParameter);
			return newParameterList;
		}

		for (int i = 0; i < parameterList.size(); i++) {
			newParameterList.add(parameterList.get(i));
		}
		newParameterList.add(newParameter);

		return newParameterList;

		// if (parameterList == null) {
		// 	parameterList = new ArrayList<>();
		// }

		// for (int i = 0; i < parameterList.size(); i++) {
		// 	if (parameterList.get(i).getName().equals(newParameter.getName())) {
		// 		parameterList.set(i, newParameter);
		// 		return parameterList;
		// 	}
		// }

		// parameterList.add(newParameter);
		// return parameterList;
	}

	private ParameterVariable getMatchingParameter(ArrayList<ParameterVariable> parameterList, String token) {
		if (parameterList == null) {
			return null;
		}

		for (int i=parameterList.size()-1; i>=0; i--) {
			if (parameterList.get(i).getName().equals(token)) {
				return parameterList.get(i);
			}
		}
		return null;
	}
}
