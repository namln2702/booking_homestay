package org.example.do_an_v1.service.impl;

import org.example.do_an_v1.dto.AdvancedHomestaySearchDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.entity.Address;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.entity.HomestayImage;
import org.example.do_an_v1.mapper.HomestayMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.AdminRepository;
import org.example.do_an_v1.repository.AmenitiesRepository;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.CustomerRepository;
import org.example.do_an_v1.repository.FacilitiesRepository;
import org.example.do_an_v1.repository.HomestayDailyPricesRepository;
import org.example.do_an_v1.repository.HomestayImageRepository;
import org.example.do_an_v1.repository.HomestayRepository;
import org.example.do_an_v1.repository.HostRepository;
import org.example.do_an_v1.repository.ImageRepository;
import org.example.do_an_v1.repository.PersonRepository;
import org.example.do_an_v1.repository.PricePerDayRepository;
import org.example.do_an_v1.repository.ReviewRepository;
import org.example.do_an_v1.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomestayServiceImplTests {

    @Mock
    private HostRepository hostRepository;
    @Mock
    private HomestayRepository homestayRepository;
    @Mock
    private FacilitiesRepository facilitiesRepository;
    @Mock
    private AmenitiesRepository amenitiesRepository;
    @Mock
    private PricePerDayRepository pricePerDayRepository;
    @Mock
    private HomestayImageRepository homestayImageRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private HomestayMapper homestayMapper;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private HomestayDailyPricesRepository homestayDailyPricesRepository;
    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private HomestayServiceImpl homestayService;

    @Test
    void searchHomestayAdvanced_matchesStateWithoutAccent() {
        AdvancedHomestaySearchDTO request = new AdvancedHomestaySearchDTO();
        request.setState("Long Bien");

        Address address = Address.builder()
                .state("Long Biên")
                .build();
        Homestay homestay = Homestay.builder()
                .title("Long Biên Light")
                .address(address)
                .build();

        HomestayDTO homestayDTO = HomestayDTO.builder()
                .title("Long Biên Light")
                .build();

        List<HomestayImage> images = Collections.emptyList();
        when(homestayRepository.searchHomestayAdvanced(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(homestay));
        when(homestayImageRepository.findByHomestay(homestay)).thenReturn(images);
        when(homestayMapper.toDto(homestay, images)).thenReturn(homestayDTO);

        ApiResponse<?> response = homestayService.searchHomestayAdvanced(request);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getData()).isInstanceOf(List.class);
        assertThat((List<?>) response.getData()).hasSize(1);

        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        verify(homestayRepository).searchHomestayAdvanced(
                any(), any(), stateCaptor.capture(),
                any(), any(), any(), any(), any()
        );
        assertThat(stateCaptor.getValue()).isEqualTo("long bien");
    }
}
