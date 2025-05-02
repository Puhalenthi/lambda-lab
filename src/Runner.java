package src;

import java.util.ArrayList;

import src.variables.BoundVariable;
import src.variables.FreeVariable;
import src.variables.ParameterVariable;
import src.variables.Variable;

public class Runner {
    public static Expression runWithDeepCopy(Expression expression) {
        System.out.println("Expression: " + expression);
        expression = deepCopy(expression, new ArrayList<>(), null);
        System.out.println("Copied expression: " + expression);
        return run(expression);
    }

    public static Expression run(Expression expression) {
        Application leftmostApplication = findLeftmostRunnableApplication(expression);

        while (leftmostApplication != null) {
            // Ensure the left side is a Function
            if (!(leftmostApplication.left instanceof Function)) {
                throw new IllegalStateException("Left side of application is not a function.");
            }

            Function leftFunction = (Function) leftmostApplication.left;
            Expression newExpression = runApplication(leftFunction.getParameter(), leftFunction.getExpression(),
                    leftmostApplication.right, new ArrayList<>());

            // Update the parent of the leftmost application
            if (leftmostApplication.parent instanceof Function) {
                ((Function) leftmostApplication.parent).setExpression(newExpression);
                if (newExpression instanceof Application) {
                    ((Application) newExpression).setParent(leftmostApplication.parent);
                }
            } else if (leftmostApplication.parent instanceof Application) {
                Application parent = (Application) leftmostApplication.parent;

                if (parent.left == leftmostApplication) {
                    parent.setLeft(newExpression);
                } else if (parent.right == leftmostApplication) {
                    parent.setRight(newExpression);
                }

                if (newExpression instanceof Application) {
                    ((Application) newExpression).setParent(leftmostApplication.parent);
                }
            } else if (leftmostApplication.parent == null) {
                // If there's no parent, the new expression becomes the root
                return run(newExpression);
            }
            // Find the next leftmost application
            leftmostApplication = findLeftmostRunnableApplication(expression);
        }
        return expression;
    }

    public static Application findLeftmostRunnableApplication(Expression expression) {
        // System.out.println("Expression: " + expression + " Class: " +
        // expression.getClass());
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
            if (leftmostApplication != null) {
                return leftmostApplication;
            } else if (a.left instanceof Function) {
                return a;
            } else {
                return null;
            }
        } else {
            System.out.println("Major problem with finding leftmost application");
            return null;
        }
    }

    // recursively goes through functionExpression and replaces all variables bound
    // to paramater with argument
    public static Expression runApplication(ParameterVariable parameter, Expression functionExpression,
            Expression argument, ArrayList<ParameterVariable> parameterList) {

        if (functionExpression instanceof Application) {
            Application a = (Application) functionExpression;
            a.left = runApplication(parameter, a.left, argument, parameterList);
            a.right = runApplication(parameter, a.right, argument, parameterList);
            return a;
        } else if (functionExpression instanceof Function) {
            Function f = (Function) functionExpression;
            parameterList.add(f.getParameter());
            f.expression = runApplication(parameter, f.expression, argument, parameterList);
            return f;
        } else if (parameter.getBoundVariables().contains(functionExpression)) {
            Expression deepCopy = deepCopy(argument, parameterList, null);
            return deepCopy;
        } else if (functionExpression instanceof Variable) {
            return functionExpression;
        } else {
            System.out.println("Major problem with running applications");
            return null;
        }
    }

    public static Expression deepCopy(Expression expression, ArrayList<ParameterVariable> parameterList,
            Expression parent) {
        if (expression instanceof FreeVariable) {
            return new FreeVariable(((FreeVariable) expression).getName());
        } else if (expression instanceof BoundVariable) {
            String variableName = ((BoundVariable) expression).getName();

            for (int i = parameterList.size() - 1; i >= 0; i--) {
                if (parameterList.get(i).getName().equals(variableName)) {
                    return parameterList.get(i).addBoundedVariable(variableName);
                }
            }

            return ((BoundVariable) expression).getParameter().addBoundedVariable(variableName);
        } else if (expression instanceof Function) {
            Function f = (Function) expression;
            ParameterVariable p = new ParameterVariable(f.getParameter().getName());
            parameterList.add(p);
            Function newFunction = new Function(p, null);
            newFunction.setExpression(deepCopy(f.getExpression(), parameterList, newFunction));
            return newFunction;

        } else if (expression instanceof Application) {
            Application a = (Application) expression;
            Application newApplication = new Application(null, null, parent);
            newApplication.setLeft(deepCopy(a.left, parameterList, newApplication));
            newApplication.setRight(deepCopy(a.right, parameterList, newApplication));
            return newApplication;
        } else {
            System.out.println("Huge error with deep copy");
            return null;
        }
    }

    public static void printExpressionTree(Expression expression, String indent) {
        if (expression instanceof Variable) {
            System.out.println(indent + "Variable: " + expression);
        } else if (expression instanceof Function) {
            Function f = (Function) expression;
            System.out.println(indent + "Function: " + f.getParameter());
            printExpressionTree(f.getExpression(), indent + "  ");
        } else if (expression instanceof Application) {
            Application a = (Application) expression;
            System.out.println(indent + "Application:");
            System.out.println(indent + "  Left:");
            printExpressionTree(a.left, indent + "    ");
            System.out.println(indent + "  Right:");
            printExpressionTree(a.right, indent + "    ");
        } else {
            System.out.println(indent + "Unknown Expression Type");
        }
    }
}
