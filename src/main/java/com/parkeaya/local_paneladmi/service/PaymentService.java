package com.parkeaya.local_paneladmi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkeaya.local_paneladmi.model.dto.PaymentDTO;
import com.parkeaya.local_paneladmi.model.entity.Payment;
import com.parkeaya.local_paneladmi.repository.PaymentRepository;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Listar pagos
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(p -> new PaymentDTO(
                        p.getId(),
                        p.getUsuario(),
                        p.getMonto(),
                        p.getMetodoPago(),
                        p.getFecha(),
                        p.getEstado()
                ))
                .collect(Collectors.toList());
    }

    // Obtener pago por ID
    public Optional<PaymentDTO> getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(p -> new PaymentDTO(
                        p.getId(),
                        p.getUsuario(),
                        p.getMonto(),
                        p.getMetodoPago(),
                        p.getFecha(),
                        p.getEstado()
                ));
    }

    // Crear pago
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Payment payment = new Payment(
                paymentDTO.getUsuario(),
                paymentDTO.getMonto(),
                paymentDTO.getMetodoPago(),
                paymentDTO.getFecha(),
                paymentDTO.getEstado()
        );
        Payment saved = paymentRepository.save(payment);
        return new PaymentDTO(
                saved.getId(),
                saved.getUsuario(),
                saved.getMonto(),
                saved.getMetodoPago(),
                saved.getFecha(),
                saved.getEstado()
        );
    }

    // Actualizar pago
    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
        payment.setUsuario(paymentDTO.getUsuario());
        payment.setMonto(paymentDTO.getMonto());
        payment.setMetodoPago(paymentDTO.getMetodoPago());
        payment.setFecha(paymentDTO.getFecha());
        payment.setEstado(paymentDTO.getEstado());

        Payment updated = paymentRepository.save(payment);
        return new PaymentDTO(
                updated.getId(),
                updated.getUsuario(),
                updated.getMonto(),
                updated.getMetodoPago(),
                updated.getFecha(),
                updated.getEstado()
        );
    }

    // Eliminar pago
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
