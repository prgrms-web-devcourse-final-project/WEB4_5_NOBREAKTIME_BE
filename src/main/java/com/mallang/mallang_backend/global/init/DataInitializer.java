package com.mallang.mallang_backend.global.init;

import com.mallang.mallang_backend.domain.member.entity.LoginPlatform;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.entity.SubscriptionType;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
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
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Value("${jwt.secret}")
    private String secretKey;

    private final MemberRepository memberRepository;
    private final WordbookRepository wordbookRepository;
    private final ExpressionBookRepository expressionBookRepository;

    @Override
    public void run(String... args) throws Exception {
        Member basicUser = createTestUser();

        createToken();

        setSecurityContext(basicUser, basicUser.getSubscriptionType().getRoleName());
    }

    // JWT 생성
    public String createToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", 1L);
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
                .language(Language.ENGLISH)
                .profileImageUrl("https://team07-mallang-bucket.s3.ap-northeast-2.amazonaws.com/profile.jpg")
                .build();
        testUser = memberRepository.save(testUser);
        testUser.updateSubscription(SubscriptionType.STANDARD);

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
