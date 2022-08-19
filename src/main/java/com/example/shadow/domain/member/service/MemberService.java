package com.example.shadow.domain.member.service;

import com.example.shadow.domain.member.dto.MemberUpdateDto;
import com.example.shadow.domain.member.entity.Member;
import com.example.shadow.domain.member.repository.MemberRepository;
import com.example.shadow.exception.SignupEmailDuplicatedException;
import com.example.shadow.exception.SignupUsernameDuplicatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member create(String username,String password, String name,  String email) throws SignupUsernameDuplicatedException, SignupEmailDuplicatedException {

        Member member = new Member(
                username,
                passwordEncoder.encode(password),
                name,
                email
        );

        save(member);

        return member;
    }

    public void save(Member member) throws SignupUsernameDuplicatedException, SignupEmailDuplicatedException {
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {

            if (memberRepository.existsByUsername(member.getUsername())) {
                throw new SignupUsernameDuplicatedException("중복된 ID 입니다.");
            } else {
                throw new SignupEmailDuplicatedException("중복된 이메일 입니다.");
            }
        }

    }


    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
    public Member findById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 사용자 입니다."));
    }

    @Transactional
    public void delete(long id) {
        memberRepository.delete(findById(id));
    }

    public void update(Member member, Member newMember) throws SignupUsernameDuplicatedException, SignupEmailDuplicatedException {
        member.updateName(newMember.getName());
        member.updateEmail(newMember.getEmail());
        member.setEncryptedPassword(newMember.getPassword());
        save(member);
    }
}