package src;

import src.variables.BoundVariable;
import src.variables.ParameterVariable;
import src.variables.Variable;

public class Runner {
    public static Expression run(Expression expression) {

        Application leftmostApplication = findLeftmostRunnableApplication(expression);

        while (leftmostApplication != null) {
            Function leftFunction = (Function) (leftmostApplication.left);
            Expression newExpression = runApplication(leftFunction.getParameter(), leftFunction.getExpression(),
                    leftmostApplication.right);
            if (leftmostApplication.parent instanceof Function) {
                ((Function) leftmostApplication.parent).setExpression(newExpression);
            } else if (leftmostApplication.parent instanceof Application) {
                if (((Application) leftmostApplication.parent).left.equals(leftmostApplication)) {
                    ((Application) leftmostApplication.parent).setLeft(newExpression);
                } else {
                    ((Application) leftmostApplication.parent).setRight(newExpression);
                }
            } else if (newExpression instanceof Application) {
                return run(newExpression);
            } else if (leftmostApplication.parent == null) {
                return newExpression;
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
            Application a = (Application) (expression);
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
        if (functionExpression instanceof Application) {
            Application a = (Application) (functionExpression);
            a.left = runApplication(parameter, a.left, argument);
            a.right = runApplication(parameter, a.right, argument);
            return functionExpression;
        } else if (functionExpression instanceof Function) {
            Function f = (Function) (functionExpression);
            f.expression = runApplication(parameter, f.expression, argument);
            return functionExpression;
        } else if (functionExpression instanceof BoundVariable
                && parameter.getBoundVariables().contains(functionExpression)) {
            return argument;
        } else if (functionExpression instanceof Variable) {
            return functionExpression;
        } else {
            System.out.println("Major problem with running applications");
            return null;
        }
    }
}
