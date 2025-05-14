package src;

import java.util.ArrayList;

public class Lexer {

	public ArrayList<String> tokenize(String input) {
		ArrayList<String> tokens = new ArrayList<String>();

		int index = 0;
		String variableName = "";
		char currentChar = input.charAt(index);

		while (currentChar != ';' && index < input.length()) {
			if (currentChar == '(' || currentChar == ')' || currentChar == '\\' || currentChar == '.'
					|| currentChar == '=' || currentChar == ' ') {
				if (!variableName.equals("")) {
					tokens.add(variableName);
					variableName = "";
				}
				if (currentChar != ' ') {
					tokens.add(currentChar + "");
				}
			} else {
				variableName += currentChar;
			}

			index++;
			if (index != input.length()) {
				currentChar = input.charAt(index);
			}
		}
		if (index == input.length() || currentChar == ';') {
			if (!variableName.equals("")) {
				tokens.add(variableName);
			}
		}
		return tokens;
	}

}
