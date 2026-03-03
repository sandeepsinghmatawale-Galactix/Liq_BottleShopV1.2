package com.barinventory.brands.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barinventory.brands.dtos.BrandDTO;
import com.barinventory.brands.dtos.BrandFormDTO;
import com.barinventory.brands.entity.Brand;
import com.barinventory.brands.entity.BrandSize;
import com.barinventory.brands.service.BrandService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BrandAdminController {

    private final BrandService brandService;

    // ── LIST ──────────────────────────────────────────────────────────
 /*   @GetMapping
    public String list(Model model) {
        model.addAttribute("brands", brandService.getAllActiveBrands());
        return "admin/brands/brand-list";
    }*/
    
    @GetMapping("/admin/brands")
    public String listBrands(Model model) {

        List<Brand> brands = brandService.getAllBrands();

        long activeCount = brands.stream()
                                 .filter(Brand::isActive)
                                 .count();

        model.addAttribute("brands", brands);
        model.addAttribute("activeCount", activeCount);

        return "admin/brands/brand-list";
    }

    // ── CREATE: show single-page form ─────────────────────────────────
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("brandFormDTO",   new BrandFormDTO());
        model.addAttribute("categories",     Brand.Category.values());
        model.addAttribute("packagingOptions", BrandSize.Packaging.values());
        return "admin/brands/brand-create";
    }

    // ── CREATE: save brand + all sizes in one POST ────────────────────
    @PostMapping("/new-with-sizes")
    public String saveWithSizes(@ModelAttribute BrandFormDTO brandFormDTO,
                                RedirectAttributes ra) {
        try {
            brandService.createBrandWithSizes(brandFormDTO);
            ra.addFlashAttribute("successMsg",
                    "Brand '" + brandFormDTO.getName() + "' created successfully!");
            return "redirect:/admin/brands";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/brands/create";
        }
    }

    // ── EDIT: show single-page form pre-populated ─────────────────────
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        BrandDTO dto = brandService.getBrandById(id);

        // Map BrandDTO → BrandFormDTO so the same form handles both create & edit
        BrandFormDTO form = new BrandFormDTO();
        form.setId(dto.getId());
        form.setName(dto.getName());
        form.setParentCompany(dto.getParentCompany());
        form.setCategory(dto.getCategory());
        form.setExciseCode(dto.getExciseCode());
        form.setActive(dto.isActive());

        if (dto.getSizes() != null) {
            dto.getSizes().forEach(s -> {
                BrandFormDTO.SizeRow row = new BrandFormDTO.SizeRow();
                row.setSizeLabel(s.getSizeLabel());
                row.setPackaging(s.getPackaging());
                row.setPrice(s.getPrice());
                row.setAbvPercent(s.getAbvPercent());
                row.setDisplayOrder(s.getDisplayOrder());
                form.getSizes().add(row);
            });
        }

        model.addAttribute("brandFormDTO",   form);
        model.addAttribute("categories",     Brand.Category.values());
        model.addAttribute("packagingOptions", BrandSize.Packaging.values());
        return "admin/brands/brand-create";    // reuse the same template
    }

    // ── EDIT: save updated brand + sizes ──────────────────────────────
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute BrandFormDTO brandFormDTO,
                         RedirectAttributes ra) {
        try {
            brandService.updateBrandWithSizes(id, brandFormDTO);
            ra.addFlashAttribute("successMsg",
                    "Brand '" + brandFormDTO.getName() + "' updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/brands";
    }

    // ── DEACTIVATE (soft delete) ──────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            brandService.deactivateBrand(id);
            ra.addFlashAttribute("successMsg", "Brand deactivated.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/brands";
    }

    // ── OLD /new redirect (backward compat) ──────────────────────────
    @GetMapping("/new")
    public String legacyNew() {
        return "redirect:/admin/brands/create";
    }
}