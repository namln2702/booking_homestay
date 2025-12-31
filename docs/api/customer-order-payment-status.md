## Customer Order Payment Status – Deposit Handling Update

### Background
`GET /api/customer/orders/{billId}` (implemented via `CustomerServiceImpl#getCustomerOrderDetail`) previously treated the bill status `DEPOSIT_FAILED` as if the deposit had succeeded, due to legacy naming. The enum has now been clarified—`DEPOSIT_FAILED` truly means the deposit attempt failed—so the payment snapshot exposed to the frontend has changed.

### What Changed
1. **Deposit completion flags**
   - The service no longer considers `DEPOSIT_FAILED` as a “deposit completed” state.
   - `paymentStatus.depositPaid` becomes `true` only when the bill advances to `REMAINING_PAYMENT_PENDING` or any later phase.
   - Bills stuck (or terminated) at `DEPOSIT_FAILED` now return `depositPaid=false` and `awaitingDeposit=false` (because the flow ended unsuccessfully).

2. **Remaining payment requirement**
   - `paymentStatus.remainingPaymentRequired` is `true` only for `REMAINING_PAYMENT_PENDING`.
   - A bill in `DEPOSIT_FAILED` will not prompt the UI to collect the remaining payment anymore.

3. **Phases/messages**
   - `paymentStatus.phase` still maps `DEPOSIT_FAILED` to `DEPOSIT_FAILED_CHECKIN`, so existing label rendering remains valid; only the boolean flags above have been corrected.

### Frontend Impact
- Any UI logic that relied on `depositPaid` / `awaitingDeposit` should re-test the following cases:
  - **Deposit succeeded** → expect `depositPaid=true`, `awaitingDeposit=false`.
  - **Deposit in progress (`DEPOSIT_PENDING`)** → `awaitingDeposit=true`, `depositPaid=false`.
  - **Deposit failed (`DEPOSIT_FAILED`)** → `depositPaid=false`, `awaitingDeposit=false`, phase `"DEPOSIT_FAILED_CHECKIN"`.
- Flows that prompt for remaining payment must gate on `remainingPaymentRequired=true` rather than the old `DEPOSIT_FAILED` check.

No payload shape changed; only the boolean values were corrected to match the clarified status semantics. Update any conditional UI logic accordingly.***
