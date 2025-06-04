//Zevi Cohen & Puhalenthi Ramesh Vidhya
package src;

import src.variables.ParameterVariable;

public class Function implements Expression {
   ParameterVariable parameter;
   Expression expression;

    public Function(ParameterVariable parameter, Expression expression){
        this.parameter = parameter;
        this.expression = expression;
    }


    public ParameterVariable getParameter(){
        return parameter;
    }

    public Expression getExpression(){
        return expression;
    }

    public void setExpression(Expression e){
        expression = e;
    }


    public String toString(){
        return "(λ" + parameter + "." + expression + ")";
    }
}
