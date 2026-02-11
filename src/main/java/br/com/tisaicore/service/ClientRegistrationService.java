package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.ClientRegistrationRequest;
import br.com.tisaicore.dto.response.ClientRegistrationResponse;
import br.com.tisaicore.dto.response.CnpjResponse;
import br.com.tisaicore.dto.response.CompanyResponse;
import br.com.tisaicore.dto.response.UserResponse;
import br.com.tisaicore.entity.Company;
import br.com.tisaicore.entity.Role;
import br.com.tisaicore.entity.User;
import br.com.tisaicore.repository.CompanyRepository;
import br.com.tisaicore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientRegistrationService {

    private final CnpjValidationService cnpjValidationService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientRegistrationService(CnpjValidationService cnpjValidationService,
                                     UserRepository userRepository,
                                     CompanyRepository companyRepository,
                                     PasswordEncoder passwordEncoder) {
        this.cnpjValidationService = cnpjValidationService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClientRegistrationResponse register(ClientRegistrationRequest request) {
        CnpjResponse cnpjResponse = cnpjValidationService.validate(request.cnpj());

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use: " + request.email());
        }

        String sanitizedCnpj = request.cnpj().replaceAll("[^0-9]", "");
        if (companyRepository.existsByCnpj(sanitizedCnpj)) {
            throw new IllegalArgumentException("CNPJ already registered: " + sanitizedCnpj);
        }

        Company company = new Company();
        company.setTradeName(
                cnpjResponse.nomeFantasia() != null && !cnpjResponse.nomeFantasia().isBlank()
                        ? cnpjResponse.nomeFantasia()
                        : request.tradeName()
        );
        company.setLegalName(cnpjResponse.razaoSocial());
        company.setCnpj(sanitizedCnpj);
        company.setPhone(request.phone());
        company = companyRepository.save(company);

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.SELLER);
        user.setCompany(company);
        user = userRepository.save(user);

        return new ClientRegistrationResponse(
                UserResponse.from(user),
                CompanyResponse.from(company)
        );
    }
}
