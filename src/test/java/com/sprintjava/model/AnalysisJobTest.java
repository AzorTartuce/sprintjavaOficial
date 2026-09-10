package com.sprintjava.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisJobTest {

    @Test
    void novoJobComecaComoQueued() {
        AnalysisJob job = new AnalysisJob(1L, 1L, "reuniao.txt", "transcrição da reunião...");
        assertEquals(AnalysisJob.STATUS_QUEUED, job.getStatus());
        assertTrue(job.podeSerRecuperado());
    }

    @Test
    void iniciarProcessamentoMudaParaRunning() {
        AnalysisJob job = new AnalysisJob(1L, 1L, "reuniao.txt", "transcrição...");
        job.iniciarProcessamento();
        assertEquals(AnalysisJob.STATUS_RUNNING, job.getStatus());
    }

    @Test
    void concluirComSucessoExigeStatusRunning() {
        AnalysisJob job = new AnalysisJob(1L, 1L, "reuniao.txt", "transcrição...");
        assertThrows(IllegalStateException.class, () -> job.concluirComSucesso(10L));
    }

    @Test
    void concluirComSucessoAssociaReuniaoELimpaErro() {
        AnalysisJob job = new AnalysisJob(1L, 1L, "reuniao.txt", "transcrição...");
        job.iniciarProcessamento();
        job.concluirComSucesso(42L);

        assertEquals(AnalysisJob.STATUS_DONE, job.getStatus());
        assertEquals(42L, job.getMeetingId());
        assertNull(job.getErrorDetail());
    }

    @Test
    void jobFalhoNaoPodeSerReiniciadoDiretamente() {
        AnalysisJob job = new AnalysisJob(1L, 1L, "reuniao.txt", "transcrição...");
        job.iniciarProcessamento();
        job.falhar("timeout no serviço Python");

        assertThrows(IllegalStateException.class, job::iniciarProcessamento);
    }

    @Test
    void falharRegistraStatusEDetalheDoErro() {
        AnalysisJob job = new AnalysisJob(1L, 1L, "reuniao.txt", "transcrição...");
        job.iniciarProcessamento();
        job.falhar("Serviço Python indisponível (503)");

        assertEquals(AnalysisJob.STATUS_FAILED, job.getStatus());
        assertEquals("Serviço Python indisponível (503)", job.getErrorDetail());
        assertFalse(job.podeSerRecuperado());
    }
}
