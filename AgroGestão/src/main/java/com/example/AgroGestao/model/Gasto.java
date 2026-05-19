package com.example.AgroGestao.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private Double valor;

    private LocalDate data;

    // Relacionando o gasto com a propriedade correspondente
    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public Gasto() {
    }

    public Gasto(Long id, String descricao, Double valor, LocalDate data, Propriedade propriedade) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.propriedade = propriedade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Propriedade getPropriedade() {
        return propriedade;
    }

    public void setPropriedade(Propriedade propriedade) {
        this.propriedade = propriedade;
    }
}