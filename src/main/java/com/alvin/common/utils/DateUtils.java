package com.alvin.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    public static boolean validDate(String date) {
        try {
            DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String dateTimeToFormattedString(LocalDateTime dateTime){
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME)+"Z";
    }

    public static List<LocalDate> generateDateList(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();

        // Iterate through each date in the range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        return dateList;
    }

}
