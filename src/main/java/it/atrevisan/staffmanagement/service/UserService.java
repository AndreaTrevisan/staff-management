package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.mapper.UserMapper;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    private static User buildUser(String username, String password, Set<String> roles, boolean enabled) {
        return User.builder()
                .username(username)
                .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                .enabled(enabled)
                .roles(roles)
                .build();
    }

    public void createUser(String username, String password, Set<String> roles){
        createUser(username, password, roles, true);
    }
    
    public void createUser(String username, String password, Set<String> roles, boolean enabled){
        if (!userExists(username)) {
            User user = buildUser(username, password, roles, enabled);
            userRepository.save(user);
        } else {
            throw new IllegalStateException("User already defined");
        }
    }
    
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

    public void updatePassword(String username, String oldPassword, String newPassword){
        Optional<User> oldUser = userRepository.findByUsername(username);
        if(!oldUser.isPresent()){
            throw new IllegalArgumentException("Username does not exist");
        }

        User user = oldUser.get();

        if(BCrypt.checkpw(oldPassword, user.getPassword())){
            throw new IllegalArgumentException("Old Password does not match");
        }

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));

        userRepository.save(user);
    }

    public List<UserDTO> getAllUsers(){
        return userRepository.findAll().stream().map(UserMapper::map).collect(Collectors.toList());
    }

    public void deleteUser(String username) {
        userRepository.deleteByUsername(username);
    }

    public void toggleUser(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.setEnabled(!user.isEnabled());
                    userRepository.save(user);
                });

    }

    public void updateRoles(String username, Set<String> roles) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRoles(roles);
        userRepository.save(user);
    }
}
