package rl.sage.rangerlevels.capability;

public enum RewardStatus {
    BLOCKED,    // No está disponible aún
    UNBLOCKED,  // Está desbloqueado pero no marcado como pendiente
    PENDING,    // Listo para reclamar
    CLAIMED     // Ya reclamado
}
