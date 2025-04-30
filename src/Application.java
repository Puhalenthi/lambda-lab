package src;

public class Application implements Expression {

    Expression left;
    Expression right;
    Expression parent;

    public Application(Expression a, Expression b, Expression parent) {
        left = a;
        right = b;
        this.parent = parent;
    }

    public Application(Expression a, Expression b) {
        this(a, b, null);
    }

    public String toString() {
        return "[" + left + " " + right + "]";
    }

    public void setLeft(Expression e) {
        left = e;
    }

    public void setRight(Expression e) {
        right = e;
    }
}
