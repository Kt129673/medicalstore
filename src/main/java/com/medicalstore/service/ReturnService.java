package com.medicalstore.service;

import com.medicalstore.model.Return;
import com.medicalstore.model.Sale;
import com.medicalstore.model.SaleItem;
import com.medicalstore.model.Medicine;
import com.medicalstore.repository.ReturnRepository;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.SaleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final MedicineRepository medicineRepository;
    private final SaleItemRepository saleItemRepository;

    public List<Return> getAllReturns() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
        if (tenantId != null)
            return returnRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return returnRepository.findByOwnerId(ownerId);
        return returnRepository.findAll();
    }

    public Optional<Return> getReturnById(Long id) {
        Optional<Return> ret = returnRepository.findById(id);
        if (ret.isPresent() && ret.get().getSale() != null && ret.get().getSale().getBranch() != null) {
            Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(ret.get().getSale().getBranch().getId())) {
                return Optional.empty();
            }
            Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
            if (ownerId != null && ret.get().getSale().getBranch().getOwner() != null
                    && !ownerId.equals(ret.get().getSale().getBranch().getOwner().getId())) {
                return Optional.empty();
            }
        }
        return ret;
    }

    @Transactional
    public Return createReturn(Return returnItem) {
        if (returnItem.getSaleItem() == null || returnItem.getSaleItem().getId() == null) {
            throw new IllegalArgumentException("A specific Sale Item must be selected for return.");
        }

        // Reload SaleItem from DB (form binding only provides the ID, not a managed entity)
        SaleItem item = saleItemRepository.findById(returnItem.getSaleItem().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Sale Item selected."));

        // Re-attach the fully loaded saleItem and its parent sale to the returnItem
        returnItem.setSaleItem(item);
        returnItem.setSale(item.getSale());

        if (returnItem.getReturnQuantity() > item.getQuantity()) {
            throw new IllegalArgumentException(
                    "Return quantity cannot exceed sold quantity (" + item.getQuantity() + ")");
        }

        // Restore medicine stock
        Medicine medicine = item.getMedicine();
        medicine.setQuantity(medicine.getQuantity() + returnItem.getReturnQuantity());
        medicineRepository.save(medicine);

        return returnRepository.save(returnItem);
    }

    public List<Return> getReturnsBySale(Long saleId) {
        return returnRepository.findBySaleId(saleId);
    }

    @Transactional
    public void deleteReturn(Long id) {
        Return returnItem = returnRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Return record not found: " + id));

        // Reverse the stock restoration that was done when return was created
        com.medicalstore.model.SaleItem item = returnItem.getSaleItem();
        if (item != null && item.getMedicine() != null) {
            Medicine medicine = medicineRepository.findById(item.getMedicine().getId())
                    .orElse(null);
            if (medicine != null) {
                int currentQty = medicine.getQuantity();
                int returnedQty = returnItem.getReturnQuantity();
                // Deduct the previously restored quantity (undo the return)
                medicine.setQuantity(Math.max(0, currentQty - returnedQty));
                medicineRepository.save(medicine);
            }
        }

        returnRepository.deleteById(id);
    }
}
