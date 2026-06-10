package com.example.AgroGestao.repository;

import com.example.AgroGestao.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; //retorna a lista de gastos

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    //
    List<Gasto> findByPropriedadeId(Long propriedadeId);
}