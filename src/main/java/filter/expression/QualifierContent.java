package filter.expression;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QualifierContent {

    //Only one of these will exist in a QualifierContent object
    private Optional<DateRange> dateRange = Optional.empty();
    private Optional<String> content = Optional.empty();
    private Optional<LocalDate> date = Optional.empty();
    private Optional<NumberRange> numberRange = Optional.empty();
    private Optional<Integer> number = Optional.empty();
    private List<SortKey> sortKeys = new ArrayList<>();

    public QualifierContent(DateRange dateRange) {
        this.dateRange = Optional.of(dateRange);
    }

    public QualifierContent(String content) {
        this.content = Optional.of(content);
    }

    public QualifierContent(LocalDate date) {
        this.date = Optional.of(date);
    }

    public QualifierContent(NumberRange numberRange) {
        this.numberRange = Optional.of(numberRange);
    }

    public QualifierContent(int number) {
        this.number = Optional.of(number);
    }

    public QualifierContent(List<SortKey> sortKeys) {
        this.sortKeys = new ArrayList<>(sortKeys);
    }

    public Optional<DateRange> getDateRange() {
        return dateRange;
    }

    public Optional<String> getContent() {
        return content;
    }

    public Optional<LocalDate> getDate() {
        return date;
    }

    public Optional<NumberRange> getNumberRange() {
        return numberRange;
    }

    public Optional<Integer> getNumber() {
        return number;
    }

    public List<SortKey> getSortKeys() {
        return sortKeys;
    }

}
