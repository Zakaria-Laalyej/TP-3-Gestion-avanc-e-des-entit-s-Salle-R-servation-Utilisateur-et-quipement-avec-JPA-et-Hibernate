package com.example;

import com.example.model.*;
import com.example.service.ReservationService;

import javax.persistence.*;
import java.time.LocalDateTime;

public class App {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
        EntityManager em = emf.createEntityManager();

        try {
            // ===== 1. Insertion =====
            em.getTransaction().begin();

            Utilisateur user = new Utilisateur();
            user.setEmail("zakaria@example.com");
            user.setNom("Zakaria Laalyej");

            Salle salle = new Salle();
            salle.setNom("Salle A");

            Equipement projecteur = new Equipement();
            projecteur.setNom("Projecteur");
            salle.addEquipement(projecteur);

            Reservation reservation = new Reservation();
            reservation.setDateDebut(LocalDateTime.of(2026, 10, 20, 9, 0));   // sur l'objet
            reservation.setDateFin(LocalDateTime.of(2026, 10, 20, 11, 0));
            reservation.setSalle(salle);
            reservation.setUtilisateur(user);

            user.addReservation(reservation);
            salle.addReservation(reservation);

            em.persist(user);
            em.persist(salle);

            em.getTransaction().commit();
            System.out.println("Insertion réussie");

            // ===== 2. Exercices supplémentaires (après l'insertion) =====
            ReservationService service = new ReservationService(emf);

            System.out.println("=== Disponibilité ===");
            // Chevauche la réservation 9h-11h : doit afficher false
            System.out.println(service.estDisponible(1L,
                    LocalDateTime.of(2026, 10, 20, 10, 0), LocalDateTime.of(2026, 10, 20, 12, 0)));
            // Créneau libre : doit afficher true
            System.out.println(service.estDisponible(1L,
                    LocalDateTime.of(2026, 10, 20, 14, 0), LocalDateTime.of(2026, 10, 20, 16, 0)));

            System.out.println("=== Requêtes JPQL ===");
            System.out.println("Réservations de l'utilisateur 1 : " + service.reservationsDeUtilisateur(1L));
            System.out.println("Salles avec projecteur : " + service.sallesAvecEquipement("Projecteur"));

            System.out.println("=== Gestion d'erreur ===");
            try {
                service.reserver(1L, 1L,
                        LocalDateTime.of(2026, 10, 20, 10, 0), LocalDateTime.of(2026, 10, 20, 12, 0));
            } catch (ReservationService.GestionException e) {
                System.out.println("Erreur attendue : " + e.getMessage());
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();

        }
    }
}