package com.lab.model.service;

import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CalendarService {
    List<LocalDate> publicHolidays = Arrays.asList(
            LocalDate.of(Year.now().getValue(), 1, 1),   // Anul Nou
            LocalDate.of(Year.now().getValue(), 1, 2),   // Sărbătorirea Anului Nou
            LocalDate.of(Year.now().getValue(), 1, 6),   // Boboteaza
            LocalDate.of(Year.now().getValue(), 1, 7),   // Soborul Sfântului Ioan Botezătorul
            LocalDate.of(Year.now().getValue(), 1, 24),  // Unirea Principatelor Române
            LocalDate.of(Year.now().getValue(), 5, 1),   // Ziua Muncii
            LocalDate.of(Year.now().getValue(), 5, 3),   // Vinerea Mare
            LocalDate.of(Year.now().getValue(), 5, 5),   // Prima zi de Paște
            LocalDate.of(Year.now().getValue(), 5, 6),   // A doua zi de Paște
            LocalDate.of(Year.now().getValue(), 6, 1),   // Ziua copilului
            LocalDate.of(Year.now().getValue(), 6, 23),  // Prima zi de Rusalii
            LocalDate.of(Year.now().getValue(), 6, 24),  // A doua zi de Rusalii
            LocalDate.of(Year.now().getValue(), 8, 15),  // Adormirea Maicii Domnului
            LocalDate.of(Year.now().getValue(), 11, 30), // Sfântul Andrei
            LocalDate.of(Year.now().getValue(), 12, 1),  // Marea Unire
            LocalDate.of(Year.now().getValue(), 12, 25), // Prima zi de Crăciun
            LocalDate.of(Year.now().getValue(), 12, 26)  // A doua zi de Crăciun
    );

    public List<List<Integer>> generateCalendarData(int year, Month month) {
        List<List<Integer>> calendar = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);

        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        int dayCount = 1;

        for (int i = 0; i < 6; i++) {
            List<Integer> week = new ArrayList<>();

            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < firstDayOfWeek) {
                    week.add(null);
                } else if (dayCount <= daysInMonth) {
                    week.add(dayCount);
                    dayCount++;
                } else {
                    week.add(null);
                }
            }

            calendar.add(week);
        }

        return calendar;
    }

    public long calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        long workingDays = 0;

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isWorkingDay(currentDate) && !isPublicHoliday(currentDate)) {
                workingDays++;
            }

            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }

    public boolean isWorkingDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    public boolean isPublicHoliday(LocalDate date) {
        return publicHolidays.contains(date);
    }
}
