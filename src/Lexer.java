package src;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

	/*
	 * A lexer (or "tokenizer") converts an input into tokens that
	 * eventually need to be interpreted.
	 *
	 * Given the input
	 *    (\bat  .bat flies)cat  λg.joy! )
	 * you should output the ArrayList of strings
	 *    [(, \, bat, ., bat, flies, ), cat, \, g, ., joy!, )]
	 *
	 */
	public ArrayList<String> tokenize(String input) {
		ArrayList<String> tokens = new ArrayList<String>();

		int index = 0;
		String variableName = "";
		char currentChar = input.charAt(index);

		while (currentChar != ';' && index<input.length()){
			if (currentChar=='(' || currentChar==')' || currentChar=='\\' || currentChar=='.' || currentChar=='=' || currentChar==' '){
				if (!variableName.equals("")){
					tokens.add(variableName);
					variableName = "";
				}
				if (currentChar!=' '){
					tokens.add(currentChar+"");
				}
			} else {
				variableName += currentChar;
			}

			index++;
			if (index!=input.length()){
				currentChar = input.charAt(index);
			}
		}

		if (index==input.length()){
			if (!variableName.equals("")){
				tokens.add(variableName);
			}
		}

		return tokens;
	}



}
