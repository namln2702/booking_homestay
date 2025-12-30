package org.example.do_an_v1.dto.response;

import lombok.*;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.HostDTO;
import org.example.do_an_v1.dto.TransactionDTO;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostWithPendingPayoutTransactionsResponse {

    private HostDTO host;
    private List<HomestayWithTransactionsDTO> homestays;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HomestayWithTransactionsDTO {
        private HomestayDTO homestay;
        private List<TransactionDTO> transactions;
    }
}

