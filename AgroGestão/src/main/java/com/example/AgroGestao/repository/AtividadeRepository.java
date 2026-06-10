package com.example.AgroGestao.repository;

import com.example.AgroGestao.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    // Conta as atividades baseadas no ID da propriedade
    long countByPropriedadeId(Long propriedadeId);
}