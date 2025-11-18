package com.medicalstore.config;

import com.medicalstore.model.*;
import com.medicalstore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MedicineRepository medicineRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;
    
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            initializeUsers();
            initializeSuppliers();
            initializeMedicines();
            initializeCustomers();
            initializeSales();
        };
    }
    
    private void initializeUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setEmail("admin@medicalstore.com");
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            admin.setRoles(Set.of("ADMIN", "USER"));
            admin.setCreatedDate(LocalDateTime.now());
            admin.setCreatedBy("SYSTEM");
            
            userRepository.save(admin);
            log.info("✅ Admin user created - Username: admin, Password: admin123");
        }
    }
    
    private void initializeSuppliers() {
        if (supplierRepository.count() == 0) {
            List<Supplier> suppliers = new ArrayList<>();
            
            suppliers.add(createSupplier("Apollo Pharmaceuticals", "Rajesh Kumar", "9876543210", 
                "rajesh@apollo-pharma.com", "29ABCDE1234F1Z5", "123 Medical Plaza, Mumbai"));
            
            suppliers.add(createSupplier("MediCare Distributors", "Priya Sharma", "9876543211", 
                "priya@medicare-dist.com", "27FGHIJ5678K2L3", "456 Health Street, Delhi"));
            
            suppliers.add(createSupplier("HealthPlus Suppliers", "Amit Patel", "9876543212", 
                "amit@healthplus.com", "24MNOPQ9012R3S4", "789 Wellness Road, Bangalore"));
            
            suppliers.add(createSupplier("Global Meds India", "Sunita Reddy", "9876543213", 
                "sunita@globalmeds.in", "36TUVWX3456Y5Z6", "321 Pharma Hub, Hyderabad"));
            
            suppliers.add(createSupplier("PharmaLink Solutions", "Vijay Singh", "9876543214", 
                "vijay@pharmalink.com", "22ABCDE7890F7G8", "654 Medical Center, Pune"));
            
            supplierRepository.saveAll(suppliers);
            log.info("✅ {} suppliers created", suppliers.size());
        }
    }
    
    private Supplier createSupplier(String name, String contactPerson, String phone, 
                                   String email, String gstNumber, String address) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setGstNumber(gstNumber);
        supplier.setAddress(address);
        return supplier;
    }
    
    private void initializeMedicines() {
        if (medicineRepository.count() == 0) {
            List<Supplier> suppliers = supplierRepository.findAll();
            if (suppliers.isEmpty()) return;
            
            List<Medicine> medicines = new ArrayList<>();
            Random random = new Random();
            
            // Pain Relief & Fever
            medicines.add(createMedicine("Paracetamol 500mg", "Pain Relief", "Apollo Pharma", 
                150, 5.00, "BATCH001", LocalDate.now().plusMonths(18)));
            
            medicines.add(createMedicine("Dolo 650", "Pain Relief", "Micro Labs", 
                200, 8.50, "BATCH002", LocalDate.now().plusMonths(24)));
            
            medicines.add(createMedicine("Crocin Advance", "Pain Relief", "GSK Pharma", 
                180, 12.00, "BATCH003", LocalDate.now().plusMonths(20)));
            
            medicines.add(createMedicine("Ibuprofen 400mg", "Pain Relief", "Abbott", 
                120, 15.00, "BATCH004", LocalDate.now().plusMonths(22)));
            
            medicines.add(createMedicine("Combiflam", "Pain Relief", "Sanofi India", 
                90, 18.50, "BATCH005", LocalDate.now().plusMonths(15)));
            
            // Antibiotics
            medicines.add(createMedicine("Azithromycin 500mg", "Antibiotic", "Cipla", 
                80, 45.00, "BATCH006", LocalDate.now().plusMonths(12)));
            
            medicines.add(createMedicine("Amoxicillin 500mg", "Antibiotic", "Dr Reddy's", 
                100, 35.00, "BATCH007", LocalDate.now().plusMonths(14)));
            
            medicines.add(createMedicine("Ciprofloxacin 500mg", "Antibiotic", "Sun Pharma", 
                70, 40.00, "BATCH008", LocalDate.now().plusMonths(16)));
            
            // Cold & Cough
            medicines.add(createMedicine("Sinarest Tablet", "Cold & Cough", "Centaur Pharma", 
                160, 22.00, "BATCH009", LocalDate.now().plusMonths(18)));
            
            medicines.add(createMedicine("Benadryl Cough Syrup", "Cold & Cough", "Johnson & Johnson", 
                50, 85.00, "BATCH010", LocalDate.now().plusMonths(12)));
            
            medicines.add(createMedicine("Vicks Vaporub", "Cold & Cough", "P&G Health", 
                75, 120.00, "BATCH011", LocalDate.now().plusMonths(36)));
            
            // Digestive Health
            medicines.add(createMedicine("Digene Tablet", "Antacid", "Abbott", 
                140, 10.00, "BATCH012", LocalDate.now().plusMonths(24)));
            
            medicines.add(createMedicine("Eno Sachet", "Antacid", "GSK Consumer", 
                200, 5.00, "BATCH013", LocalDate.now().plusMonths(30)));
            
            medicines.add(createMedicine("Gelusil Syrup", "Antacid", "Pfizer", 
                60, 95.00, "BATCH014", LocalDate.now().plusMonths(18)));
            
            // Diabetes
            medicines.add(createMedicine("Metformin 500mg", "Diabetes", "USV Pharma", 
                250, 6.50, "BATCH015", LocalDate.now().plusMonths(20)));
            
            medicines.add(createMedicine("Glimepiride 2mg", "Diabetes", "Torrent Pharma", 
                180, 12.00, "BATCH016", LocalDate.now().plusMonths(22)));
            
            // Cardiovascular
            medicines.add(createMedicine("Amlodipine 5mg", "Blood Pressure", "Macleods Pharma", 
                200, 8.00, "BATCH017", LocalDate.now().plusMonths(24)));
            
            medicines.add(createMedicine("Atorvastatin 10mg", "Cholesterol", "Ranbaxy Labs", 
                150, 15.00, "BATCH018", LocalDate.now().plusMonths(18)));
            
            // Vitamins & Supplements
            medicines.add(createMedicine("Vitamin D3 60K", "Vitamin", "Mankind Pharma", 
                100, 35.00, "BATCH019", LocalDate.now().plusMonths(24)));
            
            medicines.add(createMedicine("Becosules Capsules", "Multivitamin", "Pfizer", 
                120, 25.00, "BATCH020", LocalDate.now().plusMonths(20)));
            
            medicines.add(createMedicine("Calcium + Vitamin D3", "Supplement", "Cipla Health", 
                90, 45.00, "BATCH021", LocalDate.now().plusMonths(22)));
            
            // Skin Care
            medicines.add(createMedicine("Betnovate Cream", "Skin Care", "GSK Pharma", 
                40, 85.00, "BATCH022", LocalDate.now().plusMonths(18)));
            
            medicines.add(createMedicine("Lacto Calamine", "Skin Care", "Piramal Healthcare", 
                55, 120.00, "BATCH023", LocalDate.now().plusMonths(24)));
            
            // Low Stock Items (for alerts demo)
            medicines.add(createMedicine("Aspirin 75mg", "Blood Thinner", "Bayer", 
                8, 10.00, "BATCH024", LocalDate.now().plusMonths(12)));
            
            medicines.add(createMedicine("Pantoprazole 40mg", "Antacid", "Alkem Labs", 
                5, 18.00, "BATCH025", LocalDate.now().plusMonths(15)));
            
            medicines.add(createMedicine("Levocetrizine 5mg", "Allergy", "Glenmark", 
                3, 8.00, "BATCH026", LocalDate.now().plusMonths(18)));
            
            // Expiring Soon
            medicines.add(createMedicine("Cetirizine 10mg", "Allergy", "Sun Pharma", 
                45, 6.00, "BATCH027", LocalDate.now().plusDays(25)));
            
            medicines.add(createMedicine("Domperidone 10mg", "Digestive", "Cipla", 
                35, 12.00, "BATCH028", LocalDate.now().plusDays(20)));
            
            medicines.add(createMedicine("Ranitidine 150mg", "Antacid", "Lupin", 
                28, 9.00, "BATCH029", LocalDate.now().plusDays(15)));
            
            medicineRepository.saveAll(medicines);
            log.info("✅ {} medicines created", medicines.size());
        }
    }
    
    private Medicine createMedicine(String name, String category, String manufacturer, 
                                   int quantity, double price, String batchNumber, LocalDate expiryDate) {
        Medicine medicine = new Medicine();
        medicine.setName(name);
        medicine.setCategory(category);
        medicine.setManufacturer(manufacturer);
        medicine.setQuantity(quantity);
        medicine.setPrice(price);
        medicine.setBatchNumber(batchNumber);
        medicine.setExpiryDate(expiryDate);
        medicine.setDescription("High quality " + category.toLowerCase() + " medication");
        return medicine;
    }
    
    private void initializeCustomers() {
        if (customerRepository.count() == 0) {
            List<Customer> customers = new ArrayList<>();
            
            customers.add(createCustomer("Rahul Verma", "9876543220", "rahul.verma@email.com", 
                "101, Green Park, New Delhi", LocalDate.of(1985, 5, 15), 250));
            
            customers.add(createCustomer("Anjali Mehta", "9876543221", "anjali.mehta@email.com", 
                "202, Rose Garden, Mumbai", LocalDate.of(1990, 8, 20), 180));
            
            customers.add(createCustomer("Suresh Reddy", "9876543222", "suresh.reddy@email.com", 
                "303, Lotus Apartments, Bangalore", LocalDate.of(1978, 3, 10), 420));
            
            customers.add(createCustomer("Pooja Singh", "9876543223", "pooja.singh@email.com", 
                "404, Sunrise Complex, Pune", LocalDate.of(1995, 12, 25), 150));
            
            customers.add(createCustomer("Amit Shah", "9876543224", "amit.shah@email.com", 
                "505, Pearl Heights, Ahmedabad", LocalDate.of(1982, 7, 8), 310));
            
            customers.add(createCustomer("Sneha Kapoor", "9876543225", "sneha.kapoor@email.com", 
                "606, Diamond Plaza, Hyderabad", LocalDate.of(1988, 11, 30), 220));
            
            customers.add(createCustomer("Vikram Joshi", "9876543226", "vikram.joshi@email.com", 
                "707, Royal Enclave, Chennai", LocalDate.of(1975, 2, 14), 380));
            
            customers.add(createCustomer("Neha Gupta", "9876543227", "neha.gupta@email.com", 
                "808, Silver Oak, Kolkata", LocalDate.of(1992, 9, 5), 190));
            
            customers.add(createCustomer("Rajesh Kumar", "9876543228", "rajesh.kumar@email.com", 
                "909, Golden Valley, Jaipur", LocalDate.of(1980, 6, 18), 280));
            
            customers.add(createCustomer("Priya Desai", "9876543229", "priya.desai@email.com", 
                "1010, Sapphire Towers, Surat", LocalDate.of(1993, 4, 22), 160));
            
            customerRepository.saveAll(customers);
            log.info("✅ {} customers created", customers.size());
        }
    }
    
    private Customer createCustomer(String name, String phone, String email, 
                                   String address, LocalDate dob, int loyaltyPoints) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setDateOfBirth(dob);
        customer.setLoyaltyPoints(loyaltyPoints);
        customer.setRegisteredDate(LocalDate.now().minusMonths(new Random().nextInt(12)));
        return customer;
    }
    
    private void initializeSales() {
        if (saleRepository.count() == 0) {
            List<Medicine> medicines = medicineRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            if (medicines.isEmpty()) return;
            
            List<Sale> sales = new ArrayList<>();
            Random random = new Random();
            String[] paymentMethods = {"Cash", "Card", "UPI"};
            
            // Create 15 sample sales over the past 7 days
            for (int i = 0; i < 15; i++) {
                Medicine medicine = medicines.get(random.nextInt(medicines.size()));
                Customer customer = random.nextBoolean() && !customers.isEmpty() 
                    ? customers.get(random.nextInt(customers.size())) : null;
                
                int quantity = random.nextInt(5) + 1; // 1-5 units
                double discountPercent = random.nextInt(3) * 5.0; // 0%, 5%, or 10%
                int gstPercent = 5; // 5% GST
                
                Sale sale = createSale(medicine, customer, quantity, medicine.getPrice(), 
                    discountPercent, gstPercent, paymentMethods[random.nextInt(3)], 
                    LocalDateTime.now().minusDays(random.nextInt(7)));
                
                sales.add(sale);
            }
            
            saleRepository.saveAll(sales);
            log.info("✅ {} sales records created", sales.size());
        }
    }
    
    private Sale createSale(Medicine medicine, Customer customer, int quantity, 
                          double unitPrice, double discountPercent, int gstPercent, 
                          String paymentMethod, LocalDateTime saleDate) {
        Sale sale = new Sale();
        sale.setMedicine(medicine);
        sale.setCustomer(customer);
        sale.setQuantity(quantity);
        sale.setUnitPrice(unitPrice);
        
        double subtotal = quantity * unitPrice;
        double discountAmount = (subtotal * discountPercent) / 100;
        double amountAfterDiscount = subtotal - discountAmount;
        double gstAmount = (amountAfterDiscount * gstPercent) / 100;
        double finalAmount = amountAfterDiscount + gstAmount;
        
        sale.setDiscountPercentage(discountPercent);
        sale.setDiscountAmount(discountAmount);
        sale.setGstPercentage((double) gstPercent);
        sale.setGstAmount(gstAmount);
        sale.setTotalAmount(subtotal);
        sale.setFinalAmount(finalAmount);
        sale.setPaymentMethod(paymentMethod);
        sale.setSaleDate(saleDate);
        
        return sale;
    }
}
