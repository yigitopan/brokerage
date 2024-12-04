package com.opan.brokerageapi.services;

import com.opan.brokerageapi.entities.Asset;
import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.repositories.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    public List < Asset > listAssets(Customer customer, String assetNameFilter) {
        if (assetNameFilter != null && !assetNameFilter.isEmpty()) {
            return assetRepository.findByCustomerAndAssetNameContainingIgnoreCase(customer, assetNameFilter);
        } else {
            return assetRepository.findByCustomer(customer);
        }
    }

    public Asset addAsset(Customer customer, Asset asset) {
        asset.setCustomer(customer);
        return assetRepository.save(asset);
    }

}