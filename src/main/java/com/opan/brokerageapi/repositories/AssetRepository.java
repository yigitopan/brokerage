package com.opan.brokerageapi.repositories;

import com.opan.brokerageapi.entities.Asset;
import com.opan.brokerageapi.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCustomer(Customer customer);
    List<Asset> findByCustomerAndAssetNameContainingIgnoreCase(Customer customer, String assetName);
}