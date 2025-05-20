package tercero.com.base.controller.DataStruc.stack;


import tercero.com.base.controller.DataStruc.List.Linkendlist;

public class StackImplementation<E> extends Linkendlist<E> {
    private final Integer top;

    //getter 
    public Integer getTop() {
        return top;
    }
    
    public StackImplementation(Integer top) {
       
        this.top = top;
    }

    protected boolean isFull() {
        return getLength() > this.top;
    }

    protected void push(E info) throws Exception {
        if (!isFull()) {
            add( info, 0);
        } else {
            throw new ArrayIndexOutOfBoundsException("Stack is full");
        }
    }

    protected E pop()throws Exception {
        return deleteFirst(); 
    }

    
}
