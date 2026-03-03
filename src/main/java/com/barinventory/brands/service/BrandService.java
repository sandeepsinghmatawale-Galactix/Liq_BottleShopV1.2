package com.barinventory.brands.service;

import java.util.List;

import com.barinventory.brands.dtos.BrandDTO;
import com.barinventory.brands.dtos.BrandFormDTO;
import com.barinventory.brands.dtos.BrandSizeDTO;
import com.barinventory.brands.entity.Brand;

public interface BrandService {

	BrandDTO createBrand(BrandDTO dto);

	BrandDTO createBrandWithSizes(BrandFormDTO formDTO);

	BrandDTO updateBrand(Long id, BrandDTO dto);

	void deactivateBrand(Long id);

	void addSizeToBrand(Long brandId, BrandSizeDTO dto);

	void deactivateSize(Long sizeId);

	BrandDTO getBrandById(Long id);

	List<BrandDTO> getAllActiveBrands();

	/** Edit mode: replaces all sizes with the new set from the form */
	BrandDTO updateBrandWithSizes(Long id, BrandFormDTO form);

	List<Brand> getAllBrands();
}
