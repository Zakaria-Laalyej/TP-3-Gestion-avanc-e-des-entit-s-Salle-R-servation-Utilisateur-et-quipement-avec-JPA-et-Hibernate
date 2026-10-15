package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nullable retiré : le code du prof ne renseigne pas les dates
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @PrePersist
    @PreUpdate
    private void validerDates() {
        if (dateDebut == null || dateFin == null) {
            throw new IllegalStateException("Les dates de début et de fin sont obligatoires");
        }
        if (!dateDebut.isBefore(dateFin)) {
            throw new IllegalStateException("La date de début doit être antérieure à la date de fin");
        }
    }
    public Reservation() {}

    public Reservation(LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Long getId() { return id; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", du " + dateDebut + " au " + dateFin + "}";
    }
}