package com.hyunu.garagecare.member.domain;

@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UnigueConstraints(name = "uk_mamber_email", columnNames = "email")
        }
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, lengh = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumeraed(EnumType.STRING)
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
            String name;
            String email;
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