//Zevi Cohen & Puhalenthi Ramesh Vidhya
package src;

import java.lang.reflect.Parameter;
import java.util.ArrayList;

import src.variables.BoundVariable;
import src.variables.FreeVariable;
import src.variables.ParameterVariable;
import src.variables.Variable;

public class Runner {
    /*
     * Creates a deep copy of the given expression, runs it, performs alpha reduction,
     * and returns either the reduced expression or a free variable if a matching expression is found.
     */
    public static Expression runWithDeepCopy(Expression expression) {
        expression = deepCopy(expression, new ArrayList<>(), null);
        Expression runExpression = run(expression);
        performAlphaReduction(runExpression, new ArrayList<>());

        String matched = findMatchingExpressionInMemory(runExpression);

        return (matched == null) ? runExpression : new FreeVariable(matched);
    }

    /*
     * Executes the expression by repeatedly performing function application (beta reduction)
     * until no further runnable application is found.
     */
    public static Expression run(Expression expression) {
        Application leftmostApplication = findLeftmostRunnableApplication(expression);

        while (leftmostApplication != null) {
            // Ensure the left side is a Function
            if (!(leftmostApplication.left instanceof Function)) {
                throw new IllegalStateException("Left side of application is not a function.");
            }

            Function leftFunction = (Function) leftmostApplication.left;
            Expression newExpression = runApplication(leftFunction.getParameter(), leftFunction.getExpression(),
                    leftmostApplication.right, null);

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
                    if (newExpression instanceof Application a) {
                        a.setParent(null);
                    }
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

    /*
     * Recursively finds the leftmost function application that can be executed (runnable).
     * Returns the application if found, otherwise null.
     */
    private static Application findLeftmostRunnableApplication(Expression expression) {
        switch (expression) {
            case Variable v:
                return null;
            case Function f:
                return findLeftmostRunnableApplication(f.expression);
            case Application a:

                if (a.left instanceof Function) {
                    return a;
                }

                Application leftmostApplication = findLeftmostRunnableApplication(a.left);

                // checking the left side of the application
                if (leftmostApplication != null) {
                    return leftmostApplication;
                }

                return findLeftmostRunnableApplication(a.right);
            default:
                return null;
        }
    }

    /*
     * Performs beta reduction by replacing occurrences of the variable (bound by a parameter)
     * with the given argument in the function expression.
     */
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

    /*
     * Creates a deep copy of the expression while preserving the variable binding
     * by tracking bound variables in the parameter list.
     */
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

    /*
     * Performs alpha reduction on the expression to rename bound variables,
     * preventing variable capture by ensuring unique parameter names.
     */
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

    /*
     * Generates a new unique name for a parameter based on the existing names in the parameter list.
     */
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

    /*
     * Searches through memory entries to find an expression that is alpha-equivalent
     * to the provided expression. Returns the corresponding key if found.
     */
    private static String findMatchingExpressionInMemory(Expression expression1) {
        for (var v : Memory.getEntries()) {
            if (isAlphaEquivalent(expression1, v.getValue(), new ArrayList<>(), new ArrayList<>())) {
                return v.getKey();
            }
        }
        return null;
    }

    /*
     * Checks if the two expressions are alpha-equivalent by comparing their structure and
     * the context of bound variables.
     */
    private static boolean isAlphaEquivalent(Expression e1, Expression e2, ArrayList<String> boundVars1, ArrayList<String> boundVars2) {
        if (e1 == null || e2 == null) return e1 == e2;
        if (e1.getClass() != e2.getClass()) return false;


        if (e1 instanceof FreeVariable fv1 && e2 instanceof FreeVariable fv2) {
            return fv1.getName().equals(fv2.getName());
        } else if (e1 instanceof BoundVariable bv1 && e2 instanceof BoundVariable bv2) {
            int index1 = boundVars1.lastIndexOf(bv1.getName());
            int index2 = boundVars2.lastIndexOf(bv2.getName());
            return index1 == index2 && index1 != -1;
        } else if (e1 instanceof Function f1 && e2 instanceof Function f2) {
            ArrayList<String> newBoundVars1 = new ArrayList<>(boundVars1);
            ArrayList<String> newBoundVars2 = new ArrayList<>(boundVars2);
            newBoundVars1.add(f1.getParameter().getName());
            newBoundVars2.add(f2.getParameter().getName());
            return isAlphaEquivalent(f1.getExpression(), f2.getExpression(), newBoundVars1, newBoundVars2);
        } else if (e1 instanceof Application a1 && e2 instanceof Application a2) {
            return isAlphaEquivalent(a1.left, a2.left, boundVars1, boundVars2)
            && isAlphaEquivalent(a1.right, a2.right, boundVars1, boundVars2);
        } else {
            return false;
        }
    }
}
