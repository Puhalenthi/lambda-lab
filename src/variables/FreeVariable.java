package src.variables;

public class FreeVariable extends Variable {
	public FreeVariable(String name) {
		this.name = name;
	}

	public String toString() {
		return "\033[32m" + this.name + "\033[0m";
	}
}
