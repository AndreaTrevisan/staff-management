package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void createUserThreeArgsUsesEnabledTrueAndSavesHashedPassword() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);

        userService.createUser("admin", "secret", Collections.singleton("ADMIN"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("admin", saved.getUsername());
        assertTrue(saved.isEnabled());
        assertNotEquals("secret", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"));
    }

    @Test
    void createUserFourArgsRespectsEnabledFlag() {
        when(userRepository.existsByUsername("staff")).thenReturn(false);

        userService.createUser("staff", "secret", Collections.singleton("STAFF"), false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isEnabled());
    }

    @Test
    void createUserThrowsWhenAlreadyExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.createUser("admin", "secret", Collections.singleton("ADMIN")));

        assertEquals("User already defined", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createIfNotExistCreatesOnlyWhenMissing() {
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        userService.createIfNotExist("newUser", "pwd", Collections.singleton("STAFF"));
        userService.createIfNotExist("existing", "pwd", Collections.singleton("STAFF"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void userExistsDelegatesToRepository() {
        when(userRepository.existsByUsername("u1")).thenReturn(true);
        assertTrue(userService.userExists("u1"));
    }

    @Test
    void checkUserCoversMissingDisabledAndValid() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        User disabled = User.builder()
                .username("disabled")
                .password("$2a$10$7EqJtq98hPqEX7fNZaFWoOHiNnDx5A5u5Vf3L7hIwrKyYVJZZzK5e")
                .enabled(false)
                .build();
        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(disabled));

        User enabled = User.builder()
                .username("enabled")
                .password("$2a$10$7EqJtq98hPqEX7fNZaFWoOHiNnDx5A5u5Vf3L7hIwrKyYVJZZzK5e")
                .enabled(true)
                .build();
        when(userRepository.findByUsername("enabled")).thenReturn(Optional.of(enabled));

        assertFalse(userService.checkUser("missing", "password"));
        assertFalse(userService.checkUser("disabled", "password"));
        assertTrue(userService.checkUser("enabled", "password"));
    }

    @Test
    void findByUsernameMapsWhenPresentAndEmptyWhenMissing() {
        User user = User.builder().username("mario").password("p").enabled(true).roles(Collections.singleton("STAFF")).build();
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

        assertTrue(userService.findByUsername("mario").isPresent());
        assertFalse(userService.findByUsername("none").isPresent());
    }

    @Test
    void updatePasswordThrowsWhenUsernameMissing() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.updatePassword("mario", "old", "new"));
    }

    @Test
    void updatePasswordThrowsWhenOldPasswordMismatch() {
        User user = User.builder()
                .username("mario")
                .password("$2a$10$7EqJtq98hPqEX7fNZaFWoOHiNnDx5A5u5Vf3L7hIwrKyYVJZZzK5e")
                .enabled(true)
                .build();
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.updatePassword("mario", "wrong", "newPassword"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePasswordSavesHashedNewPasswordWhenOldMatches() {
        User user = User.builder()
                .username("mario")
                .password("$2a$10$7EqJtq98hPqEX7fNZaFWoOHiNnDx5A5u5Vf3L7hIwrKyYVJZZzK5e")
                .enabled(true)
                .build();
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(user));

        userService.updatePassword("mario", "password", "newPassword");

        verify(userRepository).save(user);
        assertNotEquals("newPassword", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2"));
    }

    @Test
    void getAllUsersMapsList() {
        User u1 = User.builder().username("u1").enabled(true).roles(Collections.singleton("STAFF")).build();
        User u2 = User.builder().username("u2").enabled(false).roles(Collections.singleton("ADMIN")).person(Person.builder().documentId("D1").build()).build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    void deleteUserDelegatesToRepository() {
        userService.deleteUser("u1");
        verify(userRepository).deleteByUsername("u1");
    }

    @Test
    void toggleUserThrowsWhenNotFound() {
        when(userRepository.findByUsername("u1")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> userService.toggleUser("u1"));
    }

    @Test
    void toggleUserFlipsStateAndSaves() {
        User user = User.builder().username("u1").enabled(true).build();
        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));

        userService.toggleUser("u1");

        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void updateRolesThrowsWhenNotFound() {
        when(userRepository.findByUsername("u1")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> userService.updateRoles("u1", Collections.singleton("ADMIN")));
    }

    @Test
    void updateRolesSavesWhenFound() {
        User user = User.builder().username("u1").enabled(true).roles(Collections.singleton("STAFF")).build();
        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));

        userService.updateRoles("u1", Collections.singleton("ADMIN"));

        assertEquals(Collections.singleton("ADMIN"), user.getRoles());
        verify(userRepository).save(user);
    }
}
