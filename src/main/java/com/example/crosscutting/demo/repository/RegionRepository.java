package com.example.crosscutting.demo.repository;


import com.example.crosscutting.demo.common.db.Schema;
import com.example.crosscutting.demo.common.db.Table;
import org.springframework.stereotype.Repository;


@Repository
@Table(schema= Schema.SYSTEM)
public interface RegionRepository {
    void getRegions();
}
