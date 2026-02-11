package br.com.tisaicore.repository;

import br.com.tisaicore.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
}
