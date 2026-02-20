package br.com.tisaicore.repository;

import br.com.tisaicore.entity.SisFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SisFileRepository extends JpaRepository<SisFile, Long> {
}
