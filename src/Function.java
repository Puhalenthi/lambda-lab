package src;

import src.Variables.Variable;

public class Function implements Expression {

   Variable parameter;
   Expression expression;

    public Function(Variable parameter, Expression expression){
        this.parameter = parameter;
        this.expression = expression;
    }

    public String toString(){
        return "";
    }
}
