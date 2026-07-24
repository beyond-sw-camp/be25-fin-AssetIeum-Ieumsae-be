package com.ieumsae.assetieum.domain.dashboard.cache;

import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.TicketProgressSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.repository.DashboardRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardCacheWarmupService {

	private final DashboardRepository dashboardRepository;
	private final DashboardCacheService dashboardCacheService;

	@Value("${app.kafka.dashboard-cache.warmup-enabled:true}")
	private boolean warmupEnabled;

	public void warmUpCompanySummaries(UUID companyId) {
		if (!warmupEnabled || companyId == null) {
			return;
		}

		try {
			dashboardCacheService.getOrLoad(
				companyId,
				"ticket:all",
				TicketProgressSummaryResponse.class,
				() -> dashboardRepository.getTicketProgressSummary(companyId, null)
			);
			dashboardCacheService.getOrLoad(
				companyId,
				"owned:all",
				OwnedAssetSummaryResponse.class,
				() -> dashboardRepository.getOwnedAssetSummary(companyId, null)
			);
			dashboardCacheService.getOrLoad(
				companyId,
				"expiring:all",
				ExpiringAssetSummaryResponse.class,
				() -> dashboardRepository.getExpiringAssetSummary(companyId, null)
			);
		} catch (RuntimeException exception) {
			log.warn("Dashboard cache warm-up failed; cache will be filled on demand. companyId={}",
				companyId, exception);
		}
	}
}
