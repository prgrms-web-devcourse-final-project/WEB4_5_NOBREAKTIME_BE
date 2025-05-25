package com.mallang.mallang_backend.domain.subscription.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 컨텍스트 재생성
class SubscriptionQueryRepositoryTest {

    @Autowired
    private SubscriptionQueryRepository queryRepository;

    @Test
    @Transactional
    @DisplayName("ACTIVE 상태이고, expiredAt이 오늘 0시보다 이전인 구독의 ID만 조회하는지 검증")
    void findActiveSubWithMember() throws Exception {
        //given & when
        List<Long> ids = queryRepository.findActiveSubWithMember();

        //then
        log.info("ids: {}", ids);
        assertThat(ids).hasSize(4);
    }

    @Test
    @DisplayName("구독 상태가 EXPIRED로 정상 변경되는지 검증")
    void bulkUpdateStatus() throws Exception {
        //given & when
        List<Long> ids = queryRepository.findActiveSubWithMember();
        long count = queryRepository.bulkUpdateStatus(ids);

        //then
        assertThat(count).isEqualTo(4);
    }
}