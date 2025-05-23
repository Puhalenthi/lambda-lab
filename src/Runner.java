package src;

import java.lang.reflect.Parameter;
import java.util.ArrayList;

import src.variables.BoundVariable;
import src.variables.FreeVariable;
import src.variables.ParameterVariable;
import src.variables.Variable;

public class Runner {
    public static Expression runWithDeepCopy(Expression expression) {
        expression = deepCopy(expression, new ArrayList<>(), null);
        System.out.println("Expression after deep copy: " + expression);
        Expression runExpression = run(expression);

        System.out.println("Expression before alpha reduction: " + runExpression);

        performAlphaReduction(runExpression, new ArrayList<>());
        return runExpression;
    }

    public static Expression run(Expression expression) {
        Application leftmostApplication = findLeftmostRunnableApplication(expression);
        // printExpressionTree(expression, " ");

        while (leftmostApplication != null) {
            // System.out.println("Leftmost: " + leftmostApplication);
            // Ensure the left side is a Function
            if (!(leftmostApplication.left instanceof Function)) {
                throw new IllegalStateException("Left side of application is not a function.");
            }

            Function leftFunction = (Function) leftmostApplication.left;
            Expression newExpression = runApplication(leftFunction.getParameter(), leftFunction.getExpression(),
                    leftmostApplication.right, null);
            // System.out.println("New Expression: " + newExpression);

            // Update the parent of the leftmost application
            switch (leftmostApplication.parent) {
                case Function f:
                    f.setExpression(newExpression);
                    break;
                case Application a:
                    if (a.left == leftmostApplication) {
                        a.setLeft(newExpression);
                    } else if (a.right == leftmostApplication) {
                        a.setRight(newExpression);
                    }
                    break;
                case null:
                    return run(newExpression);
                default:
                    break;
            }

            switch (newExpression) {
                case Application a:
                    a.setParent(leftmostApplication.parent);
                    break;
                default:
                    break;
            }

            leftmostApplication = findLeftmostRunnableApplication(expression);
        }
        return expression;
    }

    private static Application findLeftmostRunnableApplication(Expression expression) {
        switch (expression) {
            case Variable v:
                return null;
            case Function f:
                return findLeftmostRunnableApplication(f.expression);
            case Application a:
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
            default:
                return null;
        }
    }

    // recursively goes through functionExpression and replaces all the variables
    // bound
    // to parameter with the argument
    private static Expression runApplication(ParameterVariable parameter, Expression functionExpression,
            Expression argument, Expression parent) {

        switch (functionExpression) {
            case Application a:
                a.left = runApplication(parameter, a.left, argument, a);
                a.right = runApplication(parameter, a.right, argument, a);
                return a;
            case Function f:
                f.expression = runApplication(parameter, f.expression, argument, f);
                return f;
            case BoundVariable b:
                if (parameter.getBoundVariables().contains(b)) {
                    Expression deepCopy = deepCopy(argument, new ArrayList<>(), null);
                    if (deepCopy instanceof Application a) {
                        a.setParent(parent);
                    }
                    return deepCopy;
                }
            default:
                return functionExpression;
        }
    }

    private static Expression deepCopy(Expression expression, ArrayList<ParameterVariable> parameterList,
            Expression parent) {

        switch (expression) {
            case FreeVariable fv:
                return new FreeVariable(fv.getName());
            case BoundVariable b:
                String variableName = b.getName();

                for (int i = parameterList.size() - 1; i >= 0; i--) {
                    if (parameterList.get(i).getName().equals(variableName)) {
                        return parameterList.get(i).addBoundVariable(variableName);
                    }
                }

                return b.getParameter().replaceBoundVariable(b);
            case Function f:
                ParameterVariable p = new ParameterVariable(f.getParameter().getName());
                Function newFunction = new Function(p, null);

                ArrayList<ParameterVariable> newParameterList = new ArrayList<>();
                for (int i = 0; i < parameterList.size(); i++) {
                    newParameterList.add(parameterList.get(i));
                }
                newParameterList.add(p);

                newFunction.setExpression(deepCopy(f.getExpression(), newParameterList, newFunction));
                return newFunction;
            case Application a:
                Application newApplication = new Application(null, null, parent);
                newApplication.setLeft(deepCopy(a.left, parameterList, newApplication));
                newApplication.setRight(deepCopy(a.right, parameterList, newApplication));
                return newApplication;
            default:
                return null;
        }
    }

    private static void performAlphaReduction(Expression expression, ArrayList<ParameterVariable> parameterList) {
        switch (expression) {
            case FreeVariable fv:
                for (ParameterVariable pv : parameterList) {
                    if (pv.getName().equals(fv.getName())) {
                        pv.setName(createNewParameterName(parameterList, pv));
                    }
                }
                break;
            case BoundVariable bv:
                ParameterVariable parameterToBeChanged;
                do {
                    parameterToBeChanged = null;
                    for (ParameterVariable pv : parameterList) {
                        if (pv.getName().equals(bv.getName()) && !pv.getBoundVariables().contains(bv)) {
                            parameterToBeChanged = pv;
                            break;
                        }
                    }
                    if (parameterToBeChanged != null) {
                        parameterToBeChanged.setName(createNewParameterName(parameterList, parameterToBeChanged));
                    }
                } while (parameterToBeChanged != null);
                break;
            case Function f:
                ParameterVariable p = f.getParameter();
                parameterList.add(p);
                performAlphaReduction(f.getExpression(), parameterList);
                break;
            case Application a:
                performAlphaReduction(a.left, parameterList);
                performAlphaReduction(a.right, parameterList);
                break;
            default:
                break;
        }
    }

    private static String createNewParameterName(ArrayList<ParameterVariable> parameterList, ParameterVariable v) {
        String baseName = v.getName();
        String newName = baseName;
        int counter = 1;

        for (int i = 0; i < parameterList.size(); i++) {
            if (parameterList.get(i).getName().equals(newName)) {
                newName = baseName + counter;
                counter++;
                i = -1;
            }
        }

        return newName;
    }

    private static void printExpressionTree(Expression expression, String indent) {
        if (expression instanceof FreeVariable) {
            System.out.println(indent + "Free Variable: " + expression);
        } else if (expression instanceof BoundVariable) {
            System.out.println(indent + "Bound Variable: " + expression);
        } else if (expression instanceof Function) {
            Function f = (Function) expression;
            System.out.println(indent + "Function: " + f.getParameter());
            System.out.println(indent + "Bounded Parameters: " + f.getParameter().getBoundVariables());
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
