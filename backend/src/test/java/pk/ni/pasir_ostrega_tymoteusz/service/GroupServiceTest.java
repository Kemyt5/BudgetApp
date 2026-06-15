package pk.ni.pasir_ostrega_tymoteusz.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pk.ni.pasir_ostrega_tymoteusz.dto.GroupDTO;
import pk.ni.pasir_ostrega_tymoteusz.model.Group;
import pk.ni.pasir_ostrega_tymoteusz.model.Membership;
import pk.ni.pasir_ostrega_tymoteusz.model.User;
import pk.ni.pasir_ostrega_tymoteusz.repository.DebtRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.GroupRepository;
import pk.ni.pasir_ostrega_tymoteusz.repository.MembershipRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private GroupService groupService;

    private User owner;
    private Group group;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        group = new Group();
        group.setId(10L);
        group.setOwner(owner);
    }

    @Test
    void shouldAddOwnerAsMemberWhenCreatingGroup() {
        GroupDTO dto = new GroupDTO();
        dto.setName("Test Group");

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        Group createdGroup = groupService.createGroup(dto);

        assertNotNull(createdGroup);
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerDeletesGroup() {
        User nonOwner = new User();
        nonOwner.setId(2L);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(currentUserService.getCurrentUser()).thenReturn(nonOwner);

        assertThrows(AccessDeniedException.class, () -> groupService.deleteGroup(10L));
    }

    @Test
    void shouldDeleteGroupAndAssociatedDataWhenOwnerDeletesGroup() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        groupService.deleteGroup(10L);

        verify(debtRepository, times(1)).deleteByGroupId(10L);
        verify(membershipRepository, times(1)).deleteByGroupId(10L);
        verify(groupRepository, times(1)).delete(group);
    }

    @Test
    void shouldReturnGroupsWhenUserIsMember() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(groupRepository.findByMemberships_User(owner)).thenReturn(java.util.List.of(group));

        java.util.List<Group> groups = groupService.getAllGroups();

        assertFalse(groups.isEmpty());
        assertEquals(1, groups.size());
        assertEquals(10L, groups.get(0).getId());
    }
}