package pk.ni.pasir_ostrega_tymoteusz.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pk.ni.pasir_ostrega_tymoteusz.dto.MembershipDTO;
import pk.ni.pasir_ostrega_tymoteusz.model.Group;
import pk.ni.pasir_ostrega_tymoteusz.model.Membership;
import pk.ni.pasir_ostrega_tymoteusz.model.User;
import pk.ni.pasir_ostrega_tymoteusz.repository.GroupRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.MembershipRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock private MembershipRepository membershipRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks private MembershipService membershipService;

    private User owner;
    private User nonOwner;
    private Group group;
    private Membership membership;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        nonOwner = new User();
        nonOwner.setId(2L);

        group = new Group();
        group.setId(10L);
        group.setOwner(owner);

        membership = new Membership();
        membership.setId(100L);
        membership.setGroup(group);
        membership.setUser(owner);
    }

    @Test
    void shouldThrowWhenNonOwnerAddsMember() {
        MembershipDTO dto = new MembershipDTO();
        dto.setGroupId(10L);
        dto.setUserEmail("test@test.com");

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(currentUserService.getCurrentUser()).thenReturn(nonOwner);

        assertThrows(AccessDeniedException.class, () -> membershipService.addMember(dto));
    }

    @Test
    void shouldThrowWhenNonMemberFetchesMembers() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(currentUserService.getCurrentUser()).thenReturn(nonOwner);
        when(membershipRepository.existsByGroupIdAndUserId(10L, 2L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> membershipService.getGroupMembers(10L));
    }

    @Test
    void shouldThrowWhenRemovingOwner() {
        when(membershipRepository.findById(100L)).thenReturn(Optional.of(membership));
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        assertThrows(IllegalStateException.class, () -> membershipService.removeMember(100L));
    }

    @Test
    void shouldRemoveMemberProperly() {
        Membership nonOwnerMembership = new Membership();
        nonOwnerMembership.setId(101L);
        nonOwnerMembership.setGroup(group);
        nonOwnerMembership.setUser(nonOwner);

        when(membershipRepository.findById(101L)).thenReturn(Optional.of(nonOwnerMembership));
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        membershipService.removeMember(101L);

        verify(membershipRepository, times(1)).delete(nonOwnerMembership);
    }
}