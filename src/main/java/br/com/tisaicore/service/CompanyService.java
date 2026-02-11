package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.CreateCompanyRequest;
import br.com.tisaicore.dto.response.CompanyResponse;
import br.com.tisaicore.entity.Company;
import br.com.tisaicore.exception.ResourceNotFoundException;
import br.com.tisaicore.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        if (companyRepository.existsByCnpj(request.cnpj())) {
            throw new IllegalArgumentException("CNPJ already registered: " + request.cnpj());
        }

        Company company = new Company();
        company.setTradeName(request.tradeName());
        company.setLegalName(request.legalName());
        company.setCnpj(request.cnpj());
        company.setEmail(request.email());
        company.setPhone(request.phone());

        return CompanyResponse.from(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> findAll(Pageable pageable) {
        return companyRepository.findAll(pageable).map(CompanyResponse::from);
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        return CompanyResponse.from(findEntityById(id));
    }

    @Transactional
    public CompanyResponse update(Long id, CreateCompanyRequest request) {
        Company company = findEntityById(id);
        company.setTradeName(request.tradeName());
        company.setLegalName(request.legalName());
        company.setCnpj(request.cnpj());
        company.setEmail(request.email());
        company.setPhone(request.phone());

        return CompanyResponse.from(companyRepository.save(company));
    }

    @Transactional
    public void delete(Long id) {
        Company company = findEntityById(id);
        company.setActive(false);
        companyRepository.save(company);
    }

    public Company findEntityById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }
}
