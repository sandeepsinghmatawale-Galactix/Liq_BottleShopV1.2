package com.barinventory.brands.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.barinventory.brands.entity.Brand;
import com.barinventory.brands.entity.BrandSize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single-page brand creation DTO. Carries brand fields + an inline list of
 * sizes. Used by the brand-create.html form.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandFormDTO {

	// ── Brand fields ──────────────────────────────────────────
	private Long id; // null on create, populated on edit
	private String name;
	private String parentCompany;
	private Brand.Category category;
	private String exciseCode;

	@Builder.Default
	private boolean active = true;

	// ── Inline sizes ──────────────────────────────────────────
	@Builder.Default
	private List<SizeRow> sizes = new ArrayList<>();

	// ── Nested DTO for each size row ──────────────────────────
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SizeRow {
		private String sizeLabel; // "180ml", "750ml", etc.
		
		private BrandSize.Packaging packaging;
		private BigDecimal price;
		private Double abvPercent;
		private Integer displayOrder;
	}
}