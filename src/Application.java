package src;


public class Application implements Expression {

    Expression left;
    Expression right;

    public Application(Expression a, Expression b){
        left = a;
        right = b;
    }

    public String toString() {
        return "(" + left + " " + right + ")";
    }
}
