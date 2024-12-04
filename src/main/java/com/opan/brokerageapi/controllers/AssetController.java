package com.opan.brokerageapi.controllers;

import com.opan.brokerageapi.entities.Asset;
import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.requests.AssetRequestDto;
import com.opan.brokerageapi.services.AssetService;
import com.opan.brokerageapi.services.CustomerService;
import com.opan.brokerageapi.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private CustomerService customerService;

    @GetMapping
    @PreAuthorize("(hasRole('CUSTOMER') and #customerId == principal.id) or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse< List < Asset >>> listAssets(
            @PathVariable Long customerId,
            @RequestParam(required = false) String assetName) {
        Customer customer = customerService.getCustomerById(customerId);
        List<Asset> assets = assetService.listAssets(customer, assetName);
        return ResponseEntity.ok(new ApiResponse<>(true, "Assets retrieved successfully", assets));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') and #customerId == principal.id")
    public ResponseEntity<ApiResponse< Asset >> addAsset(
            @PathVariable Long customerId,
            @RequestBody AssetRequestDto asset) {
        Customer customer = customerService.getCustomerById(customerId);
        Asset newAsset = new Asset();
        newAsset.setCustomer(customer);
        newAsset.setAssetName(asset.getAssetName());
        newAsset.setSize(asset.getSize());
        newAsset.setUsableSize(asset.getSize());
        assetService.addAsset(customer, newAsset);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset added successfully", newAsset));
    }
}