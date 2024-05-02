package com.jbaacount.service;

import com.jbaacount.global.dto.SliceDto;
import com.jbaacount.global.exception.BusinessLogicException;
import com.jbaacount.global.exception.ExceptionMessage;
import com.jbaacount.mapper.MemberMapper;
import com.jbaacount.model.Member;
import com.jbaacount.payload.request.member.MemberUpdateRequest;
import com.jbaacount.payload.response.member.MemberDetailResponse;
import com.jbaacount.payload.response.member.MemberMultiResponse;
import com.jbaacount.payload.response.member.MemberScoreResponse;
import com.jbaacount.payload.response.member.MemberUpdateResponse;
import com.jbaacount.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService
{
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FileService fileService;


    @Transactional
    public Member save(Member member)
    {
        return memberRepository.save(member);
    }

    @Transactional
    public MemberUpdateResponse updateMember(MemberUpdateRequest request, MultipartFile multipartFile, Member currentMember)
    {
        Member findMember = getMemberById(currentMember.getId());

        log.info("===updateMember===");
        log.info("findMember email = {}", findMember.getEmail());

        if(multipartFile != null && !multipartFile.isEmpty())
        {
            fileService.deleteProfilePhoto(findMember);
            fileService.storeProfileImage(multipartFile, findMember);
            log.info("profile image = {}", findMember.getFile().getUrl());
        }

        if(request != null)
        {
            Optional.ofNullable(request.getNickname())
                    .ifPresent(findMember::updateNickname);
            Optional.ofNullable(request.getPassword())
                    .ifPresent(password -> findMember.updatePassword(passwordEncoder.encode(password)));
        }

        return MemberMapper.INSTANCE.toMemberUpdateResponse(findMember);
    }

    public Member getMemberById(long id)
    {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessLogicException(ExceptionMessage.USER_NOT_FOUND));
    }

    public MemberDetailResponse getMemberDetailResponse(Long memberId)
    {
        Member member = getMemberById(memberId);

        var response = MemberMapper.INSTANCE.toMemberDetailResponse(member);


        return response;
    }

    public SliceDto<MemberMultiResponse> getAllMembers(String keyword, Long memberId, Pageable pageable)
    {
        return memberRepository.findAllMembers(keyword, memberId, pageable);
    }

    public List<MemberScoreResponse> findTop3MembersByScore(LocalDateTime now)
    {
        LocalDateTime startMonth = LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0);
        LocalDateTime endMonth = startMonth.plusMonths(1);

        log.info("findTop3Members");
        return memberRepository.memberResponseForReward(startMonth, endMonth);
    }

    @Transactional
    public void deleteById(Member member)
    {

        log.info("deleted Member nickname = {}", member.getNickname());

        if(member.getFile() != null)
        {
            log.info("delete image = {}", member.getFile().getUrl());
            fileService.deleteProfilePhoto(member);
        }

        memberRepository.deleteById(member.getId());
    }

    public boolean verifyExistEmail(String email)
    {
        return memberRepository.existsByEmail(email);
    }


    public boolean verifyExistNickname(String nickname)
    {
         return memberRepository.existsByNickname(nickname);
    }

    public String checkExistEmail(String email)
    {
        boolean response = memberRepository.findByEmail(email).isPresent();

        return response ? "이미 사용중인 이메일입니다." : "사용할 수 있는 이메일입니다.";
    }

    public String checkExistNickname(String nickname)
    {
        boolean response = memberRepository.findByNickname(nickname).isPresent();

        return response ? "이미 사용중인 닉네임입니다." : "사용할 수 있는 닉네임입니다.";
    }

    public Member findMemberByEmail(String email)
    {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionMessage.USER_NOT_FOUND));
    }

    public Optional<Member> findOptionalMemberByEmail(String email)
    {
        return memberRepository.findByEmail(email);
    }

}
