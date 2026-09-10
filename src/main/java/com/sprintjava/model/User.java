package com.sprintjava.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$");

    private Long id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String company;
    private String jobTitle;
    private String department;
    private String phone;
    private String city;
    private String state;
    private String linkedinUrl;
    private String bio;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.createdAt = LocalDateTime.now();
    }

    public User(Long id, String email, String passwordHash, String fullName, String company,
                String jobTitle, String department, String phone, String city, String state,
                String linkedinUrl, String bio, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.company = company;
        this.jobTitle = jobTitle;
        this.department = department;
        this.phone = phone;
        this.city = city;
        this.state = state;
        this.linkedinUrl = linkedinUrl;
        this.bio = bio;
        this.createdAt = createdAt;
    }

    /**
     * Valida o formato do e-mail cadastrado. Regra de negócio: sem um e-mail
     * sintaticamente válido o cadastro/login não pode ser concluído.
     */
    public boolean emailValido() {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Um perfil é considerado completo quando os dados usados para segmentar
     * o comercial (empresa, cargo e departamento) foram preenchidos.
     */
    public boolean perfilCompleto() {
        return isPreenchido(company) && isPreenchido(jobTitle) && isPreenchido(department);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", company='" + company + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
