package com.campinho.campinho.domain.service;

import com.campinho.campinho.domain.request.CreateReservationRequest;
import com.campinho.campinho.domain.entity.Reservation;
import com.campinho.campinho.domain.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public UUID createReservation(CreateReservationRequest data) {

        // Validação do horário
        if(!isValidReservationTime(data.startTime(), data.endTime()))
            throw new IllegalArgumentException("O horário de inicio deve ser antes do horário de termino");

        // Validação da disponibilidade
        if(findReservationInRange(data.startTime(), data.endTime(),
                reservationRepository.findReservationsNotExpired(LocalDateTime.now())) != null) {
            throw new IllegalArgumentException("O horário desta reserva está chocando com outra");
        }

        var newReservation = new Reservation(data);

        var reservationSaved = reservationRepository.save(newReservation);

        return reservationSaved.getId();
    }

    public Optional<Reservation> getReservationById(String reservationId) {
        try {
            return reservationRepository.findById(UUID.fromString(reservationId));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id inválido");
        }
    }

    public List<Reservation> getActiveReservations() {
        return reservationRepository.findReservationsNotExpired(LocalDateTime.now());
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public void deleteReservationById(String reservationId) {
        var reservation = getReservationById(reservationId);

        if (reservation.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada com o Id: " + reservationId);

        reservationRepository.delete(reservation.get());
    }

    private Reservation findReservationInRange(LocalDateTime startTime, LocalDateTime endTime, List<Reservation> reservations) {
        for(Reservation reservation : reservations)
            if (reservation.getEndTime().isAfter(startTime) && reservation.getStartTime().isBefore(endTime))
                return reservation;

        return null;
    }

    private boolean isValidReservationTime(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.isBefore(endTime);
    }
}
