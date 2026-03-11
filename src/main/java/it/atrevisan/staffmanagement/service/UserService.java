package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.mapper.UserMapper;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.PersonRepository;
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
    private final PersonRepository personRepository;

    private static User buildUser(String username, String password, Set<String> roles, boolean enabled) {
        return User.builder()
                .username(username)
                .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                .enabled(enabled)
                .roles(roles)
                .build();
    }

    @Transactional
    public void createUser(String username, String password, Set<String> roles){
        createUser(username, password, roles, true);
    }

    @Transactional
    public void createUser(String username, String password, Set<String> roles, boolean enabled){
        log.debug("Creating User [username={}, roles={}, enabled={}]", username, roles, enabled);
        if (!userExists(username)) {
            User user = buildUser(username, password, roles, enabled);
            userRepository.save(user);
        } else {
            throw new IllegalStateException("User already defined");
        }
        log.info("Created User [username={}, roles={}, enabled={}]", username, roles, enabled);
    }

    @Transactional
    public void createIfNotExist(String username, String password, Set<String> roles){
        if (!userExists(username)) {
            createUser(username, password, roles);
        }
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean checkUser(String username, String password){
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null &&
                user.isEnabled() &&
                BCrypt.checkpw(password, user.getPassword());
    }

    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserMapper::map);
    }

    @Transactional
    public void updatePassword(String username, String oldPassword, String newPassword){
        log.debug("Updating password fo User {}", username);
        Optional<User> oldUser = userRepository.findByUsername(username);
        if(!oldUser.isPresent()){
            throw new IllegalArgumentException("Username does not exist");
        }

        User user = oldUser.get();

        if(!BCrypt.checkpw(oldPassword, user.getPassword())){
            throw new IllegalArgumentException("Old Password does not match");
        }

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));

        userRepository.save(user);
        log.info("Updated password fo User {}", username);
    }

    public List<UserDTO> getAllUsers(){
        return userRepository.findAll().stream().map(UserMapper::map).collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(String username) {
        log.debug("Deleting User {}", username);
        userRepository.deleteByUsername(username);
        log.info("Deleted User {}", username);
    }

    public void toggleUser(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    log.debug("Toggling User {}. Enabled initial state {}", username, user.isEnabled());
                    user.setEnabled(!user.isEnabled());
                    userRepository.save(user);
                    log.info("Toggled User {}. Enabled final state {}", username, user.isEnabled());
                });

    }

    public void updateRoles(String username, Set<String> roles) {
        log.debug("Updating Roles for User {}. New roles {}", username, roles);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRoles(roles);
        userRepository.save(user);
        log.debug("Updated Roles for User {}. New roles {}", username, roles);
    }
}
