package com.example.AgroGestao.repository;

import com.example.AgroGestao.model.Safra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafraRepository extends JpaRepository<Safra, Long> {
    // Adicione esta linha para contar safras por propriedade
    long countByPropriedadeId(Long propriedadeId);
}