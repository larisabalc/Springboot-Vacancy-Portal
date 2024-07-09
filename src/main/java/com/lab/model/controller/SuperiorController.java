package com.lab.model.controller;

import com.lab.model.model.DaysOff;
import com.lab.model.model.UserEntity;
import com.lab.model.repository.DaysOffRepository;
import com.lab.model.repository.UserRepository;
import com.lab.model.service.CalendarService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/superior")
public class SuperiorController {
    private final CalendarService calendarService;
    private final DaysOffRepository daysOffRepository;
    private final UserRepository userRepository;

    public SuperiorController(
            CalendarService calendarService,
            DaysOffRepository daysOffRepository,
            UserRepository userRepository) {
        this.calendarService = calendarService;
        this.daysOffRepository = daysOffRepository;
        this.userRepository = userRepository;
    }

    @GetMapping()
    public String open(Model model, Authentication authentication, @RequestParam(name = "year", required = false) Integer year,
                       @RequestParam(name = "month", required = false) Integer month,
                       @RequestParam(name = "prevYear", required = false) Integer prevYear,
                       @RequestParam(name = "nextYear", required = false) Integer nextYear,
                       @RequestParam(name = "prevMonth", required = false) Integer prevMonth,
                       @RequestParam(name = "nextMonth", required = false) Integer nextMonth){

        String username = authentication.getName();
        Optional<UserEntity> superiorUser = userRepository.findByEmail(username);
        Iterable<DaysOff> userDaysOff = daysOffRepository.findByUser_User(superiorUser);

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
        Long noDaysOffDisplayed = userRepository.getNoDaysOff(superiorUser.get());

        Iterable<UserEntity> subordinateUsers = userRepository.findByUser(superiorUser);
        List<UserEntity> userDaysOffDtoList = new ArrayList<>();

        for (UserEntity subordinateUser : subordinateUsers) {
            Long noDaysOff = subordinateUser.getNoDaysOff();
            userDaysOffDtoList.add(new UserEntity(subordinateUser.getUsername(), noDaysOff));
        }

        model.addAttribute("calendar", calendar);
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", month);
        model.addAttribute("userDaysOff", userDaysOff);
        model.addAttribute("isSuperior", true);
        model.addAttribute("subordinateUsers", subordinateUsers);
        model.addAttribute("noDaysOff", noDaysOffDisplayed);

        return "superior";
    }

    @PostMapping()
    public String handleSuperiorActions(
            @RequestParam(required = false) Long daysOffId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String selectedStartDate,
            @RequestParam(required = false) String selectedEndDate,
            @RequestParam(required = false) String declineMessage,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String updatedDaysOff) {

        if (userId != null && updatedDaysOff != null) {
            userRepository.updateNoDaysOff(userId, Long.valueOf(updatedDaysOff));
            return "redirect:/superior";
        } else if (daysOffId != null && status != null) {
            try {
                if ("Approved".equals(status)) {
                    daysOffRepository.updateStatusById(daysOffId, true);
                } else if ("Declined".equals(status)) {
                    if (!Objects.equals(declineMessage, "")) {
                        daysOffRepository.updateStatusAndMessageById(daysOffId, false, declineMessage);
                    } else {
                        return "redirect:/superior?error=NoMessageProvided";
                    }
                }
                return "redirect:/superior";
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/superior?error=ErrorUpdatingStatus";
            }
        }else if (selectedStartDate != null && selectedEndDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

            ZonedDateTime startDateTime = ZonedDateTime.parse(selectedStartDate, formatter);
            ZonedDateTime endDateTime = ZonedDateTime.parse(selectedEndDate, formatter);

            LocalDate currentDate = LocalDate.now();
            if (startDateTime.getYear() != currentDate.getYear() || endDateTime.getYear() != currentDate.getYear()) {
                return "redirect:/superior?error=InvalidDates";
            }

            ZonedDateTime oneWeekAhead = ZonedDateTime.now().plusWeeks(1);
            if (startDateTime.isBefore(oneWeekAhead) || endDateTime.isBefore(oneWeekAhead)) {
                return "redirect:/superior?error=DatesTooSoon";
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

            DaysOff daysOff = new DaysOff(startDateTime.toLocalDate(), endDateTime.toLocalDate(), null, "", user.get());

            long workingDays = calendarService.calculateWorkingDays(startDateTime.toLocalDate(), endDateTime.toLocalDate());

            userRepository.updateNoDaysOff(user.get().getId(), userRepository.getNoDaysOff(user.get()) - workingDays);

            daysOffRepository.save(daysOff);

            return "redirect:/superior";
        }

        return "redirect:/superior/error";
    }
}
