package com.example.AgroGestao.repository;

import com.example.AgroGestao.model.Propriedade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// MEU REPOSITÓRIO DA PROPRIEDADE:
// Deixei essa interface estendendo o JpaRepository para o Spring Boot criar os métodos
// padrão de CRUD (findAll, save, findById, delete) automaticamente, sem eu precisar escrever SQL na mão.
@Repository
public interface PropriedadeRepository extends JpaRepository<Propriedade, Long> {
}