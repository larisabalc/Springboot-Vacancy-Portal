package com.lab.model.service;

import com.lab.model.model.DaysOff;
import com.lab.model.repository.DaysOffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DaysOffService {
    private DaysOffRepository daysOffRepository;

    @Autowired
    public DaysOffService(DaysOffRepository daysOffRepository) {
        this.daysOffRepository = daysOffRepository;
    }

    public DaysOff insertDaysOff(DaysOff daysOff) {
        return daysOffRepository.save(daysOff);
    }
}
