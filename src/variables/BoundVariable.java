//Zevi Cohen & Puhalenthi Ramesh Vidhya
package src.variables;


public class BoundVariable extends Variable {
    ParameterVariable parameter;

    public BoundVariable(String name, ParameterVariable parameter) {
        this.name = name;
        this.parameter = parameter;
    }


    public ParameterVariable getParameter() {
        return parameter;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String toString() {
        return "\u001B[31m" + this.name + "\u001B[0m";
    }
}
