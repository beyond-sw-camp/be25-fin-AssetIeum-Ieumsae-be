package com.ieumsae.assetieum.batch.intangibleasset.scheduler;

import com.ieumsae.assetieum.batch.intangibleasset.config.IntangibleAssetReminderJobConfig;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntangibleAssetReminderJobScheduler {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final JobLauncher jobLauncher;
	private final Job intangibleAssetReminderJob;

	@Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
	public void run() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(
				IntangibleAssetReminderJobConfig.BASE_DATE_PARAMETER,
				LocalDate.now(SEOUL_ZONE).toString()
			)
			.toJobParameters();
		try {
			jobLauncher.run(intangibleAssetReminderJob, jobParameters);
		} catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException ignored) {
		}
	}
}
