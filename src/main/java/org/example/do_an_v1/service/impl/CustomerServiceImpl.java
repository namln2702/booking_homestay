package org.example.do_an_v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.configuration.SessionConfig;
import org.example.do_an_v1.dto.BillDTO;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.dto.CustomerDTO;
import org.example.do_an_v1.dto.PaymentDTO;
import org.example.do_an_v1.dto.request.UserRegistrationRequest;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.*;
import org.example.do_an_v1.mapper.BillMapper;
import org.example.do_an_v1.mapper.CustomerBookingInfoMapper;
import org.example.do_an_v1.mapper.CustomerMapper;
import org.example.do_an_v1.mapper.profile.ProfileMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.CustomerService;
import org.example.do_an_v1.service.support.UserRegistrationSupport;
import org.example.do_an_v1.utils.Date;
import org.example.do_an_v1.utils.GenNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ProfileMapper profileMapper;
    private final UserRegistrationSupport userRegistrationSupport;
    private final CustomerBookingInfoRepository customerBookingInfoRepository;
    private final HomestayDailyPricesRepository homestayDailyPricesRepository;
    private final BillRepository billRepository;
    private final HomestayRepository homestayRepository;
    private final PreferenceRepository preferenceRepository;
    private final SessionConfig sessionConfig;
    private final ImageRepository imageRepository;
    private final ComplaintRepository complaintRepository;






    @Override
    @Transactional
    public ApiResponse<CustomerDTO> upsertCustomerProfile(CustomerDTO dto) throws RuntimeException {
        if (dto == null || dto.getIdUser() == null) {
            throw new IllegalArgumentException("Customer DTO must include idUser");
        }

        User user = userRegistrationSupport.getUserOrThrow(dto.getIdUser());

        Customer customer = customerRepository.findById(user.getId()).orElse(null);
        boolean isNew = false;

        if (customer == null) {
            customer = Customer.builder()
                    .user(user)
                    .role(RoleUser.CUSTOMER)
                    .build();
            isNew = true;
        }

        boolean hasChanges = isNew;

        UserRegistrationRequest userRequest = new UserRegistrationRequest(
                dto.getIdUser(),
                dto.getUsername(),
                dto.getName(),
                dto.getPhone(),
                dto.getAge(),
                dto.getAvatarUrl()
        );

        if (userRegistrationSupport.applyUserAttributes(user, userRequest)) {
            hasChanges = true;
        }

        if (dto.getStatus() != null && !Objects.equals(dto.getStatus(), customer.getStatus())) {
            customer.setStatus(dto.getStatus());
            hasChanges = true;
        }

        if (dto.getDateOfBirth() != null && !Objects.equals(dto.getDateOfBirth(), customer.getDateOfBirth())) {
            customer.setDateOfBirth(dto.getDateOfBirth());
            hasChanges = true;
        }

        if (dto.getQrCodeUrl() != null && !Objects.equals(dto.getQrCodeUrl(), customer.getQrCodeUrl())) {
            customer.setQrCodeUrl(dto.getQrCodeUrl());
            hasChanges = true;
        }

        // lastBooking will be maintained by booking workflows; ignore incoming value for now

        if (customer.getRole() != RoleUser.CUSTOMER) {
            customer.setRole(RoleUser.CUSTOMER);
            hasChanges = true;
        }

        if (!hasChanges) {
            return new ApiResponse<>(200, "Customer information already up to date", profileMapper.toCustomerDTO(customer));
        }

        // createdAt/updatedAt live on BaseEntity and are populated through auditing, never via the request payload
        Customer savedCustomer = customerRepository.save(customer);
        String message = isNew ? "Customer profile created successfully" : "Customer information updated successfully";

        return new ApiResponse<>(200, message, profileMapper.toCustomerDTO(savedCustomer));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerDTO> getCustomerByUserId(Long userId) {
        Customer customer = customerRepository.findById(userId).orElse(null);
        if (customer == null) {
            return new ApiResponse<>(404, "Customer profile not found", null);
        }
        return new ApiResponse<>(200, "Customer profile retrieved successfully", profileMapper.toCustomerDTO(customer));
    }

    @Override
    @Transactional
    public ApiResponse<?> booking(BillDTO billDTO) {

        Homestay homestay = homestayRepository.findById(billDTO.getId()).orElseThrow(() -> new RuntimeException("Homestay not exits"));
        String start = Date.DateToString(billDTO.getCheckIn());
        String end = Date.DateToString(billDTO.getCheckOut());




        // Check homestay availability
        if(homestayDailyPricesRepository.checkHomestayAvailability(homestay.getId(), start, end))
            return new ApiResponse<>(422, "Room has been booked", null );


        // Save bill
        Bill bill = BillMapper.toEntity(billDTO);
        Bill billResult = billRepository.save(bill);

        // Save customer into bill
        Customer customer = CustomerMapper.toEntity(billDTO.getCustomerDTO());
        customer.setId(Long.parseLong( (String) sessionConfig.httpSession().getAttribute("id")));
        billResult.setCustomer(customer);


        // Save HomestayDailyPrices into bill
        Bill finalBillResult = billResult;
        billDTO.getHomestayDailyPricesDTOS()
                .forEach(pricePerDayDTO -> {
                    homestayDailyPricesRepository.save(HomestayDailyPrice.builder()
                                    .isBooked(true)
                                    .price(pricePerDayDTO.getPrice())
                                    .homestay(homestay)
                                    .bill(finalBillResult)
                            .build());
                });


        // luu thong tin CustomerBookingInfo neu la nguoi moi
        if(!Objects.isNull(billDTO.getCustomerBookingInfoDTO())){
            CustomerBookingInfo customerBookingInfo = CustomerBookingInfoMapper.toEntity(billDTO.getCustomerBookingInfoDTO());
            customerBookingInfo = customerBookingInfoRepository.save(customerBookingInfo);
            billResult.setCustomerBookingInfo(customerBookingInfo);
        }


        // Create transaction
        Transaction transaction = Transaction.builder()
                .completedAt(LocalDateTime.now().plusMinutes(15))
                .transactionType(TypeTransaction.BOOKING_PAYMENT)
                .status(StatusTransaction.PENDING)
                .bill(billResult)
                .fromUser(customer.getUser())
                .build();


        // Create code
        bill.setCode(GenNumber.generate());
        bill.setStatus(StatusBill.PAYMENT_PENDING);
        billResult = billRepository.save(billResult);

        return new ApiResponse<>(200, "Save bill success", billResult);
    }

    public ApiResponse<?> payment(PaymentDTO paymentDTO){
        return null;
    }

    @Override
    public ApiResponse<?> updateCustomer(CustomerDTO customerDTO) {

        Long userId = customerDTO.getIdCustomer();
        Customer customer = customerRepository.findById(userId).orElseThrow( () -> new RuntimeException("Customer not exits"));
        Set<Preference> preferenceList = customer.getListPreferences();


        customerDTO.getListPreference().forEach(idPreference ->
        {
            Preference preference = preferenceRepository.findById(idPreference).orElseThrow(() -> new RuntimeException("Preference not exits"));
            preferenceList.add(preference);
        });
        customer.setListPreferences(preferenceList);
        customer.setStatus(Status.ACTIVE);
        return new ApiResponse<>(200, "Success", customerRepository.save(customer));

    }
}
