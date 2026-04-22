# POS Screen Testing Checklist

## Pre-Testing Setup
- [ ] Clear browser cache
- [ ] Clear localStorage
- [ ] Open browser console (F12)
- [ ] Check for JavaScript errors
- [ ] Verify application is running on port 8081

## 1. Page Load Tests

### Initial State
- [ ] Page loads without errors
- [ ] Search input is visible and focused
- [ ] Customer dropdown is populated
- [ ] Cart table shows "empty cart" message
- [ ] Submit buttons are disabled and gray
- [ ] Payment method "Cash" is selected by default
- [ ] Bill summary shows all zeros
- [ ] Tips card is visible
- [ ] Keyboard shortcuts are shown

### CSS Verification
- [ ] POS layout is two-column on desktop
- [ ] Cards have proper shadows and borders
- [ ] Colors match design system
- [ ] Icons are visible
- [ ] Fonts are correct

## 2. Search Functionality Tests

### Basic Search
- [ ] Type "para" in search input
- [ ] Loading spinner appears
- [ ] Results appear after ~150ms
- [ ] Results show medicine name, batch, stock, price
- [ ] Low stock items have red background
- [ ] Stock badges are color-coded (green/yellow/red)

### Search Caching
- [ ] Search for "para"
- [ ] Clear search and search "para" again
- [ ] Results appear instantly (from cache)
- [ ] No loading spinner on cached results

### Search Edge Cases
- [ ] Search with no results shows "No medicines found"
- [ ] Search with special characters works
- [ ] Search with numbers works
- [ ] Empty search shows nothing

### Dropdown Behavior
- [ ] Click outside dropdown closes it
- [ ] Press Escape closes dropdown
- [ ] Clicking a result adds to cart and closes dropdown
- [ ] Search input refocuses after selection

## 3. Add to Cart Tests

### Adding Items
- [ ] Click a medicine in search results
- [ ] Item appears in cart table
- [ ] Toast notification shows "Added X to cart"
- [ ] Empty cart message disappears
- [ ] Submit buttons become enabled and green
- [ ] Bill summary updates

### Duplicate Items
- [ ] Add same medicine twice
- [ ] Quantity increases instead of duplicate row
- [ ] Toast shows "Increased quantity of X"
- [ ] Bill summary updates correctly

### Stock Validation
- [ ] Try to add item with 0 stock
- [ ] Toast shows "Out of stock: X"
- [ ] Item is not added to cart
- [ ] Try to add more than available stock
- [ ] Toast shows "Cannot add more. Only X units available"

## 4. Cart Management Tests

### Quantity Updates
- [ ] Change quantity in cart
- [ ] Bill summary updates in real-time
- [ ] Quantity input turns yellow at 70% of stock
- [ ] Quantity input turns red at 90% of stock
- [ ] Cannot enter quantity > available stock
- [ ] Toast shows warning when exceeding stock

### Remove Items
- [ ] Click trash icon on cart item
- [ ] Item is removed from cart
- [ ] Toast shows "Removed X from cart"
- [ ] Bill summary updates
- [ ] If last item, cart shows empty message
- [ ] Submit buttons become disabled

### Quantity Edge Cases
- [ ] Enter 0 in quantity → resets to 1
- [ ] Enter negative number → resets to 1
- [ ] Enter decimal → rounds to integer
- [ ] Enter text → resets to 1

## 5. Discount & GST Tests

### Discount Input
- [ ] Enter discount 0-100% → works correctly
- [ ] Enter discount > 100% → clamps to 100%
- [ ] Toast shows "Discount cannot exceed 100%"
- [ ] Enter negative discount → resets to 0
- [ ] Bill summary updates in real-time
- [ ] Discount amount shows in orange

### GST Selection
- [ ] Change GST rate dropdown
- [ ] Bill summary updates immediately
- [ ] GST amount shows in green
- [ ] Default is 18% (most medicines)
- [ ] All rates (0%, 5%, 12%, 18%, 28%) work

