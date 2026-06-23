package com.ieumsae.assetieum.batch.hrevent.scheduler;

import com.ieumsae.assetieum.batch.hrevent.config.HrEventExecutionJobConfig;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HrEventExecutionJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job hrEventExecutionJob;

    // 매일 10시에 오늘 실행일인 HR 이벤트를 대상으로 hrEventExecutionJob을 실행합니다.
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void run() throws Exception {
        // JobParameters가 같으면 Spring Batch가 같은 JobInstance로 판단하므로 실행 기준일을 명시합니다.
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(
                        HrEventExecutionJobConfig.HR_EVENT_EXECUTION_DATE_PARAMETER,
                        LocalDate.now().toString()
                )
                .toJobParameters();

        jobLauncher.run(hrEventExecutionJob, jobParameters);
    }
}
