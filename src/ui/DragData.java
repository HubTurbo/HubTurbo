package ui;

import com.google.gson.Gson;

/**
 *
 * @author darius
 */
public class DragData {
    

    
//    public static final int TREE_MILESTONES = 0;
//    public static final int TREE_ISSUE = 0;
    
    public DragSource source; // TODO make private
    public int index;
    public String text;
    
    public DragData(DragSource source) {
        this.source = source;
    }
    
    public String serialize() {
        return new Gson().toJson(this);
    }
    
    public static DragData deserialize(String json) {
        Gson gson = new Gson();
        DragData dd = gson.fromJson(json, DragData.class);
        return dd;
    }
}
