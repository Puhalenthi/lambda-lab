package src.Variables;

import java.util.ArrayList;

public class ParameterVariable extends Variable{
    ArrayList<BoundVariable> boundedVariables = new ArrayList<>();

    public ParameterVariable(String name) {
		this.name = name;
	}
}
