# Testing Branch Comparison - Complete Guide

## Prerequisites
- Application running on port 8081
- Database cleared or fresh start to see new seed data

## Test Accounts

### Owner with 2 Branches (PRO Plan)
- **Username**: `owner_pro`
- **Password**: `OwnerPro@123`
- **Branches**:
  - Sharma Medicals - Main Branch (HIGH performance)
  - Sharma Medicals - North Branch (LOW performance)

### Owner with 3 Branches (ENTERPRISE Plan)
- **Username**: `owner_enterprise`
- **Password**: `OwnerEnt@123`
- **Branches**:
  - Patel HealthCare - Central Store (HIGH performance)
  - Patel HealthCare - West Store (MEDIUM performance)
  - Patel HealthCare - East Store (LOW performance)

## Expected Data Variations

### HIGH Performance Profile
- **Medicines**: 12 different medicines
- **Stock Levels**: 300 base stock
- **Customers**: 5 customers
- **Sales**: 15 transactions in current month
- **Low Stock Items**: 1 item

### MEDIUM Performance Profile
- **Medicines**: 8 different medicines
- **Stock Levels**: 150 base stock
- **Customers**: 3 customers
- **Sales**: 8 transactions in current month
- **Low Stock Items**: 2 items

### LOW Performance Profile
- **Medicines**: 6 different medicines
- **Stock Levels**: 80 base stock
- **Customers**: 2 customers
- **Sales**: 3 transactions in current month
- **Low Stock Items**: 3 items

## Testing Steps

### 1. Clear Existing Data (Optional but Recommended)
To see the new varied data, you may want to clear the database:
```bash
# Stop the application
# Delete the database file or run:
./mvnw clean
# Restart the application
./mvnw spring-boot:run
```

### 2. Login as owner_pro
1. Navigate to http://localhost:8081/login
2. Enter username: `owner_pro`
3. Enter password: `OwnerPro@123`
4. Click Login

### 3. Navigate to Branch Comparison
1. From the owner dashboard, click "Branch Comparison" in the sidebar
2. Or navigate directly to: http://localhost:8081/owner/compare

### 4. Verify Display

#### Check KPI Cards
- **Main Branch** should show:
  - Higher today's sales (₹)
  - Higher monthly revenue (₹)
  - More medicines (12)
  - More customers (5)
  - Fewer low stock items (1)

- **North Branch** should show:
  - Lower today's sales (₹)
  - Lower monthly revenue (₹)
  - Fewer medicines (6)
  - Fewer customers (2)
  - More low stock items (3)

#### Check Performance Indicators
- **Top Performer** badge should show: "Sharma Medicals - Main Branch"
- **Needs Attention** badge should show: "Sharma Medicals - North Branch"

#### Check Charts
- **Revenue Comparison Chart**: Should show clear difference between branches
- **Stock Health Chart**: Should show distribution of low stock items

#### Check Table
- All metrics should be populated
- Portfolio Total row should show correct sums
- No null or undefined values

### 5. Test with owner_enterprise
1. Logout
2. Login with username: `owner_enterprise`, password: `OwnerEnt@123`
3. Navigate to /owner/compare
4. Verify 3 branches display with varied performance:
   - Central Store: Highest metrics
   - West Store: Medium metrics
   - East Store: Lowest metrics

### 6. Verify Individual Branch Details
1. Click "Full Details" button on any branch card
2. Verify the branch detail page shows correct data
3. Check that medicine counts, sales, and other metrics match

## Expected Results

### Charts Should Show
- Clear visual differences between branches
- No errors in console
- Proper formatting of currency (₹)
- Responsive design on different screen sizes

### Table Should Show
- All columns populated
- Correct totals in footer
- Badges colored appropriately (red for low stock, green for healthy)
- Responsive hiding of columns on smaller screens

### No Errors
- No JavaScript console errors
- No Thymeleaf template errors
- No null pointer exceptions
- All aggregations calculate correctly

## Troubleshooting

### If data looks the same across branches:
- Clear the cache: Restart the application
- Check logs for "Branch varied sample data seed complete" messages
- Verify the profile parameter is being passed correctly

### If charts don't render:
- Check browser console for JavaScript errors
- Verify Chart.js is loaded
- Check that data arrays are properly populated

### If totals are wrong:
- Check for null values in the data
- Verify the template null-safe aggregation is working
- Check browser console for Thymeleaf errors

## Success Criteria

✅ All branches display with different metrics
✅ Charts render correctly showing variations
✅ Best/worst performer badges show correctly
✅ Table totals calculate accurately
✅ No console errors
✅ Responsive design works on mobile
✅ Individual branch details accessible
✅ Data persists across page refreshes
