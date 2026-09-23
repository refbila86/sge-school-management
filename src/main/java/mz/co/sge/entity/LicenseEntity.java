package mz.co.sge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import mz.co.sge.enumes.LicenseEnum.LicenseStatus;
import mz.co.sge.enumes.LicenseEnum.LicenseType;

@Entity
@Table(name = "license", uniqueConstraints = {
    @UniqueConstraint(name = "uk_license_key", columnNames = "license_key"),
    @UniqueConstraint(name = "uk_license_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_license_code", columnNames = "code")
})
public class LicenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "license_key", nullable = false, unique = true, length = 50)
    private String licenseKey;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private LicenseType type = LicenseType.ANNUAL;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays = 365;

    @Column(name = "activation_date")
    private LocalDate activationDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // compatibilidade com sua tabela antiga
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LicenseStatus status = LicenseStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "active", columnDefinition = "BIT(1)")
    private Boolean active = true;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.code == null) this.code = this.licenseKey;
        if (this.licenseKey == null) this.licenseKey = this.code;
        if (this.startDate == null) this.startDate = this.activationDate;
        if (this.endDate == null) this.endDate = this.expirationDate;
    }

    @Transient
    public long getDaysRegistered() {
        LocalDate base = activationDate != null ? activationDate : startDate;
        if (base == null) return 0;
        return ChronoUnit.DAYS.between(base, LocalDate.now());
    }

    @Transient
    public long getDaysRemaining() {
        LocalDate base = expirationDate != null ? expirationDate : endDate;
        if (base == null) return durationDays != null ? durationDays : 0;
        long r = ChronoUnit.DAYS.between(LocalDate.now(), base);
        return r < 0 ? 0 : r;
    }

    @Transient
    public boolean isExpired() { return getDaysRemaining() == 0; }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LicenseType getType() { return type; }
    public void setType(LicenseType type) { this.type = type; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public LocalDate getActivationDate() { return activationDate; }
    public void setActivationDate(LocalDate activationDate) { this.activationDate = activationDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LicenseStatus getStatus() { return status; }
    public void setStatus(LicenseStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}