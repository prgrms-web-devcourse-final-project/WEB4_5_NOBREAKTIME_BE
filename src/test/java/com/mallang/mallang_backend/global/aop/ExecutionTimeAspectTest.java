package com.mallang.mallang_backend.global.aop;

import com.mallang.mallang_backend.global.aop.time.TimeTrace;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@SpringBootTest
@Slf4j
@Import(ExecutionTimeAspectTest.AopTestService.class)
class ExecutionTimeAspectTest {

    @Autowired
    private AopTestService aopTestService;

    @Service
    static class AopTestService {

        @TimeTrace
        public void test1() throws InterruptedException {
            log.info("test1 실행 중");
            Thread.sleep(1000);
        }
    }

    @Test
    void t1() throws Exception {
        //given
        aopTestService.test1();
        // 예시: AopTestService.test1() - 실행 시간 : 1004 ms
    }

}