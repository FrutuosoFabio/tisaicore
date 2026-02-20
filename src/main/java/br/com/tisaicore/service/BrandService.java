package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.BrandRequest;
import br.com.tisaicore.dto.response.BrandResponse;
import br.com.tisaicore.entity.Brand;
import br.com.tisaicore.exception.ResourceNotFoundException;
import br.com.tisaicore.repository.BrandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Marca já cadastrada: " + request.name());
        }

        Brand brand = new Brand();
        brand.setName(request.name());
        brand.setDescription(request.description());

        return BrandResponse.from(brandRepository.save(brand));
    }

    @Transactional(readOnly = true)
    public Page<BrandResponse> findAll(Pageable pageable) {
        return brandRepository.findAll(pageable).map(BrandResponse::from);
    }

    @Transactional(readOnly = true)
    public BrandResponse findById(Long id) {
        return BrandResponse.from(findEntityById(id));
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = findEntityById(id);
        brand.setName(request.name());
        brand.setDescription(request.description());
        return BrandResponse.from(brandRepository.save(brand));
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = findEntityById(id);
        brand.setActive(false);
        brandRepository.save(brand);
    }

    public Brand findEntityById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", id));
    }
}
