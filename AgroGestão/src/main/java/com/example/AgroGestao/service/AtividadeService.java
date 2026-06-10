package com.example.AgroGestao.service;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AtividadeService {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private InsumosRepository insumoRepository;

    // MEU MÉTODO DE AUTOMATIZAÇÃO DE ESTOQUE:
    // Criei essa regra de negócio para que, sempre que eu registrar um manejo, o sistema
    // tente dar baixa nas sementes ou adubos automaticamente sem o usuário fazer na mão.
    public Atividade salvarEAtualizarEstoque(Atividade atividade) {

        // 1. Salva a atividade de campo normalmente na tabela do banco de dados
        Atividade atividadeSalva = atividadeRepository.save(atividade);

        // 2. Regra condicional: só tenta abater insumos se for um manejo de Plantio ou Adubação
        String tipo = atividade.getTipo().toLowerCase();
        if (tipo.contains("plantio") || tipo.contains("adubacao") || tipo.contains("adubação")) {

            // Puxo tudo o que eu tenho guardado no meu armazém de insumos
            List<Insumos> estoque = insumoRepository.findAll();

            for (Insumos insumo : estoque) {

                // Inteligência de Vínculo por Texto: se o nome ou a descrição da tarefa contiver
                // o nome do insumo (ex: Tarefa "Adubação com NPK" bate com Insumo "NPK")
                if (atividade.getDescricao().toLowerCase().contains(insumo.getNome().toLowerCase()) ||
                        atividade.getNome().toLowerCase().contains(insumo.getNome().toLowerCase())) {

                    // CORREÇÃO DO BUG: Mudei de double para int (50) para casar certinho com o tipo Integer
                    // que configuramos lá na nossa classe modelo Insumos.java!
                    int quantidadeUsada = 50;

                    // Checa se a quantidade física atual no galpão suporta dar essa baixa
                    if (insumo.getQuantidade() != null && insumo.getQuantidade() >= quantidadeUsada) {

                        // Faz a subtração matemática segura e atualiza o saldo do insumo
                        insumo.setQuantidade(insumo.getQuantidade() - quantidadeUsada);

                        // Executa o comando UPDATE no banco gravando a nova quantidade física
                        insumoRepository.save(insumo);

                        System.out.println("Estoque atualizado automaticamente! Baixa de " + quantidadeUsada + " un de " + insumo.getNome());
                    } else {
                        System.out.println("Aviso: Saldo de " + insumo.getNome() + " é menor do que a quantidade exigida no manejo.");
                    }
                    break; // Para o laço de repetição porque já localizou e tratou o insumo correto
                }
            }
        }

        return atividadeSalva;
    }
}