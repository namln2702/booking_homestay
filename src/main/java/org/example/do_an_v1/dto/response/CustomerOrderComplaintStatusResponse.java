package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderComplaintStatusResponse {
    private String phase;
    private boolean complaintRelated;
    private boolean inComplaintWindow;
    private boolean underHostReview;
    private boolean underAdminReview;
    private boolean refundInProgress;
    private boolean resolvedWithRefund;
    private boolean resolvedWithoutRefund;
    private LocalDateTime complaintDeadline;
    private boolean withinComplaintDeadline;
    private Long latestComplaintId;
    private boolean canFileComplaint;
    private boolean canCancelComplaint;
    private boolean canUpdateComplaint;
}
