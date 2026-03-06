package com.medicalstore.config;

import com.medicalstore.model.Branch;
import com.medicalstore.model.Customer;
import com.medicalstore.model.Medicine;
import com.medicalstore.model.Permission;
import com.medicalstore.model.Sale;
import com.medicalstore.model.SaleItem;
import com.medicalstore.model.SubscriptionFeature;
import com.medicalstore.model.Supplier;
import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.model.User;
import com.medicalstore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PermissionRepository permissionRepository;
    private final SubscriptionFeatureRepository subscriptionFeatureRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // 1. Ensure platform admin — password set only on first creation
        User adminUser;
        if (!userRepository.existsByUsername("admin")) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setFullName("Platform Administrator");
            adminUser.setEmail("admin@medicalstore.com");
            adminUser.setRoles(Set.of("ADMIN"));
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEnabled(true);
            adminUser.setAccountNonLocked(true);
            userRepository.save(adminUser);
            log.info("Platform admin created: admin / admin123");
        } else {
            adminUser = userRepository.findByUsername("admin").orElseThrow();
            boolean changed = false;
            if (adminUser.getPassword() == null || !adminUser.getPassword().startsWith("$2a$")) {
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                changed = true;
                log.info("Upgraded password for admin");
            }
            // Ensure account is not inadvertently locked
            if (!adminUser.getEnabled() || !adminUser.getAccountNonLocked()) {
                adminUser.setEnabled(true);
                adminUser.setAccountNonLocked(true);
                changed = true;
            }
            if (changed) {
                userRepository.save(adminUser);
            }
        }

        // 2. Ensure default owner — password set only on first creation
        User defaultOwner;
        if (!userRepository.existsByUsername("default_owner")) {
            defaultOwner = new User();
            defaultOwner.setUsername("default_owner");
            defaultOwner.setFullName("Default Owner");
            defaultOwner.setEmail("defaultowner@medicalstore.com");
            defaultOwner.setRoles(Set.of("OWNER"));
            defaultOwner.setPassword(passwordEncoder.encode("Owner@123"));
            defaultOwner.setEnabled(true);
            defaultOwner.setAccountNonLocked(true);
            defaultOwner = userRepository.save(defaultOwner);
            log.info("Default owner created: default_owner / Owner@123");
        } else {
            defaultOwner = userRepository.findByUsername("default_owner").orElseThrow();
            boolean changed = false;
            if (defaultOwner.getPassword() == null || !defaultOwner.getPassword().startsWith("$2a$")) {
                defaultOwner.setPassword(passwordEncoder.encode("Owner@123"));
                changed = true;
                log.info("Upgraded password for default_owner");
            }
            if (!defaultOwner.getEnabled() || !defaultOwner.getAccountNonLocked()) {
                defaultOwner.setEnabled(true);
                defaultOwner.setAccountNonLocked(true);
                changed = true;
            }
            if (changed) {
                defaultOwner = userRepository.save(defaultOwner);
            }
        }

        // 2b. Auto-provision FREE subscription for default_owner if missing
        if (subscriptionPlanRepository.findByOwnerId(defaultOwner.getId()).isEmpty()) {
            SubscriptionPlan plan = new SubscriptionPlan();
            plan.setOwner(defaultOwner);
            plan.setPlanType("FREE");
            plan.setExpiryDate(LocalDate.now().plusYears(10));
            plan.setMaxUsers(50);
            plan.setMaxBranches(10);
            plan.setActive(true);
            subscriptionPlanRepository.save(plan);
            log.info("FREE subscription plan created for default_owner");
        }

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

        // 3b. Ensure default shopkeeper assigned to Default Branch — password set only
        // on first creation
        User shopkeeper;
        if (!userRepository.existsByUsername("shop1")) {
            shopkeeper = new User();
            shopkeeper.setUsername("shop1");
            shopkeeper.setFullName("Demo Shopkeeper");
            shopkeeper.setEmail("shop1@medicalstore.com");
            shopkeeper.setRoles(Set.of("SHOPKEEPER"));
            shopkeeper.setPassword(passwordEncoder.encode("shop123"));
            shopkeeper.setEnabled(true);
            shopkeeper.setAccountNonLocked(true);
            shopkeeper.setBranch(defaultBranch);
            userRepository.save(shopkeeper);
            log.info("Default shopkeeper created: shop1 / shop123 (branch={})", defaultBranch.getName());
        } else {
            shopkeeper = userRepository.findByUsername("shop1").orElseThrow();
            boolean changed = false;
            if (shopkeeper.getPassword() == null || !shopkeeper.getPassword().startsWith("$2a$")) {
                shopkeeper.setPassword(passwordEncoder.encode("shop123"));
                changed = true;
                log.info("Upgraded password for shopkeeper");
            }
            if (!shopkeeper.getEnabled() || !shopkeeper.getAccountNonLocked()) {
                shopkeeper.setEnabled(true);
                shopkeeper.setAccountNonLocked(true);
                changed = true;
            }
            // Ensure branch is always assigned
            if (shopkeeper.getBranch() == null) {
                shopkeeper.setBranch(defaultBranch);
                changed = true;
            }
            if (changed) {
                userRepository.save(shopkeeper);
            }
        }

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

        // 5. Seed sample data for testing / bug discovery (idempotent)
        seedSampleData(defaultBranch);

        // 6. Seed fine-grained permission codes (idempotent)
        seedPermissions();

        // 7. Seed subscription feature flags per plan tier (idempotent)
        seedSubscriptionFeatures();

        // 8. Seed additional demo owners with multiple branches and different
        // subscriptions
        seedDemoOwners();
    }

    /**
     * Seeds two additional demo owners demonstrating the full hierarchy:
     * admin → multiple owners → multiple branches per owner → different
     * subscriptions.
     * All operations are idempotent — nothing is duplicated on re-run.
     */
    private void seedDemoOwners() {

        // ── Owner 2: PRO plan, 2 branches ─────────────────────────────────────────
        User ownerPro = ensureOwner("owner_pro", "Amit Sharma",
                "owner.pro@medicalstore.com", "OwnerPro@123");
        ensureSubscription(ownerPro, "PRO", 12, 10, 5);

        Branch b2a = ensureBranch(ownerPro,
                "Sharma Medicals - Main Branch",
                "45 Connaught Place, New Delhi",
                "9811000001", "GST07SHRM0001");
        Branch b2b = ensureBranch(ownerPro,
                "Sharma Medicals - North Branch",
                "12 Kamla Nagar, Delhi",
                "9811000002", "GST07SHRM0002");

        ensureShopkeeper("shop_pro_1", "Shopkeeper Pro 1",
                "shop.pro1@medicalstore.com", "ShopPro@123", b2a);
        ensureShopkeeper("shop_pro_2", "Shopkeeper Pro 2",
                "shop.pro2@medicalstore.com", "ShopPro@123", b2b);

        seedBranchSampleData(b2a, "B2A", 200_000_000L);
        seedBranchSampleData(b2b, "B2B", 200_100_000L);

        // ── Owner 3: ENTERPRISE plan, 3 branches ──────────────────────────────────
        User ownerEnt = ensureOwner("owner_enterprise", "Priya Patel",
                "owner.enterprise@medicalstore.com", "OwnerEnt@123");
        ensureSubscription(ownerEnt, "ENTERPRISE", 24, 30, 20);

        Branch b3a = ensureBranch(ownerEnt,
                "Patel HealthCare - Central Store",
                "101 Marine Drive, Mumbai",
                "9922000001", "GST27PATL0001");
        Branch b3b = ensureBranch(ownerEnt,
                "Patel HealthCare - West Store",
                "78 Linking Road, Mumbai",
                "9922000002", "GST27PATL0002");
        Branch b3c = ensureBranch(ownerEnt,
                "Patel HealthCare - East Store",
                "56 LBS Marg, Mumbai",
                "9922000003", "GST27PATL0003");

        ensureShopkeeper("shop_ent_1", "Shopkeeper Ent 1",
                "shop.ent1@medicalstore.com", "ShopEnt@123", b3a);
        ensureShopkeeper("shop_ent_2", "Shopkeeper Ent 2",
                "shop.ent2@medicalstore.com", "ShopEnt@123", b3b);
        ensureShopkeeper("shop_ent_3", "Shopkeeper Ent 3",
                "shop.ent3@medicalstore.com", "ShopEnt@123", b3c);

        seedBranchSampleData(b3a, "B3A", 300_000_000L);
        seedBranchSampleData(b3b, "B3B", 300_100_000L);
        seedBranchSampleData(b3c, "B3C", 300_200_000L);
    }

    /**
     * Creates an OWNER user if one with the given username does not already exist.
     */
    private User ensureOwner(String username, String fullName, String email, String password) {
        if (!userRepository.existsByUsername(username)) {
            User owner = new User();
            owner.setUsername(username);
            owner.setFullName(fullName);
            owner.setEmail(email);
            owner.setRoles(Set.of("OWNER"));
            owner.setPassword(passwordEncoder.encode(password));
            owner.setEnabled(true);
            owner.setAccountNonLocked(true);
            owner = userRepository.save(owner);
            log.info("Demo owner created: {} / {}", username, password);
            return owner;
        }
        return userRepository.findByUsername(username).orElseThrow();
    }

    /**
     * Creates a SubscriptionPlan for the given owner if one does not already exist.
     */
    private void ensureSubscription(User owner, String planType,
            int monthsUntilExpiry, int maxUsers, int maxBranches) {
        if (subscriptionPlanRepository.findByOwnerId(owner.getId()).isEmpty()) {
            SubscriptionPlan plan = new SubscriptionPlan();
            plan.setOwner(owner);
            plan.setPlanType(planType);
            plan.setExpiryDate(LocalDate.now().plusMonths(monthsUntilExpiry));
            plan.setMaxUsers(maxUsers);
            plan.setMaxBranches(maxBranches);
            plan.setActive(true);
            subscriptionPlanRepository.save(plan);
            log.info("{} subscription plan created for {}", planType, owner.getUsername());
        }
    }

    /**
     * Returns an existing Branch (matched by name + owner) or creates a new one.
     */
    private Branch ensureBranch(User owner, String name, String address,
            String phone, String gstNumber) {
        return branchRepository.findByOwnerId(owner.getId()).stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Branch b = new Branch();
                    b.setName(name);
                    b.setAddress(address);
                    b.setPhone(phone);
                    b.setGstNumber(gstNumber);
                    b.setIsActive(true);
                    b.setOwner(owner);
                    Branch saved = branchRepository.save(b);
                    log.info("Branch created: '{}' for owner '{}'", name, owner.getUsername());
                    return saved;
                });
    }

    /**
     * Creates a SHOPKEEPER user assigned to the given branch if one does not
     * already exist.
     */
    private void ensureShopkeeper(String username, String fullName,
            String email, String password, Branch branch) {
        if (!userRepository.existsByUsername(username)) {
            User sk = new User();
            sk.setUsername(username);
            sk.setFullName(fullName);
            sk.setEmail(email);
            sk.setRoles(Set.of("SHOPKEEPER"));
            sk.setPassword(passwordEncoder.encode(password));
            sk.setEnabled(true);
            sk.setAccountNonLocked(true);
            sk.setBranch(branch);
            userRepository.save(sk);
            log.info("Demo shopkeeper created: {} (branch={})", username, branch.getName());
        }
    }

    /**
     * Seeds a representative set of medicines, customers and sales for any branch.
     * All barcodes are namespaced with {@code prefix} to remain globally unique.
     * Phone numbers are derived from {@code phoneBase} to avoid conflicts.
     * All operations are idempotent.
     */
    private void seedBranchSampleData(Branch branch, String prefix, long phoneBase) {

        // --- Suppliers ---
        Supplier sup1 = seedSupplier(branch, "Sun Pharma", "Raj Kumar",
                "9001000001", "sunpharma@example.com", "Mumbai, Maharashtra", "GST27SUNPH0001");
        Supplier sup2 = seedSupplier(branch, "Cipla Ltd", "Anita Sharma",
                "9001000002", "cipla@example.com", "Pune, Maharashtra", "GST27CIPLA0001");

        // --- Medicines: normal stock ---
        seedMedicine(branch, sup1, "Paracetamol 500mg", "Analgesics",
                "Sun Pharma", 10.0, 200, LocalDate.now().plusYears(2),
                prefix + "-BATCH-P001", prefix + "-BC-001", "30049099", "Paracetamol", 7.0, 12.0, 5.0, "OTC");
        seedMedicine(branch, sup1, "Amoxicillin 250mg", "Antibiotics",
                "Sun Pharma", 45.0, 150, LocalDate.now().plusYears(1),
                prefix + "-BATCH-A001", prefix + "-BC-002", "30041000", "Amoxicillin Trihydrate", 35.0, 55.0, 12.0,
                "H");
        seedMedicine(branch, sup2, "Cetirizine 10mg", "Antihistamines",
                "Cipla Ltd", 8.0, 300, LocalDate.now().plusYears(2),
                prefix + "-BATCH-C001", prefix + "-BC-003", "30045090", "Cetirizine Hydrochloride", 5.0, 10.0, 5.0,
                "OTC");
        seedMedicine(branch, sup2, "Metformin 500mg", "Antidiabetic",
                "Cipla Ltd", 25.0, 180, LocalDate.now().plusYears(2),
                prefix + "-BATCH-M001", prefix + "-BC-004", "30046000", "Metformin Hydrochloride", 18.0, 30.0, 5.0,
                "H");

        // --- Medicine: low stock ---
        seedMedicine(branch, sup1, "Azithromycin 500mg", "Antibiotics",
                "Sun Pharma", 85.0, 4, LocalDate.now().plusYears(1),
                prefix + "-BATCH-AZ001", prefix + "-BC-005", "30041000", "Azithromycin", 60.0, 100.0, 12.0, "H");

        // --- Medicine: near-expiry ---
        seedMedicine(branch, sup2, "Vitamin C 500mg", "Vitamins",
                "Cipla Ltd", 15.0, 50, LocalDate.now().plusDays(25),
                prefix + "-BATCH-VTC001", prefix + "-BC-006", "30049099", "Ascorbic Acid", 10.0, 18.0, 5.0, "OTC");

        // --- Customers (phone numbers derived from phoneBase to stay unique) ---
        Customer c1 = seedCustomer(branch, "Ramesh Patel", null,
                branchPhone(phoneBase, 1), "12 Main Road, Bangalore",
                LocalDate.of(1985, 6, 15));
        Customer c2 = seedCustomer(branch, "Priya Singh", null,
                branchPhone(phoneBase, 2), "45 Park Street, Kolkata",
                LocalDate.of(1990, 3, 22));

        // --- Sales ---
        if (saleRepository.countByBranchId(branch.getId()) == 0) {
            Medicine med1 = medicineRepository.findByBarcode(prefix + "-BC-001").orElse(null);
            Medicine med3 = medicineRepository.findByBarcode(prefix + "-BC-003").orElse(null);
            Medicine med4 = medicineRepository.findByBarcode(prefix + "-BC-004").orElse(null);
            if (med1 != null && med3 != null) {
                createSampleSale(branch, c1, "Cash", 0.0, 5.0,
                        List.of(new SaleItemData(med1, 3, 10.0, 7.0),
                                new SaleItemData(med3, 2, 8.0, 5.0)));
            }
            if (med4 != null) {
                createSampleSale(branch, c2, "UPI", 5.0, 12.0,
                        List.of(new SaleItemData(med4, 1, 25.0, 18.0)));
            }
            log.info("Sample sales created for branch '{}'", branch.getName());
        }

        log.info("Branch sample data seed complete for '{}'", branch.getName());
    }

    /**
     * Builds a 10-digit Indian mobile number from a base value and a per-customer
     * offset.
     */
    private static String branchPhone(long phoneBase, int offset) {
        return String.format("9%09d", phoneBase + offset);
    }

    /**
     * Seeds sample suppliers, medicines, customers, and sales for the Default
     * Branch
     * so that all application features can be exercised and bugs discovered.
     * All operations are idempotent — existing records are never duplicated.
     */
    private void seedSampleData(Branch branch) {

        // --- Suppliers ---
        Supplier sunPharma = seedSupplier(branch, "Sun Pharma", "Raj Kumar",
                "9001000001", "sunpharma@example.com", "Mumbai, Maharashtra", "GST27SUNPH0001");
        Supplier cipla = seedSupplier(branch, "Cipla Ltd", "Anita Sharma",
                "9001000002", "cipla@example.com", "Pune, Maharashtra", "GST27CIPLA0001");
        Supplier drReddy = seedSupplier(branch, "Dr. Reddy's Labs", "Suresh Reddy",
                "9001000003", "drreddy@example.com", "Hyderabad, Telangana", "GST36DRRDY0001");

        // --- Medicines: normal stock ---
        seedMedicine(branch, sunPharma, "Paracetamol 500mg", "Analgesics",
                "Sun Pharma", 10.0, 200, LocalDate.now().plusYears(2),
                "BATCH-P001", "MED-BC-001", "30049099", "Paracetamol", 7.0, 12.0, 5.0, "OTC");
        seedMedicine(branch, sunPharma, "Amoxicillin 250mg", "Antibiotics",
                "Sun Pharma", 45.0, 150, LocalDate.now().plusYears(1),
                "BATCH-A001", "MED-BC-002", "30041000", "Amoxicillin Trihydrate", 35.0, 55.0, 12.0, "H");
        seedMedicine(branch, cipla, "Cetirizine 10mg", "Antihistamines",
                "Cipla Ltd", 8.0, 300, LocalDate.now().plusYears(2),
                "BATCH-C001", "MED-BC-003", "30045090", "Cetirizine Hydrochloride", 5.0, 10.0, 5.0, "OTC");
        seedMedicine(branch, cipla, "Metformin 500mg", "Antidiabetic",
                "Cipla Ltd", 25.0, 180, LocalDate.now().plusYears(2),
                "BATCH-M001", "MED-BC-004", "30046000", "Metformin Hydrochloride", 18.0, 30.0, 5.0, "H");
        seedMedicine(branch, drReddy, "Omeprazole 20mg", "Antacids",
                "Dr. Reddy's Labs", 30.0, 120, LocalDate.now().plusMonths(18),
                "BATCH-O001", "MED-BC-005", "30049099", "Omeprazole", 22.0, 38.0, 12.0, "H");
        seedMedicine(branch, drReddy, "Atorvastatin 10mg", "Statins",
                "Dr. Reddy's Labs", 55.0, 90, LocalDate.now().plusMonths(20),
                "BATCH-AT001", "MED-BC-006", "30046000", "Atorvastatin Calcium", 40.0, 65.0, 12.0, "H");

        // --- Medicines: low stock (to exercise low-stock alerts) ---
        seedMedicine(branch, sunPharma, "Azithromycin 500mg", "Antibiotics",
                "Sun Pharma", 85.0, 5, LocalDate.now().plusYears(1),
                "BATCH-AZ001", "MED-BC-007", "30041000", "Azithromycin", 60.0, 100.0, 12.0, "H");
        seedMedicine(branch, cipla, "Pantoprazole 40mg", "Antacids",
                "Cipla Ltd", 35.0, 3, LocalDate.now().plusMonths(16),
                "BATCH-PAN001", "MED-BC-008", "30049099", "Pantoprazole Sodium", 25.0, 42.0, 12.0, "H");

        // --- Medicine: near-expiry (to exercise expiry alerts) ---
        seedMedicine(branch, drReddy, "Vitamin C 500mg", "Vitamins",
                "Dr. Reddy's Labs", 15.0, 50, LocalDate.now().plusDays(20),
                "BATCH-VTC001", "MED-BC-009", "30049099", "Ascorbic Acid", 10.0, 18.0, 5.0, "OTC");

        // --- Medicine: out-of-stock ---
        seedMedicine(branch, sunPharma, "Dolo 650", "Analgesics",
                "Sun Pharma", 12.0, 0, LocalDate.now().plusYears(2),
                "BATCH-DL001", "MED-BC-010", "30049099", "Paracetamol 650mg", 8.0, 15.0, 5.0, "OTC");

        // --- Customers ---
        Customer c1 = seedCustomer(branch, "Ramesh Patel", "ramesh.patel@example.com",
                "9100000001", "12 MG Road, Bangalore", LocalDate.of(1985, 6, 15));
        Customer c2 = seedCustomer(branch, "Priya Singh", "priya.singh@example.com",
                "9100000002", "45 Park Street, Kolkata", LocalDate.of(1990, 3, 22));
        Customer c3 = seedCustomer(branch, "Suresh Kumar", null,
                "9100000003", "78 Anna Salai, Chennai", LocalDate.of(1975, 11, 8));

        // --- Sales (only when the branch has no existing sales) ---
        if (saleRepository.countByBranchId(branch.getId()) == 0) {
            Medicine med1 = medicineRepository.findByBarcode("MED-BC-001").orElse(null);
            Medicine med2 = medicineRepository.findByBarcode("MED-BC-002").orElse(null);
            Medicine med3 = medicineRepository.findByBarcode("MED-BC-003").orElse(null);
            Medicine med4 = medicineRepository.findByBarcode("MED-BC-004").orElse(null);
            Medicine med5 = medicineRepository.findByBarcode("MED-BC-005").orElse(null);

            if (med1 != null && med3 != null) {
                createSampleSale(branch, c1, "Cash", 0.0, 5.0,
                        List.of(new SaleItemData(med1, 3, 10.0, 7.0),
                                new SaleItemData(med3, 2, 8.0, 5.0)));
            }
            if (med2 != null && med4 != null) {
                createSampleSale(branch, c2, "Card", 5.0, 12.0,
                        List.of(new SaleItemData(med2, 2, 45.0, 35.0),
                                new SaleItemData(med4, 1, 25.0, 18.0)));
            }
            if (med5 != null) {
                createSampleSale(branch, c3, "UPI", 0.0, 12.0,
                        List.of(new SaleItemData(med5, 1, 30.0, 22.0)));
            }
            if (med1 != null && med5 != null) {
                createSampleSale(branch, null, "Cash", 10.0, 5.0,
                        List.of(new SaleItemData(med1, 5, 10.0, 7.0),
                                new SaleItemData(med5, 2, 30.0, 22.0)));
            }
            log.info("Sample sales created for Default Branch");
        }

        log.info("Sample data seed complete for branch '{}'", branch.getName());
    }

    private Supplier seedSupplier(Branch branch, String name, String contactPerson,
            String phone, String email, String address, String gstNumber) {
        boolean exists = supplierRepository.findByBranchId(branch.getId())
                .stream().anyMatch(s -> s.getName().equalsIgnoreCase(name));
        if (!exists) {
            Supplier s = new Supplier();
            s.setName(name);
            s.setContactPerson(contactPerson);
            s.setPhone(phone);
            s.setEmail(email);
            s.setAddress(address);
            s.setGstNumber(gstNumber);
            s.setBranch(branch);
            return supplierRepository.save(s);
        }
        return supplierRepository.findByBranchId(branch.getId())
                .stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElseThrow();
    }

    private void seedMedicine(Branch branch, Supplier supplier, String name, String category,
            String manufacturer, Double price, Integer quantity, LocalDate expiryDate,
            String batchNumber, String barcode, String hsnCode, String saltComposition,
            Double purchasePrice, Double mrp, Double gstPercentage, String scheduleType) {
        if (medicineRepository.findByBarcode(barcode).isEmpty()) {
            Medicine m = new Medicine();
            m.setName(name);
            m.setCategory(category);
            m.setManufacturer(manufacturer);
            m.setPrice(price);
            m.setQuantity(quantity);
            m.setExpiryDate(expiryDate);
            m.setBatchNumber(batchNumber);
            m.setBarcode(barcode);
            m.setHsnCode(hsnCode);
            m.setSaltComposition(saltComposition);
            m.setPurchasePrice(purchasePrice);
            m.setMrp(mrp);
            m.setGstPercentage(gstPercentage);
            m.setScheduleType(scheduleType);
            m.setBranch(branch);
            m.setSupplier(supplier);
            medicineRepository.save(m);
        }
    }

    private Customer seedCustomer(Branch branch, String name, String email, String phone,
            String address, LocalDate dateOfBirth) {
        return customerRepository.findByPhone(phone).orElseGet(() -> {
            Customer c = new Customer();
            c.setName(name);
            c.setEmail(email);
            c.setPhone(phone);
            c.setAddress(address);
            c.setDateOfBirth(dateOfBirth);
            c.setLoyaltyPoints(0);
            c.setBranch(branch);
            return customerRepository.save(c);
        });
    }

    /** Holds data for a single line-item in a sample sale. */
    private record SaleItemData(Medicine medicine, int quantity, double unitPrice, double costPrice) {
    }

    private void createSampleSale(Branch branch, Customer customer, String paymentMethod,
            Double discountPercentage, Double gstPercentage, List<SaleItemData> itemData) {
        Sale sale = new Sale();
        sale.setBranch(branch);
        sale.setCustomer(customer);
        sale.setPaymentMethod(paymentMethod);
        sale.setDiscountPercentage(discountPercentage);
        sale.setGstPercentage(gstPercentage);
        for (SaleItemData row : itemData) {
            SaleItem si = new SaleItem();
            si.setMedicine(row.medicine());
            si.setQuantity(row.quantity());
            si.setUnitPrice(row.unitPrice());
            si.setCostPrice(row.costPrice());
            sale.addItem(si);
        }
        saleRepository.save(sale);
    }

    /**
     * Idempotent seed of the fine-grained permission matrix.
     * Each entry is only inserted when the code does not yet exist.
     * Hierarchy: ADMIN inherits OWNER which inherits SHOPKEEPER,
     * so permissions granted to OWNER are also implicitly available to ADMIN
     * at the service/controller level — but we store them explicitly here
     * for correctness and clarity.
     */
    private void seedPermissions() {
        // permission code → roles that are directly granted it
        Map<String, Set<String>> matrix = Map.ofEntries(
                Map.entry("MEDICINE_DELETE", Set.of("ADMIN", "OWNER")),
                Map.entry("MEDICINE_BULK_IMPORT", Set.of("ADMIN", "OWNER")),
                Map.entry("USER_DELETE", Set.of("ADMIN")),
                Map.entry("USER_RESTORE", Set.of("ADMIN")),
                Map.entry("CUSTOMER_DELETE", Set.of("ADMIN", "OWNER")),
                Map.entry("REPORT_EXPORT_EXCEL", Set.of("ADMIN", "OWNER")),
                Map.entry("REPORT_VIEW_ANALYTICS", Set.of("ADMIN", "OWNER", "SHOPKEEPER")),
                Map.entry("SALE_DELETE", Set.of("ADMIN")),
                Map.entry("INVOICE_PRINT", Set.of("ADMIN", "OWNER", "SHOPKEEPER")),
                Map.entry("SUPPLIER_MANAGE", Set.of("ADMIN", "OWNER")),
                Map.entry("BULK_EXPORT", Set.of("ADMIN", "OWNER")));

        int seeded = 0;
        for (var entry : matrix.entrySet()) {
            if (!permissionRepository.existsByCode(entry.getKey())) {
                Permission p = Permission.builder()
                        .code(entry.getKey())
                        .description(entry.getKey().replace('_', ' ').toLowerCase(java.util.Locale.ROOT))
                        .roles(new java.util.HashSet<>(entry.getValue()))
                        .build();
                permissionRepository.save(p);
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("Seeded {} permission(s) into the permission matrix", seeded);
        }
    }

    /**
     * Idempotent seed of the subscription feature matrix.
     * Plan tiers: FREE, PRO, ENTERPRISE
     */
    private void seedSubscriptionFeatures() {
        // feature code → plan tiers that include it
        Map<String, Set<String>> featureMatrix = Map.ofEntries(
                Map.entry("INVOICE_PRINT", Set.of("FREE", "PRO", "ENTERPRISE")),
                Map.entry("BASIC_REPORTS", Set.of("FREE", "PRO", "ENTERPRISE")),
                Map.entry("ADVANCED_ANALYTICS", Set.of("PRO", "ENTERPRISE")),
                Map.entry("EXCEL_EXPORT", Set.of("PRO", "ENTERPRISE")),
                Map.entry("BULK_EXPORT", Set.of("ENTERPRISE")),
                Map.entry("API_ACCESS", Set.of("ENTERPRISE")));

        int seeded = 0;
        for (var entry : featureMatrix.entrySet()) {
            String featureCode = entry.getKey();
            for (String planType : entry.getValue()) {
                if (!subscriptionFeatureRepository.existsByPlanTypeAndFeatureCode(planType, featureCode)) {
                    subscriptionFeatureRepository.save(
                            SubscriptionFeature.builder()
                                    .planType(planType)
                                    .featureCode(featureCode)
                                    .build());
                    seeded++;
                }
            }
        }
        if (seeded > 0) {
            log.info("Seeded {} subscription feature flag(s)", seeded);
        }
    }
}
