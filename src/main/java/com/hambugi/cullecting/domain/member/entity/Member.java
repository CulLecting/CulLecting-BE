package com.hambugi.cullecting.domain.member.entity;

import com.hambugi.cullecting.domain.member.util.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Entity
@Table(name="member")
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false, length=255)
    private String password;

    @Column(nullable=false, unique = true)
    private String nickname;

    @Column(nullable = true)
    @Convert(converter = JsonStringListConverter.class)
    private List<String> locationList;

    @Column(nullable = true)
    @Convert(converter = JsonStringListConverter.class)
    private List<String> categoryList;

}
