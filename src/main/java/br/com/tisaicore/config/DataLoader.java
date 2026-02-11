package br.com.tisaicore.config;

import br.com.tisaicore.entity.Role;
import br.com.tisaicore.entity.User;
import br.com.tisaicore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("fabio.frutuoso@tisailag.com.br").isEmpty()) {
            User admin = new User();
            admin.setName("Fabio Frutuoso");
            admin.setEmail("fabio.frutuoso@tisailag.com.br");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setEmailActive(true);
            userRepository.save(admin);
        }
    }
}
