package com.lab.model.controller;

import com.lab.model.model.RoleEntity;
import com.lab.model.model.UserEntity;
import com.lab.model.repository.RoleRepository;
import com.lab.model.repository.UserRepository;
import com.lab.model.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private UserService userService;
    @Autowired
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    public AdminController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping()
    //@PreAuthorize("hasAnyRole('MANAGE_ACCOUNTS')")
    public String open(Model model){
        List<UserEntity> employees = userService.findAll();
        List<UserEntity> superiors = new ArrayList<>();
        for (var employee : employees) {
            if (hasRole(employee.getRoles(), "APPROVE_DAYS_OFF_REQUEST")) {
                superiors.add(employee);
            }
        }

        model.addAttribute("employees", employees);
        model.addAttribute("superiors", superiors);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("user", new UserEntity());
        return "admin-dashboard";
    }

    private boolean hasRole(List<RoleEntity> roles, String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    @PostMapping()
    public String updateRole(@RequestParam(value = "userId", required = false) Long userId,
                             @RequestParam(value = "selectedEmployeeIds", required = false) String selectedEmployeeIds,
                             @RequestParam(value = "action", required = false) String action,
                             @RequestParam(value = "selectedSuperiorId", required = false) Long selectedSuperiorId,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(value="employeeId", required = false) Long employeeToDelete,
                             @RequestParam(value = "roles", required = false) Long ... roles) {
        logger.info("update user called");

        if ("changeRole".equals(action)) {
            UserEntity user = userService.findById(userId);
            user.getRoles().clear();

            for (Long roleId : roles) {
                RoleEntity role = roleRepository.findById(roleId).orElseThrow(); // Add appropriate error handling
                user.addRole(role);
                userService.save(user);
            }
        } else if ("updateDatabase".equals(action)) {
            List<Long> selectedIds = Arrays.stream(selectedEmployeeIds.split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (selectedIds.size() >= 2) {
                UserEntity employee1 = userRepository.findById(selectedIds.get(0)).orElse(null);
                UserEntity employee2 = userRepository.findById(selectedIds.get(1)).orElse(null);

                if (employee1 != null && employee2 != null ) {
                    if (hasRole(employee1.getRoles(), "APPROVE_DAYS_OFF_REQUEST")) {
                        UserEntity temp = employee1;
                        employee1 = employee2;
                        employee2 = temp;
                    }
                    userRepository.updateSuperior(employee1.getId(), employee2);
                }
            }
        } else if("displaySubordinates".equals(action)) {
            Iterable<UserEntity> employeesSuperior = userRepository.findByUser(userRepository.findById(selectedSuperiorId));
            List<UserEntity> subordinates = (List<UserEntity>) employeesSuperior;
            redirectAttributes.addFlashAttribute("subordinates", subordinates);
        } else if("deleteEmployee".equals(action)) {
            userRepository.updateSuperior(employeeToDelete, null);
        }
        return "redirect:/admin";
    }

}
