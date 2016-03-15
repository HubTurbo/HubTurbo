package backend.results;

import java.util.Date;
import java.util.List;

public class ListStringDateResult<T> {

    private List<T> list;
    private String string;
    private Date date;

    public ListStringDateResult(List<T> list, String string, Date date){
        this.list = list;
        this.string = string;
        this.date = date;
    }

    public List<T> getList() {
        return this.list;
    }

    public String getString() {
        return this.string;
    }

    public Date getDate() {
        return this.date;
    }
}
