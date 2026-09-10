package com.sprintjava.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void emailValidoAceitaFormatoCorreto() {
        User user = new User("ana@totvs.com.br", "hash", "Ana Souza");
        assertTrue(user.emailValido());
    }

    @Test
    void emailValidoRejeitaFormatoIncorreto() {
        User user = new User("ana-arroba-totvs", "hash", "Ana Souza");
        assertFalse(user.emailValido());
    }

    @Test
    void perfilCompletoExigeEmpresaCargoEDepartamento() {
        User user = new User("ana@totvs.com.br", "hash", "Ana Souza");
        assertFalse(user.perfilCompleto());

        user.setCompany("TOTVS");
        user.setJobTitle("Account Manager");
        user.setDepartment("Comercial");
        assertTrue(user.perfilCompleto());
    }
}
