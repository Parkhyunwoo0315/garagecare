package com.hyunu.garagecare.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_email", columnNames = "email")
        }
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private boolean active;

    private Member(
            String name,
            String email,
            String password,
            MemberRole role
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    public static Member create(
            String name,
            String email,
            String encodedPassword
    ) {
        return new Member(
                name,
                email,
                encodedPassword,
                MemberRole.MEMBER
        );
    }

    public void deactivate() {
        this.active = false;
    }
}