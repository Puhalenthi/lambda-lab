package src.variables;

import java.util.ArrayList;

public class ParameterVariable extends Variable {
  ArrayList<BoundVariable> boundVariables = new ArrayList<>();

  public ParameterVariable(String name) {
    this.name = name;
  }

  public BoundVariable addBoundedVariable(String token) {
    BoundVariable newBoundVariable = new BoundVariable(token);
    boundVariables.add(newBoundVariable);
    return newBoundVariable;
  }

  // public String toString(){
  // return "P:"+this.name;
  // }

  public ArrayList<BoundVariable> getBoundVariables() {
    return boundVariables;
  }

  public void replaceBoundVariable(BoundVariable oldVariable, BoundVariable newVariable) {
    boundVariables.remove(oldVariable);
    boundVariables.add(newVariable);
  }
}
