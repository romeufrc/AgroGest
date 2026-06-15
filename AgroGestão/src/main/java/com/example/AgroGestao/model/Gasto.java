package com.example.AgroGestao.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "gastos")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;
    private Double valor;

    @Column(name = "data_gasto")
    private LocalDate dataGasto;

    //Força o carregamento imediato do relacionamento com a Propriedade
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    //Construtores
    public Gasto() {}

    public Gasto(Long id, String descricao, Double valor, LocalDate dataGasto, Propriedade propriedade) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.dataGasto = dataGasto;
        this.propriedade = propriedade;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public LocalDate getDataGasto() { return dataGasto; }
    public void setDataGasto(LocalDate dataGasto) { this.dataGasto = dataGasto; }

    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propiedad) { this.propriedade = propiedad; }
}