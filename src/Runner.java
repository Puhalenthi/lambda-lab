package src;

import java.util.ArrayList;

import src.variables.BoundVariable;
import src.variables.FreeVariable;
import src.variables.ParameterVariable;
import src.variables.Variable;

public class Runner {
    public static Expression run(Expression expression) {

        Application leftmostApplication = findLeftmostRunnableApplication(expression);

        while (leftmostApplication != null) {
            Function leftFunction = (Function) leftmostApplication.left;
            Expression newExpression = runApplication(leftFunction.getParameter(), leftFunction.getExpression(),
                    leftmostApplication.right);
            System.out.println("New expression: " + newExpression);

            if (leftmostApplication.parent instanceof Function) {
                ((Function) leftmostApplication.parent).setExpression(newExpression);
            } else if (leftmostApplication.parent instanceof Application) {
                Application parent = (Application) leftmostApplication.parent;
                if (parent.left == leftmostApplication) {
                    parent.setLeft(newExpression);
                } else {
                    parent.setRight(newExpression);
                }
            } else if (leftmostApplication.parent == null) {
                return run(newExpression);
            }
            leftmostApplication = findLeftmostRunnableApplication(expression);
        }

        return expression;
    }

    public static Application findLeftmostRunnableApplication(Expression expression) {
        if (expression instanceof Variable) {
            return null;
        } else if (expression instanceof Function) {
            return findLeftmostRunnableApplication(((Function) expression).expression);
        } else if (expression instanceof Application) {
            Application a = (Application) expression;
            Application leftmostApplication = findLeftmostRunnableApplication(a.left);

            // checking the left side of the application
            if (leftmostApplication != null) {
                return leftmostApplication;
            }

            leftmostApplication = findLeftmostRunnableApplication(a.right);

            // checking the right side of the application
            if (leftmostApplication != null || !(a.left instanceof Function)) {
                return leftmostApplication;
            } else {
                return a;
            }
        } else {
            System.out.println("Major problem with finding leftmost application");
            return null;
        }
    }

    // recursively goes through functionExpression and replaces all variables bound
    // to paramater with argument
    public static Expression runApplication(ParameterVariable parameter, Expression functionExpression,
            Expression argument) {
        // System.out.println("Parameter: " + parameter + " Expression: " +
        // functionExpression + " Argument: " + argument);

        if (functionExpression instanceof Application) {
            Application a = (Application) functionExpression;
            a.left = runApplication(parameter, a.left, argument);
            a.right = runApplication(parameter, a.right, argument);
            return a;
        } else if (functionExpression instanceof Function) {
            Function f = (Function) functionExpression;
            f.expression = runApplication(parameter, f.expression, argument);
            return f;
        } else if (parameter.getBoundVariables().contains(functionExpression)) {
            return deepCopy(argument, new ArrayList<>());
        } else if (functionExpression instanceof Variable) {
            return functionExpression;
        } else {
            System.out.println("Major problem with running applications");
            return null;
        }
    }

    public static Expression deepCopy(Expression expression, ArrayList<ParameterVariable> parameterList) {
        if (expression instanceof FreeVariable) {
            return new FreeVariable(((FreeVariable) expression).getName());
        } else if (expression instanceof BoundVariable) {
            String variableName = ((BoundVariable) expression).getName();
            for (ParameterVariable p : parameterList) {
                if (p.getName().equals(variableName)) {
                    return p.addBoundedVariable(variableName);
                }
            }
            return null;
        } else if (expression instanceof Function) {
            Function f = (Function) expression;
            ParameterVariable p = new ParameterVariable(f.getParameter().getName());
            parameterList.add(p);
            return new Function(p, deepCopy(f.getExpression(), parameterList));
        } else if (expression instanceof Application) {
            Application a = (Application) expression;
            return new Application(deepCopy(a.left, parameterList), deepCopy(a.right, parameterList));
        } else {
            System.out.println("Huge error with deep copy");
            return null;
        }
    }
}
