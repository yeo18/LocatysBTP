// ============ Types LOCATYSBTP · gestion de chantiers ============

export type ID = number;

export type ChantierType =
  | "Résidentiel"
  | "Commercial"
  | "Industriel"
  | "Infrastructure"
  | "Rénovation"
  | "-";

export type ChantierStatut =
  | "Planifié"
  | "En cours"
  | "En pause"
  | "Terminé"
  | "Annulé"
  | "En attente"
  | "Livré";

export interface Chantier {
  id: ID;
  nom: string;
  type: ChantierType;
  statut: ChantierStatut;
  localisation: string;
  coordonnees: string;
  dateDebut: string;
  dateFin: string;
  photo?: string;
  description: string;
  equipeIds: ID[];
  progression?: number;
  responsableNom?: string;
}

export type Priorite = "Basse" | "Moyenne" | "Haute" | "Urgente";
export type TacheStatut = "À faire" | "En cours" | "Bloquée" | "Validée" | "Refusée";

export interface ChecklistItem {
  label: string;
  done: boolean;
}

export interface Tache {
  id: ID;
  titre: string;
  description: string;
  chantierId: ID;
  chantierNom?: string;
  priorite: Priorite;
  statut: TacheStatut;
  assigneId: ID | null;
  equipeId: ID | null;
  dateDebut: string;
  dateFin: string;
  cout: number;
  checklist: ChecklistItem[];
  progression?: number;
}

export interface User {
  id: ID;
  prenom: string;
  nom: string;
  email: string;
  telephone: string;
  profilId: ID;
  profilNom?: string;
  equipeId: ID | null;
  permissions: string[];
  /** Droits effectifs calculés AVEC les exceptions scopées, par chantier (id chantier -> codes). */
  permissionsParChantier?: Record<number, string[]>;
  password: string;
  role: "admin" | "user";
  avatar: string; // gradient classes
  dateCreation?: string;
  dateModification?: string;
}