### Calculation Verification
- [ ] Add item for ₹100
- [ ] Set discount to 10%
- [ ] Set GST to 18%
- [ ] Verify: Subtotal = ₹100
- [ ] Verify: Discount = ₹10
- [ ] Verify: After discount = ₹90
- [ ] Verify: GST = ₹16.20 (18% of ₹90)
- [ ] Verify: Final = ₹106.20

## 6. Payment Method Tests

### Selection
- [ ] Click "Cash" button → becomes selected (blue)
- [ ] Click "Card" button → Cash deselects, Card selects
- [ ] Click "UPI" button → Card deselects, UPI selects
- [ ] Hover effects work on all buttons

### Persistence
- [ ] Select "Card" payment method
- [ ] Refresh page (F5)
- [ ] Verify "Card" is still selected
- [ ] Check localStorage has 'pos_payment_method' = 'Card'

## 7. Customer Selection Tests

### Walk-in Customer
- [ ] Leave customer dropdown as "Walk-in Customer"
- [ ] Complete sale
- [ ] Verify sale is created without customer

### Registered Customer
- [ ] Select a customer from dropdown
- [ ] Complete sale
- [ ] Verify customer receives loyalty points
- [ ] Check customer record updated

## 8. Keyboard Shortcuts Tests

### F2 - Focus Search
- [ ] Press F2 from anywhere on page
- [ ] Search input receives focus
- [ ] Can start typing immediately

### F5 - Complete Sale
- [ ] Add items to cart
- [ ] Press F5
- [ ] Confirmation dialog appears
- [ ] Sale is submitted

### Escape - Close/Cancel
- [ ] Open search dropdown
- [ ] Press Escape → dropdown closes
- [ ] Press Escape again → cancel confirmation appears
- [ ] Confirm → redirects to /sales

### Keyboard Shortcuts While Typing
- [ ] Focus on quantity input
- [ ] Press F5 → should NOT submit (user is typing)
- [ ] Press Escape → should NOT cancel (user is typing)

## 9. Submit Sale Tests

### Empty Cart
- [ ] Try to submit with empty cart
- [ ] Submit buttons are disabled
- [ ] Cannot click submit

### Valid Sale
- [ ] Add items to cart
- [ ] Click "Complete Sale"
- [ ] Buttons show loading spinner
- [ ] Buttons are disabled during submission
- [ ] Success dialog appears
- [ ] Options: "View Invoice" or "New Sale"

### Stock Validation on Submit
- [ ] Add item with quantity = available stock
- [ ] Manually increase quantity in cart beyond stock
- [ ] Try to submit
- [ ] Error dialog shows stock issue
- [ ] Sale is not submitted

### Network Error
- [ ] Disconnect network
- [ ] Try to submit sale
- [ ] Error dialog shows "Failed to complete sale"
- [ ] Buttons are re-enabled
- [ ] Cart data is preserved

## 10. Success Flow Tests

### View Invoice Option
- [ ] Complete a sale
- [ ] Click "View Invoice" in success dialog
- [ ] Redirects to invoice page
- [ ] Invoice shows correct data

### New Sale Option
- [ ] Complete a sale
- [ ] Click "New Sale" in success dialog
- [ ] Page reloads
- [ ] Cart is empty
- [ ] Search cache is cleared
- [ ] Payment method is preserved

## 11. Mobile Responsiveness Tests

### Tablet (768px - 1024px)
- [ ] Layout switches to single column
- [ ] Bill card is no longer sticky
- [ ] All buttons are touch-friendly
- [ ] Text is readable

### Mobile (< 768px)
- [ ] Payment buttons stack vertically
- [ ] Search input is full width
- [ ] Cart table is scrollable
- [ ] Bill summary is readable
- [ ] Keyboard shortcuts still work

## 12. Performance Tests

### Search Performance
- [ ] Type quickly in search
- [ ] Only one API call is made (debouncing works)
- [ ] Results appear within 200ms

