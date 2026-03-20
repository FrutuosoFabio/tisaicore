package br.com.tisaicore.config;

import br.com.tisaicore.entity.Company;
import br.com.tisaicore.entity.Role;
import br.com.tisaicore.entity.User;
import br.com.tisaicore.repository.CompanyRepository;
import br.com.tisaicore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-password:changeme}")
    private String defaultPassword;

    public DataLoader(UserRepository userRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("fabio_frutuoso@hotmail.com").isEmpty()) {
            User admin = new User();
            admin.setName("Fabio Frutuoso");
            admin.setEmail("fabio_frutuoso@hotmail.com");
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setEmailActive(true);
            userRepository.save(admin);
        }
        if (userRepository.findByEmail("junio20018@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setName("Lucivaldo Junio");
            admin.setEmail("junio20018@gmail.com");
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setEmailActive(true);
            userRepository.save(admin);
        }

        if (companyRepository.findByCnpj("00000000000100").isEmpty()) {
            Company testCompany = new Company();
            testCompany.setTradeName("Empresa Teste");
            testCompany.setLegalName("Empresa Teste LTDA");
            testCompany.setCnpj("00000000000100");
            testCompany.setEmail("teste@empresateste.com");
            testCompany.setPhone("11999999999");
            testCompany = companyRepository.save(testCompany);

            if (userRepository.findByEmail("cliente@teste.com").isEmpty()) {
                User seller = new User();
                seller.setName("Cliente Teste");
                seller.setEmail("cliente@teste.com");
                seller.setPassword(passwordEncoder.encode(defaultPassword));
                seller.setRole(Role.SELLER);
                seller.setActive(true);
                seller.setEmailActive(true);
                seller.setCompany(testCompany);
                userRepository.save(seller);
            }
        }
    }
}
