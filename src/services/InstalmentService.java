package services;

import models.Instalment;
import models.Contract;
import java.time.LocalDate;
import java.util.List;

public class InstalmentService {

    public boolean createInstalment(int idKontrak, int jumlah, int tenor, LocalDate tanggal, int idStaff) {
        try {
            // 1. Validasi input
            if (!validateInput(idKontrak, jumlah, tenor, tanggal, idStaff)) {
                return false;
            }

            // 2. Validasi business rules
            if (!validateBusinessRules(idKontrak, tenor, tanggal)) {
                return false;
            }

            // 3. Buat cicilan baru
            Instalment cicilan = new Instalment();
            cicilan.setIdKontrak(idKontrak);
            cicilan.setJumlahCicilan(jumlah);
            cicilan.setTenor(tenor);
            cicilan.setTanggalCicilan(tanggal);
            cicilan.setIdStaff(idStaff);

            // 4. Simpan cicilan
            if (!cicilan.save()) {
                return false;
            }

            // 5. Update kontrak payment
            updateContractPayment(idKontrak, jumlah);

            return true;

        } catch (Exception e) {
            System.err.println("Error creating instalment: " + e.getMessage());
            return false;
        }
    }

    public List<Instalment> getAllInstalments() {
        return Instalment.findAll();
    }

    public List<Instalment> getInstalmentsByContract(int idKontrak) {
        return Instalment.findByKontrak(idKontrak);
    }

    public Instalment getInstalmentById(int id) {
        return Instalment.findById(id);
    }


    public int getNextTenor(int idKontrak) {
        return Instalment.getLastTenor(idKontrak) + 1;
    }


    public LocalDate getLastPaymentDate(int idKontrak) {
        return Instalment.getLastPaymentDate(idKontrak);
    }


    public boolean canPayInstalment(int idKontrak, int tenor, LocalDate tanggal) {
        return validateBusinessRules(idKontrak, tenor, tanggal);
    }

    // Private helper methods
    private boolean validateInput(int idKontrak, int jumlah, int tenor, LocalDate tanggal, int idStaff) {
        if (idKontrak <= 0) {
            System.err.println("Invalid contract ID");
            return false;
        }
        
        if (jumlah <= 0) {
            System.err.println("Invalid payment amount");
            return false;
        }
        
        if (tenor <= 0) {
            System.err.println("Invalid tenor");
            return false;
        }
        
        if (tanggal == null) {
            System.err.println("Invalid payment date");
            return false;
        }
        
        if (idStaff <= 0) {
            System.err.println("Invalid staff ID");
            return false;
        }
        
        return true;
    }

    private boolean validateBusinessRules(int idKontrak, int tenor, LocalDate tanggal) {
        // 1. Cek apakah kontrak exist
        Contract contract = Contract.findById(idKontrak);
        if (contract == null) {
            System.err.println("Contract not found");
            return false;
        }

        // 2. Cek apakah kontrak masih aktif
        if (!contract.isStatus()) { // false = lunas
            System.err.println("Contract is already paid off");
            return false;
        }

        // 3. Cek apakah tenor sudah dibayar
        if (Instalment.isTenorPaid(idKontrak, tenor)) {
            System.err.println("Tenor " + tenor + " already paid");
            return false;
        }

        // 4. Cek apakah tenor berurutan
        int lastTenor = Instalment.getLastTenor(idKontrak);
        if (tenor != lastTenor + 1) {
            System.err.println("Tenor must be sequential. Expected: " + (lastTenor + 1) + ", Got: " + tenor);
            return false;
        }

        // 5. Cek apakah tenor tidak melebihi maksimal
        if (tenor > contract.getTenor()) {
            System.err.println("Tenor exceeds contract maximum");
            return false;
        }

        // 6. Validasi tanggal pembayaran
        LocalDate lastPaymentDate = Instalment.getLastPaymentDate(idKontrak);
        if (lastPaymentDate != null) {
            LocalDate minimumDate = lastPaymentDate.plusMonths(1);
            if (tanggal.isBefore(minimumDate)) {
                System.err.println("Payment date must be at least 1 month after last payment");
                return false;
            }
        }

        return true;
    }

    private void updateContractPayment(int idKontrak, int paymentAmount) {
        try {
            Contract contract = Contract.findById(idKontrak);
            if (contract != null) {
                int newTotal = contract.getJumlah_bayar() + paymentAmount;
                boolean isLunas = (Instalment.getLastTenor(idKontrak) >= contract.getTenor());
                
                contract.updatePayment(newTotal, !isLunas); // status true = aktif, false = lunas
            }
        } catch (Exception e) {
            System.err.println("Error updating contract payment: " + e.getMessage());
        }
    }
}