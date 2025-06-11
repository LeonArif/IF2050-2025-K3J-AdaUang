package services;

import java.util.List;
import models.Contract;

public class ContractService {
    
    public List<Contract> getAllContracts() {
        return Contract.findAll();
    }
    
    public boolean createContract(String namaUser, int total, int tenor, int idUser) {
        Contract contract = new Contract();
        contract.setNama_user(namaUser);
        contract.setTotal(total);
        contract.setTenor(tenor);
        contract.setJumlah_bayar(0);
        contract.setStatus(true);
        contract.setTanggal_pinjam(new java.util.Date());
        contract.setId_user(idUser);
        
        return contract.save();
    }
    
    public Contract getContractById(int id) {
        return Contract.findById(id);
    }

    public boolean updateContract(Contract contract) {
    return contract.save();
}
}