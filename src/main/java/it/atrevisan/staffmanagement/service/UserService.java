package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.mapper.UserMapper;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private static User buildUser(String username, String password, Set<String> roles, boolean enabled) {
        return User.builder()
                .username(username)
                .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                .enabled(enabled)
                .roles(roles)
                .build();
    }

    @Transactional
    public void createUser(String username, String password, Set<String> roles) {
        createUser(username, password, roles, true);
    }

    @Transactional
    public void createUser(String username, String password, Set<String> roles, boolean enabled) {
        log.debug("Creating user [username={}, roles={}, enabled={}]", username, roles, enabled);
        if (userExists(username)) {
            throw new IllegalStateException("User already defined");
        }

        User user = buildUser(username, password, roles, enabled);
        userRepository.save(user);
        log.info("Created user [username={}, enabled={}]", username, enabled);
    }

    @Transactional
    public void createIfNotExist(String username, String password, Set<String> roles) {
        if (!userExists(username)) {
            createUser(username, password, roles);
        }
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean checkUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && user.isEnabled() && BCrypt.checkpw(password, user.getPassword());
    }

    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserMapper::map);
    }

    @Transactional
    public void updatePassword(String username, String oldPassword, String newPassword) {
        log.debug("Updating password for user [username={}]", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username does not exist"));

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.save(user);
        log.info("Updated password for user [username={}]", username);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::map).collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(String username) {
        log.debug("Deleting user [username={}]", username);
        userRepository.deleteByUsername(username);
        log.info("Deleted user [username={}]", username);
    }

    @Transactional
    public void toggleUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        boolean initialState = user.isEnabled();
        user.setEnabled(!initialState);
        userRepository.save(user);

        log.info("Toggled user [username={}, previousEnabled={}, newEnabled={}]",
                username, initialState, user.isEnabled());
    }

    @Transactional
    public void updateRoles(String username, Set<String> roles) {
        log.debug("Updating roles for user [username={}, roles={}]", username, roles);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        user.setRoles(roles);
        userRepository.save(user);
        log.info("Updated roles for user [username={}, roles={}]", username, roles);
    }
}
