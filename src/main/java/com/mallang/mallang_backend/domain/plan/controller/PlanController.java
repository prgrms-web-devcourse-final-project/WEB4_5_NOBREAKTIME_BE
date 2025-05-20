package com.mallang.mallang_backend.domain.plan.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallang.mallang_backend.domain.plan.dto.PlanResponse;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository planRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::convertToPlanResponse)
                .toList();
    }

    private PlanResponse convertToPlanResponse(Plan plan) {
        try {
            PlanResponse response = objectMapper.readValue(plan.getBenefits(), PlanResponse.class);
            response.setType(plan.getType());
            response.setPeriod(plan.getPeriod());
            response.setAmount(plan.getAmount());
            response.setDescription(plan.getDescription());
            return response;
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "플랜 정보 변환 중 오류 발생",
                    e
            );
        }
    }
}
