/**
 * Validation de formulaire centralisee (cote client).
 *
 * Double securite : meme si le backend valide deja (Bean Validation), on
 * controle ici avant l'envoi pour un retour immediat et un affichage
 * d'erreur champ par champ. Le frontend n'insere JAMAIS de HTML brut
 * (aucun dangerouslySetInnerHTML) : React echappe par defaut => pas de XSS.
 */

const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
/** Regex alignee sur le backend : ^[0-9+ .-]*$ (max 20).
 *  Accepte : 0612345678, +225 07 12 34 56 78, 07-12-34-56-78... */
const PHONE_RE = /^[0-9+][0-9+ .-]*$/;
const NAME_RE = /^[^<>{}]+$/;

export function isValidEmail(email?: string | null): boolean {
  return typeof email === "string" && email.trim().length > 0 && EMAIL_RE.test(email.trim());
}

export function isValidPhone(phone?: string | null): boolean {
  if (!phone || phone.trim() === "") return true; // optionnel
  const p = phone.trim();
  return p.length <= 20 && PHONE_RE.test(p);
}

/** Nom / prenom : non vide, sans caractere HTML non autorise. */
export function isValidName(name?: string | null): boolean {
  return typeof name === "string" && name.trim().length > 0 && NAME_RE.test(name);
}

/** Date ISO (aaaa-mm-jj) valide ET >= aujourd'hui (pas de date passee). */
export function isValidDate(date?: string | null): boolean {
  if (!date) return true;
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return false;
  const d = new Date(date + "T00:00:00");
  if (Number.isNaN(d.getTime())) return false;
  return d >= new Date(today());
}

/** Date ISO valide, sans contrainte de passe. */
export function isAnyValidDate(date?: string | null): boolean {
  if (!date) return true;
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return false;
  return !Number.isNaN(new Date(date + "T00:00:00").getTime());
}

export function today(): string {
  return new Date().toISOString().slice(0, 10);
}

/** dateFin doit etre >= dateDebut ; retourne le message d'erreur ou null. */
export function validateDateRange(dateDebut?: string | null, dateFin?: string | null): string | null {
  if (!dateDebut || !dateFin) return null;
  if (!isAnyValidDate(dateDebut) || !isAnyValidDate(dateFin)) return "Date invalide.";
  if (dateFin < dateDebut) return "La date de fin doit être après la date de début.";
  return null;
}