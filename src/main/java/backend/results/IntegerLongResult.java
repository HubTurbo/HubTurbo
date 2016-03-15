package backend.results;

public class IntegerLongResult {

    private Integer intNum;
    private Long longNum;

    public IntegerLongResult (Integer intNum, Long longNum){
        this.intNum = intNum;
        this.longNum = longNum;
    }

    public Integer getInt() {
        return this.intNum;
    }

    public Long getLong() {
        return this.longNum;
    }

}
