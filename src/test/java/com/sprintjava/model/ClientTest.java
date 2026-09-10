package com.sprintjava.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientTest {

    @Test
    void gerarNameKeyNormalizaAcentosEEspacos() {
        Client client = new Client(1L, "  Água & Vidro Comércio  ");
        assertEquals("agua & vidro comercio", client.getNameKey());
    }

    @Test
    void novoClienteComecaComoProspect() {
        Client client = new Client(1L, "Cliente Teste");
        assertEquals(Client.STATUS_PROSPECT, client.getStatus());
    }

    @Test
    void atualizarStatusAceitaValorValido() {
        Client client = new Client(1L, "Cliente Teste");
        client.atualizarStatus(Client.STATUS_ATIVO);
        assertEquals(Client.STATUS_ATIVO, client.getStatus());
    }

    @Test
    void atualizarStatusRejeitaValorInvalido() {
        Client client = new Client(1L, "Cliente Teste");
        assertThrows(IllegalArgumentException.class, () -> client.atualizarStatus("inexistente"));
    }

    @Test
    void possuiContatoCompletoExigeNomeEmailETelefone() {
        Client client = new Client(1L, "Cliente Teste");
        assertFalse(client.possuiContatoCompleto());

        client.setContactName("João");
        client.setContactEmail("joao@cliente.com");
        client.setContactPhone("11999999999");
        assertTrue(client.possuiContatoCompleto());
    }
}
