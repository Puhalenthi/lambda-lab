package src.variables;

import src.Expression;

public abstract class Variable implements Expression {
	protected String name;

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
