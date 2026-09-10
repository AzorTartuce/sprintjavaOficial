package com.sprintjava.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeetingTest {

    @Test
    void novaReuniaoAindaNaoTemAnaliseCompleta() {
        Meeting meeting = new Meeting(1L, "reuniao-cliente-x.txt");
        assertFalse(meeting.possuiAnaliseCompleta());
    }

    @Test
    void possuiAnaliseCompletaQuandoFinalReportPreenchido() {
        Meeting meeting = new Meeting(1L, "reuniao-cliente-x.txt");
        meeting.setFinalReportJson("{\"conta\":\"Cliente X\",\"status\":\"ok\"}");
        assertTrue(meeting.possuiAnaliseCompleta());
    }

    @Test
    void quantidadeAgentesSelecionadosContaCorretamente() {
        Meeting meeting = new Meeting(1L, "reuniao-cliente-x.txt");
        meeting.setSelectedAgentsJson("[\"oportunidade\", \"churn\", \"ecossistema\"]");
        assertEquals(3, meeting.quantidadeAgentesSelecionados());
    }

    @Test
    void quantidadeAgentesSelecionadosZeroQuandoVazio() {
        Meeting meeting = new Meeting(1L, "reuniao-cliente-x.txt");
        assertEquals(0, meeting.quantidadeAgentesSelecionados());
    }
}
