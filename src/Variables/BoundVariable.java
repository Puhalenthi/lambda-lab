package src.Variables;

public class BoundVariable extends Variable {
    public BoundVariable(String name){
		this.name = name;
	}

    public void setName(String name){
        this.name = name;
    }

    public String toString(){
        return "B:" + this.name;
    }
}
