package com.mallang.mallang_backend.global.init;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.token.TokenPair;
import com.mallang.mallang_backend.global.token.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final WordbookRepository wordbookRepository;
    private final ExpressionBookRepository expressionBookRepository;

    @Override
    public void run(String... args) throws Exception {
        Member basicUser = createTestUser();

        TokenPair tokenPair1 = tokenService.createTokenPair(
            basicUser.getId(),
            basicUser.getSubscriptionType().getRoleName()
        );

        // 생성된 토큰 로깅
        log.info("======= 테스트 사용자 인증 정보 =======");
        log.info("Access Token: {}", tokenPair1.getAccessToken());
        log.info("Refresh Token: {}", tokenPair1.getRefreshToken());
        log.info("===================================");

        setSecurityContext(basicUser, basicUser.getSubscriptionType().getRoleName());
    }

    private Member createTestUser() throws IOException {
        Member testUser = Member.builder()
                .platformId("123123AK")
                .email("google123@gmail.com")
                .nickname("TestUser1")
                .loginPlatform(LoginPlatform.GOOGLE)
                .language(Language.ENGLISH)
                .profileImageUrl("https://team07-mallang-bucket.s3.ap-northeast-2.amazonaws.com/profile.jpg")
                .build();
        // testUser.updateSubscription(Subscription.STANDARD);
        testUser = memberRepository.save(testUser);

        List<Wordbook> wordbooks = Wordbook.createDefault(testUser);
        wordbookRepository.saveAll(wordbooks);

        List<ExpressionBook> expressionBooks = ExpressionBook.createDefault(testUser);
        expressionBookRepository.saveAll(expressionBooks);
        /*
        스탠다드 유저가 필요할 때 -> 하단의 리턴을 주석처리 후 사용
        memberRepository.save(testUser);
        testUser.updateSubscription(Subscription.STANDARD);
        return testUser;*/


        /* 프리미엄 유저가 필요할 때 -> 하단의 리턴을 주석처리 후 사용
        memberRepository.save(testUser);
        testUser.updateSubscription(Subscription.PREMIUM);
        return testUser;*/

        return testUser;
    }

    // 시큐리티 객체에 인증 정보 추가
    private void setSecurityContext(Member member, String roleName) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                member.getId(),
                null,
                List.of(new SimpleGrantedAuthority(roleName))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
