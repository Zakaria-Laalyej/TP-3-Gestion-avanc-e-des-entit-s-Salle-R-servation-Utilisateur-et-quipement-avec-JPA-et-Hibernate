package com.example.service;

import com.example.model.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class ReservationService {

    /** Exception métier : évite de propager les détails techniques de JPA. */
    public static class GestionException extends RuntimeException {
        public GestionException(String message, Throwable cause) { super(message, cause); }
        public GestionException(String message) { super(message); }
    }

    private final EntityManagerFactory emf;

    public ReservationService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // ===== 4. Gestion robuste des erreurs : une transaction réutilisable =====
    private <T> T executer(Function<EntityManager, T> action) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultat = action.apply(em);
            tx.commit();
            return resultat;
        } catch (GestionException e) {
            if (tx.isActive()) tx.rollback();
            throw e;                      // erreur métier : on la garde telle quelle
        } catch (PersistenceException | IllegalStateException e) {
            if (tx.isActive()) tx.rollback();
            throw new GestionException("Échec de l'opération : " + e.getMessage(), e);
        } finally {
            em.close();                   // toujours fermer, même en cas d'erreur
        }
    }

    // ===== 2. Disponibilité d'une salle =====
    public boolean estDisponible(Long salleId, LocalDateTime debut, LocalDateTime fin) {
        return executer(em -> estDisponible(em, salleId, debut, fin));
    }

    private boolean estDisponible(EntityManager em, Long salleId,
                                  LocalDateTime debut, LocalDateTime fin) {
        // Deux périodes se chevauchent si : début existante < fin demandée
        // ET fin existante > début demandé.
        Long conflits = em.createQuery(
                        "SELECT COUNT(r) FROM Reservation r " +
                                "WHERE r.salle.id = :salleId " +
                                "AND r.dateDebut < :fin AND r.dateFin > :debut", Long.class)
                .setParameter("salleId", salleId)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getSingleResult();
        return conflits == 0;
    }

    /** Crée une réservation si les dates sont valides et la salle libre. */
    public Reservation reserver(Long utilisateurId, Long salleId,
                                LocalDateTime debut, LocalDateTime fin) {
        return executer(em -> {
            Utilisateur u = em.find(Utilisateur.class, utilisateurId);
            Salle s = em.find(Salle.class, salleId);
            if (u == null) throw new GestionException("Utilisateur introuvable : " + utilisateurId);
            if (s == null) throw new GestionException("Salle introuvable : " + salleId);
            if (!estDisponible(em, salleId, debut, fin)) {
                throw new GestionException("La salle " + s.getNom() + " est déjà réservée sur cette période");
            }
            Reservation r = new Reservation();
            r.setDateDebut(debut);
            r.setDateFin(fin);
            u.addReservation(r);
            s.addReservation(r);
            em.persist(r);               // @PrePersist vérifie début < fin
            return r;
        });
    }

    // ===== 3. Requêtes JPQL =====
    public List<Reservation> reservationsDeUtilisateur(Long utilisateurId) {
        return executer(em -> em.createQuery(
                        "SELECT r FROM Reservation r WHERE r.utilisateur.id = :id ORDER BY r.dateDebut",
                        Reservation.class)
                .setParameter("id", utilisateurId)
                .getResultList());
    }

    public List<Salle> sallesAvecEquipement(String nomEquipement) {
        return executer(em -> em.createQuery(
                        "SELECT DISTINCT s FROM Salle s JOIN s.equipements e WHERE e.nom = :nom",
                        Salle.class)
                .setParameter("nom", nomEquipement)
                .getResultList());
    }
}