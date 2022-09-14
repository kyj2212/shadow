package com.example.shadow.domain.shadow.repository;

import com.example.shadow.domain.member.entity.Member;
import com.example.shadow.domain.shadow.entity.Shadow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShadowRepository extends JpaRepository<Shadow, Long> {

    Optional<Shadow> findByMainurl(String url);
    Shadow findByName(String name);
    Shadow findByNameAndMember(String name, Member member);

}