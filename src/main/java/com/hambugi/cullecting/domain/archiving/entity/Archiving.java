package com.hambugi.cullecting.domain.archiving.entity;

import com.hambugi.cullecting.domain.archiving.util.CardTemplate;
import com.hambugi.cullecting.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="archiving")
@Getter
@Setter
public class Archiving {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable=false, length=20)
    private String title;

    @Column(nullable=false, length=500)
    private String description;

    @Column(nullable=false, length=10)
    private String date; // YYYY-MM-DD

    @Column(nullable=false, length=255)
    private String imageURL;

    @Column(nullable=false, length=255)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=255)
    private CardTemplate template;
}
