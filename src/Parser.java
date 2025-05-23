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
	public Expression parse(ArrayList<String> tokens)
			throws ParseException, DuplicateKeyException, NumberFormatException {
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
			Expression newExpression = parse(new ArrayList<String>(tokens.subList(1, tokens.size())));
			return Runner.runWithDeepCopy(newExpression);
		}

		// extra credit 2
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

	private void preparse(ArrayList<String> tokens) {
		Stack<Integer> lambdaPositions = new Stack<>();

		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equals("\\")) {
				// if lambda operator is not preceded by an opening parenthesis, insert one
				if (i == 0 || !tokens.get(i - 1).equals("(")) {
					tokens.add(i, "(");
					lambdaPositions.push(i);
					i++; // move past the inserted parenthesis and lambda operator
				}
			} else if (tokens.get(i).equals("(")) {
				lambdaPositions.push(i);
			} else if (tokens.get(i).equals(")")) {
				// if a closing parenthesis is found, try to balance an inserted lambda opening
				if (!lambdaPositions.isEmpty()) {
					lambdaPositions.pop();
				}
			}
		}

		// add missing closing parentheses for every unmatched lambda opening
		while (!lambdaPositions.isEmpty()) {
			tokens.add(")");
			lambdaPositions.pop();
		}

		System.out.println("Tokens: " + tokens);
	}

	private Expression runParse(ArrayList<String> tokens, ArrayList<ParameterVariable> parameters) {
		if (tokens.size() == 0) {
			return null;
		}

		// the tokens describe a function
		if (tokens.get(0).equals("\\")) {

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

		// the tokens describe a single variable
		if (topLevelItems.size() == 1 && topLevelItems.get(0).size() == 1) {
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
