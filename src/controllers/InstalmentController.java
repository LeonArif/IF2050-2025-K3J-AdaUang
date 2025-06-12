package controllers;

import models.Instalment;
import services.InstalmentService;
import java.time.LocalDate;
import java.util.List;

public class InstalmentController extends BaseController {
    
    private final InstalmentService instalmentService;

    public InstalmentController() {
        this.instalmentService = new InstalmentService();
    }


    public boolean addInstalment(int idKontrak, int jumlah, int tenor, LocalDate tanggal, int idStaff) {
        try {
            return instalmentService.createInstalment(idKontrak, jumlah, tenor, tanggal, idStaff);
        } catch (Exception e) {
            System.err.println("Controller error adding instalment: " + e.getMessage());
            return false;
        }
    }


    public List<Instalment> getAllInstalments() {
        return instalmentService.getAllInstalments();
    }


    public List<Instalment> getInstalmentsByContract(int idKontrak) {
        return instalmentService.getInstalmentsByContract(idKontrak);
    }


    public Instalment getInstalmentById(int id) {
        return instalmentService.getInstalmentById(id);
    }


    public int getNextTenor(int idKontrak) {
        return instalmentService.getNextTenor(idKontrak);
    }


    public LocalDate getLastPaymentDate(int idKontrak) {
        return instalmentService.getLastPaymentDate(idKontrak);
    }


    public boolean validatePayment(int idKontrak, int tenor, LocalDate tanggal) {
        return instalmentService.canPayInstalment(idKontrak, tenor, tanggal);
    }


    public LocalDate getMinimumPaymentDate(int idKontrak) {
        LocalDate lastDate = instalmentService.getLastPaymentDate(idKontrak);
        return lastDate != null ? lastDate.plusMonths(1) : LocalDate.now();
    }

}