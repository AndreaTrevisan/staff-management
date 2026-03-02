package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    private static User getUser(String username, String password, Set<String> roles) {
        return User.builder()
                .username(username)
                .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                .enabled(true)
                .roles(roles)
                .build();
    }
    
    public void save(String username, String password, Set<String> roles){
        if (!userExists(username)) {
            User user = getUser(username, password, roles);
            userRepository.save(user);
        } else {
            throw new IllegalStateException("User already defined");
        }
    }
    
    public void saveIfNotExist(String username, String password, Set<String> roles){
        if (!userExists(username)) {
            save(username, password, roles);
        }
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
