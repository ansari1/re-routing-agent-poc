package com.commerce.inventory.domain;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByRegionNotIn(Collection<String> regions, Sort sort);
}
