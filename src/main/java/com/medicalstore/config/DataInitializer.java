package com.medicalstore.config;

import com.medicalstore.model.Branch;
import com.medicalstore.model.Customer;
import com.medicalstore.model.Medicine;
import com.medicalstore.model.Sale;
import com.medicalstore.model.Supplier;
import com.medicalstore.model.User;
import com.medicalstore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Runs on every startup.
 * 1. Ensures admin user exists.
 * 2. Ensures a "default_owner" user exists (credentials printed to console on
 * first run).
 * 3. Ensures a "Default Branch" exists owned by default_owner.
 * 4. Migrates all existing medicines/sales/customers/suppliers with null
 * branch_id
 * to the Default Branch.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // 1. Ensure platform admin — reset password every startup
        User adminUser;
        if (!userRepository.existsByUsername("admin")) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setFullName("Platform Administrator");
            adminUser.setEmail("admin@medicalstore.com");
            adminUser.setRoles(Set.of("ADMIN"));
            log.info("Platform admin created: admin / admin123");
        } else {
            adminUser = userRepository.findByUsername("admin").orElseThrow();
        }
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setEnabled(true);
        adminUser.setAccountNonLocked(true);
        userRepository.save(adminUser);

        // 2. Ensure default owner — reset password every startup
        User defaultOwner;
        if (!userRepository.existsByUsername("default_owner")) {
            defaultOwner = new User();
            defaultOwner.setUsername("default_owner");
            defaultOwner.setFullName("Default Owner");
            defaultOwner.setEmail("defaultowner@medicalstore.com");
            defaultOwner.setRoles(Set.of("OWNER"));
            log.info("Default owner created: default_owner / Owner@123");
        } else {
            defaultOwner = userRepository.findByUsername("default_owner").orElseThrow();
        }
        defaultOwner.setPassword(passwordEncoder.encode("Owner@123"));
        defaultOwner.setEnabled(true);
        defaultOwner.setAccountNonLocked(true);
        defaultOwner = userRepository.save(defaultOwner);

        // 3. Ensure Default Branch
        Branch defaultBranch;
        List<Branch> existing = branchRepository.findByOwnerId(defaultOwner.getId());
        if (existing.isEmpty()) {
            Branch b = new Branch();
            b.setName("Default Branch");
            b.setAddress("Default Address — please update from Admin panel");
            b.setPhone("0000000000");
            b.setGstNumber("DEFAULT-GST");
            b.setIsActive(true);
            b.setOwner(defaultOwner);
            defaultBranch = branchRepository.save(b);
            log.info("Default Branch created with id={}", defaultBranch.getId());
        } else {
            defaultBranch = existing.get(0);
        }

        // 3b. Ensure default shopkeeper assigned to Default Branch — reset password every startup
        User shopkeeper;
        if (!userRepository.existsByUsername("shop1")) {
            shopkeeper = new User();
            shopkeeper.setUsername("shop1");
            shopkeeper.setFullName("Demo Shopkeeper");
            shopkeeper.setEmail("shop1@medicalstore.com");
            shopkeeper.setRoles(Set.of("SHOPKEEPER"));
            log.info("Default shopkeeper created: shop1 / shop123 (branch={})", defaultBranch.getName());
        } else {
            shopkeeper = userRepository.findByUsername("shop1").orElseThrow();
        }
        shopkeeper.setPassword(passwordEncoder.encode("shop123"));
        shopkeeper.setEnabled(true);
        shopkeeper.setAccountNonLocked(true);
        shopkeeper.setBranch(defaultBranch); // always ensure branch is set
        userRepository.save(shopkeeper);

        // 4. Migrate existing data (null branch_id => Default Branch)
        final Branch branch = defaultBranch;
        int migrated = 0;

        List<Medicine> medicines = medicineRepository.findAll()
                .stream().filter(m -> m.getBranch() == null).toList();
        for (Medicine m : medicines) {
            m.setBranch(branch);
            medicineRepository.save(m);
            migrated++;
        }

        List<Customer> customers = customerRepository.findAll()
                .stream().filter(c -> c.getBranch() == null).toList();
        for (Customer c : customers) {
            c.setBranch(branch);
            customerRepository.save(c);
            migrated++;
        }

        List<Supplier> suppliers = supplierRepository.findAll()
                .stream().filter(s -> s.getBranch() == null).toList();
        for (Supplier s : suppliers) {
            s.setBranch(branch);
            supplierRepository.save(s);
            migrated++;
        }

        List<Sale> sales = saleRepository.findAll()
                .stream().filter(s -> s.getBranch() == null).toList();
        for (Sale s : sales) {
            s.setBranch(branch);
            saleRepository.save(s);
            migrated++;
        }

        if (migrated > 0) {
            log.info("Migrated {} existing records to Default Branch", migrated);
        }
    }
}
