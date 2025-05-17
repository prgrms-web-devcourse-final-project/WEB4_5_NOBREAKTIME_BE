package com.mallang.mallang_backend.domain.plan.entity.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyGoal {
	private int videoGoal;
	private int wordGoal;
	private double achievementRate;
	private AchievementDetail achievementDetail;
}
