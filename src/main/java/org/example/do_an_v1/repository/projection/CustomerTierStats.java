package org.example.do_an_v1.repository.projection;

public interface CustomerTierStats {
    Long getSuccessfulBookings();
    Long getRefundedBookings();
}
