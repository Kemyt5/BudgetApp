package pk.ni.pasir_ostrega_tymoteusz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pk.ni.pasir_ostrega_tymoteusz.dto.GroupTransactionDTO;
import pk.ni.pasir_ostrega_tymoteusz.model.Group;
import pk.ni.pasir_ostrega_tymoteusz.model.Membership;
import pk.ni.pasir_ostrega_tymoteusz.model.User;
import pk.ni.pasir_ostrega_tymoteusz.repository.DebtRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.GroupRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.MembershipRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupTransactionServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private MembershipRepository membershipRepository;
    @Mock private DebtRepository debtRepository;
    @Mock private MembershipService membershipService;
    // DODANO MOCKA dla SimpMessagingTemplate
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private GroupTransactionService groupTransactionService;

    private User currentUser;
    private User otherUser;
    private Group group;
    private Membership m1;
    private Membership m2;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current@test.com"); // DODANO EMAIL

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@test.com"); // DODANO EMAIL

        group = new Group();
        group.setId(10L);
        group.setName("Testowa Grupa"); // DODANO NAZWĘ GRUPY dla powiadomień

        m1 = new Membership();
        m1.setUser(currentUser);
        m1.setGroup(group);

        m2 = new Membership();
        m2.setUser(otherUser);
        m2.setGroup(group);
    }

    @Test
    void shouldCreateDebtsFromCurrentUserToOthersOnIncome() {
        GroupTransactionDTO dto = new GroupTransactionDTO();
        dto.setGroupId(10L);
        dto.setAmount(100.0);
        dto.setType("INCOME");
        dto.setTitle("Test Income");

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupId(10L)).thenReturn(List.of(m1, m2));

        groupTransactionService.addGroupTransaction(dto, currentUser);

        verify(debtRepository, times(1)).save(argThat(debt ->
                debt.getDebtor().getId().equals(currentUser.getId()) &&
                        debt.getCreditor().getId().equals(otherUser.getId()) &&
                        debt.getAmount() == 50.0
        ));
    }

    @Test
    void shouldCreateDebtsFromOthersToCurrentUserOnExpense() {
        GroupTransactionDTO dto = new GroupTransactionDTO();
        dto.setGroupId(10L);
        dto.setAmount(100.0);
        dto.setType("EXPENSE");
        dto.setTitle("Test Expense");

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupId(10L)).thenReturn(List.of(m1, m2));

        groupTransactionService.addGroupTransaction(dto, currentUser);

        verify(debtRepository, times(1)).save(argThat(debt ->
                debt.getDebtor().getId().equals(otherUser.getId()) &&
                        debt.getCreditor().getId().equals(currentUser.getId()) &&
                        debt.getAmount() == 50.0
        ));
    }
}