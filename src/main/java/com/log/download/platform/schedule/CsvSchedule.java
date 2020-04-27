package com.log.download.platform.schedule;

import com.log.download.platform.service.MenuService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * CsvShedule
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-27 9:07
 * Modified by:
 */
@Log4j2
@Component
public class CsvSchedule {
	
	@Resource
	private MenuService menuService;

	/**
	 * 每40分钟执行一次
	 */
	@Scheduled(cron = "0 0/40 * * * ? ")
	private void csvSchedule() {
		log.info("begin csv schedule at: {}", LocalDateTime::now);
		menuService.readCSV();
		log.info("csv schedule end at: {}", LocalDateTime::now);
	}
}
