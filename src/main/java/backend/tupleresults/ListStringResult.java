package backend.tupleresults;

import java.util.List;

public class ListStringResult<T> {

    private List<T> list;
    private String string;

    public ListStringResult(List<T> list, String string){
        this.list = list;
        this.string = string;
    }

    public List<T> getList() {
        return this.list;
    }

    public String getString() {
        return this.string;
    }
}
