package com.mallang.mallang_backend.domain.sentence.expressionbook.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.entity.ExpressionQuizResult;
import com.mallang.mallang_backend.domain.quiz.expressionquizresult.repository.ExpressionQuizResultRepository;
import com.mallang.mallang_backend.domain.sentence.expression.entity.Expression;
import com.mallang.mallang_backend.domain.sentence.expression.repository.ExpressionRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.dto.*;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import com.mallang.mallang_backend.domain.sentence.expressionbook.repository.ExpressionBookRepository;
import com.mallang.mallang_backend.domain.sentence.expressionbook.service.ExpressionBookService;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItem;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.entity.ExpressionBookItemId;
import com.mallang.mallang_backend.domain.sentence.expressionbookitem.repository.ExpressionBookItemRepository;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mallang.mallang_backend.domain.member.entity.SubscriptionType.BASIC;
import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_EXPRESSION_BOOK_NAME;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpressionBookServiceImpl implements ExpressionBookService {

    private final ExpressionBookRepository expressionBookRepository;
    private final MemberRepository memberRepository;
    private final ExpressionBookItemRepository expressionBookItemRepository;
    private final ExpressionRepository expressionRepository;
    private final VideoRepository videoRepository;
    private final GptService gptService;
    private final ExpressionQuizResultRepository expressionQuizResultRepository;
    private final SubtitleRepository subtitleRepository;

    @Override
    @Transactional
    public Long create(ExpressionBookRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        if (member.getSubscriptionType() == BASIC) {
            throw new ServiceException(NO_EXPRESSION_BOOK_CREATE_PERMISSION);
        }

        if (member.getLanguage() == Language.NONE) {
            throw new ServiceException(LANGUAGE_IS_NONE);
        }

        if (request.getName().equals(DEFAULT_EXPRESSION_BOOK_NAME)) {
            throw new ServiceException(EXPRESSION_BOOK_CREATE_DEFAULT_FORBIDDEN);
        }

        if (expressionBookRepository.existsByMemberAndName(member, request.getName())) {
            throw new ServiceException(DUPLICATE_EXPRESSION_BOOK_NAME);
        }

        ExpressionBook expressionBook = ExpressionBook.builder()
                .name(request.getName())
                .language(member.getLanguage())
                .member(member)
                .build();

        return expressionBookRepository.save(expressionBook).getId();
    }

    @Override
    public List<ExpressionBookResponse> getByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        List<ExpressionBook> books = expressionBookRepository.findAllByMemberIdAndLanguage(memberId, member.getLanguage());

        if (!member.canUseAdditaional()) {
            books = books.stream()
                    .filter(eb -> ExpressionBook.isDefault(eb))
                    .toList();
        }

        return books.stream()
                .map(book -> ExpressionBookResponse.from(
                        book,
                        expressionBookItemRepository.countByIdExpressionBookId(book.getId()),
                        expressionBookItemRepository.countByIdExpressionBookIdAndLearnedTrue(book.getId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public void updateName(Long expressionBookId, Long memberId, String newName) {
        ExpressionBook expressionBook = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

        if (!expressionBook.getMember().getId().equals(memberId)) {
            throw new ServiceException(FORBIDDEN_EXPRESSION_BOOK);
        }

        if (!expressionBook.getMember().canUseAdditaional()) {
            throw new ServiceException(NO_EXPRESSION_BOOK_CREATE_PERMISSION);
        }

        if (DEFAULT_EXPRESSION_BOOK_NAME.equals(expressionBook.getName())) {
            throw new ServiceException(EXPRESSION_BOOK_RENAME_DEFAULT_FORBIDDEN);
        }

        expressionBook.updateName(newName);
    }

    @Override
    @Transactional
    public void delete(Long expressionBookId, Long memberId) {
        ExpressionBook expressionBook = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

        if (!expressionBook.getMember().getId().equals(memberId)) {
            throw new ServiceException(FORBIDDEN_EXPRESSION_BOOK);
        }

        if (!expressionBook.getMember().canUseAdditaional()) {
            throw new ServiceException(NO_EXPRESSION_BOOK_CREATE_PERMISSION);
        }

        // 기본 표현함을 삭제 시도하면 실패
        if (DEFAULT_EXPRESSION_BOOK_NAME.equals(expressionBook.getName())) {
            throw new ServiceException(EXPRESSION_BOOK_DELETE_DEFAULT_FORBIDDEN);
        }

        // 삭제하려는 표현함의 표현에 대한 퀴즈 결과를 모두 삭제
        expressionQuizResultRepository.deleteAllByExpressionBook(expressionBook);

        // 삭제하려는 표현함의 표현들을 삭제
        expressionBookItemRepository.deleteAllById_ExpressionBookId(expressionBookId);

        // 추가 표현함 삭제
        expressionBookRepository.delete(expressionBook);
    }

    @Override
    public List<ExpressionResponse> getExpressionsByBook(List<Long> expressionBookIds, Long memberId) {
        // 사용자 인증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        validateExpressionBookIdsExist(expressionBookIds);

        List<ExpressionBookItem> items = findExpressionBookItems(expressionBookIds, member);

        return convertToExpressionResponses(items);
    }

    private List<ExpressionBookItem> findExpressionBookItems(List<Long> expressionBookIds, Member member) {

        // 기본 표현함 조회 - 표현함 ID가 비어있거나 null인 경우
        if (expressionBookIds == null || expressionBookIds.isEmpty()) {
            ExpressionBook defaultBook = expressionBookRepository.findByMemberAndNameAndLanguage(member, DEFAULT_EXPRESSION_BOOK_NAME, member.getLanguage())
                    .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

            return expressionBookItemRepository.findAllById_ExpressionBookIdOrderByCreatedAtDesc(defaultBook.getId());
        }

        // 단일 표현함 조회 - 표현함 Id가 1개인 경우
        if (expressionBookIds.size() == 1) {
            ExpressionBook expressionBook = expressionBookRepository.findById(expressionBookIds.get(0))
                    .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

            if (!expressionBook.getMember().getId().equals(member.getId())) {
                throw new ServiceException(FORBIDDEN_EXPRESSION_BOOK);
            }

            return expressionBookItemRepository.findAllById_ExpressionBookIdOrderByCreatedAtDesc(expressionBook.getId());
        }

        // 여러 표현함 조회 - 표현함 Id가 2개 이상인 경우
        List<ExpressionBook> expressionBooks = expressionBookRepository.findAllById(expressionBookIds);

        // 표현함 소유자 확인
        if (expressionBooks.stream().anyMatch(eb -> !eb.getMember().getId().equals(member.getId()))) {
            throw new ServiceException(FORBIDDEN_EXPRESSION_BOOK);
        }

        return expressionBookItemRepository.findAllById_ExpressionBookIdInOrderByCreatedAtDesc(expressionBookIds);
    }

    private List<ExpressionResponse> convertToExpressionResponses(List<ExpressionBookItem> items) {

        // 표현 ID만 추출하여 리스트로 변환 (중복 포함)
        List<Long> expressionIds = items.stream()
                .map(item -> item.getId().getExpressionId())
                .toList();

        // 한 번에 Expression 조회
        Map<Long, Expression> expressionMap = expressionRepository.findAllById(expressionIds).stream()
                .collect(Collectors.toMap(Expression::getId, Function.identity()));

        // ExpressionResponse 리스트로 변환
        return items.stream()
                .map(item -> {
                    Expression expression = expressionMap.get(item.getId().getExpressionId());
                    if (expression == null) {
                        throw new ServiceException(EXPRESSION_NOT_FOUND);
                    }

                    return ExpressionResponse.from(
                            expression,
                            item.getCreatedAt(),
                            item.getId().getExpressionBookId()
                    );
                })
                .toList();
    }

    private void validateExpressionBookIdsExist(List<Long> requestedIds) {
        // 표현함 ID가 비어있거나 null인 경우
        if (requestedIds == null || requestedIds.isEmpty()) {
            return; // 기본 표현함 케이스는 통과
        }

        // 실제 존재하는 ID 조회
        Set<Long> existingIdSet = expressionBookRepository.findAllById(requestedIds).stream()
                .map(ExpressionBook::getId)
                .collect(Collectors.toSet());

        List<Long> invalidIds = requestedIds.stream()
                .filter(id -> !existingIdSet.contains(id))
                .toList();

        if (!invalidIds.isEmpty()) {
            throw new ServiceException(EXPRESSION_BOOK_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void save(ExpressionSaveRequest request, Long expressionBookId, Long memberId) {
        String videoId = request.getVideoId();
        Long subtitleId = request.getSubtitleId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        ExpressionBook expressionBook = expressionBookRepository.findById(expressionBookId)
                .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

        // 자신의 표현함이 아니면 표현 추가 실패
        if (!expressionBook.getMember().getId().equals(memberId)) {
            throw new ServiceException(FORBIDDEN_EXPRESSION_BOOK);
        }

        // 추가 표현함 사용 권한이 없으면 추가 표현함에 표현 추가 실패
        if (!member.canUseAdditaional() && !ExpressionBook.isDefault(expressionBook)) {
            throw new ServiceException(NO_PERMISSION);
        }

        Subtitle subtitle = subtitleRepository.findById(subtitleId)
                .orElseThrow(() -> new ServiceException(SUBTITLE_NOT_FOUND));

        String sentence = subtitle.getOriginalSentence();
        String description = subtitle.getTranslatedSentence();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime subtitleAt = LocalTime.parse(subtitle.getStartTime(), formatter);

        Expression expression = getOrCreateExpression(videoId, sentence, description, subtitleAt, member.getLanguage());

        ExpressionBookItemId itemId = new ExpressionBookItemId(expression.getId(), expressionBook.getId());
        if (!expressionBookItemRepository.existsById(itemId)) {
            expressionBookItemRepository.save(new ExpressionBookItem(itemId.getExpressionId(), itemId.getExpressionBookId()));
        }
    }

    private Expression getOrCreateExpression(String videoId, String sentence, String description, LocalTime subtitleAt, Language language) {
        return expressionRepository
                .findByVideosIdAndSentenceAndSubtitleAt(videoId, sentence, subtitleAt)
                .orElseGet(() -> {
                    String gptResult = gptService.analyzeSentence(sentence, description, language);
                    if (gptResult == null || gptResult.isBlank()) {
                        throw new ServiceException(GPT_RESPONSE_EMPTY);
                    }

                    Videos video = videoRepository.findById(videoId)
                            .orElseThrow(() -> new ServiceException(VIDEO_ID_SEARCH_FAILED));

                    return expressionRepository.save(Expression.builder()
                            .sentence(sentence)
                            .description(description)
                            .sentenceAnalysis(gptResult)
                            .videos(video)
                            .subtitleAt(subtitleAt)
                            .build());
                });
    }

    // 표현함에서 표현 삭제
    @Transactional
    @Override
    public void deleteExpressionsFromExpressionBook(DeleteExpressionsRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        ExpressionBook expressionBook = expressionBookRepository.findById(request.getExpressionBookId())
                .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

        // 자신의 표현함이 아니면 표현 추가 실패
        if (!expressionBook.getMember().getId().equals(memberId)) {
            throw new ServiceException(FORBIDDEN_EXPRESSION_BOOK);
        }

        // 추가 단어장 사용 권한이 없으면 추가 단어장의 단어 삭제 불가능
        if (!ExpressionBook.isDefault(expressionBook) && !member.canUseAdditaional()) {
            throw new ServiceException(ErrorCode.NO_PERMISSION);
        }

        // 삭제할 ID 리스트
        List<ExpressionBookItemId> ids = request.getExpressionIds().stream()
                .map(expressionId -> new ExpressionBookItemId(expressionId, request.getExpressionBookId()))
                .toList();

        // 표현에 대한 퀴즈 결과 삭제
        expressionQuizResultRepository.deleteAllByExpression_IdInAndExpressionBook(request.getExpressionIds(), expressionBook);
        // 표현 삭제
        expressionBookItemRepository.deleteAllById(ids);
    }

    // 표현의 표현함 이동
    @Transactional
    @Override
    public void moveExpressions(MoveExpressionsRequest request, Long memberId) {
        ExpressionBook sourceBook = expressionBookRepository.findById(request.getSourceExpressionBookId())
                .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

        ExpressionBook targetBook = expressionBookRepository.findById(request.getTargetExpressionBookId())
                .orElseThrow(() -> new ServiceException(EXPRESSION_BOOK_NOT_FOUND));

        if (sourceBook.getMember().getSubscriptionType() == BASIC ||
                targetBook.getMember().getSubscriptionType() == BASIC) {
            throw new ServiceException(NO_EXPRESSION_BOOK_CREATE_PERMISSION);
        }

        if (!sourceBook.getMember().getId().equals(memberId) ||
                !targetBook.getMember().getId().equals(memberId)) {
            throw new ServiceException(FORBIDDEN_EXPRESSION_BOOK);
        }

        List<ExpressionBookItemId> deleteIds = request.getExpressionIds().stream()
                .map(expressionId -> new ExpressionBookItemId(expressionId, sourceBook.getId()))
                .toList();


        // 새로운 표현을 생성하면서 목표 표현함에 저장
        for (Long expressionId : request.getExpressionIds()) {
            ExpressionBookItemId newId = new ExpressionBookItemId(expressionId, targetBook.getId());

            if (expressionBookItemRepository.existsById(newId)) continue;

            ExpressionBookItem newItem = ExpressionBookItem.builder()
                    .expressionId(expressionId)
                    .expressionBookId(targetBook.getId())
                    .build();

            expressionBookItemRepository.save(newItem);
        }

        // 표현 퀴즈 결과의 표현 연결 변경
        List<ExpressionQuizResult> quizResults =
                expressionQuizResultRepository.findAllByExpression_IdInAndExpressionBook(request.getExpressionIds(), sourceBook);
        for (ExpressionQuizResult quizResult : quizResults) {
            quizResult.updateExpressionBook(targetBook);
        }
        expressionQuizResultRepository.saveAll(quizResults);

        // 원본 표현 표현함에서 삭제
        expressionBookItemRepository.deleteAllById(deleteIds);
    }

    // 표현함에서 표현 검색
    @Override
    public List<ExpressionResponse> searchExpressions(Long memberId, String keyword) {
        // 검색 결과 ExpressionBookItem 기준으로 받아오기
        List<ExpressionBookItem> items = expressionBookItemRepository.findByMemberIdAndKeyword(memberId, keyword);

        List<Long> expressionIds = items.stream()
                .map(i -> i.getId().getExpressionId())
                .distinct()
                .toList();

        Map<Long, Expression> expressionMap = expressionRepository.findAllById(expressionIds).stream()
                .collect(Collectors.toMap(Expression::getId, Function.identity()));

        return items.stream()
                .filter(item -> expressionMap.containsKey(item.getId().getExpressionId()))
                .sorted(Comparator.comparing(ExpressionBookItem::getCreatedAt).reversed())
                .map(item -> ExpressionResponse.from(
                        expressionMap.get(item.getId().getExpressionId()),
                        item.getCreatedAt(),
                        item.getId().getExpressionBookId()
                ))
                .toList();
    }
}
