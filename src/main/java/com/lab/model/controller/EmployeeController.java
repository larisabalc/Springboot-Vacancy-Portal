package com.lab.model.controller;

import com.lab.model.model.DaysOff;
import com.lab.model.model.UserEntity;
import com.lab.model.repository.DaysOffRepository;
import com.lab.model.repository.UserRepository;
import com.lab.model.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employee")
public class EmployeeController {
    private final CalendarService calendarService;
    private final DaysOffRepository daysOffRepository;
    private final UserRepository userRepository;

    @Autowired
    public EmployeeController(
            CalendarService calendarService,
            DaysOffRepository daysOffRepository,
            UserRepository userRepository) {
        this.calendarService = calendarService;
        this.daysOffRepository = daysOffRepository;
        this.userRepository = userRepository;
    }

    @GetMapping()
    public String open(Model model, Authentication authentication,  @RequestParam(name = "year", required = false) Integer year,
                       @RequestParam(name = "month", required = false) Integer month,
                       @RequestParam(name = "prevYear", required = false) Integer prevYear,
                       @RequestParam(name = "nextYear", required = false) Integer nextYear,
                       @RequestParam(name = "prevMonth", required = false) Integer prevMonth,
                       @RequestParam(name = "nextMonth", required = false) Integer nextMonth){

        String username = authentication.getName();
        Optional<UserEntity> user = userRepository.findByEmail(username);
        Iterable<DaysOff> userDaysOff = daysOffRepository.findByUser(user);

        if (prevYear != null) {
            year = year - 1;
        } else if (nextYear != null) {
            year = year + 1;
        } else if (prevMonth != null) {
            month = month - 1;
        } else if (nextMonth != null) {
            month = month + 1;
        }

        LocalDate currentDate = LocalDate.now();
        if (year == null) {
            year = currentDate.getYear();
        }
        if (month == null) {
            month = currentDate.getMonthValue();
        }

        List<List<Integer>> calendar = calendarService.generateCalendarData(year, Month.of(month));
        Long noDaysOff = userRepository.getNoDaysOff(user.get());

        model.addAttribute("calendar", calendar);
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", month);
        model.addAttribute("userDaysOff", userDaysOff);
        model.addAttribute("isSuperior", false);
        model.addAttribute("noDaysOff", noDaysOff);

        return "employee";
    }

    @PostMapping()
    public String processSelectedDates(
            @RequestParam("selectedStartDate") String selectedStartDate,
            @RequestParam("selectedEndDate") String selectedEndDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        ZonedDateTime startDateTime = ZonedDateTime.parse(selectedStartDate, formatter);
        ZonedDateTime endDateTime = ZonedDateTime.parse(selectedEndDate, formatter);

        LocalDate currentDate = LocalDate.now();
        if (startDateTime.getYear() != currentDate.getYear() || endDateTime.getYear() != currentDate.getYear()) {
            return "redirect:/employee?error=InvalidDates";
        }

        ZonedDateTime oneWeekAhead = ZonedDateTime.now().plusWeeks(1);
        if (startDateTime.isBefore(oneWeekAhead) || endDateTime.isBefore(oneWeekAhead)) {
            return "redirect:/employee?error=DatesTooSoon";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<UserEntity> user = userRepository.findByEmail(username);
        Iterable<DaysOff> userDaysOff = daysOffRepository.findByUser(user);
        for (DaysOff existingDaysOff : userDaysOff) {
            LocalDate existingStartDate = existingDaysOff.getStartDate();
            LocalDate existingEndDate = existingDaysOff.getEndDate();

            if (!(endDateTime.toLocalDate().isBefore(existingStartDate) || startDateTime.toLocalDate().isAfter(existingEndDate))) {
                return "redirect:/employee?error=OverlappingDates";
            }
        }

        long noDaysOff = userRepository.getNoDaysOff(user.get());
        if(noDaysOff == 0) {
            return "redirect:/employee?error=NoDaysOffRemained";
        }

        long workingDays = calendarService.calculateWorkingDays(startDateTime.toLocalDate(), endDateTime.toLocalDate());

        DaysOff daysOff = new DaysOff(startDateTime.toLocalDate(), endDateTime.toLocalDate(), null, "", user.get());

        userRepository.updateNoDaysOff(user.get().getId(), noDaysOff - workingDays);

        daysOffRepository.save(daysOff);

        return "redirect:/employee";
    }
}
