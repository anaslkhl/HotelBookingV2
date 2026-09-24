package service;

import model.enums.PaymentStatus;
import util.ReservationValidation;
import model.*;
import exception.*;
import model.enums.ReservationStatus;
import model.enums.RoomStatus;
import repository.ClientRepository;
import repository.ReservationRepository;
import repository.RoomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class ReservationService {



    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final RoomRepository roomRepository;
    private final PaymentService paymentService;


    public ReservationService(ReservationRepository reservationRepository, ClientRepository clientRepository, RoomRepository roomRepository, PaymentService paymentService){
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.roomRepository = roomRepository;
        this.paymentService = paymentService;
    }



    public Reservation ajouterReservation(UUID userId, String roomNumber, LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {

        if (!ReservationValidation.validateUser(userId) ||
                !ReservationValidation.validateRoom(roomNumber) ||
                !ReservationValidation.numberOfGuests(numberOfGuests) ||
                !ReservationValidation.validateCheckIn(checkIn) ||
                !ReservationValidation.validateCheckOut(checkOut)) {

            return null;
        }
        Client client = clientRepository.findById(userId);
        Room room = roomRepository.getByNumber(roomNumber);


        if(client == null){
            throw new ClientNotFoundException("Client not found with this id" + userId);
        }
        if(room == null){
            throw new RoomNotFoundException("Room not found !");
        }

        if (room.getCapacity() < numberOfGuests) {

            throw new RoomCapacityException("This room capacity is insufficient ! ");
        }

        if (!checkOut.isAfter(checkIn)) {

            throw new InvalidReservationInfoException("Check-out date must be after check-in date ! ");
        }

        for (Reservation res : reservationRepository.getAllReservations()) {

            if (res.getRoomNumber().equals(roomNumber)
                    && res.getStatus().equals(ReservationStatus.CONFIRMED)) {

                boolean overlap = checkIn.isBefore(res.getCheckOut()) && checkOut.isAfter(res.getCheckIn());

                if (overlap) {
                    throw new RoomNotAvailableException("This room is not available for the selected dates ! ");
                }
            }
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);

        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

        Reservation reservation = new Reservation(
                userId,
                roomNumber,
                checkIn,
                checkOut,
                numberOfGuests,
                numberOfNights,
                totalPrice
        );

        reservation.setTotalPrice(totalPrice);

        reservationRepository.save(reservation);

        Payment payment = new Payment(reservation.getId(), reservation.getTotalPrice(), PaymentStatus.PENDING, null, null);
        paymentService.save(payment);

        room.setStatus(RoomStatus.MAINTENANCE);
        roomRepository.save(room.getRoomNumber(), room.getType(), room.getStatus(), room.getCapacity(), room.getPricePerNight());

        System.out.println(" << Reservation created successfully >>");
        return reservation;
    }


    public List<Reservation> findByUserId(UUID userId){

        return reservationRepository.findByUserId(userId);
    }


    public void afficherReservation(List<Reservation> reservations) {

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("Reservation not found ! ");
        }

        System.out.println("\n==================== MES RESERVATIONS ====================");

        for (Reservation reservation : reservations) {

            System.out.println("---------------------------------------------");
            System.out.println("Reservation ID : " + reservation.getId());
            System.out.println("Chambre        : " + reservation.getRoomNumber());
            System.out.println("Date debut     : " + reservation.getCheckIn());
            System.out.println("Date fin       : " + reservation.getCheckOut());
            System.out.println("Statut         : " + reservation.getStatus());
        }

        System.out.println("===========================================================\n");
    }

    public Reservation getReservationShortId(String id){

        List<Reservation> reservations = reservationRepository.getAllReservations();
        if(reservations.isEmpty() || reservations == null){
            throw new ReservationNotFoundException("Reservation not found !");
        }

        for(Reservation res : reservations){
            if(res.getId().toString().startsWith(id)){
                return res;
            }
        }
        return null;
    }


    public Reservation reservationUpdate(UUID reservationID, UUID userId, LocalDate checkIn,
                                         LocalDate checkOut, String roomId){


        Reservation reservation = reservationRepository.getByUuid(reservationID);

        if(reservation == null){

            throw new ReservationNotFoundException("There is no reservation with that ID !!" + reservationID);
        }

        if (!reservation.getUserId().equals(userId)) {
            System.out.println("You cannot modify this reservation!");
            return null;
        }

        if (!reservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
            System.out.println("This reservation cannot be modified!");
            return null;
        }

        if(!ReservationValidation.validateCheckIn(checkIn) || !ReservationValidation.validateCheckOut(checkOut)){

            System.out.println("You have to enter a valide date !! ");
            return null;
        }

        Room room = roomRepository.getByNumber(roomId);

        if(room == null){
            throw new RoomNotFoundException("Room not found with this ID : " + roomId + '!');
        }

        if (room.getCapacity() < reservation.getNumberOfGuests()) {

            throw new RoomCapacityException("This room capacity is insufficient !");
        }

        if(!room.getStatus().equals(RoomStatus.AVAILABLE)){
            throw new RoomNotAvailableException("This room is not available !! ");
        }

        for(Reservation res : reservationRepository.getAllReservations()){

            if(res.getId().equals(reservationID)){

                continue;
            }
            if(res.getRoomNumber().equals(roomId)){

                boolean overlap = checkIn.isBefore(res.getCheckOut()) && checkOut.isAfter(res.getCheckIn());

                if (overlap){
                    System.out.println("This room is already reserved for these dates!!");
                    return null;
                }
            }
        }

        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

        String oldRoomNumber = reservation.getRoomNumber();

        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setRoomNumber(roomId);
        reservation.setNumberOfNights(numberOfNights);
        reservation.setTotalPrice(totalPrice);

        if (!oldRoomNumber.equals(roomId)) {

            room.setStatus(RoomStatus.MAINTENANCE);
            roomRepository.save(room.getRoomNumber(), room.getType(), room.getStatus(), room.getCapacity(), room.getPricePerNight());

            Room oldRoom = roomRepository.getByNumber(oldRoomNumber);

            if (oldRoom != null) {
                oldRoom.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(oldRoom.getRoomNumber(), oldRoom.getType(), oldRoom.getStatus(), oldRoom.getCapacity(), oldRoom.getPricePerNight());
            }
        }

        reservationRepository.save(reservation);
        System.out.println(" <<<<< Reservation updated successfully >>>>");

        return reservation;
    }

    public Reservation reservationCancelation(UUID reservId, UUID userId){

        Reservation reservation = reservationRepository.getByUuid(reservId);
        Client client = clientRepository.findById(userId);

        if(reservation == null){
            throw new ReservationNotFoundException("Reservation not found whit this ID : " + reservId);
        }

        if(!reservation.getUserId().equals(userId)){

            System.out.println("You can't cancel this reservation !!");
            return null;
        }

        if(!reservation.getStatus().equals(ReservationStatus.CONFIRMED)){

            System.out.println("You cannot cancel this reservation !! ");
            return null;
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        Room room = roomRepository.getByNumber(reservation.getRoomNumber());

        if(room != null){

            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room.getRoomNumber(),  room.getType() , room.getStatus(), room.getCapacity(), room.getPricePerNight());
        }

        reservationRepository.save(reservation);
        System.out.println(" << Reservation canceled successfully >> ");
        return reservation;
    }

}

