package com.task.library_managment_system.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;
    private String phone;

    @Column(name = "membership_date", nullable = false)
    private LocalDate membershipDate;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<BorrowingTransaction> transactions = new ArrayList<>();
}
