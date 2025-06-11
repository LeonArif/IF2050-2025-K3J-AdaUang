package controllers;

import java.time.LocalDate;
import java.util.List;
import models.Cicilan;
import models.Contract;
import services.InstalmentService; // Pastikan untuk mengimpor model Contract

public class InstalmentController {
    private final InstalmentService instalmentService;

    public InstalmentController() {
        this.instalmentService = new InstalmentService();
    }

    public boolean tambahCicilan(int idKontrak, int jumlah, int tenor, LocalDate tanggal, int idStaff) {
        boolean success = instalmentService.addCicilan(idKontrak, jumlah, tenor, tanggal, idStaff);
        return success;
    }

    public List<Cicilan> getAllCicilan() {
        return instalmentService.getAllCicilan();
    }
    
    public int getNextTenor(int idKontrak) {
        // Bisa menggunakan ContractController atau langsung ke Contract
        Contract contract = Contract.findById(idKontrak);
        return contract != null ? contract.getNextTenor() : 1;
    }
    
    public List<Cicilan> getCicilanByKontrak(int idKontrak) {
        return instalmentService.getCicilanByKontrak(idKontrak);
    }
}
