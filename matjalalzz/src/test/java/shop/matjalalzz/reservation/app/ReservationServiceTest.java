package shop.matjalalzz.reservation.app;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import shop.matjalalzz.global.util.AuditorAwareImpl;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.user.dao.UserRepository;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private AuditorAwareImpl auditor;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;




}