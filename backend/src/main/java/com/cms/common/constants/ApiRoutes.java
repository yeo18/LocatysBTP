package com.cms.common.constants;

/**
 * Routes API centralisees.
 * Prefixe de base : /api/v1
 */
public final class ApiRoutes {

    private ApiRoutes() {
    }

    public static final String BASE_API = "/api/v1";

    public static final String AUTH = BASE_API + "/auth";
    public static final String USERS = BASE_API + "/users";
    public static final String PROFILS = BASE_API + "/profils";
    public static final String PERMISSIONS = BASE_API + "/permissions";
    public static final String CHANTIERS = BASE_API + "/chantiers";
    public static final String TACHES = BASE_API + "/taches";
    public static final String EQUIPES = BASE_API + "/equipes";
    public static final String ANALYSE_SITE = BASE_API + "/analyse-site";

    public static final String TEMPLATE_CHANTIERS = BASE_API + "/template-chantiers";
    public static final String TEMPLATE_TACHES = BASE_API + "/template-taches";

}
