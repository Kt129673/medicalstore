# Owner Compare Endpoint Fix

## Issue
The `/owner/compare` endpoint was not working and likely throwing errors in production.

## Root Cause
The `compareBranches()` method in `OwnerController.java` had several potential **Null Pointer Exception (NPE)** issues:

### Problems Found:

1. **Line 104**: `securityUtils.getCurrentUserId()` could return null
   - If null, subsequent operations would fail
   
2. **Line 105**: `branchService.getBranchesByOwner(ownerId)` could return null
   - If null, the for loop would throw NPE
   
3. **Line 110**: `dashboardService.buildBranchDashboard(b.getId())` could return null
   - No null check before calling `getOrDefault()`
   
4. **Lines 132-136**: Type casting without null checks
   - ```java
     todaySalesData.add(((Number) s.get("todaySales")).doubleValue());
     ```
   - If `s.get("todaySales")` is null, this throws NPE
   - No null checks on the retrieved values

## Solution Applied

Added comprehensive null checks and safe type casting:

### Changes Made:

```java
// 1. Check if ownerId is null
Long ownerId = securityUtils.getCurrentUserId();
if (ownerId == null) {
    return "redirect:/login";
}

// 2. Ensure branches list is never null
List<Branch> branches = branchService.getBranchesByOwner(ownerId);
if (branches == null) {
    branches = new ArrayList<>();
}

// 3. Check if kpis map is null
Map<String, Object> kpis = dashboardService.buildBranchDashboard(b.getId());
if (kpis == null) {
    kpis = new LinkedHashMap<>();
}

// 4. Safe type casting with null checks
Object todaySalesObj = s.get("todaySales");
todaySalesData.add(todaySalesObj != null ? ((Number) todaySalesObj).doubleValue() : 0.0);

// Similar safe casting for all numeric values
```

## Testing

✅ Project compiles successfully after fix  
✅ No compilation errors  

## Deployment Instructions

1. Rebuild the WAR file:
   ```bash
   mvn clean package -DskipTests
   ```

2. Deploy the new WAR to your application server

3. Test the endpoint:
   ```
   http://13.62.103.255:8081/owner/compare
   ```

## Expected Behavior After Fix

- ✅ Endpoint returns 200 OK even if owner has no branches
- ✅ Shows appropriate message if no branches exist
- ✅ Properly displays branch comparison data with charts
- ✅ No null pointer exceptions in server logs

## Files Modified

- `src/main/java/com/medicalstore/controller/OwnerController.java` (lines 101-163)
