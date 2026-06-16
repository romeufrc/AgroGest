package com.example.AgroGestao.repository;

import com.example.AgroGestao.model.Insumos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsumosRepository extends JpaRepository<Insumos, Long> {

    @Query("SELECT i FROM Insumos i WHERE i.propriedade.usuario.id = :usuarioId")
    List<Insumos> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}