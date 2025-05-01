package src.variables;

public class BoundVariable extends Variable {
    ParameterVariable parameter;

    public BoundVariable(String name, ParameterVariable parameter) {
        this.name = name;
        this.parameter = parameter;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterVariable getParameter() {
        return parameter;
    }

    // public String toString(){
    // return "B:" + this.name;
    // }
}
