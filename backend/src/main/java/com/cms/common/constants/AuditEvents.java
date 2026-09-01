package com.cms.common.constants;

/**
 * Evenements d'audit prepares pour la journalisation.
 *
 * <p>Ces codes sont references dans les journaux
 * ({@code AUDIT|<code>|...}) pour faciliter le filtrage des traces.
 */
public final class AuditEvents {

    private AuditEvents() {
    }

    // Profils
    public static final String CREATION_PROFIL = "CREATION_PROFIL";
    public static final String MODIFICATION_PROFIL = "MODIFICATION_PROFIL";
    public static final String SUPPRESSION_PROFIL = "SUPPRESSION_PROFIL";
    public static final String ACTIVATION_PROFIL = "ACTIVATION_PROFIL";
    public static final String DESACTIVATION_PROFIL = "DESACTIVATION_PROFIL";

    // Permissions
    public static final String CREATION_PERMISSION = "CREATION_PERMISSION";
    public static final String MODIFICATION_PERMISSION = "MODIFICATION_PERMISSION";
    public static final String SUPPRESSION_PERMISSION = "SUPPRESSION_PERMISSION";
    public static final String ACTIVATION_PERMISSION = "ACTIVATION_PERMISSION";
    public static final String DESACTIVATION_PERMISSION = "DESACTIVATION_PERMISSION";

    // Associations profil-permission
    public static final String ATTRIBUTION_PERMISSION = "ATTRIBUTION_PERMISSION";
    public static final String RETRAIT_PERMISSION = "RETRAIT_PERMISSION";

    // Exceptions individuelles utilisateur
    public static final String ACCORD_PERMISSION = "ACCORD_PERMISSION";
    public static final String REFUS_PERMISSION = "REFUS_PERMISSION";
    public static final String RETRAIT_PERMISSION_INDIVIDUELLE = "RETRAIT_PERMISSION_INDIVIDUELLE";

}
