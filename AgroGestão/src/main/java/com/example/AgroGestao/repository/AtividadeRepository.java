package com.example.AgroGestao.repository;

import com.example.AgroGestao.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Indica que esta interface é responsável pelo acesso aos dados da entidade Atividade
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    // Conta quantas atividades existem em uma determinada propriedade
    long countByPropriedadeId(Long propriedadeId);

    // Retorna todas as atividades vinculadas a uma propriedade específica
    List<Atividade> findByPropriedadeId(Long propriedadeId);

    // Consulta personalizada para buscar todas as atividades pertencentes ao usuário logado
    @Query("SELECT a FROM Atividade a WHERE a.propriedade.usuario.id = :usuarioId")

    // Recebe o ID do usuário como parâmetro da consulta
    List<Atividade> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}