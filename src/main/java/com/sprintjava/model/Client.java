package com.sprintjava.model;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public class Client {

    public static final String STATUS_PROSPECT = "prospect";
    public static final String STATUS_ATIVO = "ativo";
    public static final String STATUS_INATIVO = "inativo";

    private static final Set<String> STATUS_VALIDOS = Set.of(
            STATUS_PROSPECT, STATUS_ATIVO, STATUS_INATIVO);

    private Long id;
    private Long userId;
    private String name;
    private String nameKey;
    private String segment;
    private String companySize;
    private String website;
    private String city;
    private String state;
    private String contactName;
    private String contactRole;
    private String contactEmail;
    private String contactPhone;
    private String owner;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public Client() {
    }

    public Client(Long userId, String name) {
        this.userId = userId;
        this.name = name;
        this.nameKey = gerarNameKey();
        this.status = STATUS_PROSPECT;
        this.createdAt = LocalDateTime.now();
    }

    public Client(Long id, Long userId, String name, String nameKey, String segment, String companySize,
                  String website, String city, String state, String contactName, String contactRole,
                  String contactEmail, String contactPhone, String owner, String status, String notes,
                  LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.nameKey = nameKey;
        this.segment = segment;
        this.companySize = companySize;
        this.website = website;
        this.city = city;
        this.state = state;
        this.contactName = contactName;
        this.contactRole = contactRole;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.owner = owner;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    /**
     * Deriva a chave de deduplicação do cliente a partir do nome (minúsculo,
     * sem acento, sem espaços redundantes). É o que garante a unicidade
     * (user_id, name_key) no banco quando dois cadastros citam a mesma empresa.
     */
    public String gerarNameKey() {
        if (name == null) {
            return null;
        }
        String semAcento = Normalizer.normalize(name.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.replaceAll("\\s+", " ");
    }

    /**
     * Aplica uma transição de status validando contra o vocabulário permitido,
     * evitando que a análise de churn/negociação registre um valor inválido.
     */
    public void atualizarStatus(String novoStatus) {
        if (novoStatus == null || !STATUS_VALIDOS.contains(novoStatus)) {
            throw new IllegalArgumentException("Status de cliente inválido: " + novoStatus);
        }
        this.status = novoStatus;
    }

    /**
     * Indica se há dados de contato suficientes para o vendedor acionar o cliente.
     */
    public boolean possuiContatoCompleto() {
        return isPreenchido(contactName) && isPreenchido(contactEmail) && isPreenchido(contactPhone);
    }

    private boolean isPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactRole() {
        return contactRole;
    }

    public void setContactRole(String contactRole) {
        this.contactRole = contactRole;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
