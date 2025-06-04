package src;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import src.variables.FreeVariable;
import src.variables.ParameterVariable;

public class Parser {

	ArrayList<ParameterVariable> debugParameterList = new ArrayList<>();

	/*
	 * Turns a set of tokens into an expression.
	 * Handles assignment, run, and populate commands
	 */
	public Expression parse(ArrayList<String> tokens)
			throws ParseException, DuplicateKeyException, NumberFormatException {
		preparse(tokens);

		if (tokens.size() > 2 && tokens.get(1).equals("=")) {
			Expression expression = Memory.add(tokens.get(0),
					parse(new ArrayList<String>(tokens.subList(2, tokens.size()))));
			System.out.println("Added " + expression + " as " + tokens.get(0));
			return null;
		}

		if (tokens.size() > 1 && tokens.get(0).equals("run")) {
			Expression newExpression = parse(new ArrayList<String>(tokens.subList(1, tokens.size())));
			return Runner.runWithDeepCopy(newExpression);
		}

		if (tokens.size() > 1 && tokens.get(0).equals("populate")) {
			for (int i = Integer.parseInt(tokens.get(1)); i <= Integer.parseInt(tokens.get(2)); i++) {
				if (i < 0) {
					System.out.println("Cannot populate negative numbers.");
					return null;
				}
				if (Memory.contains(i + "")) {
					continue;
				}
				Function f = new Function(new ParameterVariable("f"), new Function(new ParameterVariable("x"), null));
				Function x = (Function) f.getExpression();
				x.setExpression(churchEncoding(i, f.getParameter(), x.getParameter()));
				Memory.add(i + "", f);
			}
			System.out.println("Populated numbers " + tokens.get(1) + " to " + tokens.get(2));
			return null;
		}

		Expression expression = runParse(tokens, null);
		return expression;
	}


	/*
	 * Adjusts tokens by adding parenthesis before and after lambda expressions
	 */
	private void preparse(ArrayList<String> tokens) {
		Stack<Character> parenBalancer = new Stack<>();
		parenBalancer.add('b');

		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("\\") && (i == 0 || !tokens.get(i - 1).equals("("))) {
				tokens.add(i, "(");
				parenBalancer.add('a');
				i++;
			} else if (tokens.get(i).equals("(")) {
				parenBalancer.add('(');
			} else if (tokens.get(i).equals(")")) {
				while (parenBalancer.peek() == 'a') {
					tokens.add(i, ")");
					i++;
					parenBalancer.pop();
				}
				if (parenBalancer.peek() == '(') {
					parenBalancer.pop();
				}
			}
		}
		while (parenBalancer.peek() != 'b') {
			tokens.add(")");
			parenBalancer.pop();
		}
	}


	/*
	 * Recursively parses tokens into an expression with proper parameter scoping
	 */
	private Expression runParse(ArrayList<String> tokens, ArrayList<ParameterVariable> parameters) {
		if (tokens.size() == 0) {
			return null;
		}

		if (tokens.get(0).equals("\\")) {
			// handles lambda abstraction; constructs a function expression
			ParameterVariable parameterVariable = new ParameterVariable(tokens.get(1));
			ArrayList<ParameterVariable> newParameterList = addOrUpdateParameterList(parameters, parameterVariable);
			Expression parsedExpression = runParse(new ArrayList<String>(tokens.subList(3, tokens.size())),
					newParameterList);
			Function newFunction = new Function(parameterVariable, parsedExpression);
			if (parsedExpression instanceof Application parsedApplication) {
				parsedApplication.setParent(newFunction);
			}
			return newFunction;
		}

		ArrayList<ArrayList<String>> topLevelItems = separateTopLevelItems(tokens);

		if (topLevelItems.size() == 1 && topLevelItems.get(0).size() == 1) {
			// returns a variable expression as either free or bound based on current context
			ParameterVariable matchingParameter = getMatchingParameter(parameters, topLevelItems.get(0).get(0));
			if (matchingParameter == null) {
				Expression memoryItem = Memory.get(topLevelItems.get(0).get(0));
				if (memoryItem == null) {
					return new FreeVariable(topLevelItems.get(0).get(0));
				}
				return memoryItem;
			}
			return matchingParameter.addBoundVariable(topLevelItems.get(0).get(0));
		}

		Expression head = null;
		for (int i = 0; i < topLevelItems.size(); i++) {
			// constructs application expressions by consecutively combining items
			Expression currentExpression = runParse(topLevelItems.get(i),
					parameters == null ? parameters : new ArrayList<>(parameters));
			if (head == null) {
				head = currentExpression;
			} else {
				if (head instanceof Application aHead) {
					Application newApplication = new Application(head, currentExpression);
					aHead.parent = newApplication;
					if (currentExpression instanceof Application aCurrentExpression) {
						aCurrentExpression.setParent(newApplication);
					}
					head = aHead.parent;
				} else {
					Application newApplication = new Application(head, currentExpression);
					if (currentExpression instanceof Application aCurrentExpression) {
						aCurrentExpression.setParent(newApplication);
					}
					head = newApplication;
				}
			}
		}
		return head;
	}


	/*
	 * Seperated tokens into top-level items by tracking balanced parenthesis
	 */
	private ArrayList<ArrayList<String>> separateTopLevelItems(ArrayList<String> tokens) {
		ArrayList<ArrayList<String>> topLevelItems = new ArrayList<>();
		ArrayList<String> currentItem = new ArrayList<>();

		int openingParenCount = 0;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
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


	/*
	 * Adds a new parameter to the existing list or creates a new list if none exists
	 */
	private ArrayList<ParameterVariable> addOrUpdateParameterList(ArrayList<ParameterVariable> parameterList,
			ParameterVariable newParameter) {
		ArrayList<ParameterVariable> newParameterList = new ArrayList<>();
		if (parameterList == null) {
			newParameterList.add(newParameter);
			return newParameterList;
		}
		for (int i = 0; i < parameterList.size(); i++) {
			newParameterList.add(parameterList.get(i));
		}
		newParameterList.add(newParameter);
		return newParameterList;
	}

	/*
	 * Retrieves the most recent parameter matching the token
	 */
	private ParameterVariable getMatchingParameter(ArrayList<ParameterVariable> parameterList, String token) {
		if (parameterList == null) {
			return null;
		}
		for (int i = parameterList.size() - 1; i >= 0; i--) {
			if (parameterList.get(i).getName().equals(token)) {
				return parameterList.get(i);
			}
		}
		return null;
	}

	/*
	 * Recursively constructs the church numeral representation for the number
	 */
	private Expression churchEncoding(int n, ParameterVariable parameterF, ParameterVariable parameterX) {
		if (n < 0) {
			return null;
		}
		if (n == 0) {
			return parameterX.addBoundVariable("x");
		}
		return new Application(parameterF.addBoundVariable("f"), churchEncoding(n - 1, parameterF, parameterX));
	}
}
