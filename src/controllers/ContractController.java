package controllers;

import java.util.List;
import models.Contract;
import services.ContractService;

public class ContractController extends BaseController {
    
    private final ContractService contractService;

    public ContractController() {
        this.contractService = new ContractService();
    }
    
    public List<Contract> getAllContracts() {
        return contractService.getAllContracts();
    }

    public boolean addContract(String namaUser, int total, int tenor, int idUser) {
        if (namaUser == null || namaUser.trim().isEmpty() || total <= 0 || tenor <= 0) {
            return false;
        }
        return contractService.createContract(namaUser, total, tenor, idUser);
    }

    public Contract getContractById(int id) {
        return contractService.getContractById(id);
    }

    public boolean updateContract(Contract contract) {
    return contractService.updateContract(contract);
}

    public List<Contract> getAllKontrakAktif() {
        return Contract.findAllAktif();
    }
    
    public List<Contract> getAllKontrak() {
        return Contract.findAll();
    }
    
    public Contract getKontrakById(int id) {
        return Contract.findById(id);
    }
    
    public int getNextTenor(int idKontrak) {
        Contract contract = Contract.findById(idKontrak);
        return contract != null ? contract.getNextTenor() : 1;
    }
    
    public boolean updateKontrakPayment(int idKontrak, int newJumlahBayar, boolean isLunas) {
        Contract contract = Contract.findById(idKontrak);
        if (contract != null) {
            return contract.updatePayment(newJumlahBayar, isLunas);
        }
        return false;
    }
}