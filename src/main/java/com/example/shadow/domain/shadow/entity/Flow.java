package com.example.shadow.domain.shadow.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Flow {

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String name;

    @Column
    private String description;

    @Column
    private String url;

    @OneToMany(mappedBy = "flow", cascade = {CascadeType.ALL})
    private List<Flowchart> flowcharts;

    // 홈버튼, 마이페이, 조회페이지
}