package com.example.AgroGestao.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "safras")
public class Safra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cultura;
    private LocalDate dataInicio;
    private LocalDate dataFimPrevista; // Nome alinhado com o formulário
    private String status;

    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade propriedade;

    public Safra() {}

    public Safra(Long id, String nome, String cultura, LocalDate dataInicio, LocalDate dataFimPrevista, String status, Propriedade propriedade) {
        this.id = id;
        this.nome = nome;
        this.cultura = cultura;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.status = status;
        this.propriedade = propriedade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCultura() { return cultura; }
    public void setCultura(String cultura) { this.cultura = cultura; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public void setDataFimPrevista(LocalDate dataFimPrevista) { this.dataFimPrevista = dataFimPrevista; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
}