package ru.practicum.statsvc.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "statistics")
@RequiredArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Integer id;
    @Column(length = 128)
    private final String app;
    @Column(length = 128)
    private final String uri;
    @Column(length = 40)
    private final String ip;
    private final LocalDateTime timestamp;
}