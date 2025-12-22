package org.example.do_an_v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderActionPermissionResponse {
    private boolean canCancel;
    private boolean canPayRemaining;
    private boolean canCheckIn;
    private boolean canFileComplaint;
}
