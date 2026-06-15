package pk.ni.pasir_ostrega_tymoteusz.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pk.ni.pasir_ostrega_tymoteusz.dto.GroupNotificationDTO;
import pk.ni.pasir_ostrega_tymoteusz.dto.GroupTransactionDTO;
import pk.ni.pasir_ostrega_tymoteusz.model.Debt;
import pk.ni.pasir_ostrega_tymoteusz.model.Group;
import pk.ni.pasir_ostrega_tymoteusz.model.Membership;
import pk.ni.pasir_ostrega_tymoteusz.model.User;
import pk.ni.pasir_ostrega_tymoteusz.repository.DebtRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.GroupRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.MembershipRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class GroupTransactionService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final MembershipService membershipService;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupTransactionService(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            DebtRepository debtRepository,
            MembershipService membershipService,
            SimpMessagingTemplate messagingTemplate) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.membershipService = membershipService;
        this.messagingTemplate = messagingTemplate;
    }

    public void addGroupTransaction(GroupTransactionDTO transactionDTO, User currentUser) {
        Group group = groupRepository.findById(transactionDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Grupy"));

        membershipService.assertCurrentUserIsGroupMember(group.getId());

        List<Membership> members = membershipRepository.findByGroupId(group.getId());

        List<Membership> selectedMembers = selectParticipants(transactionDTO, members, currentUser);

        if (selectedMembers.isEmpty()) {
            throw new IllegalStateException("Grupa nie ma członków, nie można dodać transakcji.");
        }

        double amountPerUser = transactionDTO.getAmount() / selectedMembers.size();
        boolean expense = "EXPENSE".equals(transactionDTO.getType());

        for (Membership member : selectedMembers) {
            User otherUser = member.getUser();

            if (!otherUser.getId().equals(currentUser.getId())) {
                Debt debt = new Debt();
                debt.setDebtor(expense ? otherUser : currentUser);
                debt.setCreditor(expense ? currentUser : otherUser);
                debt.setGroup(group);
                debt.setAmount(amountPerUser);
                debt.setTitle(transactionDTO.getTitle());

                debtRepository.save(debt);

                String messageText = String.format(Locale.US, "%s dodał wydatek \"%s\" w grupie %s. Twoja część: %.2f zł.",
                        currentUser.getEmail(), transactionDTO.getTitle(), group.getName(), amountPerUser);

                GroupNotificationDTO notification = GroupNotificationDTO.builder()
                        .type("GROUP_EXPENSE_ADDED")
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .title(transactionDTO.getTitle())
                        .amount(transactionDTO.getAmount())
                        .userShare(amountPerUser)
                        .createdByEmail(currentUser.getEmail())
                        .message(messageText)
                        .build();

                // LOGOWANIE DLA CELÓW DEBUGOWANIA
                System.out.println("DEBUG: Wysyłam powiadomienie do: " + otherUser.getEmail());

                // Zmieniono ścieżkę na /queue/notifications, aby była zgodna z frontendem
                messagingTemplate.convertAndSendToUser(
                        otherUser.getEmail(),
                        "/queue/notifications",
                        notification
                );
            }
        }
    }

    private List<Membership> selectParticipants(GroupTransactionDTO transactionDTO, List<Membership> members, User currentUser) {
        List<Long> selectedUserIds = transactionDTO.getSelectedUserIds();

        if (selectedUserIds == null || selectedUserIds.isEmpty()) {
            return members;
        }

        Set<Long> uniqueSelectedUserIds = new HashSet<>(selectedUserIds);
        List<Membership> selectedMembers = members.stream()
                .filter(membership -> uniqueSelectedUserIds.contains(membership.getUser().getId()))
                .toList();

        if (selectedMembers.size() != uniqueSelectedUserIds.size()) {
            throw new IllegalStateException("Wszyscy wybrani uzytkownicy musza byc członkami grupy.");
        }

        boolean currentUserSelected = selectedMembers.stream()
                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));

        if (!currentUserSelected) {
            throw new IllegalStateException("Aktualny uzytkownik musi byc uczestnikiem transakcji grupowej.");
        }

        if (selectedMembers.size() < 2) {
            throw new IllegalStateException("Transakcja grupowa wymaga co najmniej dwoch uczestnikow.");
        }

        return selectedMembers;
    }
}