package com.sprintjava.controller;

import com.sprintjava.dao.StrategyDAO;
import com.sprintjava.exception.ApiException;
import com.sprintjava.model.Strategy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

    private final StrategyDAO strategyDAO;

    public StrategyController(StrategyDAO strategyDAO) {
        this.strategyDAO = strategyDAO;
    }

    @GetMapping
    public Map<String, List<Strategy>> listar(@RequestParam(value = "finding_type", required = false) String findingType) {
        try {
            List<Strategy> strategies = strategyDAO.listarTodas();
            if (findingType != null && !findingType.isBlank()) {
                strategies = strategies.stream().filter(s -> s.aplicaA(findingType)).collect(Collectors.toList());
            }
            return Map.of("items", strategies);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar estratégias", e);
        }
    }
}
