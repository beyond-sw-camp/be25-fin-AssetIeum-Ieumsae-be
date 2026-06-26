package com.ieumsae.assetieum.batch.tangibleasset.scheduler;

import com.ieumsae.assetieum.batch.tangibleasset.config.TangibleAssetReminderJobConfig;
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
public class TangibleAssetReminderJobScheduler {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final JobLauncher jobLauncher;
	private final Job tangibleAssetReminderJob;

	@Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
	public void run() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(
				TangibleAssetReminderJobConfig.BASE_DATE_PARAMETER,
				LocalDate.now(SEOUL_ZONE).toString()
			)
			.addLong("run.id", System.currentTimeMillis())
			.toJobParameters();
		try {
			jobLauncher.run(tangibleAssetReminderJob, jobParameters);
		} catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException ignored) {
		}
	}
}
