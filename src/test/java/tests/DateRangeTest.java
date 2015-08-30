package tests;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;

import filter.expression.DateRange;

public class DateRangeTest {

    DateRange dateRange = new DateRange(LocalDate.now(), LocalDate.now());
    DateRange dateRange1 = new DateRange(LocalDate.MIN, LocalDate.MAX);
    DateRange dateRange2 = new DateRange(LocalDate.MIN, LocalDate.now());
    DateRange dateRange3 = new DateRange(LocalDate.now(), LocalDate.now(), true);

    DateRange startNull = new DateRange(null, LocalDate.now());
    DateRange endNull = new DateRange(LocalDate.now(), null);

    @Test
    public void intervalEquality() {
        assertEquals(false, endNull.equals(startNull));
        assertEquals(false, startNull.equals(endNull));
        assertEquals(false, dateRange.equals(dateRange2));
        assertEquals(false, dateRange.equals(dateRange3));
    }

    @Test
    public void equality() {
        assertEquals(true, dateRange.equals(dateRange));
        assertEquals(false, dateRange.equals(null));
        assertEquals(false, dateRange.equals(1));
        assertEquals(false, dateRange.equals(dateRange1));
    }
}
