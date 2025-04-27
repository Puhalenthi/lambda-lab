package src.Variables;

import src.Expression;

public abstract class Variable implements Expression {
	protected String name;

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

}
