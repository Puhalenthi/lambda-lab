//Zevi Cohen & Puhalenthi Ramesh Vidhya
package src.variables;

import java.util.ArrayList;

public class ParameterVariable extends Variable {
  ArrayList<BoundVariable> boundVariables = new ArrayList<>();

  public ParameterVariable(String name) {
    this.name = name;
  }



  public ArrayList<BoundVariable> getBoundVariables() {
    return boundVariables;
  }

  public BoundVariable addBoundVariable(String token) {
    BoundVariable newBoundVariable = new BoundVariable(token, this);
    boundVariables.add(newBoundVariable);
    return newBoundVariable;
  }

  public BoundVariable replaceBoundVariable(BoundVariable oldVariable) {
    boundVariables.remove(oldVariable);
    return addBoundVariable(oldVariable.name);
  }



  public void setName(String name) {
    for (BoundVariable bv : boundVariables) {
      bv.setName(name);
    }
    this.name = name;
  }


  public String toString() {
    return "\u001B[34m" + this.name + "\u001B[0m";
  }
}
