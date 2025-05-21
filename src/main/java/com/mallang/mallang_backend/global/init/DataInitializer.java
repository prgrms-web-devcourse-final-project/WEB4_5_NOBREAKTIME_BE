package com.mallang.mallang_backend.global.init;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.plan.entity.Plan;
import com.mallang.mallang_backend.domain.plan.repository.PlanRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.subscription.entity.Subscription;
import com.mallang.mallang_backend.domain.subscription.repository.SubscriptionRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.global.common.Language;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static com.mallang.mallang_backend.global.init.factory.EntityTestFactory.createMember;
import static com.mallang.mallang_backend.global.init.factory.EntityTestFactory.createSubscription;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Value("${jwt.secret}")
    private String secretKey;

    private final MemberRepository memberRepository;
    private final WordbookRepository wordbookRepository;
    private final ExpressionBookRepository expressionBookRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;


    @Override
    public void run(String... args) throws Exception {

        Member basicUser = createTestUser();

        String key = createToken();
        System.out.println("access token: " + key);

        setSecurityContext(basicUser, basicUser.getSubscriptionType().getRoleName());

        Member member1 = createMember();
        Member member2 = createMember();
        Member member3 = createMember();
        Member member4 = createMember();
        Member member5 = createMember();
        Member member6 = createMember();

        List<Member> members = List.of(member1, member2, member3, member4, member5, member6);
        memberRepository.saveAll(members);

        Plan stan = planRepository.findById(4L).get();
        Plan pre = planRepository.findById(7L).get();

        LocalDateTime now = LocalDateTime.now();

        // [ACTIVE] 구독 결제 대상
        Subscription subscription1 = createSubscription(member1, pre, now.minusMonths(1));
        Subscription subscription2 = createSubscription(member2, pre, now.minusMonths(1));
        Subscription subscription3 = createSubscription(member3, stan, now.minusMonths(1));
        Subscription subscription4 = createSubscription(member4, stan, now.minusMonths(1));
        Subscription subscription5 = createSubscription(member5, pre, now.minusMonths(1).plusDays(1));
        Subscription subscription6 = createSubscription(member6, pre, now.minusMonths(1).plusDays(1));

        List<Subscription> subscriptions = List.of(subscription1, subscription2, subscription3,
                subscription4, subscription5, subscription6);
        subscriptionRepository.saveAll(subscriptions);

        log.info("저장된 구독 목록 ID: {}, {}", subscription1.getId(), subscription2.getId());
        log.info("저장 완료");

        if (memberRepository.findById(1L).isPresent()) {
            log.info("데이터 초기화가 이미 진행되었습니다.");
            return;
        }
    }

    // JWT 생성
    public String createToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", 4L);
        claims.put("role", "ROLE_STANDARD");

        Date start = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
        Date end = Date.from(Instant.parse("2025-10-01T00:00:00Z"));
        SecretKey key = Keys.hmacShaKeyFor(HexFormat.of().parseHex(secretKey));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(start)
                .setExpiration(end)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Member createTestUser() throws IOException {
        Member testUser = Member.builder()
                .platformId("123123AK")
                .email("google123@gmail.com")
                .nickname("TestUser1")
                .loginPlatform(LoginPlatform.GOOGLE)
                .language(Language.JAPANESE)
                .profileImageUrl("https://team07-mallang-bucket.s3.ap-northeast-2.amazonaws.com/profile.jpg")
                .build();
        testUser.updateSubscription(SubscriptionType.STANDARD);
        testUser = memberRepository.save(testUser);

        List<Wordbook> wordbooks = Wordbook.createDefault(testUser);
        wordbookRepository.saveAll(wordbooks);

        List<ExpressionBook> expressionBooks = ExpressionBook.createDefault(testUser);
        expressionBookRepository.saveAll(expressionBooks);

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
