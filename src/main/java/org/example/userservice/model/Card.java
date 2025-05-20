package org.example.userservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.mapping.List;

import java.time.LocalDate;

@Entity
@Table(name = "card_info")
@Getter
@Setter
@RequiredArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private byte[] number;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String holder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;



}
