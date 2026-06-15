package pk.ni.pasir_ostrega_tymoteusz.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pk.ni.pasir_ostrega_tymoteusz.dto.DebtDTO;
import pk.ni.pasir_ostrega_tymoteusz.model.Debt;
import pk.ni.pasir_ostrega_tymoteusz.model.Group;
import pk.ni.pasir_ostrega_tymoteusz.model.User;
import pk.ni.pasir_ostrega_tymoteusz.repository.DebtRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.GroupRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtServiceTest {

    @Mock private DebtRepository debtRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;
    @Mock private MembershipService membershipService;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks private DebtService debtService;

    private User owner;
    private User member1;
    private User member2;
    private Group group;
    private Debt debt;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        member1 = new User();
        member1.setId(2L);
        member2 = new User();
        member2.setId(3L);

        group = new Group();
        group.setId(10L);
        group.setOwner(owner);

        debt = new Debt();
        debt.setId(100L);
        debt.setGroup(group);
        debt.setDebtor(member1);
        debt.setCreditor(member2);
    }

    @Test
    void shouldThrowWhenCreatingDebtToSelf() {
        DebtDTO dto = new DebtDTO();
        dto.setGroupId(10L);
        dto.setDebtorId(2L);
        dto.setCreditorId(2L);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member1));

        assertThrows(IllegalStateException.class, () -> debtService.createDebt(dto));
    }

    @Test
    void shouldAllowOwnerToCreateDebtBetweenOthers() {
        DebtDTO dto = new DebtDTO();
        dto.setGroupId(10L);
        dto.setDebtorId(2L);
        dto.setCreditorId(3L);
        dto.setAmount(50.0);
        dto.setTitle("Test");

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(member2));
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(debtRepository.save(any(Debt.class))).thenReturn(new Debt());

        assertNotNull(debtService.createDebt(dto));
    }

    @Test
    void shouldThrowWhenNonParticipantCreatesDebt() {
        DebtDTO dto = new DebtDTO();
        dto.setGroupId(10L);
        dto.setDebtorId(1L);
        dto.setCreditorId(3L);
        dto.setAmount(50.0);
        dto.setTitle("Test");

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(3L)).thenReturn(Optional.of(member2));
        when(currentUserService.getCurrentUser()).thenReturn(member1);

        assertThrows(AccessDeniedException.class, () -> debtService.createDebt(dto));
    }

    @Test
    void shouldAllowParticipantToDeleteDebt() {
        when(debtRepository.findById(100L)).thenReturn(Optional.of(debt));
        when(currentUserService.getCurrentUser()).thenReturn(member1);

        debtService.deleteDebt(100L);
        verify(debtRepository, times(1)).delete(debt);
    }

    @Test
    void shouldAllowOwnerToDeleteDebtBetweenOthers() {
        when(debtRepository.findById(100L)).thenReturn(Optional.of(debt));
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        debtService.deleteDebt(100L);
        verify(debtRepository, times(1)).delete(debt);
    }

    @Test
    void shouldThrowWhenNonParticipantDeletesDebt() {
        User nonParticipant = new User();
        nonParticipant.setId(4L);

        when(debtRepository.findById(100L)).thenReturn(Optional.of(debt));
        when(currentUserService.getCurrentUser()).thenReturn(nonParticipant);

        assertThrows(AccessDeniedException.class, () -> debtService.deleteDebt(100L));
    }
}