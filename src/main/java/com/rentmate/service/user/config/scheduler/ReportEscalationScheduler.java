package com.rentmate.service.user.config.scheduler;

import com.rentmate.service.user.service.ReportEscalationService;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component @Slf4j
public class ReportEscalationScheduler {
    private final ReportEscalationService ecalationService;

    public ReportEscalationScheduler(ReportEscalationService ecalationService) {
        this.ecalationService = ecalationService;
    }

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 3)
    public void escalateOverdueReports(){
        try{
            ecalationService.escalateOverdueReportsToThieving();
        }catch (Exception e){
            log.error("Error while escalating overdue reports", e);
        }
    }
}
