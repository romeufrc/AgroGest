package com.example.AgroGestao.repository;

import com.example.AgroGestao.model.Insumos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsumosRepository extends JpaRepository<Insumos, Long> {
}
