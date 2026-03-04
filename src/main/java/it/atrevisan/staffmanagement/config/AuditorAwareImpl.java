package it.atrevisan.staffmanagement.config;

import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.views.session.SessionUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        try {
            UserDTO user = SessionUtils.getSessionUser();
            if (user != null) {
                return Optional.of(user.getUsername());
            }

            return Optional.of("SYSTEM");
        } catch (Exception e){
            return Optional.of("SYSTEM");
        }
    }
}