### Cart Rendering
- [ ] Add 10 items to cart
- [ ] Page remains responsive
- [ ] No lag when updating quantities
- [ ] Bill summary updates smoothly

### Memory Leaks
- [ ] Open POS page
- [ ] Add items, remove items, search multiple times
- [ ] Navigate away
- [ ] Check browser console for errors
- [ ] Check memory usage (should not increase)

## 13. Error Handling Tests

### API Errors
- [ ] Simulate 500 error from search API
- [ ] Toast shows "Search failed"
- [ ] Loading spinner stops
- [ ] Page remains functional

### Invalid Data
- [ ] Try to add item with null price
- [ ] Error is handled gracefully
- [ ] Toast shows error message

### CSRF Token
- [ ] Verify CSRF token is included in submit request
- [ ] Check request headers in network tab

## 14. Accessibility Tests

### Keyboard Navigation
- [ ] Tab through all form elements
- [ ] Focus indicators are visible
- [ ] Can submit form with Enter key

### Screen Reader
- [ ] Use screen reader (NVDA/JAWS)
- [ ] All buttons have labels
- [ ] Form fields have labels
- [ ] Error messages are announced

### Color Contrast
- [ ] Check contrast ratios meet WCAG AA
- [ ] Text is readable on all backgrounds
- [ ] Color is not the only indicator (icons too)

## 15. Browser Compatibility Tests

### Chrome
- [ ] All features work
- [ ] No console errors
- [ ] Animations are smooth

### Firefox
- [ ] All features work
- [ ] No console errors
- [ ] Animations are smooth

### Safari
- [ ] All features work
- [ ] No console errors
- [ ] Animations are smooth

### Edge
- [ ] All features work
- [ ] No console errors
- [ ] Animations are smooth

## 16. Integration Tests

### With Backend
- [ ] Sale is saved to database
- [ ] Stock is reduced correctly
- [ ] Customer loyalty points are updated
- [ ] Invoice is generated correctly

### With Other Pages
- [ ] Navigate from dashboard to POS
- [ ] Navigate from sales list to POS
- [ ] Complete sale and view invoice
- [ ] Return to sales list

## 17. Edge Cases

### Concurrent Users
- [ ] Two users add same item simultaneously
- [ ] Stock validation prevents overselling
- [ ] Appropriate error message shown

### Long Sessions
- [ ] Keep POS page open for 1 hour
- [ ] Session doesn't expire
- [ ] Can still complete sales

### Large Cart
- [ ] Add 50 items to cart
- [ ] Page remains responsive
- [ ] Bill calculation is correct
- [ ] Submission works

## 18. Visual Regression Tests

### Compare Screenshots
- [ ] Take screenshot of empty cart
- [ ] Take screenshot with items in cart
- [ ] Take screenshot of search results
- [ ] Take screenshot of success dialog
- [ ] Compare with design mockups

## Test Results Summary

| Category | Tests | Passed | Failed | Notes |
|----------|-------|--------|--------|-------|
| Page Load | 10 | | | |
| Search | 12 | | | |
| Add to Cart | 8 | | | |
| Cart Management | 10 | | | |
| Discount & GST | 12 | | | |
| Payment Method | 6 | | | |
| Customer Selection | 4 | | | |
| Keyboard Shortcuts | 8 | | | |
| Submit Sale | 8 | | | |
| Success Flow | 6 | | | |
| Mobile | 8 | | | |
| Performance | 6 | | | |
| Error Handling | 6 | | | |
| Accessibility | 6 | | | |
| Browser Compat | 12 | | | |
| Integration | 6 | | | |
| Edge Cases | 6 | | | |
| Visual | 5 | | | |
| **TOTAL** | **139** | | | |

## Critical Issues Found

1. 
2. 
3. 

## Minor Issues Found

1. 
2. 
3. 

## Recommendations

1. 
2. 
3. 

## Sign-off

- [ ] All critical tests passed
- [ ] All bugs documented
- [ ] Ready for production

**Tested by:** _______________
**Date:** _______________
**Environment:** _______________
**Browser:** _______________
