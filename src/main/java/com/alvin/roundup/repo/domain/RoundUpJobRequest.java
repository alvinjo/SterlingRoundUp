package com.alvin.roundup.repo.domain;

import com.alvin.roundup.repo.RoundUpRepo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RoundUpJobRequest {

    private String accountId;
    private String categoryId;
    private String startDate;
    private String endDate;

    public static void main(String[] args) {

        LocalDate localDate = LocalDate.parse("2024-12-14");

        System.out.println(LocalDateTime.of(localDate, LocalTime.MIN));
        System.out.println(LocalDateTime.of(localDate, LocalTime.MAX));


        System.out.println(generateDateList(LocalDate.parse("2024-12-14"), LocalDate.parse("2024-12-17")));

        RoundUpJob r = new RoundUpJob();


    }



    private static List<LocalDate> generateDateList(LocalDate startDate, LocalDate endDate) {
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
