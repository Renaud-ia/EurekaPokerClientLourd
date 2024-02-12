package analyzor.modele.poker.evaluation.subset;

import analyzor.modele.config.ValeursConfig;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * gère l'encryptage et la récupération des subsets
 * utilisé par CreateurSubset pour enregistrer les subsets dans BDD Berkeley
 * chargé de la récupération des subsets
 */
public class GestionnaireSubset {
    //todo gérer les logs
    private static Map<Integer, Map<Integer, Integer>> indexTableSubsets;
    private static final File repertoireRessources = trouverRepertoireRessources();

    public GestionnaireSubset() {
        recupererToutesLesTables();
    }

    // interface pour obtenir les maps
    public Map<Integer, Integer> obtenirTable(int nSubsets) {
        return indexTableSubsets.get(nSubsets);
    }

    public Integer[] subsetsDisponibles() {
        return indexTableSubsets.keySet().toArray(new Integer[0]);
    }

    // récupération des données

    private void recupererToutesLesTables() {;
        File[] listOfFiles = repertoireRessources.listFiles();
        if (listOfFiles == null) return;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".sub")) {
                String[] parts = file.getName().split("_");
                if (parts.length > 0) {
                    String hexString = parts[0];
                    int value;
                    try { value = Integer.parseInt(hexString, 16); }
                    catch (NumberFormatException e) { continue; }

                    HashMap<Integer, Integer> mapTrouvee = deserializeMap(file);
                    if (mapTrouvee == null) continue;
                    indexTableSubsets.put(value, mapTrouvee);
                }
            }
        }
    }

    private static HashMap<Integer, Integer> deserializeMap(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object objet = ois.readObject();
            if (verifierMapSerialisee(objet)) {
                @SuppressWarnings("unchecked")
                HashMap<Integer, Integer> map = (HashMap<Integer, Integer>) objet;
                return map;
            }
            else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //méthodes utilisées par GenerateurSubset dans sa méthode main

    public static void enregistrerTableSubsets(HashMap<Integer, Integer> tableSubsets) {
        LocalDate currentDate = LocalDate.now();
        int nombreSubsets = tableSubsets.size();
        String nomFichier = Integer.toHexString(nombreSubsets) + "_" +  dateToHexString(currentDate) + ".sub";
        File cheminFichier = new File(repertoireRessources + File.separator + nomFichier);

        try (FileOutputStream fos = new FileOutputStream(cheminFichier);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(tableSubsets);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static File trouverRepertoireRessources() {
        if (ValeursConfig.mode == ValeursConfig.Mode.PRODUCTION) {
            if (ValeursConfig.dossierRessourcesProduction.exists()) {
                return ValeursConfig.dossierRessourcesProduction;
            }
        }
        return ValeursConfig.dossierRessourcesDeveloppement;
    }

    private static boolean verifierMapSerialisee(Object objet) {
        if (objet instanceof HashMap) {
            HashMap<?, ?> map = (HashMap<?, ?>) objet;
            if (!map.isEmpty()) {
                Object key = map.keySet().iterator().next();
                Object value = map.values().iterator().next();
                return key instanceof Integer && value instanceof Integer;
            }
            return true;
        }
        return false;
    }

    private static String dateToHexString(LocalDate date) {
        LocalDate epoch = LocalDate.of(1970, 1, 1);
        long daysSinceEpoch = ChronoUnit.DAYS.between(epoch, date);
        return Long.toHexString(daysSinceEpoch).toUpperCase();
    }
}
