package com.barinventory.brands.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barinventory.brands.dtos.BrandDTO;
import com.barinventory.brands.dtos.BrandFormDTO;
import com.barinventory.brands.dtos.BrandSizeDTO;
import com.barinventory.brands.entity.Brand;
import com.barinventory.brands.entity.BrandSize;
import com.barinventory.brands.repository.BrandRepository;
import com.barinventory.brands.repository.BrandSizeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository     brandRepository;
    private final BrandSizeRepository brandSizeRepository;

    // ── CREATE (basic) ────────────────────────────────────────────────
    @Override
    @Transactional
    public BrandDTO createBrand(BrandDTO dto) {
        if (brandRepository.existsByNameIgnoreCase(dto.getName()))
            throw new RuntimeException("Brand already exists: " + dto.getName());
        Brand brand = Brand.builder()
                .name(dto.getName())
                .parentCompany(dto.getParentCompany())
                .category(dto.getCategory())
                .exciseCode(dto.getExciseCode())
                .active(true)
                .build();
        return mapToDTO(brandRepository.save(brand));
    }
    
    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
    // ── CREATE WITH SIZES (single-page form) ──────────────────────────
    @Override
    @Transactional
    public BrandDTO createBrandWithSizes(BrandFormDTO form) {
        if (brandRepository.existsByNameIgnoreCase(form.getName()))
            throw new RuntimeException("Brand already exists: " + form.getName());
        Brand brand = Brand.builder()
                .name(form.getName().trim())
                .parentCompany(form.getParentCompany())
                .category(form.getCategory())
                .exciseCode(form.getExciseCode())
                .active(form.isActive())
                .build();
        addSizeRows(brand, form);
        return mapToDTO(brandRepository.save(brand));
    }

    // ── UPDATE WITH SIZES (edit mode — same form, brand + sizes) ──────
    @Override
    @Transactional
    public BrandDTO updateBrandWithSizes(Long id, BrandFormDTO form) {
        Brand brand = brandRepository.findByIdWithSizes(id)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));

        brand.setName(form.getName().trim());
        brand.setParentCompany(form.getParentCompany());
        brand.setCategory(form.getCategory());
        brand.setExciseCode(form.getExciseCode());
        brand.setActive(form.isActive());

        // Replace sizes: clearSizes() + orphanRemoval deletes old rows from DB
        brand.clearSizes();
        addSizeRows(brand, form);

        return mapToDTO(brandRepository.save(brand));
    }

    // ── UPDATE (brand fields only) ────────────────────────────────────
    @Override
    @Transactional
    public BrandDTO updateBrand(Long id, BrandDTO dto) {
        Brand brand = brandRepository.findByIdWithSizes(id)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
        brand.setName(dto.getName());
        brand.setParentCompany(dto.getParentCompany());
        brand.setCategory(dto.getCategory());
        brand.setExciseCode(dto.getExciseCode());
        return mapToDTO(brandRepository.save(brand));
    }

    // ── DEACTIVATE BRAND ──────────────────────────────────────────────
    @Override
    @Transactional
    public void deactivateBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
        brand.setActive(false);
        brandRepository.save(brand);
    }

    // ── ADD SINGLE SIZE ───────────────────────────────────────────────
    @Override
    @Transactional
    public void addSizeToBrand(Long brandId, BrandSizeDTO dto) {
        Brand brand = brandRepository.findByIdWithSizes(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + brandId));
        if (brandSizeRepository.existsByBrandIdAndSizeLabelIgnoreCase(brandId, dto.getSizeLabel()))
            throw new RuntimeException("Size '" + dto.getSizeLabel() + "' already exists");
        BrandSize size = BrandSize.builder()
                .sizeLabel(dto.getSizeLabel())
                .price(dto.getPrice())
                .packaging(dto.getPackaging())
                .abvPercent(dto.getAbvPercent())
                .displayOrder(dto.getDisplayOrder())
                .active(true)
                .build();
        brand.addSize(size);
        brandRepository.save(brand);
    }

    // ── DEACTIVATE SIZE ───────────────────────────────────────────────
    @Override
    @Transactional
    public void deactivateSize(Long sizeId) {
        BrandSize size = brandSizeRepository.findByIdAndActiveTrue(sizeId)
                .orElseThrow(() -> new RuntimeException("Size not found: " + sizeId));
        size.setActive(false);
        brandSizeRepository.save(size);
    }

    // ── READ ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public BrandDTO getBrandById(Long id) {
        return brandRepository.findByIdWithSizes(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandDTO> getAllActiveBrands() {
        return brandRepository.findAllActiveWithSizes()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ── HELPERS ───────────────────────────────────────────────────────
    private void addSizeRows(Brand brand, BrandFormDTO form) {
        if (form.getSizes() == null) return;
        for (BrandFormDTO.SizeRow row : form.getSizes()) {
            if (row.getSizeLabel() == null || row.getSizeLabel().isBlank()) continue;
            if (row.getPrice() == null) continue;
            BrandSize size = BrandSize.builder()
                    .sizeLabel(row.getSizeLabel().trim())
                    .price(row.getPrice())
                    .packaging(row.getPackaging() != null
                            ? row.getPackaging()
                            : BrandSize.Packaging.GLASS_BOTTLE)
                    .abvPercent(row.getAbvPercent())
                    .displayOrder(row.getDisplayOrder())
                    .active(true)
                    .build();
            brand.addSize(size);
        }
    }

    // ── MAPPER ────────────────────────────────────────────────────────
    private BrandDTO mapToDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .parentCompany(brand.getParentCompany())
                .category(brand.getCategory())
                .exciseCode(brand.getExciseCode())
                .active(brand.isActive())
                .sizes(brand.getSizes().stream()
                        .filter(BrandSize::isActive)
                        .map(s -> BrandSizeDTO.builder()
                                .id(s.getId())
                                .sizeLabel(s.getSizeLabel())
                                .price(s.getPrice())
                                .packaging(s.getPackaging())
                                .abvPercent(s.getAbvPercent())
                                .displayOrder(s.getDisplayOrder())
                                .active(s.isActive())
                                .build())
                        .toList())
                .build();
    }
}